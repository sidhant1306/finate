package com.example.finatebackend.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlacklistService {
    private final ConcurrentHashMap<String, Boolean> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token) {
        blacklist.put(token, true);
        System.out.println("Token blacklisted in memory");
    }

    public boolean isTokenBlacklisted(String token) {
        if(!blacklist.containsKey(token)) return false;
        return blacklist.get(token);
    }


}
