package com.iot.stayflowdev.superAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.Message;
import com.iot.stayflowdev.superAdmin.adapter.MessagesAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessagingTestActivity extends BaseSuperAdminActivity {

    private static final String TAG = "MessagingTestActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration messagesListener;

    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;
    private EditText messageInput;
    private FloatingActionButton sendButton;
    private TextView chatTitleText;
    private TextView connectionStatusText;

    private List<Message> messagesList = new ArrayList<>();
    private String currentChatId;
    private String currentUserId;
    private String currentUserName;
    private String receiverUserId;
    private String receiverUserName;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.superadmin_base_superadmin_activity;
    }

    @Override
    protected int getBottomNavigationSelectedItem() {
        return R.id.nav_reportes; // Usar reportes como categoría temporal
    }

    @Override
    protected String getToolbarTitle() {
        return "Mensajería (Pruebas)";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar el bottom navigation en la vista de chat
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(View.GONE);
        }

        // Inflar el contenido específico de esta activity
        LayoutInflater.from(this).inflate(R.layout.superadmin_messaging_test,
                findViewById(R.id.content_frame), true);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            currentUserName = currentUser.getDisplayName() != null ?
                             currentUser.getDisplayName() : "SuperAdmin";
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener datos del usuario receptor desde Intent
        Intent intent = getIntent();
        receiverUserId = intent.getStringExtra("USER_ID");
        receiverUserName = intent.getStringExtra("USER_NAME");
        String receiverEmail = intent.getStringExtra("USER_EMAIL");
        String receiverRole = intent.getStringExtra("USER_ROLE");

        // Verificar que se recibieron los datos del usuario
        if (receiverUserId == null || receiverUserName == null) {
            Toast.makeText(this, "Error: No se pudo obtener información del usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generar ID de chat único
        currentChatId = Message.generateChatId(currentUserId, receiverUserId);

        // Debug logging
        Log.d(TAG, "=== SUPER ADMIN CHAT DEBUG INFO ===");
        Log.d(TAG, "Current User ID (SuperAdmin): " + currentUserId);
        Log.d(TAG, "Receiver User ID (Admin): " + receiverUserId);
        Log.d(TAG, "Generated Chat ID: " + currentChatId);
        Log.d(TAG, "=================================");

        // Inicializar vistas
        initializeViews();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listeners
        setupListeners();

        // Cargar mensajes existentes
        loadMessages();
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatTitleText = findViewById(R.id.chatTitleText);
        connectionStatusText = findViewById(R.id.connectionStatusText);

        // Configurar título del chat
        if (chatTitleText != null) {
            chatTitleText.setText("Chat con " + receiverUserName);
        }

        // Mostrar estado de conexión
        updateConnectionStatus(true);
    }

    private void setupRecyclerView() {
        messagesAdapter = new MessagesAdapter(messagesList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Mostrar mensajes desde abajo
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        // También enviar mensaje al presionar Enter
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadMessages() {
        // Consulta simplificada sin orderBy para evitar el problema del índice
        messagesListener = db.collection("messages")
                .whereEqualTo("chatId", currentChatId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar mensajes", e);
                        updateConnectionStatus(false);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Message> newMessagesList = new ArrayList<>();

                        Log.d(TAG, "=== FIRESTORE LISTENER TRIGGERED ===");
                        Log.d(TAG, "Chat ID buscado: " + currentChatId);
                        Log.d(TAG, "Current User ID: " + currentUserId);
                        Log.d(TAG, "Documentos encontrados: " + queryDocumentSnapshots.size());

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Message message = doc.toObject(Message.class);
                            message.setId(doc.getId());

                            // Log detallado de cada mensaje
                            Log.d(TAG, "Mensaje encontrado:");
                            Log.d(TAG, "  - ID: " + message.getId());
                            Log.d(TAG, "  - ChatID: " + message.getChatId());
                            Log.d(TAG, "  - Sender: " + message.getSenderId() + " (" + message.getSenderName() + ")");
                            Log.d(TAG, "  - Receiver: " + message.getReceiverId() + " (" + message.getReceiverName() + ")");
                            Log.d(TAG, "  - Content: " + message.getContent());
                            Log.d(TAG, "  - Es mi mensaje: " + message.getSenderId().equals(currentUserId));

                            newMessagesList.add(message);

                            // Marcar como leído si no es nuestro mensaje y no está leído
                            if (!message.getSenderId().equals(currentUserId) && !message.isRead()) {
                                markMessageAsRead(doc.getId());
                            }
                        }

                        // Ordenar manualmente por timestamp
                        newMessagesList.sort((m1, m2) -> {
                            if (m1.getTimestamp() == null && m2.getTimestamp() == null) return 0;
                            if (m1.getTimestamp() == null) return -1;
                            if (m2.getTimestamp() == null) return 1;
                            return m1.getTimestamp().compareTo(m2.getTimestamp());
                        });

                        Log.d(TAG, "Total mensajes a agregar a la lista: " + newMessagesList.size());
                        Log.d(TAG, "================================");

                        // Mejorar la lógica de actualización
                        updateMessagesList(newMessagesList);

                        // Scroll automático y actualizar estado
                        scrollToBottom();
                        updateConnectionStatus(true);

                        Log.d(TAG, "Mensajes cargados/actualizados: " + newMessagesList.size());
                    }
                });
    }

    private void updateMessagesList(List<Message> newMessagesList) {
        // Log para debugging
        Log.d(TAG, "Actualizando lista. Mensajes actuales: " + messagesList.size() + ", Nuevos: " + newMessagesList.size());

        // Si la lista está vacía, agregar todos los mensajes nuevos
        if (messagesList.isEmpty()) {
            messagesList.addAll(newMessagesList);
            messagesAdapter.notifyDataSetChanged();
            Log.d(TAG, "Lista vacía, agregados " + newMessagesList.size() + " mensajes");
            return;
        }

        // Agregar solo mensajes que realmente son nuevos
        for (Message newMessage : newMessagesList) {
            boolean messageExists = false;

            // Verificar si el mensaje ya existe por ID
            for (Message existingMessage : messagesList) {
                if (newMessage.getId() != null && newMessage.getId().equals(existingMessage.getId())) {
                    messageExists = true;
                    break;
                }
            }

            // Si el mensaje no existe, agregarlo
            if (!messageExists) {
                // Verificar si es un mensaje optimista que debe ser reemplazado
                Message optimisticToReplace = null;
                for (int i = messagesList.size() - 1; i >= 0; i--) {
                    Message existing = messagesList.get(i);
                    if (existing.getId() == null &&
                        existing.getSenderId().equals(newMessage.getSenderId()) &&
                        existing.getContent().equals(newMessage.getContent())) {
                        optimisticToReplace = existing;
                        break;
                    }
                }

                if (optimisticToReplace != null) {
                    // Reemplazar mensaje optimista con el real
                    int index = messagesList.indexOf(optimisticToReplace);
                    messagesList.set(index, newMessage);
                    messagesAdapter.notifyItemChanged(index);
                    Log.d(TAG, "Mensaje optimista reemplazado en posición " + index);
                } else {
                    // Agregar nuevo mensaje
                    messagesList.add(newMessage);
                    messagesAdapter.notifyItemInserted(messagesList.size() - 1);
                    Log.d(TAG, "Nuevo mensaje agregado: " + newMessage.getContent());
                }
            }
        }

        Log.d(TAG, "Lista actualizada. Total de mensajes: " + messagesList.size());
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Crear nuevo mensaje
        Message message = new Message(
            currentChatId,
            currentUserId,
            currentUserName,
            receiverUserId,
            receiverUserName,
            messageText
        );

        // Actualización optimista: agregar mensaje inmediatamente a la lista
        messagesList.add(message);
        messagesAdapter.notifyItemInserted(messagesList.size() - 1);
        scrollToBottom();

        // Limpiar input inmediatamente para mejor UX
        messageInput.setText("");

        Log.d(TAG, "Mensaje agregado optimísticamente");

        // Enviar a Firestore
        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Mensaje enviado a Firestore: " + documentReference.getId());
                    // El listener de tiempo real se encargará de actualizar el mensaje con el ID real
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar mensaje", e);
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                    updateConnectionStatus(false);

                    // Remover el mensaje optimista en caso de error
                    if (!messagesList.isEmpty()) {
                        Message lastMessage = messagesList.get(messagesList.size() - 1);
                        if (lastMessage.getId() == null && lastMessage.getContent().equals(messageText)) {
                            messagesList.remove(messagesList.size() - 1);
                            messagesAdapter.notifyItemRemoved(messagesList.size());
                        }
                    }
                });
    }

    private void markMessageAsRead(String messageId) {
        db.collection("messages")
                .document(messageId)
                .update("read", true)
                .addOnFailureListener(e -> Log.e(TAG, "Error al marcar como leído", e));
    }

    private void scrollToBottom() {
        if (messagesAdapter.getItemCount() > 0) {
            messagesRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);
        }
    }

    private void updateConnectionStatus(boolean connected) {
        if (connectionStatusText != null) {
            if (connected) {
                connectionStatusText.setText("🟢 Conectado");
                connectionStatusText.setTextColor(getResources().getColor(R.color.md_theme_primary, null));
            } else {
                connectionStatusText.setText("🔴 Desconectado");
                connectionStatusText.setTextColor(getResources().getColor(R.color.md_theme_error, null));
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
}
