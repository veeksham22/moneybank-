package com.progressive.banking.moneytransfer.controller;

import com.progressive.banking.moneytransfer.domain.dto.*;
import com.progressive.banking.moneytransfer.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword())
        );

        return new LoginResponse(
                jwtUtil.generateToken(request.getUsername())
        );
    }
}