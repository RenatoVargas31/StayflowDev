package com.iot.stayflowdev.adminHotel;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.adapter.ChatAdapter;
import com.iot.stayflowdev.adminHotel.model.ChatMessage;
import com.iot.stayflowdev.databinding.ActivityMensajeDetalleBinding;

import java.util.ArrayList;
import java.util.List;

public class MensajeDetalleActivity extends AppCompatActivity {

    private ActivityMensajeDetalleBinding binding;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMensajeDetalleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Chat");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        cargarMensajes();

        adapter = new ChatAdapter(messages);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChat.setAdapter(adapter);
    }

    private void cargarMensajes() {
        messages.add(new ChatMessage("Hola, ¿cómo estás?"));
        messages.add(new ChatMessage("Bien, gracias. ¿Y tú?"));
        messages.add(new ChatMessage("Todo bien. ¿Necesitas algo?"));
        messages.add(new ChatMessage("Sí, quería preguntar sobre el checkout."));
        messages.add(new ChatMessage("Claro, dime."));
    }
}
