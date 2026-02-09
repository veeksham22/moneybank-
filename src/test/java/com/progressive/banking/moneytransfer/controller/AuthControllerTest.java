package com.progressive.banking.moneytransfer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.progressive.banking.moneytransfer.domain.dto.LoginRequest;
import com.progressive.banking.moneytransfer.security.JwtUtil;

import java.util.Collections;

@Import(ObjectMapper.class)  // Import ObjectMapper
@WebMvcTest(controllers = AuthController.class)  // ✅ Changed from @SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // ✅ Changed from @Autowired to @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean  // ✅ Changed from @Autowired to @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /auth/login returns 200 and token when credentials valid")
    void login_validCredentials_returnsToken() throws Exception {
        // Prepare the LoginRequest
        LoginRequest request = new LoginRequest();
        request.setUsername("user1");
        request.setPassword("password1");

        // Mock the behavior of the authenticationManager
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                "user1", 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        // Simulate authentication success and return a valid Authentication object
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        // Mock the JwtUtil behavior to return a fake token
        when(jwtUtil.generateToken(eq("user1"))).thenReturn("fake-jwt-token");

        // Perform the request using MockMvc
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));

        // Verify that the mock dependencies were called
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("user1");
    }
}