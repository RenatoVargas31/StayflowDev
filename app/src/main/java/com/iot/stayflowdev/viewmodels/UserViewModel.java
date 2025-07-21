package com.iot.stayflowdev.viewmodels;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.repositories.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ViewModel para gestionar los datos de usuario y su interacción con la UI
 */
public class UserViewModel extends ViewModel {
    private static final String TAG = "UserViewModel";
    
    // Repository para acceder a datos
    private final UserRepository repository;
    
    // LiveData para comunicar datos a la UI
    private final MutableLiveData<User> userData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccessful = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    // Constructor
    public UserViewModel() {
        repository = UserRepository.getInstance();
    }
    
    // Getters para los LiveData
    public LiveData<User> getUserData() {
        return userData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getOperationSuccessful() {
        return operationSuccessful;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario o null si no hay sesión
     */
    public String getCurrentUserId() {
        FirebaseUser currentUser = repository.getCurrentUser();
        return (currentUser != null) ? currentUser.getUid() : null;
    }
    
    /**
     * Carga los datos del usuario actual desde Firestore
     */
    public void loadCurrentUser() {
        FirebaseUser currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            loadUserById(currentUser.getUid());
        } else {
            errorMessage.setValue("No hay usuario autenticado");
        }
    }
    
    /**
     * Carga los datos de un usuario específico por su ID
     * @param userId ID del usuario
     */
    public void loadUserById(String userId) {
        if (userId == null || userId.isEmpty()) {
            errorMessage.setValue("ID de usuario no válido");
            return;
        }
        
        isLoading.setValue(true);
        
        repository.getUserById(userId)
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    userData.setValue(user);
                } else {
                    errorMessage.setValue("Usuario no encontrado");
                }
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al cargar usuario", e);
                errorMessage.setValue("Error al cargar datos: " + e.getMessage());
                isLoading.setValue(false);
            });
    }
    
    /**
     * Actualiza el número de teléfono del usuario
     * @param userId ID del usuario
     * @param newPhone Nuevo número de teléfono
     */
    public void updateUserPhone(String userId, String newPhone) {
        if (userId == null || userId.isEmpty()) {
            errorMessage.setValue("ID de usuario no válido");
            return;
        }
        
        isLoading.setValue(true);
        
        repository.updatePhone(userId, newPhone)
            .addOnSuccessListener(aVoid -> {
                // Actualizar el objeto User en memoria
                User currentUser = userData.getValue();
                if (currentUser != null) {
                    currentUser.setTelefono(newPhone);
                    userData.setValue(currentUser);
                }
                
                successMessage.setValue("Teléfono actualizado correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar teléfono", e);
                errorMessage.setValue("Error al actualizar teléfono: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            });
    }
    
    /**
     * Actualiza el domicilio del usuario
     * @param userId ID del usuario
     * @param newAddress Nuevo domicilio
     */
    public void updateUserAddress(String userId, String newAddress) {
        if (userId == null || userId.isEmpty()) {
            errorMessage.setValue("ID de usuario no válido");
            return;
        }
        
        isLoading.setValue(true);
        
        repository.updateAddress(userId, newAddress)
            .addOnSuccessListener(aVoid -> {
                // Actualizar el objeto User en memoria
                User currentUser = userData.getValue();
                if (currentUser != null) {
                    currentUser.setDomicilio(newAddress);
                    userData.setValue(currentUser);
                }

                successMessage.setValue("Domicilio actualizado correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar domicilio", e);
                errorMessage.setValue("Error al actualizar domicilio: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            });
    }

    /**
     * Actualiza la fecha de nacimiento del usuario
     * @param userId ID del usuario
     * @param newBirthDate Nueva fecha de nacimiento
     */
    public void updateUserBirthDate(String userId, String newBirthDate) {
        if (userId == null || userId.isEmpty()) {
            errorMessage.setValue("ID de usuario no válido");
            return;
        }

        isLoading.setValue(true);

        repository.updateBirthDate(userId, newBirthDate)
            .addOnSuccessListener(aVoid -> {
                // Actualizar el objeto User en memoria
                User currentUser = userData.getValue();
                if (currentUser != null) {
                    currentUser.setFechaNacimiento(newBirthDate);
                    userData.setValue(currentUser);
                }

                successMessage.setValue("Fecha de nacimiento actualizada correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar fecha de nacimiento", e);
                errorMessage.setValue("Error al actualizar fecha: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            });
    }

    /**
     * Actualiza la información del documento de identidad del usuario
     * @param userId ID del usuario
     * @param docType Tipo de documento
     * @param docNumber Número de documento
     */
    public void updateUserDocument(String userId, String docType, String docNumber) {
        if (userId == null || userId.isEmpty()) {
            errorMessage.setValue("ID de usuario no válido");
            return;
        }

        isLoading.setValue(true);

        repository.updateIdentityDocument(userId, docType, docNumber)
            .addOnSuccessListener(aVoid -> {
                // Actualizar el objeto User en memoria
                User currentUser = userData.getValue();
                if (currentUser != null) {
                    currentUser.setTipoDocumento(docType);
                    currentUser.setNumeroDocumento(docNumber);
                    userData.setValue(currentUser);
                }

                successMessage.setValue("Documento de identidad actualizado correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar documento", e);
                errorMessage.setValue("Error al actualizar documento: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            });
    }

    /**
     * Actualiza la foto de perfil del usuario
     * @param userId ID del usuario
     * @param imageStream Stream de la imagen
     */
    public void updateProfilePhoto(String userId, InputStream imageStream) {
        if (userId == null || userId.isEmpty() || imageStream == null) {
            errorMessage.setValue("Datos no válidos para actualizar foto de perfil");
            return;
        }

        isLoading.setValue(true);

        try {
            // Convertir InputStream a ByteArray
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384]; // 16KB buffer

            while ((nRead = imageStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] imageData = buffer.toByteArray();

            // Subir la imagen
            UploadTask uploadTask = repository.uploadProfilePhoto(userId, imageData);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful() && task.getException() != null) {
                    throw task.getException();
                }

                // Obtener la URL de la imagen subida
                return uploadTask.getResult().getStorage().getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Uri downloadUri = task.getResult();
                    String photoUrl = downloadUri.toString();

                    // Actualizar la URL en Firestore
                    repository.updateProfilePhotoUrl(userId, photoUrl)
                        .addOnSuccessListener(aVoid -> {
                            // Actualizar el objeto User en memoria
                            User currentUser = userData.getValue();
                            if (currentUser != null) {
                                currentUser.setFotoPerfilUrl(photoUrl);
                                userData.setValue(currentUser);
                            }

                            successMessage.setValue("Foto de perfil actualizada correctamente");
                            operationSuccessful.setValue(true);
                            isLoading.setValue(false);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al actualizar URL de la foto", e);
                            errorMessage.setValue("Error al actualizar foto: " + e.getMessage());
                            operationSuccessful.setValue(false);
                            isLoading.setValue(false);
                        });
                } else {
                    Log.e(TAG, "Error al obtener URL de descarga", task.getException());
                    errorMessage.setValue("Error al subir imagen: " +
                            (task.getException() != null ? task.getException().getMessage() : "Error desconocido"));
                    operationSuccessful.setValue(false);
                    isLoading.setValue(false);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error al procesar imagen", e);
            errorMessage.setValue("Error al procesar imagen: " + e.getMessage());
            operationSuccessful.setValue(false);
            isLoading.setValue(false);
        }
    }

    /**
     * Cambia la contraseña del usuario actual
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     */
    public void changePassword(String currentPassword, String newPassword) {
        FirebaseUser currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("No hay usuario autenticado");
            return;
        }

        if (currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            errorMessage.setValue("El usuario no tiene un email válido");
            return;
        }

        isLoading.setValue(true);

        // Reautenticar al usuario
        AuthCredential credential = EmailAuthProvider.getCredential(
                currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential)
            .addOnSuccessListener(aVoid -> {
                // Cambiar contraseña
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid1 -> {
                        successMessage.setValue("Contraseña actualizada correctamente");
                        operationSuccessful.setValue(true);
                        isLoading.setValue(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al actualizar contraseña", e);
                        errorMessage.setValue("Error al actualizar contraseña: " + e.getMessage());
                        operationSuccessful.setValue(false);
                        isLoading.setValue(false);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error en reautenticación", e);
                errorMessage.setValue("Contraseña actual incorrecta");
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            });
    }

    /**
     * Actualiza el estado de conexión del usuario
     * @param userId ID del usuario
     * @param isConnected Estado de conexión
     */
    public void updateUserConnectionStatus(String userId, boolean isConnected) {
        if (userId == null || userId.isEmpty()) {
            return; // Silenciosamente no hacer nada
        }

        repository.updateConnectionStatus(userId, isConnected)
            .addOnFailureListener(e ->
                Log.e(TAG, "Error al actualizar estado de conexión para " + userId, e));
    }

    /**
     * Actualiza la marca de tiempo de última conexión del usuario
     * @param userId ID del usuario
     */
    public void updateUserLastConnection(String userId) {
        if (userId == null || userId.isEmpty()) {
            return; // Silenciosamente no hacer nada
        }

        repository.updateLastConnection(userId)
            .addOnFailureListener(e ->
                Log.e(TAG, "Error al actualizar última conexión para " + userId, e));
    }

    /**
     * Actualiza varios campos del usuario a la vez
     * @param userId ID del usuario
     * @param updates Mapa con los campos a actualizar
     */
    public void updateUserFields(String userId, Map<String, Object> updates) {
        if (userId == null || userId.isEmpty() || updates == null || updates.isEmpty()) {
            errorMessage.setValue("Datos no válidos para actualización");
            return;
        }

        isLoading.setValue(true);

        repository.updateUserFields(userId, updates)
            .addOnSuccessListener(aVoid -> {
                // Actualizar el objeto User en memoria
                User currentUser = userData.getValue();
                if (currentUser != null) {
                    // Actualizar cada campo en el objeto User
                    for (Map.Entry<String, Object> entry : updates.entrySet()) {
                        String field = entry.getKey();
                        Object value = entry.getValue();

                        switch (field) {
                            case "nombres":
                                if (value instanceof String) currentUser.setNombres((String) value);
                                break;
                            case "apellidos":
                                if (value instanceof String) currentUser.setApellidos((String) value);
                                break;
                            case "correo":
                                if (value instanceof String) currentUser.setEmail((String) value);
                                break;
                            case "telefono":
                                if (value instanceof String) currentUser.setTelefono((String) value);
                                break;
                            case "fechaNacimiento":
                                if (value instanceof String) currentUser.setFechaNacimiento((String) value);
                                break;
                            case "domicilio":
                                if (value instanceof String) currentUser.setDomicilio((String) value);
                                break;
                            case "tipoDocumento":
                                if (value instanceof String) currentUser.setTipoDocumento((String) value);
                                break;
                            case "numeroDocumento":
                                if (value instanceof String) currentUser.setNumeroDocumento((String) value);
                                break;
                            case "fotoPerfilUrl":
                                if (value instanceof String) currentUser.setFotoPerfilUrl((String) value);
                                break;
                            // Añadir más casos según sea necesario
                        }
                    }

                    userData.setValue(currentUser);
                }

                successMessage.setValue("Información actualizada correctamente");
                operationSuccessful.setValue(true);
                isLoading.setValue(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al actualizar campos", e);
                errorMessage.setValue("Error al actualizar información: " + e.getMessage());
                operationSuccessful.setValue(false);
                isLoading.setValue(false);
            });
    }
}
