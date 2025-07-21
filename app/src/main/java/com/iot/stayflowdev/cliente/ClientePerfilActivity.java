package com.iot.stayflowdev.cliente;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.LoginFireBaseActivity;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityClientePerfilBinding;
import com.iot.stayflowdev.model.TarjetaCredito;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.utils.ImageLoadingUtils;
import com.iot.stayflowdev.utils.UserSessionManager;
import com.iot.stayflowdev.viewmodels.TarjetaCreditoViewModel;
import com.iot.stayflowdev.viewmodels.UserViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class ClientePerfilActivity extends AppCompatActivity {

    private ActivityClientePerfilBinding binding;
    private UserViewModel userViewModel;
    private TarjetaCreditoViewModel tarjetaCreditoViewModel;
    private static final String TAG = "ClientePerfilActivity";
    private Dialog tarjetaDialog;

    // ActivityResultLauncher para el resultado de la selección de imagen
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar ViewBinding
        binding = ActivityClientePerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar toolbar
        setSupportActionBar(binding.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Sin padding inferior
            return insets;
        });

        // Inicializar el ActivityResultLauncher para la selección de imágenes
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri imageUri = data.getData();
                            if (imageUri != null) {
                                // Subir la imagen seleccionada a Firebase Storage
                                subirImagenPerfil(imageUri);
                            }
                        }
                    }
                });

        // Inicializar ViewModels
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        tarjetaCreditoViewModel = new ViewModelProvider(this).get(TarjetaCreditoViewModel.class);

        // Observar cambios en los datos
        observeUserData();
        observeTarjetaData();

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Configurar navegación inferior
        setupBottomNavigation();

        // Configurar listeners para botones de edición
        setupEditButtons();
    }

    private void observeUserData() {
        // Observar cuando los datos del usuario cambien
        userViewModel.getUserData().observe(this, user -> {
            if (user != null) {
                // Actualizar la UI con los datos del usuario
                actualizarUI(user);
            }
        });

        // Observar mensajes de error
        userViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(ClientePerfilActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observar estado de carga
        userViewModel.getIsLoading().observe(this, isLoading -> {
            // Aquí podrías mostrar un indicador de carga si es necesario
        });
    }

    private void observeTarjetaData() {
        // Observar si el usuario tiene tarjeta
        tarjetaCreditoViewModel.getHasTarjeta().observe(this, hasTarjeta -> {
            if (hasTarjeta != null) {
                if (!hasTarjeta) {
                    // No tiene tarjeta, mostrar "Sin registrar" en rojo
                    binding.tvTarjetaCredito.setText("Sin registrar");
                    binding.tvTarjetaCredito.setTextColor(Color.RED);
                }
            }
        });

        // Observar datos de la tarjeta
        tarjetaCreditoViewModel.getTarjetaData().observe(this, tarjeta -> {
            if (tarjeta != null) {
                // Mostrar información enmascarada de la tarjeta
                binding.tvTarjetaCredito.setText(tarjeta.getNumeroEnmascarado() + " • " + tarjeta.getFechaExpiracionCorta());
                binding.tvTarjetaCredito.setTextColor(getResources().getColor(R.color.md_theme_outline, null));
            }
        });

        // Observar mensajes de éxito
        tarjetaCreditoViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(ClientePerfilActivity.this, message, Toast.LENGTH_SHORT).show();
                tarjetaCreditoViewModel.limpiarMensajes();

                // Cerrar diálogo si est�� abierto
                if (tarjetaDialog != null && tarjetaDialog.isShowing()) {
                    tarjetaDialog.dismiss();
                }
            }
        });

        // Observar mensajes de error
        tarjetaCreditoViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(ClientePerfilActivity.this, message, Toast.LENGTH_SHORT).show();
                tarjetaCreditoViewModel.limpiarMensajes();
            }
        });
    }

    private void cargarDatosUsuario() {
        // Verificar si hay un usuario autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Cargar datos del usuario usando su ID
            userViewModel.loadUserById(currentUser.getUid());

            // Verificar si el usuario tiene tarjeta
            tarjetaCreditoViewModel.verificarTarjeta();
        } else {
            // No hay usuario autenticado, redirigir al login
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginFireBaseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void actualizarUI(User user) {
        // Nombre completo del usuario
        binding.tvNombreCliente.setText(user.getName());

        // Rol
        binding.tvRol.setText(user.getRoleDescription());

        // Fecha de nacimiento
        if (user.getFechaNacimiento() != null && !user.getFechaNacimiento().isEmpty()) {
            binding.tvFechaNacimiento.setText(user.getFechaNacimiento());
        } else {
            binding.tvFechaNacimiento.setText("No especificada");
        }

        // Domicilio
        if (user.getDomicilio() != null && !user.getDomicilio().isEmpty()) {
            binding.tvDomicilio.setText(user.getDomicilio());
        } else {
            binding.tvDomicilio.setText("No especificado");
        }

        // Documento de identidad
        String documentoTexto = "No especificado";
        if (user.getTipoDocumento() != null && !user.getTipoDocumento().isEmpty() &&
            user.getNumeroDocumento() != null && !user.getNumeroDocumento().isEmpty()) {
            documentoTexto = user.getTipoDocumento() + ": " + user.getNumeroDocumento();
        }
        binding.tvDocumento.setText(documentoTexto);

        // Correo electrónico
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            binding.tvCorreoUser.setText(user.getEmail());
        } else {
            binding.tvCorreoUser.setText("No especificado");
        }

        // Teléfono
        if (user.getTelefono() != null && !user.getTelefono().isEmpty()) {
            binding.tvTelefono.setText(user.getTelefono());
        } else {
            binding.tvTelefono.setText("No especificado");
        }

        // Foto de perfil
        if (user.getFotoPerfilUrl() != null && !user.getFotoPerfilUrl().isEmpty()) {
            Glide.with(this)
                .load(user.getFotoPerfilUrl())
                .placeholder(R.drawable.ic_perfil)
                .error(R.drawable.ic_perfil)
                .circleCrop()
                .into(binding.ivProfilePicture);
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.ic_perfil);
        }

        Log.d(TAG, "Datos de usuario cargados correctamente: " + user.getName());
    }

    private void setupEditButtons() {
        // Botón para editar tarjeta de crédito
        binding.btnEditarTarjeta.setOnClickListener(v -> {
            mostrarDialogoTarjetaCredito();
        });

        // También permitir hacer clic en toda la fila de tarjeta
        binding.layoutTarjeta.setOnClickListener(v -> {
            mostrarDialogoTarjetaCredito();
        });

        // Configurar botón para editar domicilio
        binding.btnEditarDomicilio.setOnClickListener(v -> {
            mostrarDialogoActualizarDomicilio();
        });

        // También permitir hacer clic en toda la fila de domicilio
        binding.layoutDomicilio.setOnClickListener(v -> {
            mostrarDialogoActualizarDomicilio();
        });

        // Configurar botón para editar teléfono
        binding.btnEditarTelefono.setOnClickListener(v -> {
            mostrarDialogoActualizarTelefono();
        });

        // También permitir hacer clic en toda la fila de teléfono
        binding.layoutTelefono.setOnClickListener(v -> {
            mostrarDialogoActualizarTelefono();
        });

        // Botón para cambiar foto de perfil
        binding.buttonChangePicture.setOnClickListener(v -> {
            // Abrir selector de imágenes
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Imagen de perfil clickeable para cambiar foto
        binding.ivProfilePicture.setOnClickListener(v -> {
            // Abrir selector de imágenes
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    private void mostrarDialogoTarjetaCredito() {
        // Crear diálogo personalizado
        tarjetaDialog = new Dialog(this);
        tarjetaDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tarjetaDialog.setContentView(R.layout.dialog_regitro_tarjeta_credito);
        tarjetaDialog.setCancelable(true);

        // Obtener referencias a las vistas del diálogo
        TextInputLayout tilNumeroTarjeta = tarjetaDialog.findViewById(R.id.til_numero_tarjeta);
        TextInputEditText etNumeroTarjeta = tarjetaDialog.findViewById(R.id.et_numero_tarjeta);
        TextInputLayout tilTitular = tarjetaDialog.findViewById(R.id.til_titular);
        TextInputEditText etTitular = tarjetaDialog.findViewById(R.id.et_titular);
        TextInputLayout tilFechaExpiracion = tarjetaDialog.findViewById(R.id.til_fecha_expiracion);
        TextInputEditText etFechaExpiracion = tarjetaDialog.findViewById(R.id.et_fecha_expiracion);
        TextInputLayout tilCvv = tarjetaDialog.findViewById(R.id.til_cvv);
        TextInputEditText etCvv = tarjetaDialog.findViewById(R.id.et_cvv);
        MaterialButton btnCancelar = tarjetaDialog.findViewById(R.id.btn_cancelar);
        MaterialButton btnRegistrar = tarjetaDialog.findViewById(R.id.btn_registrar);

        // Detectar tipo de tarjeta mientras se escribe
        etNumeroTarjeta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Formatear número de tarjeta con espacios cada 4 dígitos
                String input = s.toString().replaceAll("\\s+", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }

                if (!s.toString().equals(formatted.toString())) {
                    etNumeroTarjeta.setText(formatted.toString());
                    etNumeroTarjeta.setSelection(formatted.length());
                }

                // Detectar tipo de tarjeta
                String tipo = detectarTipoTarjeta(input);
                tarjetaDialog.findViewById(R.id.tv_tipo_tarjeta).setVisibility(View.VISIBLE);
                ((android.widget.TextView) tarjetaDialog.findViewById(R.id.tv_tipo_tarjeta)).setText(tipo);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Formatear fecha de expiración automáticamente (MM/YY)
        etFechaExpiracion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.length() == 2 && before == 0) {
                    // Agregar barra después de introducir los 2 primeros dígitos (mes)
                    etFechaExpiracion.setText(input + "/");
                    etFechaExpiracion.setSelection(input.length() + 1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar botones
        btnCancelar.setOnClickListener(v -> tarjetaDialog.dismiss());

        btnRegistrar.setOnClickListener(v -> {
            // Obtener valores de los campos
            String numeroTarjeta = etNumeroTarjeta.getText().toString().replaceAll("\\s+", "");
            String titular = etTitular.getText().toString().trim();
            String fechaExpiracion = etFechaExpiracion.getText().toString();
            String cvv = etCvv.getText().toString();

            // Validar campos
            if (numeroTarjeta.isEmpty() || titular.isEmpty() || fechaExpiracion.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(ClientePerfilActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar formato de fecha
            if (!fechaExpiracion.matches("\\d{2}/\\d{2}")) {
                tilFechaExpiracion.setError("Formato inválido. Use MM/AA");
                return;
            }

            // Extraer mes y año
            String[] fechaParts = fechaExpiracion.split("/");
            String mes = fechaParts[0];
            String anio = "20" + fechaParts[1]; // Convertir 2 dígitos a 4 dígitos

            // Validar mes
            int mesInt = Integer.parseInt(mes);
            if (mesInt < 1 || mesInt > 12) {
                tilFechaExpiracion.setError("Mes inválido (1-12)");
                return;
            }

            // Validar fecha de expiración (no vencida)
            int anioInt = Integer.parseInt(anio);
            int mesActual = Calendar.getInstance().get(Calendar.MONTH) + 1; // Calendar.MONTH es 0-based
            int anioActual = Calendar.getInstance().get(Calendar.YEAR);

            if (anioInt < anioActual || (anioInt == anioActual && mesInt < mesActual)) {
                tilFechaExpiracion.setError("La tarjeta está vencida");
                return;
            }

            // Crear objeto TarjetaCredito
            TarjetaCredito tarjeta = new TarjetaCredito(numeroTarjeta, titular, mes, anio, cvv);

            // Guardar tarjeta
            tarjetaCreditoViewModel.guardarTarjeta(tarjeta);
        });

        // Mostrar diálogo
        tarjetaDialog.show();
    }

    private String detectarTipoTarjeta(String numero) {
        if (numero.isEmpty()) {
            return "Tarjeta";
        } else if (numero.startsWith("4")) {
            return "Visa";
        } else if (numero.startsWith("5") || numero.startsWith("2")) {
            return "Mastercard";
        } else if (numero.startsWith("34") || numero.startsWith("37")) {
            return "American Express";
        } else if (numero.startsWith("6")) {
            return "Discover";
        } else {
            return "Tarjeta";
        }
    }

    private void setupBottomNavigation() {
        // Establecer Perfil como seleccionado
        binding.bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        // Configurar listener de navegación
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Si ya estamos en esta actividad, no hacer nada
            if (itemId == R.id.nav_perfil) {
                return true;
            }

            // Navegación según el ítem seleccionado
            if (itemId == R.id.nav_buscar) {
                navigateToActivity(ClienteBuscarActivity.class);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                navigateToActivity(ClienteReservasActivity.class);
                return true;
            }
            /*
            else if (itemId == R.id.nav_favoritos) {
                navigateToActivity(ClienteFavoritosActivity.class);
                return true;
            }
            */
            return false;
        });
    }

    // Método para navegar sin animación
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0); // Sin animación
        finish();
    }

    // Inflar el menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú de perfil
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    // Manejar eventos del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Manejar los clicks en los items del menú
        if (item.getItemId() == R.id.action_logout) {
            // Cerrar sesión
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Método para cerrar sesión
    private void cerrarSesion() {
        // 1. Obtener el ID del usuario actual antes de cerrar sesi��n
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserEmail = null;
        if (auth.getCurrentUser() != null) {
            currentUserEmail = auth.getCurrentUser().getEmail();
        }

        // 2. Marcar usuario como desconectado en Firestore
        if (currentUserEmail != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("usuarios")
                    .whereEqualTo("correo", currentUserEmail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            UserSessionManager.getInstance().setUserDisconnected(userId);
                            Log.d(TAG, "Usuario " + userId + " marcado como desconectado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al marcar usuario como desconectado: " + e.getMessage());
                    });
        }

        // 3. Cerrar sesión en Firebase
        auth.signOut();

        // 4. Limpiar SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // 5. Mostrar mensaje
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // 6. Redirigir al login
        Intent intent = new Intent(this, LoginFireBaseActivity.class);
        // Flags para limpiar la pila de actividades
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Al presionar atrás, regresar a la pantalla principal
        navigateToActivity(ClienteBuscarActivity.class);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navigateToActivity(ClienteBuscarActivity.class);
        return true;
    }

    private void abrirSelectorImagen() {
        // Crear un intent para abrir el selector de imágenes
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Iniciar la actividad con el launcher
        imagePickerLauncher.launch(intent);
    }

    private void subirImagenPerfil(Uri imageUri) {
        // Mostrar indicador de carga y bloquear interacciones
        mostrarCargando(true);

        // Obtener referencia a Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Crear referencia para la imagen del perfil del usuario
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            StorageReference profileImageRef = storageRef.child("fotos_perfil/" + userId + ".jpg");

            // Subir la imagen
            UploadTask uploadTask = profileImageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Obtener URL de la imagen subida
                profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    // Actualizar URL de la imagen en Firestore
                    actualizarURLImagenEnFirestore(userId, imageUrl);
                });
            }).addOnFailureListener(e -> {
                // Ocultar indicador de carga y restaurar interacciones
                mostrarCargando(false);
                Toast.makeText(ClientePerfilActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al subir imagen de perfil", e);
            });
        } else {
            mostrarCargando(false);
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarURLImagenEnFirestore(String userId, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .document(userId)
                .update("fotoPerfilUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    // Ocultar indicador de carga y restaurar interacciones
                    mostrarCargando(false);
                    Toast.makeText(ClientePerfilActivity.this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();

                    // Recargar datos del usuario para mostrar la nueva imagen
                    cargarDatosUsuario();
                })
                .addOnFailureListener(e -> {
                    // Ocultar indicador de carga y restaurar interacciones
                    mostrarCargando(false);
                    Toast.makeText(ClientePerfilActivity.this, "Error al actualizar foto de perfil", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al actualizar URL de imagen en Firestore", e);
                });
    }

    /**
     * Muestra un diálogo para actualizar el domicilio del usuario
     */
    private void mostrarDialogoActualizarDomicilio() {
        try {
            // Crear diálogo usando AlertDialog.Builder para mayor compatibilidad
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_actualizar_domicilio, null);
            builder.setView(dialogView);

            // Crear el diálogo
            androidx.appcompat.app.AlertDialog domicilioDialog = builder.create();
            domicilioDialog.setCancelable(true);

            // Obtener referencias a las vistas del diálogo
            TextInputLayout tilDomicilio = dialogView.findViewById(R.id.til_domicilio);
            TextInputEditText etDomicilio = dialogView.findViewById(R.id.et_domicilio);
            MaterialButton btnCancelar = dialogView.findViewById(R.id.btn_cancelar);
            MaterialButton btnGuardar = dialogView.findViewById(R.id.btn_guardar);

            // Obtener el domicilio actual del usuario
            User currentUser = userViewModel.getUserData().getValue();
            if (currentUser != null && currentUser.getDomicilio() != null) {
                etDomicilio.setText(currentUser.getDomicilio());
            }

            // Configurar botones
            btnCancelar.setOnClickListener(v -> domicilioDialog.dismiss());

            btnGuardar.setOnClickListener(v -> {
                // Obtener el nuevo domicilio
                String nuevoDomicilio = etDomicilio.getText().toString().trim();

                // Validar que no esté vacío
                if (nuevoDomicilio.isEmpty()) {
                    tilDomicilio.setError("El domicilio no puede estar vacío");
                    return;
                }

                // Obtener ID del usuario actual
                String userId = userViewModel.getCurrentUserId();
                if (userId != null) {
                    // Mostrar indicador de carga
                    mostrarCargando(true);

                    // Actualizar domicilio en Firestore
                    userViewModel.updateUserAddress(userId, nuevoDomicilio);

                    // Observar resultado de la operación
                    userViewModel.getOperationSuccessful().observe(this, success -> {
                        if (success != null && success) {
                            // La operación fue exitosa
                            domicilioDialog.dismiss();
                            Toast.makeText(this, "Domicilio actualizado correctamente", Toast.LENGTH_SHORT).show();
                        }
                        // Ocultar indicador de carga
                        mostrarCargando(false);
                    });
                } else {
                    Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
                }
            });

            // Mostrar diálogo
            domicilioDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar diálogo de domicilio", e);
            Toast.makeText(this, "Error al abrir diálogo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra un diálogo para actualizar el teléfono del usuario
     */
    private void mostrarDialogoActualizarTelefono() {
        try {
            // Crear diálogo usando AlertDialog.Builder para mayor compatibilidad
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_actualizar_telefono, null);
            builder.setView(dialogView);

            // Crear el diálogo
            androidx.appcompat.app.AlertDialog telefonoDialog = builder.create();
            telefonoDialog.setCancelable(true);

            // Obtener referencias a las vistas del diálogo
            TextInputLayout tilTelefono = dialogView.findViewById(R.id.til_telefono);
            TextInputEditText etTelefono = dialogView.findViewById(R.id.et_telefono);
            MaterialButton btnCancelar = dialogView.findViewById(R.id.btn_cancelar);
            MaterialButton btnGuardar = dialogView.findViewById(R.id.btn_guardar);

            // Obtener el teléfono actual del usuario
            User currentUser = userViewModel.getUserData().getValue();
            if (currentUser != null && currentUser.getTelefono() != null) {
                // Quitar el prefijo +51 si existe
                String telefono = currentUser.getTelefono();
                if (telefono.startsWith("+51 ")) {
                    telefono = telefono.substring(4);
                }
                etTelefono.setText(telefono);
            }

            // Configurar botones
            btnCancelar.setOnClickListener(v -> telefonoDialog.dismiss());

            btnGuardar.setOnClickListener(v -> {
                // Obtener el nuevo teléfono
                String nuevoTelefono = etTelefono.getText().toString().trim();

                // Validar que no esté vacío
                if (nuevoTelefono.isEmpty()) {
                    tilTelefono.setError("El teléfono no puede estar vacío");
                    return;
                }

                // Validar formato de teléfono (9 dígitos para Perú)
                if (nuevoTelefono.length() != 9 || !nuevoTelefono.matches("\\d+")) {
                    tilTelefono.setError("Ingrese un número de teléfono válido de 9 dígitos");
                    return;
                }

                // Agregar prefijo +51 (Perú)
                String telefonoCompleto = "+51 " + nuevoTelefono;

                // Obtener ID del usuario actual
                String userId = userViewModel.getCurrentUserId();
                if (userId != null) {
                    // Mostrar indicador de carga
                    mostrarCargando(true);

                    // Actualizar teléfono en Firestore
                    userViewModel.updateUserPhone(userId, telefonoCompleto);

                    // Observar resultado de la operación
                    userViewModel.getOperationSuccessful().observe(this, success -> {
                        if (success != null && success) {
                            // La operación fue exitosa
                            telefonoDialog.dismiss();
                            Toast.makeText(this, "Teléfono actualizado correctamente", Toast.LENGTH_SHORT).show();
                        }
                        // Ocultar indicador de carga
                        mostrarCargando(false);
                    });
                } else {
                    Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
                }
            });

            // Mostrar diálogo
            telefonoDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar diálogo de teléfono", e);
            Toast.makeText(this, "Error al abrir diálogo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Método para mostrar/ocultar estado de carga y bloquear interacciones
    private void mostrarCargando(boolean mostrar) {
        if (mostrar) {
            // Mostrar indicador de progreso
            binding.progressIndicator.setVisibility(View.VISIBLE);

            // Deshabilitar navegación inferior
            binding.bottomNavigation.setEnabled(false);
            for (int i = 0; i < binding.bottomNavigation.getMenu().size(); i++) {
                binding.bottomNavigation.getMenu().getItem(i).setEnabled(false);
            }

            // Deshabilitar botón de cambiar foto
            binding.buttonChangePicture.setEnabled(false);

            // Deshabilitar imagen de perfil clickeable
            binding.ivProfilePicture.setEnabled(false);
            binding.ivProfilePicture.setClickable(false);

            // Deshabilitar todas las opciones editables
            binding.layoutFechaNacimiento.setEnabled(false);
            binding.layoutFechaNacimiento.setClickable(false);
            binding.layoutDomicilio.setEnabled(false);
            binding.layoutDomicilio.setClickable(false);
            binding.layoutDocumento.setEnabled(false);
            binding.layoutDocumento.setClickable(false);
            binding.layoutCorreo.setEnabled(false);
            binding.layoutCorreo.setClickable(false);
            binding.layoutTelefono.setEnabled(false);
            binding.layoutTelefono.setClickable(false);
            binding.layoutTarjeta.setEnabled(false);
            binding.layoutTarjeta.setClickable(false);

            // Deshabilitar botones de edición
            binding.btnEditarDomicilio.setEnabled(false);
            binding.btnEditarTelefono.setEnabled(false);
            binding.btnEditarTarjeta.setEnabled(false);

            // Deshabilitar scroll
            binding.scrollView.setEnabled(false);

            // Opcional: Cambiar alpha para indicar visualmente que está deshabilitado
            binding.bottomNavigation.setAlpha(0.5f);
            binding.buttonChangePicture.setAlpha(0.5f);

        } else {
            // Ocultar indicador de progreso
            binding.progressIndicator.setVisibility(View.GONE);

            // Habilitar navegación inferior
            binding.bottomNavigation.setEnabled(true);
            for (int i = 0; i < binding.bottomNavigation.getMenu().size(); i++) {
                binding.bottomNavigation.getMenu().getItem(i).setEnabled(true);
            }

            // Habilitar botón de cambiar foto
            binding.buttonChangePicture.setEnabled(true);

            // Habilitar imagen de perfil clickeable
            binding.ivProfilePicture.setEnabled(true);
            binding.ivProfilePicture.setClickable(true);

            // Habilitar todas las opciones editables
            binding.layoutFechaNacimiento.setEnabled(true);
            binding.layoutFechaNacimiento.setClickable(true);
            binding.layoutDomicilio.setEnabled(true);
            binding.layoutDomicilio.setClickable(true);
            binding.layoutDocumento.setEnabled(true);
            binding.layoutDocumento.setClickable(true);
            binding.layoutCorreo.setEnabled(true);
            binding.layoutCorreo.setClickable(true);
            binding.layoutTelefono.setEnabled(true);
            binding.layoutTelefono.setClickable(true);
            binding.layoutTarjeta.setEnabled(true);
            binding.layoutTarjeta.setClickable(true);

            // Habilitar botones de edición
            binding.btnEditarDomicilio.setEnabled(true);
            binding.btnEditarTelefono.setEnabled(true);
            binding.btnEditarTarjeta.setEnabled(true);

            // Habilitar scroll
            binding.scrollView.setEnabled(true);

            // Restaurar alpha normal
            binding.bottomNavigation.setAlpha(1.0f);
            binding.buttonChangePicture.setAlpha(1.0f);
        }
    }
}

