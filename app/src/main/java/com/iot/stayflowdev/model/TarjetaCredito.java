package com.iot.stayflowdev.model;

import java.util.Calendar;

public class TarjetaCredito {
    private String id; // ID del documento (será el UID del usuario)
    private String numero; // Número completo (encriptado en producción)
    private String numeroEnmascarado; // **** **** **** 1234
    private String titular;
    private String mesExpiracion;
    private String anioExpiracion;
    private String cvv; // En producción debe estar encriptado
    private String tipo; // Visa, Mastercard, etc.
    private boolean activa;
    private long fechaCreacion;
    private long fechaActualizacion;

    // Constructor vacío para Firebase
    public TarjetaCredito() {
        this.activa = true;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
    }

    // Constructor principal
    public TarjetaCredito(String numero, String titular, String mesExpiracion,
                          String anioExpiracion, String cvv) {
        this();
        this.numero = numero;
        this.titular = titular;
        this.mesExpiracion = mesExpiracion;
        this.anioExpiracion = anioExpiracion;
        this.cvv = cvv;
        this.numeroEnmascarado = enmascarar(numero);
        this.tipo = detectarTipo(numero);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) {
        this.numero = numero;
        this.numeroEnmascarado = enmascarar(numero);
        this.tipo = detectarTipo(numero);
    }

    public String getNumeroEnmascarado() { return numeroEnmascarado; }
    public void setNumeroEnmascarado(String numeroEnmascarado) { this.numeroEnmascarado = numeroEnmascarado; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getMesExpiracion() { return mesExpiracion; }
    public void setMesExpiracion(String mesExpiracion) { this.mesExpiracion = mesExpiracion; }

    public String getAnioExpiracion() { return anioExpiracion; }
    public void setAnioExpiracion(String anioExpiracion) { this.anioExpiracion = anioExpiracion; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    // Métodos de utilidad
    public String getFechaExpiracionCorta() {
        return mesExpiracion + "/" + anioExpiracion.substring(2); // "12/26"
    }

    public String getFechaExpiracionLarga() {
        return mesExpiracion + "/" + anioExpiracion; // "12/2026"
    }

    public boolean estaVencida() {
        try {
            int mes = Integer.parseInt(mesExpiracion);
            int anio = Integer.parseInt(anioExpiracion);

            Calendar ahora = Calendar.getInstance();
            int mesActual = ahora.get(Calendar.MONTH) + 1; // Calendar.MONTH es 0-based
            int anioActual = ahora.get(Calendar.YEAR);

            return anio < anioActual || (anio == anioActual && mes < mesActual);
        } catch (NumberFormatException e) {
            return true; // Si no se puede parsear, considerar vencida
        }
    }

    // Enmascarar número de tarjeta
    private String enmascarar(String numero) {
        if (numero == null || numero.length() < 4) {
            return "**** **** **** ****";
        }

        String numeroLimpio = numero.replaceAll("\\s+", "");
        if (numeroLimpio.length() < 4) {
            return "**** **** **** ****";
        }

        String ultimos4 = numeroLimpio.substring(numeroLimpio.length() - 4);
        return "**** **** **** " + ultimos4;
    }

    // Detectar tipo de tarjeta basado en el número
    private String detectarTipo(String numero) {
        if (numero == null) return "Desconocida";

        String numeroLimpio = numero.replaceAll("\\s+", "");

        if (numeroLimpio.startsWith("4")) {
            return "Visa";
        } else if (numeroLimpio.startsWith("5") || numeroLimpio.startsWith("2")) {
            return "Mastercard";
        } else if (numeroLimpio.startsWith("34") || numeroLimpio.startsWith("37")) {
            return "American Express";
        } else if (numeroLimpio.startsWith("6")) {
            return "Discover";
        } else {
            return "Desconocida";
        }
    }

    // Validar número de tarjeta (Algoritmo de Luhn simplificado)
    public boolean esNumeroValido() {
        if (numero == null) return false;

        String numeroLimpio = numero.replaceAll("\\s+", "");
        if (numeroLimpio.length() < 13 || numeroLimpio.length() > 19) {
            return false;
        }

        // Verificar que solo contenga dígitos
        return numeroLimpio.matches("\\d+");
    }

    // Validar CVV
    public boolean esCvvValido() {
        if (cvv == null) return false;

        // CVV debe ser 3 o 4 dígitos
        return cvv.matches("\\d{3,4}");
    }

    // Validar fecha de expiración
    public boolean esFechaValida() {
        try {
            int mes = Integer.parseInt(mesExpiracion);
            int anio = Integer.parseInt(anioExpiracion);

            return mes >= 1 && mes <= 12 && anio >= Calendar.getInstance().get(Calendar.YEAR);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validar toda la tarjeta
    public boolean esValida() {
        return esNumeroValido() &&
                esCvvValido() &&
                esFechaValida() &&
                titular != null && !titular.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "TarjetaCredito{" +
                "id='" + id + '\'' +
                ", numeroEnmascarado='" + numeroEnmascarado + '\'' +
                ", titular='" + titular + '\'' +
                ", tipo='" + tipo + '\'' +
                ", activa=" + activa +
                '}';
    }
}
