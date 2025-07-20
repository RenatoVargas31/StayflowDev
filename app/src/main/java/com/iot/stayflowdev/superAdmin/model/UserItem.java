package com.iot.stayflowdev.superAdmin.model;

public class UserItem {
    private String id;
    private String name;
    private String email;
    private String role;
    private boolean isConnected;

    public UserItem(String id, String name, String email, String role, boolean isConnected) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isConnected = isConnected;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isConnected() { return isConnected; }

    // Método para obtener descripción del rol
    public String getRoleDescription() {
        switch (role) {
            case "adminhotel":
                return "Admin Hotel";
            case "cliente":
                return "Cliente";
            case "driver":
                return "Conductor";
            default:
                return role;
        }
    }
}
