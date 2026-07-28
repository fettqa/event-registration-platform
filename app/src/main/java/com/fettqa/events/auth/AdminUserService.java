package com.fettqa.events.auth;

import com.fettqa.events.auth.dto.UpdateUserRoleRequest;
import com.fettqa.events.auth.dto.UserResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUserService {

  private final UserRepository userRepository;

  public AdminUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<UserResponse> listAssignableUsers() {
    return userRepository.findAll().stream()
        .filter(user -> user.getRole() != Role.ADMIN)
        .map(UserResponse::from)
        .toList();
  }

  @Transactional
  public UserResponse updateRole(Long userId, UpdateUserRoleRequest request) {
    if (request.role() == Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "ADMIN role cannot be assigned via admin panel");
    }
    if (request.role() != Role.USER && request.role() != Role.SUPER_USER) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported role");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));

    if (user.getRole() == Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "cannot change ADMIN role");
    }

    user.setRole(request.role());
    return UserResponse.from(user);
  }
}
