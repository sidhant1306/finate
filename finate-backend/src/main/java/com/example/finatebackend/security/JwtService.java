package com.example.finatebackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService{

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] key = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(key);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
   }

   private String tokenGeneration(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
   }

   public String generateToken(UserDetails userDetails) {
        return tokenGeneration(new HashMap<>(), userDetails);
   }

   public boolean isTokenValid(UserDetails userDetails, String token) {
        String username = userDetails.getUsername();
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
   }

    private boolean isTokenExpired(String token) {
        return (extractTokenExpiration(token).before(new Date(System.currentTimeMillis())));
    }

    private Date extractTokenExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
