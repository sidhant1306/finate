package com.example.finatebackend.service;

import com.example.finatebackend.dao.UserRepository;
import com.example.finatebackend.dto.authdto.*;
import com.example.finatebackend.enums.UserRole;
import com.example.finatebackend.exceptions.EmailNotVerifiedException;
import com.example.finatebackend.exceptions.UserAlreadyExistsException;
import com.example.finatebackend.exceptions.UserNotFoundException;
import com.example.finatebackend.model.User;
import com.example.finatebackend.model.Wallet;
import com.example.finatebackend.security.JwtService;
import com.example.finatebackend.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, EmailService emailService, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {
        if(userRepository.findByUserEmail(registerRequestDto.userEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        // generate the otp and send it to the user's email:
        generateOtp(registerRequestDto.userEmail(), registerRequestDto.firstName());
        User user = User.builder()
                .userEmail(registerRequestDto.userEmail())
                .userRole(UserRole.BASIC)
                .userPassword(passwordEncoder.encode(registerRequestDto.userPassword()))
                .username(registerRequestDto.username())
                .firstName(registerRequestDto.firstName())
                .lastName(registerRequestDto.lastName())
                .isEmailVerified(false)
                .build();


        Wallet wallet = Wallet.builder()
                .walletUser(user)
                .currentBalance(BigDecimal.ZERO)
                .build();
        user.setUserWallet(wallet);
        userRepository.save(user);
        return new RegisterResponseDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUserRole(),
                user.getUsername()
        );
    }

    public boolean verifyOtp(VerifyOtpRequestDto request) {
        // check if the otp entered by the user is same as the email stored in the redis cache:

        String redisOtp = redisTemplate.opsForValue().get("otp::" + request.userEmail());
        User user = userRepository.findByUserEmail(request.userEmail()).orElseThrow(
                () -> new UserNotFoundException("User not found after OTP verification")
        );

        // OTP expired — clean up the unverified account
        if (redisOtp == null) {
            userRepository.removeUserByUserId(user.getUserId());
            return false;
        }

        // Wrong otp: check the count of tries of entering the wrong otp
        if (!redisOtp.equals(String.valueOf(request.otp()))) {
            String counterKey = "wrongOtpCounter::" + request.userEmail();
            String counterVal = redisTemplate.opsForValue().get(counterKey);
            int attempts = counterVal != null ? Integer.parseInt(counterVal) : 0;

            if (attempts >= 2) {
                // 3 wrong attempts — delete account, force re-register
                userRepository.removeUserByUserId(user.getUserId());
                redisTemplate.delete("otp::" + request.userEmail());
                redisTemplate.delete(counterKey);
                return false;
            } else {
                redisTemplate.opsForValue().increment(counterKey);
                return false;
            }
        }
            // correct otp :
            user.setEmailVerified(true);
            userRepository.save(user);
            emailService.sendWelcomeEmail(user.getUserEmail(), user.getFirstName());
            redisTemplate.delete("otp::" + request.userEmail());
            return true;

    }


    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.userEmail(), loginRequestDto.userPassword())
        );
        // if it didn't crash above, it means the user is valid
        User user = userRepository.findByUserEmail(loginRequestDto.userEmail()).orElseThrow(
                () -> new UserNotFoundException("User not found after authentication")
        );

        // check if the user email is verified
        if(!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email not verified");
        }

        String token = jwtService.generateToken(user);
        emailService.sendLoginEmail(user.getUserEmail(), user.getFirstName());
        return new LoginResponseDto(
                token,
                user.getFirstName(),
                user.getLastName(),
                user.getUserEmail(),
                user.getUserRole(),
                user.getUsername()
        );
    }

    public LoginResponseDto fetchMe() {
        User user = SecurityUtils.getCurrentUser();
        return new LoginResponseDto(
                jwtService.generateToken(user),
                user.getFirstName(),
                user.getLastName(),
                user.getUserEmail(),
                user.getUserRole(),
                user.getUsername()
        );
    }

    public void generateOtp(String userEmail, String firstName) {
        // generate a random 6-digit otp :
        long otp = 100000 + (long)(Math.random() * 900000); // always exactly 6 digits;

        // send the otp to the user's email :
        emailService.sendEmailVerificationOtp(otp, userEmail, firstName);

        // to store it in the redis cache in format (key, value(generated otp), duration of the otp :
        redisTemplate.opsForValue().set("otp::" +userEmail, String.valueOf(otp), Duration.ofMinutes(5));
    }

}
