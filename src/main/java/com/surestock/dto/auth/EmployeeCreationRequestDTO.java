package com.surestock.dto.auth;

import lombok.Data;

@Data
public class EmployeeCreationRequestDTO {
    private String email;
    private String password; // Raw password for initial setup
}