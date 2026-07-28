package com.fettqa.events.auth;

import com.fettqa.events.auth.dto.UpdateUserRoleRequest;
import com.fettqa.events.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @GetMapping
  public List<UserResponse> listUsers() {
    return adminUserService.listAssignableUsers();
  }

  @PutMapping("/{id}/role")
  public UserResponse updateRole(
      @PathVariable Long id,
      @Valid @RequestBody UpdateUserRoleRequest request) {
    return adminUserService.updateRole(id, request);
  }
}
