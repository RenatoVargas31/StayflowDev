package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import com.iot.stayflowdev.R;

public class UserDetailActivity extends BaseSuperAdminActivity {

    public static final String EXTRA_USER_NAME = "USER_NAME";
    public static final String EXTRA_USER_ROLE = "USER_ROLE";
    public static final String EXTRA_USER_ROLE_DESC = "USER_ROLE_DESC";
    public static final String EXTRA_USER_ENABLED = "USER_ENABLED";

    private String userName;
    private String userRole;
    private String userRoleDescription;
    private boolean userEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener datos del Intent
        userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        userRole = getIntent().getStringExtra(EXTRA_USER_ROLE);
        userRoleDescription = getIntent().getStringExtra(EXTRA_USER_ROLE_DESC);
        userEnabled = getIntent().getBooleanExtra(EXTRA_USER_ENABLED, false);

        // Validar que se recibieron los datos necesarios
        if (userName == null || userRole == null || userRoleDescription == null) {
            Toast.makeText(this, "Error: Datos de usuario incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupData();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_user_detail;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_gestion; // O el ítem que corresponda
    }

    @Override
    protected String getToolbarTitle() {
        return "Detalles de Usuario";
    }

    private void initViews() {
        // Inicializar vistas
        TextView textViewUserName = findViewById(R.id.textViewUserName);
        TextView textViewUserRole = findViewById(R.id.textViewUserRole);
        TextView textViewDNI = findViewById(R.id.textViewDNI);
        TextView textViewEmail = findViewById(R.id.textViewEmail);
        TextView textViewPhone = findViewById(R.id.textViewPhone);
        TextView textViewAccountStatus = findViewById(R.id.textViewAccountStatus);
        SwitchCompat switchAccountStatus = findViewById(R.id.switchAccountStatus);
        ImageView imageViewProfile = findViewById(R.id.imageViewProfile);

        // Configurar datos del usuario
        textViewUserName.setText(userName);
        textViewUserRole.setText(userRoleDescription);
        textViewAccountStatus.setText(userEnabled ? "Habilitado" : "Deshabilitado");
        switchAccountStatus.setChecked(userEnabled);

        // Configurar datos dummy
        // En una aplicación real, estos datos vendrían de la base de datos
        textViewDNI.setText("78965432");
        textViewEmail.setText(userName.toLowerCase().replace(" ", "") + "@gmail.com");
        textViewPhone.setText("987654321");
    }

    private void setupData() {
        // Aquí puedes configurar cualquier lógica adicional necesaria
    }
}