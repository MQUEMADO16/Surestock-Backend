package com.surestock.dto.auth;

import com.surestock.model.Role;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String email;
    private String password;
    private Role role;
    private Long businessId;
}