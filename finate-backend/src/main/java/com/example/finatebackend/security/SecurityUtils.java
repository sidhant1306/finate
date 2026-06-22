package com.example.finatebackend.security;

import com.example.finatebackend.model.User;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public static User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new InsufficientAuthenticationException("You need to login first");
        }

        return (User) authentication.getPrincipal();
    }
}
