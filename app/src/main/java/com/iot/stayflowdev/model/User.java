package com.iot.stayflowdev.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.Map;
import java.util.HashMap;

public class User {
    @DocumentId
    private String uid;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String rol;
    private String fotoPerfilUrl;
    private boolean estado; // Cambiado de String a boolean
    private String fechaNacimiento;
    private String tipoDocumento;
    private String numeroDocumento;
    private String domicilio;
    private Map<String, Object> datosEspecificos;

    // Constructor vacío requerido por Firestore
    public User() {
        // Constructor vacío necesario para Firestore
    }

    public User(String uid, String nombres, String apellidos, String email, String telefono,
                String rol, String fotoPerfilUrl, boolean estado, String fechaNacimiento,
                String tipoDocumento, String numeroDocumento, String domicilio,
                Map<String, Object> datosEspecificos) {
        this.uid = uid;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.estado = estado;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.domicilio = domicilio;
        this.datosEspecificos = datosEspecificos;
    }

    // Constructor simplificado para compatibilidad con código existente
    public User(String nombres, String apellidos, String email, String rol, boolean estado) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
        this.estado = estado;
        this.datosEspecificos = new HashMap<>();
    }

    @DocumentId
    public String getUid() {
        return uid;
    }

    @DocumentId
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    @PropertyName("correo")
    public String getEmail() {
        return email;
    }

    @PropertyName("correo")
    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    @PropertyName("datosEspecificos")
    public Map<String, Object> getDatosEspecificos() {
        return datosEspecificos;
    }

    @PropertyName("datosEspecificos")
    public void setDatosEspecificos(Map<String, Object> datosEspecificos) {
        this.datosEspecificos = datosEspecificos;
    }

    // Métodos auxiliares para compatibilidad con el código existente
    @Exclude
    public String getName() {
        // Validar si los nombres o apellidos están disponibles
        String nombre = (nombres != null && !nombres.isEmpty()) ? nombres : "Usuario";
        String apellido = (apellidos != null && !apellidos.isEmpty()) ? apellidos : "sin nombre";

        // Si ambos están disponibles, mostrar nombre y apellido
        // Si solo uno está disponible, mostrar ese componente solo
        // Si ninguno está disponible, mostrar mensaje predeterminado
        if (nombres == null && apellidos == null) {
            return "Usuario sin información";
        }

        return nombre + " " + apellido;
    }

    @Exclude
    public String getRole() {
        return rol;
    }

    @Exclude
    public String getRoleDescription() {
        switch (rol) {
            case "adminhotel":
                return "Administrador de Hotel";
            case "driver":
                return "Taxista";
            case "usuario":
                return "Cliente";
            case "superadmin":
                return "Super Administrador";
            default:
                return "";
        }
    }

    @Exclude
    public boolean isEnabled() {
        return estado;
    }

    @Exclude
    public void setEnabled(boolean enabled) {
        this.estado = enabled;
    }
}