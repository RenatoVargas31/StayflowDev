package com.iot.stayflowdev.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.model.SystemLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Servicio para crear y gestionar logs del sistema
 */
public class LogService {
    private static final String TAG = "LogService";
    private static final String COLLECTION_LOGS = "system_logs";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Constructor
    public LogService() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Crea un log cuando se crea un usuario/administrador
     * @param createdUserId ID del usuario creado
     * @param userName Nombre del usuario creado
     * @param userEmail Email del usuario creado
     * @param userRole Rol asignado al usuario
     */
    public void logUserCreation(String createdUserId, String userName, String userEmail, String userRole) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado para crear el log");
            return;
        }

        // Formato para mostrar la fecha en el título del log
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());

        // Crear título y descripción descriptivos
        String title = "Usuario creado: " + userName;
        String description = "Se ha creado un nuevo usuario de tipo " + getUserRoleDescription(userRole) +
                            " con email " + userEmail + " en fecha " + formattedDate;

        // Obtener el nombre del creador (usuario actual)
        String creatorName = currentUser.getDisplayName();
        if (creatorName == null || creatorName.isEmpty()) {
            creatorName = "SuperAdmin"; // Valor por defecto si no hay nombre disponible
        }

        // Crear el objeto SystemLog
        SystemLog systemLog = new SystemLog(
            title,
            description,
            SystemLog.CATEGORY_ACCOUNT,
            SystemLog.ACTION_USER_CREATED,
            currentUser.getUid(),
            creatorName,
            createdUserId,
            userName
        );

        // Agregar datos adicionales específicos de la creación de usuario
        systemLog.addAdditionalData("email", userEmail);
        systemLog.addAdditionalData("role", userRole);

        // Guardar en Firestore
        saveLogToFirestore(systemLog);
    }

    /**
     * Método genérico para registrar cualquier tipo de evento en el sistema
     * @param eventType Tipo de evento (ej. "creacion_usuario", "login", etc.)
     * @param description Descripción del evento
     * @param targetId ID del objeto relacionado con el evento (ej. ID de usuario, hotel, etc.)
     */
    public void registrarEvento(String eventType, String description, String targetId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado para crear el log");
            return;
        }

        // Determinar categoría basada en el tipo de evento
        String category = SystemLog.CATEGORY_ACCOUNT; // Categoría por defecto
        String actionType = eventType;

        if (eventType.contains("hotel")) {
            category = SystemLog.CATEGORY_HOTELS;
        } else if (eventType.contains("reserva")) {
            category = SystemLog.CATEGORY_RESERVATION;
        }

        // Crear título para el log
        String title = formatEventTypeToTitle(eventType);

        // Obtener el nombre del creador (usuario actual)
        String creatorName = currentUser.getDisplayName();
        if (creatorName == null || creatorName.isEmpty()) {
            creatorName = "SuperAdmin"; // Valor por defecto si no hay nombre disponible
        }

        // Crear el objeto SystemLog
        SystemLog systemLog = new SystemLog(
            title,
            description,
            category,
            actionType,
            currentUser.getUid(),
            creatorName,
            targetId,
            ""  // El nombre del target puede estar vacío en este método genérico
        );

        // Guardar en Firestore
        saveLogToFirestore(systemLog);
    }

    /**
     * Método genérico para registrar cualquier tipo de evento en el sistema con título personalizado
     * @param eventType Tipo de evento (ej. "creacion_usuario", "login", etc.)
     * @param title Título personalizado para el log
     * @param description Descripción del evento
     * @param targetId ID del objeto relacionado con el evento (ej. ID de usuario, hotel, etc.)
     */
    public void registrarEventoConTitulo(String eventType, String title, String description, String targetId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado para crear el log");
            return;
        }

        // Determinar categoría basada en el tipo de evento
        String category = SystemLog.CATEGORY_ACCOUNT; // Categoría por defecto
        String actionType = eventType;

        if (eventType.contains("hotel")) {
            category = SystemLog.CATEGORY_HOTELS;
        } else if (eventType.contains("reserva")) {
            category = SystemLog.CATEGORY_RESERVATION;
        }

        // Obtener el nombre del creador (usuario actual)
        String creatorName = currentUser.getDisplayName();
        if (creatorName == null || creatorName.isEmpty()) {
            creatorName = "SuperAdmin"; // Valor por defecto si no hay nombre disponible
        }

        // Crear el objeto SystemLog
        SystemLog systemLog = new SystemLog(
            title, // Usar el título personalizado proporcionado
            description,
            category,
            actionType,
            currentUser.getUid(),
            creatorName,
            targetId,
            ""  // El nombre del target puede estar vacío en este método genérico
        );

        // Guardar en Firestore
        saveLogToFirestore(systemLog);
    }

    /**
     * Crea un log cuando un usuario inicia sesión
     * @param userId ID del usuario que inicia sesión
     * @param userName Nombre del usuario
     * @param userEmail Email del usuario
     */
    public void logUserLogin(String userId, String userName, String userEmail) {
        // Formato para mostrar la fecha en el título del log
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());

        // Crear título y descripción descriptivos
        String title = "Inicio de sesión: " + userName;
        String description = "El usuario " + userName + " (" + userEmail + ") ha iniciado sesión en " + formattedDate;

        // Crear el objeto SystemLog
        SystemLog systemLog = new SystemLog(
            title,
            description,
            SystemLog.CATEGORY_ACCOUNT,
            SystemLog.ACTION_USER_LOGIN,
            userId,
            userName,
            userId, // En este caso el target es el mismo usuario
            userName
        );

        // Agregar datos adicionales
        systemLog.addAdditionalData("email", userEmail);
        systemLog.addAdditionalData("login_time", new Date());

        // Guardar en Firestore
        saveLogToFirestore(systemLog);
    }

    /**
     * Crea un log cuando se cambia una contraseña
     * @param userId ID del usuario que cambia su contraseña
     * @param userName Nombre del usuario
     */
    public void logPasswordChange(String userId, String userName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No hay usuario autenticado para crear el log");
            return;
        }

        // Formato para mostrar la fecha en el título del log
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());

        // Crear título y descripción descriptivos
        String title = "Cambio de contraseña: " + userName;
        String description = "El usuario " + userName + " ha cambiado su contraseña en " + formattedDate;

        // Crear el objeto SystemLog
        SystemLog systemLog = new SystemLog(
            title,
            description,
            SystemLog.CATEGORY_ACCOUNT,
            SystemLog.ACTION_PASSWORD_CHANGE,
            currentUser.getUid(),
            currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario",
            userId,
            userName
        );

        // Guardar en Firestore
        saveLogToFirestore(systemLog);
    }

    /**
     * Método privado para guardar el log en Firestore
     * @param systemLog Objeto SystemLog a guardar
     */
    private void saveLogToFirestore(SystemLog systemLog) {
        db.collection(COLLECTION_LOGS)
            .add(systemLog)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Log guardado correctamente con ID: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al guardar el log", e);
            });
    }

    /**
     * Convierte el código de rol a una descripción más amigable
     * @param role Código de rol (adminhotel, superadmin, etc.)
     * @return Descripción del rol
     */
    private String getUserRoleDescription(String role) {
        switch (role) {
            case "adminhotel":
                return "Administrador de Hotel";
            case "superadmin":
                return "Super Administrador";
            case "cliente":
                return "Cliente";
            case "staff":
                return "Personal de Hotel";
            default:
                return role;
        }
    }

    /**
     * Convierte un tipo de evento a un título más presentable
     * @param eventType Tipo de evento (formato: creacion_usuario)
     * @return Título formateado (formato: Creación de usuario)
     */
    private String formatEventTypeToTitle(String eventType) {
        String[] parts = eventType.split("_");
        StringBuilder titleBuilder = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                // Convertir primera letra a mayúscula
                titleBuilder.append(Character.toUpperCase(part.charAt(0)))
                           .append(part.substring(1))
                           .append(" ");
            }
        }

        return titleBuilder.toString().trim();
    }
}
