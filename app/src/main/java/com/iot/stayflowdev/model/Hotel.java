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
    @PropertyName("servicios")
    private String servicios; // ID de la subcolección de servicios en Firestore (4-6 servicios)
    @PropertyName("lugaresCercanos")
    private String lugaresCercanos; // ID de la subcolección de lugares cercanos en Firestore (4 lugares cercanos)
    //Habitciones como subcollection
    @PropertyName("habitaciones")
    private String habitaciones; // ID de la subcolección de habitaciones en Firestore (10 hbitaciones)
}
