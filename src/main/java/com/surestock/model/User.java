package com.surestock.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users") // "user" is a reserved SQL word, so we're using plural
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "business_id", nullable = false)
    private Long businessId;
}