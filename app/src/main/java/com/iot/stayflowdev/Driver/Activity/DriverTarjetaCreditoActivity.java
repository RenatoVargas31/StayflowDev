package com.iot.stayflowdev.Driver.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.iot.stayflowdev.Driver.Repository.TarjetaCreditoRepository;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityDriverTarjetaCreditoBinding;
import com.iot.stayflowdev.model.TarjetaCredito;

public class DriverTarjetaCreditoActivity extends AppCompatActivity {

    private ActivityDriverTarjetaCreditoBinding binding;
    private static final String TAG = "DriverTarjetaCredito";
    private TarjetaCreditoRepository tarjetaRepository;
    // Datos del usuario
    private String userId;
    private String userName;

    // Repository para tarjetas (cuando lo implementes)
    // private TarjetaCreditoRepository tarjetaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar View Binding
        binding = ActivityDriverTarjetaCreditoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Configurar toolbar
        configurarToolbar();

        // Obtener datos del intent
        obtenerDatosIntent();
        tarjetaRepository = new TarjetaCreditoRepository();
        // Inicializar repository
        // tarjetaRepository = new TarjetaCreditoRepository();

        // Configurar vistas y eventos
        configurarEventos();

        // Verificar estado de tarjeta
        verificarEstadoTarjeta();
    }

    private void configurarToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void obtenerDatosIntent() {
        userId = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");

        Log.d(TAG, "Usuario: " + userName + ", ID: " + userId);

        if (userId == null) {
            Log.e(TAG, "No se recibió user_id, cerrando actividad");
            finish();
        }
    }

    private void configurarEventos() {
        // Botón agregar tarjeta (estado sin tarjeta)
        binding.btnAgregarTarjeta.setOnClickListener(v -> {
            Log.d(TAG, "Click en agregar tarjeta");
            mostrarFormularioAgregarTarjeta();
        });

        // Botón editar tarjeta (estado con tarjeta)
        binding.btnEditarTarjeta.setOnClickListener(v -> {
            Log.d(TAG, "Click en editar tarjeta");
            mostrarFormularioEditarTarjeta();
        });

        // Botón eliminar tarjeta (estado con tarjeta)
        binding.btnEliminarTarjeta.setOnClickListener(v -> {
            Log.d(TAG, "Click en eliminar tarjeta");
            mostrarDialogoEliminarTarjeta();
        });
    }
    private void verificarEstadoTarjeta() {
        if (tarjetaRepository == null) {
            tarjetaRepository = new TarjetaCreditoRepository();
        }

        if (userId != null) {
            // Mostrar loading mientras carga
            mostrarLoading(true);

            tarjetaRepository.obtenerTarjetaUsuario(
                    tarjeta -> {
                        // SUCCESS: Hay tarjeta guardada
                        Log.d(TAG, "Tarjeta encontrada: " + tarjeta.getNumeroEnmascarado());
                        mostrarLoading(false);
                        mostrarTarjetaExistente(tarjeta);
                    },
                    error -> {
                        // ERROR: No hay tarjeta o error al cargar
                        Log.d(TAG, "No hay tarjeta registrada: " + error.getMessage());
                        mostrarLoading(false);
                        mostrarEstadoSinTarjeta();
                    }
            );
        } else {
            Log.e(TAG, "No hay userId, mostrando estado sin tarjeta");
            mostrarEstadoSinTarjeta();
        }
    }
    private void mostrarLoading(boolean mostrar) {
        if (mostrar) {
            // Ocultar ambos layouts mientras carga
            binding.layoutSinTarjeta.setVisibility(View.GONE);
            binding.layoutConTarjeta.setVisibility(View.GONE);

            // Mostrar mensaje de carga (puedes agregar un ProgressBar si quieres)
            Toast.makeText(this, "Cargando información de tarjeta...", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Mostrando estado de carga");
        }
        // Si mostrar = false, los otros métodos se encargan de mostrar el layout correcto
    }

    private void mostrarEstadoSinTarjeta() {
        Log.d(TAG, "Mostrando estado sin tarjeta");

        // Mostrar layout sin tarjeta, ocultar layout con tarjeta
        binding.layoutSinTarjeta.setVisibility(View.VISIBLE);
        binding.layoutConTarjeta.setVisibility(View.GONE);

        // Configurar textos
        binding.tvEstadoTarjeta.setText("No hay tarjeta registrada");
        binding.tvDescripcionEstado.setText("Agrega tu primera tarjeta para comenzar a recibir pagos");
        binding.btnAgregarTarjeta.setText("Agregar Tarjeta");

        Log.d(TAG, "Estado sin tarjeta configurado");
    }
    private void mostrarTarjetaExistente(TarjetaCredito tarjeta) {
        Log.d(TAG, "Mostrando tarjeta existente: " + tarjeta.getNumeroEnmascarado());

        try {
            // Ocultar layout sin tarjeta, mostrar layout con tarjeta
            binding.layoutSinTarjeta.setVisibility(View.GONE);
            binding.layoutConTarjeta.setVisibility(View.VISIBLE);

            // Configurar información en la tarjeta visual
            binding.tvCardNumber.setText(tarjeta.getNumeroEnmascarado());
            binding.tvCardHolder.setText(tarjeta.getTitular().toUpperCase());
            binding.tvCardExpiry.setText(tarjeta.getFechaExpiracionCorta());

            // Configurar información en los detalles
            binding.tvCardType.setText(tarjeta.getTipo());
            binding.tvCardNumberDetail.setText(tarjeta.getNumeroEnmascarado());
            binding.tvCardHolderDetail.setText(tarjeta.getTitular());
            binding.tvCardExpiryDetail.setText(tarjeta.getFechaExpiracionLarga());

            // Configurar logo según el tipo de tarjeta
            configurarLogoTarjeta(tarjeta.getTipo());

            // Verificar si la tarjeta está vencida
            if (tarjeta.estaVencida()) {
                // Mostrar indicador de tarjeta vencida
                Toast.makeText(this, "⚠️ Esta tarjeta está vencida", Toast.LENGTH_LONG).show();
            }

            Log.d(TAG, "Información de tarjeta configurada en UI");

        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar tarjeta: " + e.getMessage());
            mostrarEstadoSinTarjeta();
            Toast.makeText(this, "Error al mostrar información de la tarjeta", Toast.LENGTH_SHORT).show();
        }
    }
    private void configurarLogoTarjeta(String tipoTarjeta) {
        int logoResource;
        switch (tipoTarjeta.toLowerCase()) {
            case "visa":
                logoResource = R.drawable.ic_visa_logo;
                break;
            case "mastercard":
                logoResource = R.drawable.ic_mastercard_logo;
                break;
            default:
                logoResource = R.drawable.ic_credit_card;
                break;
        }
        binding.ivCardLogo.setImageResource(logoResource);
    }
    private void mostrarFormularioAgregarTarjeta() {
        Log.d(TAG, "Mostrando formulario para agregar tarjeta");

        // Usar el dialog simplificado que creamos antes
        new AlertDialog.Builder(this)
                .setTitle("Agregar Tarjeta")
                .setMessage("¿Quieres simular agregar una tarjeta?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Simular tarjeta agregada y luego recargar datos reales
                    TarjetaCredito tarjetaSimulada = crearTarjetaMock();

                    // Guardar en Firebase usando el repository
                    tarjetaRepository.agregarTarjeta(tarjetaSimulada,
                            documentId -> {
                                Log.d(TAG, "Tarjeta guardada en Firebase: " + documentId);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Tarjeta agregada exitosamente", Toast.LENGTH_SHORT).show();
                                    // Recargar datos para mostrar la tarjeta real desde Firebase
                                    verificarEstadoTarjeta();
                                });
                            },
                            error -> {
                                Log.e(TAG, "Error al guardar tarjeta: " + error.getMessage());
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Error al guardar tarjeta: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                });
                            }
                    );
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void mostrarFormularioEditarTarjeta() {
        Toast.makeText(this, "Próximamente: Editar información de tarjeta", Toast.LENGTH_SHORT).show();

        // TODO: Implementar edición de tarjeta
        /*
        FragmentManager fm = getSupportFragmentManager();
        EditarTarjetaDialogFragment dialog = EditarTarjetaDialogFragment.newInstance(tarjetaActual);
        dialog.setOnTarjetaEditadaListener(tarjeta -> {
            mostrarTarjetaExistente(tarjeta);
            Toast.makeText(this, "Tarjeta actualizada exitosamente", Toast.LENGTH_SHORT).show();
        });
        dialog.show(fm, "editar_tarjeta_dialog");
        */
    }

    private void mostrarDialogoEliminarTarjeta() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Tarjeta")
                .setMessage("¿Estás seguro de que deseas eliminar esta tarjeta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Log.d(TAG, "Usuario confirmó eliminar tarjeta");
                    eliminarTarjeta();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Log.d(TAG, "Usuario canceló eliminar tarjeta");
                    dialog.dismiss();
                })
                .setIcon(R.drawable.ic_warning)
                .show();
    }

    private void eliminarTarjeta() {
        if (tarjetaRepository == null) {
            tarjetaRepository = new TarjetaCreditoRepository();
        }

        // Mostrar loading
        Toast.makeText(this, "Eliminando tarjeta...", Toast.LENGTH_SHORT).show();

        tarjetaRepository.eliminarTarjeta(
                aVoid -> {
                    Log.d(TAG, "Tarjeta eliminada exitosamente");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Tarjeta eliminada exitosamente", Toast.LENGTH_SHORT).show();
                        mostrarEstadoSinTarjeta();
                    });
                },
                error -> {
                    Log.e(TAG, "Error al eliminar tarjeta: " + error.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al eliminar tarjeta: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
        );
    }
    private void refrescarDatos() {
        Log.d(TAG, "Refrescando datos de tarjeta");
        verificarEstadoTarjeta();
    }

    // Método público para llamar desde otras partes si es necesario
    public void actualizarVistaTarjeta() {
        refrescarDatos();
    }
    private TarjetaCredito crearTarjetaMock() {
        // Usar el constructor principal de la clase completa
        TarjetaCredito tarjeta = new TarjetaCredito(
                "4532123456789012", // número
                userName != null ? userName : "Juan Perez", // titular
                "12", // mes
                "2026", // año
                "123" // cvv
        );
        return tarjeta;
    }

    // Método para cambiar manualmente el estado (para testing)
    public void simularTarjetaExistente() {
        TarjetaCredito tarjetaMock = crearTarjetaMock();
        mostrarTarjetaExistente(tarjetaMock);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Navegación hacia atrás");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void manejarErrorCarga(Exception error) {
        String mensaje = error.getMessage();

        if (mensaje != null) {
            if (mensaje.contains("no autenticado")) {
                Toast.makeText(this, "Sesión expirada. Por favor, inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
                finish();
            } else if (mensaje.contains("No hay tarjeta")) {
                Log.d(TAG, "Usuario no tiene tarjeta registrada");
                mostrarEstadoSinTarjeta();
            } else if (mensaje.contains("desactivada")) {
                Log.d(TAG, "Tarjeta está desactivada");
                mostrarEstadoSinTarjeta();
                Toast.makeText(this, "Tu tarjeta está desactivada", Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "Error desconocido: " + mensaje);
                mostrarEstadoSinTarjeta();
                Toast.makeText(this, "Error al cargar tarjeta", Toast.LENGTH_SHORT).show();
            }
        } else {
            mostrarEstadoSinTarjeta();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Aplicar transición suave al volver
        overridePendingTransition(0, 0);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando se vuelve a la actividad
        Log.d(TAG, "onResume - recargando datos de tarjeta");
        verificarEstadoTarjeta();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}