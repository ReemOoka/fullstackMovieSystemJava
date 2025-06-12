package com.example.model;

import java.util.Set;

public class User {
    private Long id;
    private String username;
    private Set<Role> roles; // Assuming Role is an enum or class also on client

    // Constructors
    public User() {}

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}