package com.iot.stayflowdev.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Hotel {
    @DocumentId
    private String id; // ID del hotel en Firestore

    @PropertyName("nombre")
    private String nombre;

    @PropertyName("descripcion")
    private String descripcion;

    @PropertyName("administradorAsignado")
    private String administradorAsignado;

    @PropertyName("calificacion")
    private String calificacion;

    @PropertyName("ubicacion")
    private String ubicacion;

    @PropertyName("fotos")
    private List<String> fotos; // Lista de URLs de fotos del hotel

    @PropertyName("geoposicion")
    private GeoPoint geoposicion;

    // Campo temporal para el ID del administrador (no persistido en Firestore)
    private transient String adminId;

    // Constructor para los campos obligatorios
    public Hotel(String nombre, String administradorAsignado) {
        this.nombre = nombre;
        this.administradorAsignado = administradorAsignado;
    }

    // Constructor completo
    public Hotel(String id, String nombre, String administradorAsignado, String calificacion, String ubicacion, List<String> fotos) {
        this.id = id;
        this.nombre = nombre;
        this.administradorAsignado = administradorAsignado;
        this.calificacion = calificacion;
        this.ubicacion = ubicacion;
        this.fotos = fotos;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", administradorAsignado='" + administradorAsignado + '\'' +
                '}';
    }
}
