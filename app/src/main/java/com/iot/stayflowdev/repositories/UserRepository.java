package com.iot.stayflowdev.repositories;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio para gestionar operaciones relacionadas con usuarios en Firestore
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String COLLECTION_USERS = "usuarios";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    // Instancia única (Singleton)
    private static UserRepository instance;

    // Constructor privado para implementar Singleton
    private UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    // Método para obtener la instancia única
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Obtiene el usuario actual autenticado
     * @return Usuario actual o null si no hay sesión
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Obtiene un usuario por su ID desde Firestore
     * @param userId ID del usuario
     * @return Task con el resultado de la operación
     */
    public Task<DocumentSnapshot> getUserById(String userId) {
        Log.d(TAG, "Obteniendo usuario con ID: " + userId);
        return db.collection(COLLECTION_USERS).document(userId).get();
    }

    /**
     * Obtiene el perfil del usuario actualmente autenticado
     * @return Task con el resultado o null si no hay usuario autenticado
     */
    public Task<DocumentSnapshot> getCurrentUserProfile() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            return getUserById(currentUser.getUid());
        }
        return null;
    }

    /**
     * Actualiza un campo específico del usuario
     * @param userId ID del usuario
     * @param field Nombre del campo
     * @param value Nuevo valor
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateUserField(String userId, String field, Object value) {
        Log.d(TAG, "Actualizando campo " + field + " para usuario " + userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put(field, value);
        return db.collection(COLLECTION_USERS).document(userId).update(updates);
    }

    /**
     * Actualiza varios campos del usuario a la vez
     * @param userId ID del usuario
     * @param updates Mapa con los campos a actualizar
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateUserFields(String userId, Map<String, Object> updates) {
        Log.d(TAG, "Actualizando múltiples campos para usuario " + userId);
        return db.collection(COLLECTION_USERS).document(userId).update(updates);
    }

    /**
     * Actualiza el teléfono del usuario
     * @param userId ID del usuario
     * @param phone Nuevo número de teléfono
     * @return Task con el resultado de la operación
     */
    public Task<Void> updatePhone(String userId, String phone) {
        Log.d(TAG, "Actualizando tel��fono para usuario " + userId);
        return updateUserField(userId, "telefono", phone);
    }

    /**
     * Actualiza el domicilio del usuario
     * @param userId ID del usuario
     * @param address Nueva dirección
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateAddress(String userId, String address) {
        Log.d(TAG, "Actualizando domicilio para usuario " + userId);
        return updateUserField(userId, "domicilio", address);
    }

    /**
     * Actualiza la fecha de nacimiento del usuario
     * @param userId ID del usuario
     * @param birthDate Nueva fecha de nacimiento
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateBirthDate(String userId, String birthDate) {
        Log.d(TAG, "Actualizando fecha de nacimiento para usuario " + userId);
        return updateUserField(userId, "fechaNacimiento", birthDate);
    }

    /**
     * Actualiza el documento de identidad del usuario
     * @param userId ID del usuario
     * @param docType Tipo de documento
     * @param docNumber Número de documento
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateIdentityDocument(String userId, String docType, String docNumber) {
        Log.d(TAG, "Actualizando documento de identidad para usuario " + userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("tipoDocumento", docType);
        updates.put("numeroDocumento", docNumber);
        return updateUserFields(userId, updates);
    }

    /**
     * Actualiza la URL de la foto de perfil del usuario
     * @param userId ID del usuario
     * @param photoUrl Nueva URL de la foto
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateProfilePhotoUrl(String userId, String photoUrl) {
        Log.d(TAG, "Actualizando foto de perfil para usuario " + userId);
        return updateUserField(userId, "fotoPerfilUrl", photoUrl);
    }

    /**
     * Sube una foto de perfil al Storage y actualiza la URL en Firestore
     * @param userId ID del usuario
     * @param imageData Datos de la imagen en bytes
     * @return UploadTask para monitorear la subida
     */
    public UploadTask uploadProfilePhoto(String userId, byte[] imageData) {
        Log.d(TAG, "Subiendo foto de perfil para usuario " + userId);
        // Crear referencia al Storage para la foto de perfil
        StorageReference storageRef = storage.getReference()
                .child("profile_photos")
                .child(userId + "_" + System.currentTimeMillis() + ".jpg");

        // Subir la imagen
        return storageRef.putBytes(imageData);
    }

    /**
     * Actualiza la marca de tiempo de la última conexión del usuario
     * @param userId ID del usuario
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateLastConnection(String userId) {
        Log.d(TAG, "Actualizando última conexión para usuario " + userId);
        return updateUserField(userId, "ultimaConexion", FieldValue.serverTimestamp());
    }

    /**
     * Actualiza el estado de conexión del usuario
     * @param userId ID del usuario
     * @param isConnected Estado de conexión
     * @return Task con el resultado de la operación
     */
    public Task<Void> updateConnectionStatus(String userId, boolean isConnected) {
        Log.d(TAG, "Actualizando estado de conexión para usuario " + userId + ": " + isConnected);
        Map<String, Object> updates = new HashMap<>();
        updates.put("conectado", isConnected);
        if (!isConnected) {
            updates.put("ultimaConexion", FieldValue.serverTimestamp());
        }
        return updateUserFields(userId, updates);
    }

    /**
     * Busca usuarios por rol
     * @param role Rol a buscar
     * @return Task con el resultado de la consulta
     */
    public Task<QuerySnapshot> getUsersByRole(String role) {
        Log.d(TAG, "Buscando usuarios con rol: " + role);
        return db.collection(COLLECTION_USERS)
                .whereEqualTo("rol", role)
                .get();
    }

    /**
     * Crea un nuevo documento de usuario en Firestore
     * @param user Objeto usuario a crear
     * @param userId ID del usuario (opcional, si es null se genera automáticamente)
     * @return Task con el resultado de la operación
     */
    public Task<Void> createUser(User user, String userId) {
        DocumentReference userRef;
        if (userId != null && !userId.isEmpty()) {
            userRef = db.collection(COLLECTION_USERS).document(userId);
            user.setUid(userId);
        } else {
            userRef = db.collection(COLLECTION_USERS).document();
            user.setUid(userRef.getId());
        }

        Log.d(TAG, "Creando nuevo usuario con ID: " + user.getUid());
        return userRef.set(user);
    }
}
