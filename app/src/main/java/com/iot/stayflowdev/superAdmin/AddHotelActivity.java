package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.User;
import com.iot.stayflowdev.superAdmin.adapter.AdminSelectorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddHotelActivity extends AppCompatActivity {

    private static final String TAG = "AddHotelActivity";

    private TextInputEditText inputNombreHotel;
    private MaterialButton buttonSeleccionarAdmin;
    private MaterialButton buttonGuardar;
    private CircularProgressIndicator progressIndicator;
    private AlertDialog adminSelectorDialog;

    // Variables para el administrador seleccionado
    private String selectedAdminId;
    private String selectedAdminName;

    // Firebase
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin_add_hotel_form_activity);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Configurar Toolbar con navegación hacia atrás
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Referencias a los elementos de la UI
        inputNombreHotel = findViewById(R.id.inputNombreHotel);
        buttonSeleccionarAdmin = findViewById(R.id.buttonSeleccionarAdmin);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Configurar el botón para seleccionar administrador
        buttonSeleccionarAdmin.setOnClickListener(v -> {
            showAdminSelectorDialog();
        });

        // Configurar el botón para guardar el hotel
        buttonGuardar.setOnClickListener(v -> {
            if (validarFormulario()) {
                guardarHotel();
            }
        });
    }

    private void showAdminSelectorDialog() {
        // Mostrar un indicador de progreso mientras se cargan los administradores
        progressIndicator.setVisibility(View.VISIBLE);

        // Crear el diálogo pero no mostrarlo aún
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Administrador");

        // Inflar el layout con un RecyclerView
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_selector, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewAdmins);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Botón para cerrar el diálogo
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // Configurar el diálogo
        builder.setView(dialogView);
        adminSelectorDialog = builder.create();

        // Cargar los administradores de hotel desde Firestore
        db.collection("usuarios")
                .whereEqualTo("rol", "adminhotel")
                .whereEqualTo("estado", "activo")  // Solo administradores activos
                .get()
                .addOnCompleteListener(task -> {
                    progressIndicator.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<User> adminList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User admin = document.toObject(User.class);
                            admin.setUid(document.getId());
                            adminList.add(admin);
                        }

                        if (adminList.isEmpty()) {
                            Toast.makeText(AddHotelActivity.this,
                                    "No hay administradores de hotel disponibles",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Configurar el adaptador
                            AdminSelectorAdapter adapter = new AdminSelectorAdapter(adminList, admin -> {
                                // Cuando se selecciona un administrador
                                selectedAdminId = admin.getUid();
                                selectedAdminName = admin.getNombres() + " " + admin.getApellidos();

                                // Actualizar la UI para mostrar el administrador seleccionado
                                findViewById(R.id.textViewAdminSeleccionado).setVisibility(View.VISIBLE);
                                ((android.widget.TextView) findViewById(R.id.textViewAdminSeleccionado))
                                        .setText("Administrador: " + selectedAdminName);

                                // Cerrar el diálogo
                                adminSelectorDialog.dismiss();
                            });

                            recyclerView.setAdapter(adapter);
                            adminSelectorDialog.show();
                        }
                    } else {
                        Log.w(TAG, "Error al cargar administradores", task.getException());
                        Toast.makeText(AddHotelActivity.this,
                                "Error al cargar administradores: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validarFormulario() {
        boolean isValid = true;

        // Validar nombre del hotel
        String nombre = inputNombreHotel.getText().toString().trim();
        if (nombre.isEmpty()) {
            inputNombreHotel.setError("El nombre del hotel es obligatorio");
            isValid = false;
        }

        // Validar que se haya seleccionado un administrador
        if (selectedAdminId == null) {
            Toast.makeText(this, "Debe seleccionar un administrador para el hotel", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }

    private void guardarHotel() {
        // Mostrar indicador de progreso
        progressIndicator.setVisibility(View.VISIBLE);
        buttonGuardar.setEnabled(false);

        // Obtener datos del formulario
        String nombre = inputNombreHotel.getText().toString().trim();

        // Crear objeto Hotel utilizando el modelo
        Hotel hotel = new Hotel(nombre, selectedAdminId);
        Log.d(TAG, "Guardando nuevo hotel: " + hotel.toString());

        // Guardar en Firestore
        db.collection("hoteles")
                .add(hotel)
                .addOnSuccessListener(documentReference -> {
                    String hotelId = documentReference.getId();
                    Log.d(TAG, "Hotel agregado con ID: " + hotelId);
                    progressIndicator.setVisibility(View.GONE);

                    // Actualizar el documento del administrador para asociarlo a este hotel
                    DocumentReference adminRef = db.collection("usuarios").document(selectedAdminId);
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("hotelAsignado", hotelId);

                    Log.d(TAG, "Actualizando administrador ID: " + selectedAdminId + " con hotelAsignado: " + hotelId);

                    adminRef.update(updateData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Administrador actualizado correctamente");

                                // Mostrar mensaje de éxito y finalizar la actividad
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Hotel registrado correctamente", Snackbar.LENGTH_SHORT).show();

                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error al actualizar administrador", e);
                                buttonGuardar.setEnabled(true);
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Error al asignar el administrador: " + e.getMessage(),
                                        Snackbar.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al agregar hotel", e);
                    progressIndicator.setVisibility(View.GONE);
                    buttonGuardar.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Error al registrar hotel: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
    }
}
