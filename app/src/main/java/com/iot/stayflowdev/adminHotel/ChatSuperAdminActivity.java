package com.iot.stayflowdev.adminHotel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class ChatSuperAdminActivity extends AppCompatActivity {

    private static final String TAG = "ChatSuperAdminActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration messagesListener;
    private ListenerRegistration superAdminStatusListener;

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
    private String superAdminId;
    private String superAdminName = "Super Administrador";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_chat_superadmin);

        // Configurar toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat con Super Admin");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            currentUserName = currentUser.getDisplayName() != null ?
                             currentUser.getDisplayName() : "Administrador";
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Buscar el super admin real en la base de datos
        findSuperAdmin();

        // Inicializar vistas
        initializeViews();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listeners
        setupListeners();
    }

    private void findSuperAdmin() {
        db.collection("usuarios")
                .whereEqualTo("rol", "superadmin")
                .whereEqualTo("estado", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        superAdminId = document.getId();
                        String nombres = document.getString("nombres");
                        String apellidos = document.getString("apellidos");
                        if (nombres != null && apellidos != null) {
                            superAdminName = nombres + " " + apellidos;
                        }

                        // Generar ID de chat único después de encontrar el super admin
                        currentChatId = Message.generateChatId(currentUserId, superAdminId);

                        // Debug logging
                        Log.d(TAG, "=== CHAT DEBUG INFO ===");
                        Log.d(TAG, "Current User ID: " + currentUserId);
                        Log.d(TAG, "Super Admin ID: " + superAdminId);
                        Log.d(TAG, "Generated Chat ID: " + currentChatId);
                        Log.d(TAG, "====================");

                        // Cargar mensajes existentes
                        loadMessages();

                        // Actualizar título
                        updateChatTitle();

                        Log.d(TAG, "Super Admin encontrado: " + superAdminName + " (ID: " + superAdminId + ")");
                    } else {
                        Log.w(TAG, "No se encontró super admin en la base de datos");
                        Toast.makeText(this, "No se pudo conectar con el Super Admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar super admin", e);
                    Toast.makeText(this, "Error al conectar con el Super Admin", Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatTitleText = findViewById(R.id.chatTitleText);
        connectionStatusText = findViewById(R.id.connectionStatusText);

        // Configurar título del chat
        updateChatTitle();

        // Mostrar estado de conexión inicial
        updateConnectionStatus(true);
    }

    private void updateChatTitle() {
        if (chatTitleText != null) {
            chatTitleText.setText("Chat con " + superAdminName);
        }
    }

    private void setupRecyclerView() {
        messagesAdapter = new MessagesAdapter(messagesList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void loadMessages() {
        if (currentChatId == null) {
            Log.w(TAG, "No se puede cargar mensajes: currentChatId es null");
            return;
        }

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

                        updateMessagesList(newMessagesList);
                        scrollToBottom();
                        updateConnectionStatus(true);

                        Log.d(TAG, "Mensajes cargados/actualizados: " + newMessagesList.size());
                    }
                });

        startSuperAdminStatusMonitoring();
    }

    private void startSuperAdminStatusMonitoring() {
        if (superAdminId != null) {
            superAdminStatusListener = db.collection("usuarios")
                    .document(superAdminId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Error al escuchar estado del super admin", e);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Boolean isOnline = documentSnapshot.getBoolean("conectado");
                            updateSuperAdminConnectionStatus(isOnline != null && isOnline);
                        }
                    });
        }
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
        if (messageText.isEmpty() || currentChatId == null) {
            return;
        }

        Message message = new Message(
            currentChatId,
            currentUserId,
            currentUserName,
            superAdminId,
            superAdminName,
            messageText
        );

        messagesList.add(message);
        messagesAdapter.notifyItemInserted(messagesList.size() - 1);
        scrollToBottom();

        messageInput.setText("");

        Log.d(TAG, "Mensaje agregado optimísticamente");

        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Mensaje enviado a Firestore: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar mensaje", e);
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                    updateConnectionStatus(false);

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

    private void updateSuperAdminConnectionStatus(boolean isConnected) {
        if (connectionStatusText != null) {
            if (isConnected) {
                connectionStatusText.setText("🟢 Super Admin en línea");
                connectionStatusText.setTextColor(getResources().getColor(R.color.md_theme_primary, null));
            } else {
                connectionStatusText.setText("🔴 Super Admin fuera de línea");
                connectionStatusText.setTextColor(getResources().getColor(R.color.md_theme_error, null));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        // Volver al inicio del administrador
        Intent intent = new Intent(this, AdminInicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
        if (superAdminStatusListener != null) {
            superAdminStatusListener.remove();
        }
    }
}
