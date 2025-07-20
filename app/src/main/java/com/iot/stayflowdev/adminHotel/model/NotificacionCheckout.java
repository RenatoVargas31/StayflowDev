package com.iot.stayflowdev.adminHotel.model;

import com.google.firebase.Timestamp;
import java.util.Date;

public class NotificacionCheckout {
    private String id;
    private String reservaId;
    private String idUsuario;
    private String nombreHuesped;
    private String tipoHabitacion; // Cambio: tipo en lugar de número específico
    private int cantidadHabitaciones; // Cuántas habitaciones tiene la reserva
    private Timestamp fechaCheckout;
    private String costoTotal;
    private boolean leida;
    private Timestamp fechaCreacion;
    private String tipoNotificacion; // "CHECKOUT_HOY", "CHECKOUT_VENCIDO"
    private String estadoReserva; // "confirmada", "checkin"

    public NotificacionCheckout() {}

    public NotificacionCheckout(String reservaId, String idUsuario, String nombreHuesped,
                                Timestamp fechaCheckout,
                                String costoTotal, String tipoNotificacion, String estadoReserva) {
        this.reservaId = reservaId;
        this.idUsuario = idUsuario;
        this.nombreHuesped = nombreHuesped;
        this.fechaCheckout = fechaCheckout;
        this.costoTotal = costoTotal;
        this.tipoNotificacion = tipoNotificacion;
        this.estadoReserva = estadoReserva;
        this.leida = false;
        this.fechaCreacion = Timestamp.now();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReservaId() { return reservaId; }
    public void setReservaId(String reservaId) { this.reservaId = reservaId; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreHuesped() { return nombreHuesped; }
    public void setNombreHuesped(String nombreHuesped) { this.nombreHuesped = nombreHuesped; }

    public String getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    public int getCantidadHabitaciones() { return cantidadHabitaciones; }
    public void setCantidadHabitaciones(int cantidadHabitaciones) { this.cantidadHabitaciones = cantidadHabitaciones; }

    public Timestamp getFechaCheckout() { return fechaCheckout; }
    public void setFechaCheckout(Timestamp fechaCheckout) { this.fechaCheckout = fechaCheckout; }

    public String getCostoTotal() { return costoTotal; }
    public void setCostoTotal(String costoTotal) { this.costoTotal = costoTotal; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getTipoNotificacion() { return tipoNotificacion; }
    public void setTipoNotificacion(String tipoNotificacion) { this.tipoNotificacion = tipoNotificacion; }

    public String getEstadoReserva() { return estadoReserva; }
    public void setEstadoReserva(String estadoReserva) { this.estadoReserva = estadoReserva; }

    // Métodos helper
    public String getTituloNotificacion() {
        switch (tipoNotificacion) {
            case "CHECKOUT_HOY":
                return "Checkout pendiente hoy";
            case "CHECKOUT_VENCIDO":
                return "Checkout vencido";
            default:
                return "Notificación";
        }
    }

    public String getMensajeNotificacion() {

        switch (tipoNotificacion) {
            case "CHECKOUT_HOY":
                return nombreHuesped + " debe hacer checkout hoy. Por favor, procesar. " ;
            case "CHECKOUT_VENCIDO":
                return nombreHuesped + " tiene checkout vencido. Por favor, procesar. " ;
            default:
                return "Notificación para " + nombreHuesped;
        }
    }

    public int getColorPrioridad() {
        switch (tipoNotificacion) {
            case "CHECKOUT_VENCIDO":
                return android.R.color.holo_red_light;
            case "CHECKOUT_HOY":
                return android.R.color.holo_orange_light;
            default:
                return android.R.color.darker_gray;
        }
    }

    // Helper para obtener el icono según el estado
    public int getIconoEstado() {
        // Siempre es sin checkout, así que usamos icono de checkout pendiente
        return android.R.drawable.ic_menu_mylocation;
    }

    // Helper para convertir Timestamp a Date si es necesario
    public Date getFechaCheckoutAsDate() {
        return fechaCheckout != null ? fechaCheckout.toDate() : null;
    }
}