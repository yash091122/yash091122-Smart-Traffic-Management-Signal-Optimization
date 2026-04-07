package com.smarttraffic.controller;

import com.smarttraffic.dto.LoginRequest;
import com.smarttraffic.dto.LoginResponse;
import com.smarttraffic.repository.UserRepository;
import com.smarttraffic.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var user = userRepository.findByUsername(req.username()).orElseThrow();
        var roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());
        String token = jwtService.generateToken(user.getUsername(), roleNames);
        String primaryRole = roleNames.isEmpty() ? "VIEWER" : roleNames.get(0);
        return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), primaryRole));
    }
}
