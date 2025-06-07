package com.iot.stayflowdev.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hotel {
    @DocumentId
    private String id; // ID del hotel en Firestore
    @PropertyName("nombre")
    private String nombre;
    @PropertyName("calificacion")
    private String calificacion;
    @PropertyName("ubicacion")
    private String ubicacion;
    @PropertyName("fotos")
    private String[] fotos; // Lista de URLs de fotos del hotel (4-6 fotos)
}
