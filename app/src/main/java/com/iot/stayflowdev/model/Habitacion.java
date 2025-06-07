package com.iot.stayflowdev.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Habitacion {

    @DocumentId
    private String id;
    private String precio;
    private String tamano;
    private String tipo;
    private Capacidad capacidad;
    private String foto;
    private String cantidad; //FALTA
    private Boolean disponible; //FALTA

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Capacidad {
        private String adultos;
        private String ninos;
    }
}
