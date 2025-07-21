package com.iot.stayflowdev.adminHotel;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.adminHotel.adapter.ChatAdapter;
import com.iot.stayflowdev.adminHotel.model.ChatMessage;
import com.iot.stayflowdev.databinding.ActivityMensajeDetalleBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MensajeDetalleActivity extends AppCompatActivity {

    private ActivityMensajeDetalleBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration messagesListener;

    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    private String currentUserId;
    private String currentUserName;
    private String receiverUserId;
    private String receiverUserName;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMensajeDetalleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        currentUserName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Admin Hotel";

        receiverUserId = getIntent().getStringExtra("USER_ID");
        receiverUserName = getIntent().getStringExtra("USER_NAME");

        if (receiverUserId == null || receiverUserName == null) {
            Toast.makeText(this, "Datos incompletos del cliente", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatId = generateChatId(currentUserId, receiverUserId);

        setupUI();
        setupRecyclerView();
        setupListeners();
        escucharMensajes();
    }

    private void setupUI() {
        binding.chatTitleText.setText("Chat con " + receiverUserName);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(messages, currentUserId);
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChat.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.sendButton.setOnClickListener(v -> enviarMensaje());
        binding.messageInput.setOnEditorActionListener((v, actionId, event) -> {
            enviarMensaje();
            return true;
        });
    }

    private void enviarMensaje() {
        String texto = binding.messageInput.getText().toString().trim();
        if (texto.isEmpty()) return;

        ChatMessage msg = new ChatMessage(chatId, currentUserId, currentUserName, receiverUserId, receiverUserName, texto, Timestamp.now(), false);

        // Agregar optimistamente
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        binding.recyclerChat.scrollToPosition(messages.size() - 1);
        binding.messageInput.setText("");

        db.collection("messages")
                .add(msg)
                .addOnSuccessListener(docRef -> Log.d("MENSAJE", "Mensaje enviado"))
                .addOnFailureListener(e -> {
                    Log.e("MENSAJE", "Error al enviar", e);
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                    messages.remove(msg);
                    adapter.notifyItemRemoved(messages.size());
                });
    }

    private void escucharMensajes() {
        messagesListener = db.collection("messages")
                .whereEqualTo("chatId", chatId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;

                    List<ChatMessage> nuevos = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        msg.setId(doc.getId());

                        if (!msg.getSenderId().equals(currentUserId) && !msg.isRead()) {
                            marcarComoLeido(doc.getId());
                        }

                        nuevos.add(msg);
                    }

                    Collections.sort(nuevos, (a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
                    messages.clear();
                    messages.addAll(nuevos);
                    adapter.notifyDataSetChanged();
                    binding.recyclerChat.scrollToPosition(messages.size() - 1);
                });
    }

    private void marcarComoLeido(String msgId) {
        db.collection("messages").document(msgId).update("read", true);
    }

    private String generateChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    @Override
    protected void onDestroy() {
        if (messagesListener != null) {
            messagesListener.remove();
        }
        super.onDestroy();
    }
}
