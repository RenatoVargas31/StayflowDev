package com.iot.stayflowdev.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reserva {
    @DocumentId
    private String id;
    @PropertyName("idUsuario")
    private String idUsuario; // ID del usuario que hizo la reserva
    @PropertyName("idHotel")
    private String idHotel; // ID del hotel reservado
    @ServerTimestamp
    @PropertyName("fechaCreacion")
    private Timestamp fechaCreacion; // Fecha de creación de la reserva
    @PropertyName("fechaInicio")
    private Timestamp fechaInicio; // Fecha de inicio de la reserva
    @PropertyName("fechaFin")
    private Timestamp fechaFin; // Fecha de fin de la reserva
    @PropertyName("servicios")
    private List<Servicio> servicios; // Lista de servicios (Almacena objetos Servicio)
    @PropertyName("cantHuespedes")
    private CantHuespedes cantHuespedes; // Cantidad de huéspedes (adultos, niños)
    @PropertyName("habitaciones")
    private List<Habitacion> habitaciones; // Lista de habitaciones (Almacena objetos Habitacion)
    @PropertyName("costoTotal")
    private String  costoTotal; // Costo total de la reserva
    @PropertyName("estado")
    private String estado; // Estado de la reserva (confirmada, cancelada)
    @PropertyName("quieroTaxi")
    private boolean quieroTaxi; // Indica si el usuario quiere taxi
    @PropertyName("danios")
    private List<Danio> danios; // para registrar daños en la habitación

    // Clase interna para representar la cantidad de huéspedes
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CantHuespedes {
        @PropertyName("adultos")
        private String adultos; // Cantidad de adultos
        @PropertyName("ninos")
        private String ninos; // Cantidad de niños
    }
    // Clase interna para representar los servicios
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Servicio {
        @PropertyName("nombre")
        private String nombre; // Nombre del servicio
        @PropertyName("descripcion")
        private String descripcion; // Descripción del servicio
        @PropertyName("precio")
        private String precio; // Costo del servicio
    }
    // Clase interna para representar las habitaciones
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Habitacion {
        @DocumentId
        private String id; // ID de la habitación
        @PropertyName("tipo")
        private String tipo; // Tipo de habitación (individual, doble, suite)
        @PropertyName("cantidad")
        private int cantidad; // Cantidad de habitaciones de este tipo
        @PropertyName("precio")
        private String precio; // Precio por noche de la habitación
    }
    // Clase interna para representar los daños
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Danio {
        @PropertyName("descripcion")
        private String descripcion;

        @PropertyName("precio")
        private String precio;
    }
}
