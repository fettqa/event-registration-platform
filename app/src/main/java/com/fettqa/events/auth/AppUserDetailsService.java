package com.fettqa.events.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public AppUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    return userRepository.findByEmailIgnoreCase(username)
        .map(AppUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}