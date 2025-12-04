package com.surestock.controller;

import com.surestock.dto.auth.EmployeeCreationRequestDTO;
import com.surestock.dto.auth.UserResponseDTO;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.service.BusinessService;
import com.surestock.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private BusinessService businessService;

    private User getCurrentUser(@NotNull UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    /**
     * [ADMIN ONLY] Allows a Business Owner to create a new Employee for their business.
     * POST /api/users/employee
     */
    @PostMapping("/employee")
    public UserResponseDTO createEmployee(@RequestBody EmployeeCreationRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        User owner = getCurrentUser(userDetails);

        if (owner.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Only Owners can manage user accounts.");
        }

        User newEmployee = userService.createEmployee(owner.getBusinessId(), dto);

        return new UserResponseDTO(newEmployee);
    }

    /**
     * [ADMIN ONLY] Retrieves all employees for the current user's business.
     * GET /api/users/employees
     */
    @GetMapping("/employees")
    public List<UserResponseDTO> getAllEmployees(@AuthenticationPrincipal UserDetails userDetails) {
        User requester = getCurrentUser(userDetails);

        List<User> employees = userService.getAllEmployees(requester.getBusinessId());
        return employees.stream()
                .map(UserResponseDTO::new) // Convert Entity to DTO
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    public UserResponseDTO getCurrentUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return new UserResponseDTO(user);
    }

    @DeleteMapping("/employee/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User requestingUser = getCurrentUser(userDetails);

        if (requestingUser.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Only Owners can manage user accounts.");
        }

        User employeeToDelete = userService.findById(employeeId);

        if (!employeeToDelete.getBusinessId().equals(requestingUser.getBusinessId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete user from another business.");
        }

        userService.deleteUser(employeeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/owner/self")
    public ResponseEntity<Void> closeAccount(@AuthenticationPrincipal UserDetails userDetails) {
        User owner = getCurrentUser(userDetails);

        if (owner.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action restricted to account Owner.");
        }

        businessService.deleteBusinessAndAllData(owner.getBusinessId());

        return ResponseEntity.noContent().build();
    }
}