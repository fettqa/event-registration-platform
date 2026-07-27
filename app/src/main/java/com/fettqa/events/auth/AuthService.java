package com.fettqa.events.auth;

import com.fettqa.events.auth.dto.AuthResponse;
import com.fettqa.events.auth.dto.LoginRequest;
import com.fettqa.events.auth.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
    }

    User user = new User(
        request.email().trim().toLowerCase(),
        passwordEncoder.encode(request.password()), // обязательно encode!
        Role.USER);

    userRepository.save(user);
    String token = jwtService.generateToken(user);
    return AuthResponse.bearer(token, user);
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    User user = userRepository.findByEmailIgnoreCase(request.email())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials"));

    return AuthResponse.bearer(jwtService.generateToken(user), user);
  }
}