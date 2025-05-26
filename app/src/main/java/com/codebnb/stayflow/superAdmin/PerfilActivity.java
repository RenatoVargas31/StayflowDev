package com.codebnb.stayflow.superAdmin;

import android.os.Bundle;
import com.codebnb.stayflow.R;

public class PerfilActivity extends BaseSuperAdminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El layout ya se configura en BaseSuperAdminActivity
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_perfil_superadmin;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_perfil;
    }

    @Override
    protected String getToolbarTitle() {
        return "Perfil";
    }
}