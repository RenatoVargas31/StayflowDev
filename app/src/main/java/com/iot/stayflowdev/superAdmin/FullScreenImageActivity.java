package com.iot.stayflowdev.superAdmin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.iot.stayflowdev.R;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView ivFullScreenImage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ivFullScreenImage = findViewById(R.id.ivFullScreenImage);
        progressBar = findViewById(R.id.progressBar);

        String imageUrl = getIntent().getStringExtra("image_url");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            loadImage(imageUrl);
        } else {
            Toast.makeText(this, "Error: No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Cerrar al tocar la imagen
        ivFullScreenImage.setOnClickListener(v -> finish());
    }

    private void loadImage(String imageUrl) {
        progressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivFullScreenImage);

        progressBar.setVisibility(View.GONE);
    }
}
