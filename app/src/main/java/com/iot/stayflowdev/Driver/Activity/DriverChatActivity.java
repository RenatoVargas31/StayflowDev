package com.iot.stayflowdev.Driver.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.iot.stayflowdev.R;

public class DriverChatActivity extends AppCompatActivity {
    // Variables para datos del pasajero
    private String solicitudId;
    private String nombrePasajero;
    private String telefonoPasajero;
    private String destino;
    private String direccionDestino;

    // Referencias a las vistas
    private TextView toolbar_title;
    private ShapeableImageView iv_cliente_foto;
    private TextView tv_cliente_nombre;
    private TextView tv_cliente_tipo;
    private EditText et_mensaje;
    private ImageButton btn_emoji;
    private ImageButton btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar();

        // Inicializar vistas
        initViews();

        // Recibir datos del Intent
        //recibirDatosIntent();

        // Configurar listeners
       // setupListeners();
    }
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void initViews() {
        // Toolbar
        toolbar_title = findViewById(R.id.toolbar_title);
        // Barra de mensaje
        et_mensaje = findViewById(R.id.et_mensaje);
        btn_send = findViewById(R.id.btn_send);
    }
/*
    private void recibirDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Obtener datos del pasajero
            solicitudId = intent.getStringExtra("SOLICITUD_ID");
            nombrePasajero = intent.getStringExtra("NOMBRE_PASAJERO");
            telefonoPasajero = intent.getStringExtra("TELEFONO_PASAJERO");
            destino = intent.getStringExtra("DESTINO");
            direccionDestino = intent.getStringExtra("DIRECCION_DESTINO");

            // Actualizar las vistas con los datos recibidos
            actualizarVistasPasajero();

            Log.d("DriverChat", "Datos recibidos - Pasajero: " + nombrePasajero + ", Destino: " + destino);
        } else {
            Log.w("DriverChat", "No se recibieron datos del Intent");
            configurarDatosDefault();
        }
    }

    private void actualizarVistasPasajero() {
        // Actualizar título del toolbar
        if (nombrePasajero != null && !nombrePasajero.isEmpty()) {
            toolbar_title.setText("Chat con " + nombrePasajero);
            tv_cliente_nombre.setText(nombrePasajero);
        } else {
            toolbar_title.setText("Chat con pasajero");
            tv_cliente_nombre.setText("Pasajero");
        }

        // Actualizar información del destino en el tipo de cliente
        if (destino != null && !destino.isEmpty()) {
            tv_cliente_tipo.setText("Viaje a " + destino);
        } else {
            tv_cliente_tipo.setText("Cliente");
        }

        // Mostrar información completa en log para debug
        if (direccionDestino != null) {
            Log.d("DriverChat", "Dirección completa: " + direccionDestino);
        }
        if (telefonoPasajero != null) {
            Log.d("DriverChat", "Teléfono: " + telefonoPasajero);
        }
    }

    private void configurarDatosDefault() {
        // Datos por defecto si no se reciben del Intent
        toolbar_title.setText("Chat");
        tv_cliente_nombre.setText("Anna Ramirez");
        tv_cliente_tipo.setText("Cliente");
    }

    private void setupListeners() {
        // Botón enviar mensaje
        btn_send.setOnClickListener(v -> {
            enviarMensaje();
        });

        // Botón emoji (funcionalidad futura)
        btn_emoji.setOnClickListener(v -> {
            Toast.makeText(this, "Seleccionar emoji", Toast.LENGTH_SHORT).show();
            // TODO: Implementar selector de emojis
        });

        // Enter para enviar mensaje
        et_mensaje.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                enviarMensaje();
                return true;
            }
            return false;
        });
    }

    private void enviarMensaje() {
        String mensaje = et_mensaje.getText().toString().trim();

        if (mensaje.isEmpty()) {
            Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implementar envío real del mensaje
        Log.d("DriverChat", "Enviando mensaje: " + mensaje);
        Log.d("DriverChat", "Para solicitud: " + solicitudId);
        Log.d("DriverChat", "A pasajero: " + nombrePasajero);

        // Simular envío exitoso
        Toast.makeText(this, "Mensaje enviado: " + mensaje, Toast.LENGTH_SHORT).show();

        // Limpiar campo de texto
        et_mensaje.setText("");

        // Aquí puedes integrar con tu sistema de chat real
        // Por ejemplo: Firebase Firestore, Socket.IO, etc.
        enviarMensajeAFirebase(mensaje);
    }

    private void enviarMensajeAFirebase(String mensaje) {
        // TODO: Implementar envío a Firebase o tu backend de chat

        if (solicitudId != null) {
            Map<String, Object> mensajeData = new HashMap<>();
            mensajeData.put("mensaje", mensaje);
            mensajeData.put("remitente", "conductor");
            mensajeData.put("timestamp", FieldValue.serverTimestamp());
            mensajeData.put("solicitudId", solicitudId);

            FirebaseFirestore.getInstance()
                .collection("chats")
                .document(solicitudId)
                .collection("mensajes")
                .add(mensajeData)
                .addOnSuccessListener(docRef -> {
                    Log.d("DriverChat", "Mensaje guardado en Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("DriverChat", "Error al guardar mensaje: " + e.getMessage());
                });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Regresar a DriverMapaActivity con los mismos datos
            Intent intent = new Intent(this, DriverMapaActivity.class);
            if (solicitudId != null) {
                intent.putExtra("SOLICITUD_ID", solicitudId);
            }
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Regresar a DriverMapaActivity
        super.onBackPressed();
        Intent intent = new Intent(this, DriverMapaActivity.class);
        if (solicitudId != null) {
            intent.putExtra("SOLICITUD_ID", solicitudId);
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    } */
}