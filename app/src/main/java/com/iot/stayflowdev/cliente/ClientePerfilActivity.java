package com.iot.stayflowdev.cliente;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import com.iot.stayflowdev.LoginFireBaseActivity;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityClientePerfilBinding;
import com.iot.stayflowdev.model.TarjetaCredito;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.utils.UserSessionManager;
import com.iot.stayflowdev.viewmodels.TarjetaCreditoViewModel;
import com.iot.stayflowdev.viewmodels.UserViewModel;

import java.util.Calendar;

public class ClientePerfilActivity extends AppCompatActivity {

    private ActivityClientePerfilBinding binding;
    private UserViewModel userViewModel;
    private TarjetaCreditoViewModel tarjetaCreditoViewModel;
    private static final String TAG = "ClientePerfilActivity";
    private Dialog tarjetaDialog;

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

                // Cerrar diálogo si está abierto
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

        // Configurar otros botones de edición si es necesario
        binding.btnEditarDomicilio.setOnClickListener(v -> {
            // TODO: Implementar diálogo para editar domicilio
            Toast.makeText(this, "Función para editar domicilio no implementada", Toast.LENGTH_SHORT).show();
        });

        binding.btnEditarTelefono.setOnClickListener(v -> {
            // TODO: Implementar diálogo para editar teléfono
            Toast.makeText(this, "Función para editar teléfono no implementada", Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    // Manejar eventos del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Log.d(TAG, "Botón de logout presionado");
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Método para cerrar sesión
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
}
