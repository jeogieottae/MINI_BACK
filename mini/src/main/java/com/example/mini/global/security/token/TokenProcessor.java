package com.example.mini.global.security.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface TokenProcessor {
    void processToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
