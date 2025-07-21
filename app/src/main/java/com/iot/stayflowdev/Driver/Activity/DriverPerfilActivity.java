package com.iot.stayflowdev.Driver.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.Driver.Dtos.Vehiculo;
import com.iot.stayflowdev.Driver.Repository.TarjetaCreditoRepository;
import com.iot.stayflowdev.Driver.Repository.TaxistaRepository;
import com.iot.stayflowdev.Driver.Repository.VehiculoRepository;
import com.iot.stayflowdev.LoginActivity;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityDriverPerfilBinding;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.utils.ImageLoadingUtils;
import com.iot.stayflowdev.utils.UserSessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class DriverPerfilActivity extends AppCompatActivity {
    private VehiculoRepository vehiculoRepository;
    private static final String TAG = "DriverPerfilActivity";
    private ActivityDriverPerfilBinding binding;
    private TaxistaRepository taxistaRepository;
    private TarjetaCreditoRepository tarjetaRepository; // ← AGREGAR ESTO

    // ActivityResultLauncher para el resultado de la selección de imagen
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Inicializar View Binding
        binding = ActivityDriverPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Inicializar Repository
        taxistaRepository = new TaxistaRepository();
        vehiculoRepository = new VehiculoRepository();
        tarjetaRepository = new TarjetaCreditoRepository(); // ← AGREGAR ESTO

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

        // Cargar información del perfil
        cargarInformacionPerfil();
        configurarBottomNavigationFluido();
        configurarOpcionesPerfil();
    }

    private void cargarInformacionPerfil() {
        if (!taxistaRepository.usuarioEstaAutenticado()) {
            Log.e(TAG, "Usuario no autenticado");
            redirigirALogin();
            return;
        }

        taxistaRepository.obtenerTaxistaConImagen(
                taxistaConImagen -> {
                    User usuario = taxistaConImagen.getUsuario();

                    Log.d(TAG, "Información del usuario cargada exitosamente");

                    // Configurar información básica
                    configurarInformacionBasica(usuario);

                    // Configurar imagen de perfil
                    configurarImagenPerfil(taxistaConImagen);

                    // Configurar información del vehículo (si está disponible)
                    configurarInformacionVehiculo(usuario);

                    // Configurar información de la cuenta
                    configurarInformacionCuenta(usuario);
                },
                exception -> {
                    Log.e(TAG, "Error al cargar información del perfil: " + exception.getMessage());
                    manejarErrorCargaPerfil(exception);
                }
        );
    }

    private void configurarInformacionBasica(User usuario) {
        // Nombre del taxista
        binding.tvNombreTaxista.setText(usuario.getName());

        // Rol con descripción
        binding.tvRol.setText(usuario.getRoleDescription());
    }

    private void configurarImagenPerfil(TaxistaRepository.TaxistaConImagen taxistaConImagen) {
        if (taxistaConImagen.tieneImagen()) {
            Log.d(TAG, "Cargando imagen de perfil desde: " + taxistaConImagen.getUrlImagen());

            Glide.with(this)
                    .load(taxistaConImagen.getUrlImagen())
                    .placeholder(R.drawable.taxista)
                    .error(R.drawable.taxista)
                    .into(binding.ivProfilePicture);
        } else {
            Log.d(TAG, "Sin imagen de perfil, usando imagen por defecto");
            binding.ivProfilePicture.setImageResource(R.drawable.taxista);
        }
    }
    // Cargar información del vehículo

    /*
    private void configurarInformacionVehiculo(User usuario) {
        // Obtener datos básicos del usuario
        String modeloUsuario = usuario.getModelo();
        String placaUsuario = usuario.getPlaca();

        // Configurar información básica del vehículo desde el usuario
        if (modeloUsuario != null && !modeloUsuario.isEmpty()) {
            binding.tvModeloVehiculo.setText(modeloUsuario);
        } else {
            binding.tvModeloVehiculo.setText("Vehículo");
        }

        // Configurar placa desde el usuario - MÉTODO CORREGIDO
        if (placaUsuario != null && !placaUsuario.isEmpty()) {
            binding.tvPlacaTexto.setText(placaUsuario);
            Log.d(TAG, "Llamando cargarDetallesVehiculo con placa: " + placaUsuario);
            cargarDetallesVehiculo(placaUsuario); // ← Verifica que esto se ejecute
        } else {
            Log.w(TAG, "No se puede cargar detalles - placa vacía o null");
            binding.tvPlacaTexto.setText("XXX-000");
            binding.tvMarcaAnio.setText("Configura tu vehículo");
        }
    }   */
    private void configurarInformacionVehiculo(User usuario) {
        // Obtener la placa del usuario
        String placaUsuario = usuario.getPlaca();

        if (placaUsuario != null && !placaUsuario.isEmpty()) {
            Log.d(TAG, "Buscando vehículo con placa del usuario: " + placaUsuario);

            // Usar la placa del usuario para buscar en la colección vehiculo
            vehiculoRepository.obtenerVehiculoTaxista(placaUsuario,
                    vehiculo -> {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Vehículo encontrado: " + vehiculo.toString());
                            mostrarInformacionVehiculo(vehiculo, true);
                        });
                    },
                    exception -> {
                        runOnUiThread(() -> {
                            Log.w(TAG, "No se encontró vehículo con placa: " + placaUsuario +
                                    " - Error: " + exception.getMessage());
                            mostrarVehiculoNoConfigurado();
                        });
                    }
            );
        } else {
            Log.w(TAG, "Usuario no tiene placa configurada");
            mostrarVehiculoNoConfigurado();
        }
    }

    private void mostrarInformacionVehiculo(Vehiculo vehiculo, boolean tieneVehiculo) {
        if (tieneVehiculo && vehiculo != null) {
            // Mostrar información del vehículo encontrado
            binding.tvModeloVehiculo.setText(vehiculo.getMarcaYModelo());
            binding.tvPlacaTexto.setText(vehiculo.getPlacaFormateada());

            // Información adicional basada en el estado
            if (vehiculo.isActivo()) {
                binding.tvCapacidadNumero.setText("4 personas");
                binding.tvMarcaAnio.setText("Activo • Disponible");

                // Cambiar color a verde para activo
                binding.tvMarcaAnio.setTextColor(ContextCompat.getColor(this, R.color.green_500));
            } else {
                binding.tvCapacidadNumero.setText("No disponible");
                binding.tvMarcaAnio.setText("Inactivo • No disponible");

                // Cambiar color a rojo para inactivo
                binding.tvMarcaAnio.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
            }

            Log.d(TAG, "Vehículo mostrado: " + vehiculo.getPlacaFormateada() +
                    " - Estado: " + (vehiculo.isActivo() ? "Activo" : "Inactivo"));
        } else {
            mostrarVehiculoNoConfigurado();
        }
    }

    private void mostrarVehiculoNoConfigurado() {
        binding.tvModeloVehiculo.setText("Sin vehículo");
        binding.tvPlacaTexto.setText("XXX-000");
        binding.tvMarcaAnio.setText("Configura tu vehículo");
        binding.tvCapacidadNumero.setText("0 personas");

        // Color gris para no configurado
        binding.tvMarcaAnio.setTextColor(ContextCompat.getColor(this, R.color.md_theme_onSurfaceVariant));

        Log.d(TAG, "Vehículo no configurado - mostrando valores por defecto");
    }

    // Método específico para recargar solo la información del vehículo
    private void recargarInformacionVehiculo() {
        Log.d(TAG, "Recargando información del vehículo...");

        // Primero necesitamos obtener la información del usuario actualizada
        taxistaRepository.obtenerTaxistaConImagen(
                taxistaConImagen -> {
                    User usuario = taxistaConImagen.getUsuario();
                    String placaUsuario = usuario.getPlaca();

                    if (placaUsuario != null && !placaUsuario.isEmpty()) {
                        // Buscar vehículo por la placa del usuario
                        vehiculoRepository.obtenerVehiculoTaxista(placaUsuario,
                                vehiculo -> {
                                    runOnUiThread(() -> {
                                        mostrarInformacionVehiculo(vehiculo, true);
                                        Log.d(TAG, "Información del vehículo actualizada exitosamente");
                                    });
                                },
                                exception -> {
                                    runOnUiThread(() -> {
                                        Log.e(TAG, "Error al recargar vehículo: " + exception.getMessage());
                                        mostrarVehiculoNoConfigurado();
                                    });
                                }
                        );
                    } else {
                        runOnUiThread(() -> {
                            Log.d(TAG, "Usuario no tiene placa - mostrando estado no configurado");
                            mostrarVehiculoNoConfigurado();
                        });
                    }
                },
                exception -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error al obtener usuario para recargar vehículo: " + exception.getMessage());
                        Toast.makeText(this, "Error al actualizar información del vehículo",
                                Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }

    private void cargarDetallesVehiculo(String placa) {
        Log.d(TAG, "Iniciando carga de detalles para placa: " + placa);

        vehiculoRepository.obtenerVehiculoTaxista(placa,
                vehiculo -> {
                    Log.d(TAG, "SUCCESS - Detalles del vehículo cargados: " + vehiculo.toString());
                    Log.d(TAG, "Vehículo activo: " + vehiculo.isActivo());

                    // Información adicional (estado, etc.)
                    if (vehiculo.isActivo()) {
                        Log.d(TAG, "Configurando capacidad: 4 personas");
                        binding.tvCapacidadNumero.setText("4 personas");
                    } else {
                        Log.d(TAG, "Vehículo inactivo, configurando: No disponible");
                        binding.tvCapacidadNumero.setText("No disponible");
                    }

                    Log.d(TAG, "Capacidad configurada en TextView");
                },
                error -> {
                    Log.e(TAG, "ERROR - No se pudieron cargar detalles del vehículo: " + error.getMessage());
                    Log.d(TAG, "Configurando capacidad por defecto: 4 personas");
                    binding.tvCapacidadNumero.setText("4 personas");
                }
        );
    }
    private void configurarInformacionCuenta(User usuario) {
        // Correo electrónico
        String email = usuario.getEmail();
        binding.tvCorreoUser.setText(email != null ? email : "No disponible");

        // Tarjeta de crédito - verificar si tiene tarjeta vinculada
        verificarTarjetaCredito(usuario.getUid());
    }

    private void verificarTarjetaCredito(String userId) {
        if (tarjetaRepository == null) {
            tarjetaRepository = new TarjetaCreditoRepository();
        }

        // Verificar si tiene tarjeta real en Firebase
        tarjetaRepository.obtenerTarjetaUsuario(
                tarjeta -> {
                    // SUCCESS: Tiene tarjeta activa
                    Log.d(TAG, "Tarjeta encontrada: " + tarjeta.getNumeroEnmascarado());
                    runOnUiThread(() -> {
                        binding.tvTarjetaUser.setText(tarjeta.getNumeroEnmascarado());
                        binding.tvTarjetaUser.setTextColor(ContextCompat.getColor(this, R.color.md_theme_primary));
                    });
                },
                error -> {
                    // ERROR: No tiene tarjeta o está inactiva
                    Log.d(TAG, "No hay tarjeta o está inactiva: " + error.getMessage());
                    runOnUiThread(() -> {
                        binding.tvTarjetaUser.setText("No activado");
                        binding.tvTarjetaUser.setTextColor(ContextCompat.getColor(this, R.color.md_theme_error));
                    });
                }
        );
    }

    private void manejarErrorCargaPerfil(Exception exception) {
        String mensaje = exception.getMessage();

        // Mostrar imagen por defecto
        binding.ivProfilePicture.setImageResource(R.drawable.taxista);

        // Configurar información básica con valores por defecto
        binding.tvNombreTaxista.setText("Usuario");
        binding.tvRol.setText("Taxista");
        binding.tvCorreoUser.setText("No disponible");

        // Valores por defecto para vehículo
        binding.tvModeloVehiculo.setText("Toyota Corolla");
        binding.tvMarcaAnio.setText("Modelo 2019 • Sedán");
        binding.tvCapacidadNumero.setText("4 personas");

        // Valor por defecto para tarjeta
        binding.tvTarjetaUser.setText("Visa **** 4567");

        if (mensaje != null && mensaje.contains("no autenticado")) {
            redirigirALogin();
        } else {
            Toast.makeText(this, "Error al cargar perfil: " + mensaje, Toast.LENGTH_SHORT).show();
        }
    }
    private void redirigirALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void configurarBottomNavigationFluido() {
        // Verificar que el menú se cargó
        if (binding.bottomNavigation.getMenu() == null || binding.bottomNavigation.getMenu().size() == 0) {
            Log.e(TAG, "❌ ERROR: El menú no se cargó correctamente");
            return;
        }

        // Establecer perfil como seleccionado
        binding.bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        // Configurar listener
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "🔄 Ítem seleccionado: " + itemId);

            if (itemId == R.id.nav_perfil) {
                return true; // Ya estamos aquí
            } else if (itemId == R.id.nav_inicio) {
                navegarSinAnimacion(DriverInicioActivity.class);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                navegarSinAnimacion(DriverReservaActivity.class);
                return true;
            } else if (itemId == R.id.nav_mapa) {
                navegarSinAnimacion(DriverMapaActivity.class);
                return true;
            }

            return false;
        });
    }
    private void configurarOpcionesPerfil() {
        // Vehículo
        binding.layoutVehicleModel.setOnClickListener(v -> {
            Intent intent = new Intent(this, DriverVehiculoActivity.class);
            startActivity(intent);

        });

        // Correo
        binding.layoutCorreo.setOnClickListener(v -> {
            String email = binding.tvCorreoUser.getText().toString();
            if (!email.equals("No disponible")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "Email no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // Tarjeta de crédito - NAVEGACIÓN A NUEVA ACTIVIDAD
        binding.layoutTelefono.setOnClickListener(v -> {
            Log.d(TAG, "Click en tarjeta de crédito - navegando a DriverTarjetaCreditoActivity");
            Intent intent = new Intent(this, DriverTarjetaCreditoActivity.class);

            // Opcional: Pasar datos del usuario
            intent.putExtra("user_id", taxistaRepository.obtenerUidActual());
            intent.putExtra("user_name", binding.tvNombreTaxista.getText().toString());

            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Capacidad del vehículo
        binding.layoutCapacidad.setOnClickListener(v -> {
            Toast.makeText(this, "Configuración de capacidad próximamente", Toast.LENGTH_SHORT).show();
        });

        // Cambiar foto de perfil - Botón
        binding.buttonChangePicture.setOnClickListener(v -> {
            // Abrir el selector de imágenes
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Cambiar foto de perfil - Imagen clickeable
        binding.ivProfilePicture.setOnClickListener(v -> {
            // Abrir el selector de imágenes
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    // Método para refrescar información del perfil
    public void refrescarPerfil() {
        Log.d(TAG, "Refrescando información del perfil...");
        cargarInformacionPerfil();
    }

    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navegarSinAnimacion(DriverInicioActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar selección correcta y refrescar datos
        binding.bottomNavigation.post(() -> {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_perfil);
        });

        // Opcional: Refrescar información si el usuario pudo haber actualizado datos
        if (taxistaRepository.usuarioEstaAutenticado()) {
            refrescarPerfil();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navegarSinAnimacion(DriverInicioActivity.class);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Log.d(TAG, "Botón de logout presionado");
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        // 1. Obtener el ID del usuario actual antes de cerrar sesión
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
        Intent intent = new Intent(this, com.iot.stayflowdev.LoginFireBaseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void subirImagenPerfil(Uri imageUri) {
        // Mostrar indicador de carga y bloquear interacciones
        mostrarCargando(true);

        // Obtener referencia a Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Crear referencia para la imagen del perfil
        String userId = taxistaRepository.obtenerUidActual();
        StorageReference profileImageRef = storageRef.child("perfil/" + userId + "/foto_perfil.jpg");

        // Subir la imagen
        UploadTask uploadTask = profileImageRef.putFile(imageUri);

        // Manejar el resultado de la subida
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // La imagen se subió exitosamente, obtener la URL de descarga
            profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                Log.d(TAG, "Imagen de perfil subida y disponible en: " + imageUrl);

                // Actualizar la imagen en el perfil del taxista
                taxistaRepository.actualizarImagenPerfil(userId, imageUrl,
                        () -> {
                            // Actualización exitosa
                            Log.d(TAG, "URL de imagen de perfil actualizada en Firestore");
                            Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();

                            // Ocultar indicador de carga y restaurar interacciones
                            mostrarCargando(false);

                            // Recargar información del perfil para mostrar la nueva imagen
                            cargarInformacionPerfil();
                        },
                        e -> {
                            // Ocurrió un error al actualizar la URL de la imagen
                            Log.e(TAG, "Error al actualizar URL de imagen de perfil: " + e.getMessage());
                            Toast.makeText(this, "Error al actualizar foto de perfil", Toast.LENGTH_SHORT).show();
                            mostrarCargando(false);
                        });
            });
        }).addOnFailureListener(e -> {
            // Ocurrió un error durante la subida
            Log.e(TAG, "Error al subir imagen de perfil: " + e.getMessage());
            Toast.makeText(this, "Error al subir foto de perfil", Toast.LENGTH_SHORT).show();
            mostrarCargando(false);
        });
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

            // Deshabilitar todas las opciones del perfil
            binding.layoutVehicleModel.setEnabled(false);
            binding.layoutVehicleModel.setClickable(false);
            binding.layoutCorreo.setEnabled(false);
            binding.layoutCorreo.setClickable(false);
            binding.layoutTelefono.setEnabled(false);
            binding.layoutTelefono.setClickable(false);
            binding.layoutCapacidad.setEnabled(false);
            binding.layoutCapacidad.setClickable(false);

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

            // Habilitar todas las opciones del perfil
            binding.layoutVehicleModel.setEnabled(true);
            binding.layoutVehicleModel.setClickable(true);
            binding.layoutCorreo.setEnabled(true);
            binding.layoutCorreo.setClickable(true);
            binding.layoutTelefono.setEnabled(true);
            binding.layoutTelefono.setClickable(true);
            binding.layoutCapacidad.setEnabled(true);
            binding.layoutCapacidad.setClickable(true);

            // Habilitar scroll
            binding.scrollView.setEnabled(true);

            // Restaurar alpha normal
            binding.bottomNavigation.setAlpha(1.0f);
            binding.buttonChangePicture.setAlpha(1.0f);
        }
    }
}
