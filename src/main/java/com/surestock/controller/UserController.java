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
     * The businessId is taken from the Owner's session context.
     * POST /api/users/employee
     */
    @PostMapping("/employee")
    public UserResponseDTO createEmployee(@RequestBody EmployeeCreationRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        User owner = getCurrentUser(userDetails);

        // Only Owners can hire employees
        if (owner.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Only Owners can manage user accounts.");
        }

        // Create the employee, passing the OWNER's businessId securely
        User newEmployee = userService.createEmployee(owner.getBusinessId(), dto);

        return new UserResponseDTO(newEmployee);
    }

    /**
     * Retrieves the currently logged-in user's details.
     * Used by the frontend to persist session state on refresh.
     * GET /api/users/me
     */
    @GetMapping("/me")
    public UserResponseDTO getCurrentUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return new UserResponseDTO(user);
    }

    /**
     * [ADMIN ONLY] Allows a Business Owner to remove an Employee from the team.
     * DELETE /api/users/employee/{employeeId}
     */
    @DeleteMapping("/employee/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User requestingUser = getCurrentUser(userDetails);

        // Only Owners can manage user accounts
        if (requestingUser.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Only Owners can manage user accounts.");
        }

        // Ensure the employee belongs to the owner's business
        User employeeToDelete = userService.findById(employeeId);

        if (!employeeToDelete.getBusinessId().equals(requestingUser.getBusinessId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete user from another business.");
        }

        // Delete the Employee user record
        userService.deleteUser(employeeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * [OWNER ACTION] Deletes the current logged-in Owner and the entire Business tenant.
     * DELETE /api/users/owner/self
     */
    @DeleteMapping("/owner/self")
    public ResponseEntity<Void> closeAccount(@AuthenticationPrincipal UserDetails userDetails) {
        User owner = getCurrentUser(userDetails);

        // Ensure the user is the owner (for conceptual clarity)
        if (owner.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action restricted to account Owner.");
        }

        // This triggers the atomic deletion of all data linked to the businessId.
        businessService.deleteBusinessAndAllData(owner.getBusinessId());

        return ResponseEntity.noContent().build();
    }
}