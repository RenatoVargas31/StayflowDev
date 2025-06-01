package com.codebnb.stayflow.superAdmin.model;

public class User {
    private String name;
    private String role;
    private String roleDescription;
    private boolean enabled;

    public User(String name, String role, String roleDescription, boolean enabled) {
        this.name = name;
        this.role = role;
        this.roleDescription = roleDescription;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}