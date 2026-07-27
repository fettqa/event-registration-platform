package com.fettqa.events.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    String adminEmail = "admin@example.com";
    if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
      userRepository.save(new User(
          adminEmail,
          passwordEncoder.encode("admin123"),
          Role.ADMIN));
      System.out.println("Created default admin: admin@example.com / admin123");
    }
  }
}