package com.example.dto;

import java.util.Set;
import com.example.model.Role;

public class UserDto {
    private Long id;
    private String username;
    private String password; // Only used for registration/login request
    private Set<Role> roles;

    // Constructors
    public UserDto() {}

    public UserDto(Long id, String username, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public UserDto(String username, String password) { // For login/registration
        this.username = username;
        this.password = password;
    }


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}