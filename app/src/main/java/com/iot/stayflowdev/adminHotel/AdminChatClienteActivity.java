package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.adminHotel.model.ChatMessage;
import com.iot.stayflowdev.adminHotel.adapter.ChatAdapter;
import com.iot.stayflowdev.databinding.ActivityAdminChatClienteBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminChatClienteActivity extends AppCompatActivity {
    private ActivityAdminChatClienteBinding binding;

    private static final String TAG = "AdminChatClienteActivity";

    public static final String EXTRA_CLIENTE_ID = "cliente_id";
    public static final String EXTRA_CLIENTE_NAME = "cliente_name";
    public static final String EXTRA_CHAT_ID = "chat_id";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration messagesListener;

    private RecyclerView messagesRecyclerView;
    private ChatAdapter messagesAdapter;
    private EditText messageInput;
    private FloatingActionButton sendButton;
    private TextView chatTitleText;

    private List<ChatMessage> messagesList = new ArrayList<>();
    private String currentChatId;
    private String currentUserId;
    private String currentUserName;
    private String clienteId;
    private String clienteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AdminChatClienteActivity onCreate iniciado");
        setContentView(R.layout.activity_admin_chat_cliente);

        initializeFirebase();
        initializeViews();
        getIntentData();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
        Log.d(TAG, "AdminChatClienteActivity onCreate completado");
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            currentUserName = currentUser.getDisplayName() != null ?
                             currentUser.getDisplayName() : "Administrador";
        }
    }

    private void initializeViews() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        messagesRecyclerView = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.fabSendMessage);
        chatTitleText = findViewById(R.id.textViewChatTitle);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        clienteId = intent.getStringExtra(EXTRA_CLIENTE_ID);
        clienteName = intent.getStringExtra(EXTRA_CLIENTE_NAME);
        currentChatId = intent.getStringExtra(EXTRA_CHAT_ID);

        Log.d(TAG, "Intent data - Cliente ID: " + clienteId + ", Cliente Name: " + clienteName + ", Chat ID: " + currentChatId);

        if (clienteId == null || currentChatId == null) {
            // Buscar automáticamente el chat donde el admin es currentUserId y él es receiver o sender
            db.collection("messages")
                    .whereEqualTo("receiverId", currentUserId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            ChatMessage msg = querySnapshot.getDocuments().get(0).toObject(ChatMessage.class);
                            clienteId = msg.getSenderId();
                            currentChatId = msg.getChatId();
                            clienteName = msg.getSenderName();
                            chatTitleText.setText("Chat con " + clienteName);
                            loadMessages(); // llama aquí directamente
                        } else {
                            Toast.makeText(this, "No hay mensajes previos con ningún cliente", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
            return;
        }


        // Si no hay chat ID, generarlo
        if (currentChatId == null) {
            currentChatId = ChatMessage.generateChatId(currentUserId, clienteId);
            Log.d(TAG, "Chat ID generado: " + currentChatId);
        }

        // Configurar título del chat
        chatTitleText.setText("Chat con " + (clienteName != null ? clienteName : "Cliente"));

        // Obtener nombre del cliente si no se proporcionó
        if (clienteName == null) {
            obtenerNombreCliente();
        }
    }

    private void obtenerNombreCliente() {
        db.collection("usuarios")
            .document(clienteId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombre = documentSnapshot.getString("nombre");
                    if (nombre == null) {
                        nombre = documentSnapshot.getString("name");
                    }
                    if (nombre != null) {
                        clienteName = nombre;
                        chatTitleText.setText("Chat con " + clienteName);
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener datos del cliente", e);
            });
    }

    private void setupRecyclerView() {
        messagesAdapter = new ChatAdapter(messagesList, currentUserId);
        messagesRecyclerView.setAdapter(messagesAdapter);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadMessages() {
        if (currentChatId == null) return;

        messagesListener = db.collection("messages")
                .whereEqualTo("chatId", currentChatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al escuchar mensajes", error);
                        return;
                    }

                    messagesList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            message.setId(doc.getId());
                            messagesList.add(message);
                        }
                    }

                    messagesAdapter.notifyDataSetChanged();
                    if (!messagesList.isEmpty()) {
                        messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                    }

                    // Marcar mensajes como leídos
                    marcarMensajesComoLeidos();
                });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        if (clienteName == null) {
            clienteName = "Cliente";
        }

        // Usar timestamp local para envío instantáneo
        Timestamp currentTimestamp = Timestamp.now();

        ChatMessage message = new ChatMessage(
                currentChatId,
                currentUserId,
                currentUserName,
                clienteId,
                clienteName,
                messageText,
                currentTimestamp,
                false
        );

        // Mostrar mensaje instantáneamente
        messagesList.add(message);
        messagesAdapter.notifyItemInserted(messagesList.size() - 1);
        messagesRecyclerView.scrollToPosition(messagesList.size() - 1);

        // Limpiar el campo de entrada inmediatamente
        messageInput.setText("");

        // Enviar a Firestore en segundo plano
        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Mensaje enviado exitosamente");
                    // Actualizar el ID del mensaje con el ID real de Firestore
                    String realId = documentReference.getId();
                    for (int i = messagesList.size() - 1; i >= 0; i--) {
                        ChatMessage msg = messagesList.get(i);
                        if (msg.getId() == null && msg.getContent().equals(messageText) &&
                            msg.getSenderId().equals(currentUserId)) {
                            msg.setId(realId);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar mensaje", e);
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();

                    // Si falla, remover el mensaje de la lista y restaurar el texto
                    for (int i = messagesList.size() - 1; i >= 0; i--) {
                        ChatMessage msg = messagesList.get(i);
                        if (msg.getId() == null && msg.getContent().equals(messageText) &&
                            msg.getSenderId().equals(currentUserId)) {
                            messagesList.remove(i);
                            messagesAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                    messageInput.setText(messageText);
                });
    }

    private void marcarMensajesComoLeidos() {
        for (ChatMessage message : messagesList) {
            if (message.getId() != null && !message.isRead() && message.getReceiverId().equals(currentUserId)) {
                db.collection("messages")
                        .document(message.getId())
                        .update("read", true)
                        .addOnFailureListener(e -> Log.e(TAG, "Error al marcar mensaje como leído", e));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
