package com.iot.stayflowdev.Driver.Dtos;

import java.io.Serializable;
import java.io.Serializable;
import com.google.firebase.Timestamp; // Para Firebase Firestore
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SolicitudTaxi implements Serializable {

    // Campos de Viaje
    private String origen;
    private String origenDireccion;
    private String destino = "Aeropuerto Internacional Jorge Chávez";
    private String destinoDireccion = "Av. Elmer Faucett s/n, Callao 07031";

    // Coordenadas geográficas
    private double origenLatitud;
    private double origenLongitud;
    private double destinoLatitud;
    private double destinoLongitud;

    // Campos de Pasajero
    private String nombrePasajero;
    private String telefonoPasajero;
    private int numeroPasajeros;
    private String tipoVehiculo;
    private String notas;

    // Campos de relación con otras entidades
    private String idCliente;
    private String idHotel;
    private String reservaId;
    private String solicitudId;
    private String idTaxista;

    // Campos de fechas y tiempos
    private Timestamp fechaSalida;
    private Timestamp fechaCreacion;
    private Timestamp horaAceptacion; // Cambiado a Timestamp
    private String horaSolicitud; // Mantener como String si es necesario

    // Campos de estado y control
    private String estado; // Estado de la solicitud (pendiente, aceptada, rechazada, etc.)
    private boolean esAceptada; // Indica si la solicitud ha sido aceptada por un conductor

    // Campos de información adicional
    private double distanciaKm;
    private int tiempoEstimadoMin;

    // Campos adicionales del hotel
    private String calificacionHotel;

    // === CONSTRUCTORES ===
    public SolicitudTaxi() {
    }

    // Constructor básico
    public SolicitudTaxi(String origen, String origenDireccion, String destino, String destinoDireccion) {
        this.origen = origen;
        this.origenDireccion = origenDireccion;
        this.destino = destino;
        this.destinoDireccion = destinoDireccion;
    }

    // === GETTERS Y SETTERS PARA CAMPOS DE VIAJE ===
    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getOrigenDireccion() {
        return origenDireccion;
    }

    public void setOrigenDireccion(String origenDireccion) {
        this.origenDireccion = origenDireccion;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getDestinoDireccion() {
        return destinoDireccion;
    }

    public void setDestinoDireccion(String destinoDireccion) {
        this.destinoDireccion = destinoDireccion;
    }

    // === GETTERS Y SETTERS PARA COORDENADAS ===
    public double getOrigenLatitud() {
        return origenLatitud;
    }

    public void setOrigenLatitud(double origenLatitud) {
        this.origenLatitud = origenLatitud;
    }

    public double getOrigenLongitud() {
        return origenLongitud;
    }

    public void setOrigenLongitud(double origenLongitud) {
        this.origenLongitud = origenLongitud;
    }

    public double getDestinoLatitud() {
        return destinoLatitud;
    }

    public void setDestinoLatitud(double destinoLatitud) {
        this.destinoLatitud = destinoLatitud;
    }

    public double getDestinoLongitud() {
        return destinoLongitud;
    }

    public void setDestinoLongitud(double destinoLongitud) {
        this.destinoLongitud = destinoLongitud;
    }

    // === GETTERS Y SETTERS PARA CAMPOS DE PASAJERO ===
    public String getNombrePasajero() {
        return nombrePasajero;
    }

    public void setNombrePasajero(String nombrePasajero) {
        this.nombrePasajero = nombrePasajero;
    }

    public String getTelefonoPasajero() {
        return telefonoPasajero;
    }

    public void setTelefonoPasajero(String telefonoPasajero) {
        this.telefonoPasajero = telefonoPasajero;
    }

    public int getNumeroPasajeros() {
        return numeroPasajeros;
    }

    public void setNumeroPasajeros(int numeroPasajeros) {
        this.numeroPasajeros = numeroPasajeros;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    // === GETTERS Y SETTERS PARA CAMPOS DE RELACIÓN ===
    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(String idHotel) {
        this.idHotel = idHotel;
    }

    public String getReservaId() {
        return reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public String getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(String solicitudId) {
        this.solicitudId = solicitudId;
    }

    public String getIdTaxista() {
        return idTaxista;
    }

    public void setIdTaxista(String idTaxista) {
        this.idTaxista = idTaxista;
    }

    // === GETTERS Y SETTERS PARA FECHAS Y TIEMPOS ===
    public Timestamp getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(Timestamp fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getHoraAceptacion() {
        return horaAceptacion;
    }

    public void setHoraAceptacion(Timestamp horaAceptacion) {
        this.horaAceptacion = horaAceptacion;
    }

    // Método adicional para setear hora de aceptación como String (por compatibilidad)
    public void setHoraAceptacion(String horaAceptacionString) {
        this.horaSolicitud = horaAceptacionString; // Guardar en horaSolicitud si necesitas el String
    }

    public String getHoraSolicitud() {
        return horaSolicitud;
    }

    public void setHoraSolicitud(String horaSolicitud) {
        this.horaSolicitud = horaSolicitud;
    }

    // === GETTERS Y SETTERS PARA ESTADO Y CONTROL ===
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isEsAceptada() {
        return esAceptada;
    }

    public void setEsAceptada(boolean esAceptada) { // CORREGIDO: ahora funciona correctamente
        this.esAceptada = esAceptada;
    }

    // === GETTERS Y SETTERS PARA INFORMACIÓN ADICIONAL ===
    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public int getTiempoEstimadoMin() {
        return tiempoEstimadoMin;
    }

    public void setTiempoEstimadoMin(int tiempoEstimadoMin) {
        this.tiempoEstimadoMin = tiempoEstimadoMin;
    }

    public String getCalificacionHotel() {
        return calificacionHotel;
    }

    public void setCalificacionHotel(String calificacionHotel) {
        this.calificacionHotel = calificacionHotel;
    }

    // === MÉTODOS DE UTILIDAD PARA FECHAS ===

    // Formatear fecha de salida
    public String getFechaSalidaFormateada() {
        if (fechaSalida == null) return "No definida";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(fechaSalida.toDate());
    }

    // Formatear hora de salida
    public String getHoraSalidaFormateada() {
        if (fechaSalida == null) return "No definida";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(fechaSalida.toDate());
    }

    // Formatear fecha y hora de salida completa
    public String getFechaHoraSalidaCompleta() {
        if (fechaSalida == null) return "No definida";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault());
        return sdf.format(fechaSalida.toDate());
    }

    // Formatear fecha de creación
    public String getFechaCreacionFormateada() {
        if (fechaCreacion == null) return "No definida";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(fechaCreacion.toDate());
    }

    // Formatear hora de aceptación
    public String getHoraAceptacionFormateada() {
        if (horaAceptacion == null) return "No aceptada";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(horaAceptacion.toDate());
    }

    // Obtener tiempo transcurrido desde la creación
    public String getTiempoTranscurrido() {
        if (fechaCreacion == null) return "Desconocido";

        long tiempoActual = System.currentTimeMillis();
        long tiempoCreacion = fechaCreacion.toDate().getTime();
        long diferencia = tiempoActual - tiempoCreacion;

        long minutos = diferencia / (60 * 1000);
        long horas = diferencia / (60 * 60 * 1000);
        long dias = diferencia / (24 * 60 * 60 * 1000);

        if (dias > 0) {
            return dias + " día(s)";
        } else if (horas > 0) {
            return horas + " hora(s)";
        } else {
            return minutos + " minuto(s)";
        }
    }

    // === MÉTODOS DE UTILIDAD GENERALES ===

    // Verificar si la solicitud tiene coordenadas válidas
    public boolean tieneCoordenadasValidas() {
        return origenLatitud != 0 && origenLongitud != 0 && destinoLatitud != 0 && destinoLongitud != 0;
    }

    // Verificar si solo tiene coordenadas de origen
    public boolean tieneCoordenadasOrigen() {
        return origenLatitud != 0 && origenLongitud != 0;
    }

    // Verificar si solo tiene coordenadas de destino
    public boolean tieneCoordenadasDestino() {
        return destinoLatitud != 0 && destinoLongitud != 0;
    }

    // Obtener coordenadas de origen como string
    public String getCoordenadasOrigenString() {
        return origenLatitud + "," + origenLongitud;
    }

    // Obtener coordenadas de destino como string
    public String getCoordenadasDestinoString() {
        return destinoLatitud + "," + destinoLongitud;
    }

    // Verificar si está en un estado final
    public boolean esEstadoFinal() {
        return "finalizada".equals(estado) || "cancelada".equals(estado) || "rechazada".equals(estado);
    }

    // Verificar si está activa (puede tener cambios de estado)
    public boolean estaActiva() {
        return !esEstadoFinal();
    }

    // Obtener descripción del estado
    public String getDescripcionEstado() {
        if (estado == null) return "Estado desconocido";

        switch (estado.toLowerCase()) {
            case "pendiente":
                return "Esperando taxista";
            case "aceptada":
                return "Aceptada por taxista";
            case "en_camino":
                return "Taxista en camino";
            case "llegado":
                return "Taxista ha llegado";
            case "finalizada":
                return "Viaje completado";
            case "cancelada":
                return "Viaje cancelado";
            case "rechazada":
                return "Solicitud rechazada";
            default:
                return estado;
        }
    }

    @Override
    public String toString() {
        return "SolicitudTaxi{" +
                "solicitudId='" + solicitudId + '\'' +
                ", reservaId='" + reservaId + '\'' +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", nombrePasajero='" + nombrePasajero + '\'' +
                ", numeroPasajeros=" + numeroPasajeros +
                ", estado='" + estado + '\'' +
                ", esAceptada=" + esAceptada +
                ", fechaSalida='" + getFechaSalidaFormateada() + " " + getHoraSalidaFormateada() + '\'' +
                ", tipoVehiculo='" + tipoVehiculo + '\'' +
                ", coordenadasValidas=" + tieneCoordenadasValidas() +
                '}';
    }

    // Método para obtener información resumida
    public String getResumen() {
        return String.format("Solicitud #%s - %s a %s - %d pasajero(s) - %s",
                solicitudId != null ? solicitudId.substring(0, Math.min(8, solicitudId.length())) : "???",
                origen != null ? origen : "Origen desconocido",
                destino,
                numeroPasajeros,
                getDescripcionEstado());
    }
}