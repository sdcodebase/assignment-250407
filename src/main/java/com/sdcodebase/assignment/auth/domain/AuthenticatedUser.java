package com.sdcodebase.assignment.auth.domain;

import com.sdcodebase.assignment.user.domain.Role;

import java.security.Principal;


public record AuthenticatedUser(Long id, String email, Role role) implements Principal {

    @Override
    public String getName() {
        return email;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
