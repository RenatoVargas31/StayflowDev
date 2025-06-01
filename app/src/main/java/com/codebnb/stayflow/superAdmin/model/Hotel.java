package com.codebnb.stayflow.superAdmin.model;

public class Hotel {
    private final String id;          // opcional si lo necesitas
    private final String name;
    private final String description;

    public Hotel(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Si no necesitas un ID, puedes sobrecargar el constructor
    public Hotel(String name, String description) {
        this("", name, description);
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
}
