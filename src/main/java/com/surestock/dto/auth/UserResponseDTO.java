package com.surestock.dto.auth;

import com.surestock.model.Role;
import com.surestock.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String email;
    private Role role;
    private Long businessId;

    // Manual constructor required for Entity -> DTO mapping
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.businessId = user.getBusinessId();
    }
}