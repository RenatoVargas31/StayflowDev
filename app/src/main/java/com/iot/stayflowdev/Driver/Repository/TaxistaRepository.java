package com.iot.stayflowdev.Driver.Repository;

import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.model.User;

public class TaxistaRepository {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    public TaxistaRepository() {
        // Obtener instancias de Firebase
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    private String obtenerUidUsuario() {
        // Obtener el UID del usuario autenticado
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    // Obtener información del usuario taxista desde la colección "usuarios"
    public void obtenerInformacionTaxista(OnSuccessListener<User> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Convertir el documento a objeto User
                            User usuario = documentSnapshot.toObject(User.class);

                            if (usuario != null) {
                                // Verificar que el usuario tiene rol de driver/taxista
                                if ("driver".equals(usuario.getRol()) || "taxista".equals(usuario.getRol())) {
                                    success.onSuccess(usuario);
                                } else {
                                    failure.onFailure(new Exception("El usuario no tiene rol de taxista"));
                                }
                            } else {
                                failure.onFailure(new Exception("Error al convertir datos del usuario"));
                            }
                        } else {
                            failure.onFailure(new Exception("Usuario no encontrado"));
                        }
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Obtener URL de descarga de la imagen de perfil
    public void obtenerImagenPerfil(OnSuccessListener<String> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imagenPerfilURL = documentSnapshot.getString("fotoPerfilUrl");

                            if (imagenPerfilURL != null && !imagenPerfilURL.isEmpty()) {
                                // Si la URL ya es una URL de descarga completa, devolverla directamente
                                if (imagenPerfilURL.startsWith("https://")) {
                                    success.onSuccess(imagenPerfilURL);
                                } else {
                                    // Si es solo la ruta del Storage, obtener la URL de descarga
                                    obtenerURLDescargaStorage(imagenPerfilURL, success, failure);
                                }
                            } else {
                                failure.onFailure(new Exception("No hay imagen de perfil configurada"));
                            }
                        } else {
                            failure.onFailure(new Exception("Usuario no encontrado"));
                        }
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Obtener URL de descarga desde Storage usando la ruta
    private void obtenerURLDescargaStorage(String rutaStorage, OnSuccessListener<String> success, OnFailureListener failure) {
        StorageReference imageRef = storage.getReference().child(rutaStorage);

        imageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> success.onSuccess(uri.toString()))
                .addOnFailureListener(failure);
    }

    // Método combinado: obtener usuario CON imagen de perfil
    public void obtenerTaxistaConImagen(OnSuccessListener<TaxistaConImagen> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User usuario = documentSnapshot.toObject(User.class);

                            if (usuario != null) {
                                if ("driver".equals(usuario.getRol()) || "taxista".equals(usuario.getRol())) {

                                    String imagenPerfilURL = documentSnapshot.getString("fotoPerfilUrl");

                                    if (imagenPerfilURL != null && !imagenPerfilURL.isEmpty()) {
                                        // Obtener URL de la imagen
                                        if (imagenPerfilURL.startsWith("https://")) {
                                            // URL completa
                                            success.onSuccess(new TaxistaConImagen(usuario, imagenPerfilURL));
                                        } else {
                                            // Ruta de Storage
                                            obtenerURLDescargaStorage(imagenPerfilURL,
                                                    urlImagen -> success.onSuccess(new TaxistaConImagen(usuario, urlImagen)),
                                                    error -> success.onSuccess(new TaxistaConImagen(usuario, null))
                                            );
                                        }
                                    } else {
                                        // Sin imagen
                                        success.onSuccess(new TaxistaConImagen(usuario, null));
                                    }
                                } else {
                                    failure.onFailure(new Exception("El usuario no tiene rol de taxista"));
                                }
                            } else {
                                failure.onFailure(new Exception("Error al convertir datos del usuario"));
                            }
                        } else {
                            failure.onFailure(new Exception("Usuario no encontrado"));
                        }
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Subir nueva imagen de perfil
    public void subirImagenPerfil(Uri imagenUri, OnSuccessListener<String> success, OnFailureListener failure,
                                  OnProgressListener<UploadTask.TaskSnapshot> progress) {
        try {
            String uid = obtenerUidUsuario();

            // Crear referencia con nombre único
            String nombreArchivo = "perfil_" + uid + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storage.getReference()
                    .child("perfiles_taxistas")
                    .child(nombreArchivo);

            // Subir imagen
            UploadTask uploadTask = imageRef.putFile(imagenUri);

            // Progreso de subida
            if (progress != null) {
                uploadTask.addOnProgressListener(progress);
            }

            // Al completar la subida
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Obtener URL de descarga
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String downloadUrl = task.getResult().toString();

                    // Actualizar la URL en Firestore
                    actualizarURLImagenPerfil(downloadUrl, success, failure);
                } else {
                    failure.onFailure(task.getException());
                }
            });

        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Actualizar URL de imagen en Firestore
    private void actualizarURLImagenPerfil(String downloadUrl, OnSuccessListener<String> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection("usuarios")
                    .document(uid)
                    .update("fotoPerfilUrl", downloadUrl)
                    .addOnSuccessListener(aVoid -> success.onSuccess(downloadUrl))
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    public boolean usuarioEstaAutenticado() {
        return mAuth.getCurrentUser() != null;
    }

    public String obtenerUidActual() {
        return obtenerUidUsuario();
    }

    // Método público para actualizar la imagen de perfil con callbacks
    public void actualizarImagenPerfil(String userId, String imageUrl, Runnable onSuccess, OnFailureListener onFailure) {
        db.collection("usuarios")
                .document(userId)
                .update("fotoPerfilUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // Clase helper para devolver usuario con imagen
    public static class TaxistaConImagen {
        private User usuario;
        private String urlImagen;

        public TaxistaConImagen(User usuario, String urlImagen) {
            this.usuario = usuario;
            this.urlImagen = urlImagen;
        }

        public User getUsuario() {
            return usuario;
        }

        public String getUrlImagen() {
            return urlImagen;
        }

        public boolean tieneImagen() {
            return urlImagen != null && !urlImagen.isEmpty();
        }
    }
}
