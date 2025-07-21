package com.iot.stayflowdev.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

public class ClienteChatActivity extends AppCompatActivity {

    private static final String TAG = "ClienteChatActivity";

    public static final String EXTRA_HOTEL_ID = "hotel_id";
    public static final String EXTRA_HOTEL_NAME = "hotel_name";
    public static final String EXTRA_ADMIN_ID = "admin_id";

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
    private String adminId;
    private String adminName;
    private String hotelId;
    private String hotelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ClienteChatActivity onCreate iniciado");
        setContentView(R.layout.activity_cliente_chat);

        initializeFirebase();
        initializeViews();
        getIntentData();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
        Log.d(TAG, "ClienteChatActivity onCreate completado");
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            currentUserName = currentUser.getDisplayName() != null ?
                             currentUser.getDisplayName() : "Cliente";
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        messagesRecyclerView = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.fabSendMessage);
        chatTitleText = findViewById(R.id.textViewChatTitle);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        hotelId = intent.getStringExtra(EXTRA_HOTEL_ID);
        hotelName = intent.getStringExtra(EXTRA_HOTEL_NAME);
        adminId = intent.getStringExtra(EXTRA_ADMIN_ID);

        Log.d(TAG, "Intent data - Hotel ID: " + hotelId + ", Hotel Name: " + hotelName + ", Admin ID: " + adminId);

        if (hotelId == null || adminId == null) {
            Log.e(TAG, "Error: hotelId o adminId son nulos");
            Toast.makeText(this, "Error al cargar el chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generar chat ID único entre cliente y administrador del hotel
        currentChatId = ChatMessage.generateChatId(currentUserId, adminId);
        Log.d(TAG, "Chat ID generado: " + currentChatId);

        // Configurar título del chat
        chatTitleText.setText("Chat con " + (hotelName != null ? hotelName : "Administrador"));

        // Obtener nombre del administrador
        obtenerNombreAdministrador();
    }

    private void obtenerNombreAdministrador() {
        db.collection("usuarios")  // Cambiar de "hotelAdmins" a "usuarios"
            .document(adminId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    adminName = documentSnapshot.getString("nombre");
                    if (adminName == null) {
                        adminName = documentSnapshot.getString("name");
                    }
                    if (adminName == null) {
                        adminName = "Administrador del Hotel";
                    }
                } else {
                    adminName = "Administrador del Hotel";
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener datos del administrador", e);
                adminName = "Administrador del Hotel";
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

                    // ✅ EVITAR DUPLICADOS - Fusionar mensajes de Firestore con mensajes locales
                    List<ChatMessage> firestoreMessages = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            message.setId(doc.getId());
                            firestoreMessages.add(message);
                        }
                    }

                    // Fusionar mensajes evitando duplicados
                    fusionarMensajes(firestoreMessages);

                    // Marcar mensajes como leídos
                    marcarMensajesComoLeidos();
                });
    }

    /**
     * Fusiona mensajes de Firestore con mensajes locales evitando duplicados
     */
    private void fusionarMensajes(List<ChatMessage> firestoreMessages) {
        try {
            // Crear una nueva lista para los mensajes finales
            List<ChatMessage> mensajesFusionados = new ArrayList<>();

            // Añadir todos los mensajes de Firestore
            mensajesFusionados.addAll(firestoreMessages);

            // Añadir mensajes locales que no estén en Firestore (mensajes pendientes)
            for (ChatMessage localMsg : messagesList) {
                if (localMsg == null) continue; // Validación de seguridad

                boolean existeEnFirestore = false;

                // Verificar si el mensaje local ya existe en Firestore
                for (ChatMessage firestoreMsg : firestoreMessages) {
                    if (firestoreMsg == null) continue; // Validación de seguridad

                    if ((localMsg.getId() != null && localMsg.getId().equals(firestoreMsg.getId())) ||
                        (localMsg.getContent() != null && localMsg.getContent().equals(firestoreMsg.getContent()) &&
                         localMsg.getSenderId() != null && localMsg.getSenderId().equals(firestoreMsg.getSenderId()) &&
                         localMsg.getTimestamp() != null && firestoreMsg.getTimestamp() != null &&
                         Math.abs(localMsg.getTimestamp().getSeconds() - firestoreMsg.getTimestamp().getSeconds()) < 5)) {
                        existeEnFirestore = true;
                        break;
                    }
                }

                // Si no existe en Firestore, mantenerlo (mensaje pendiente)
                if (!existeEnFirestore) {
                    mensajesFusionados.add(localMsg);
                }
            }

            // Ordenar por timestamp (con validación de nulos)
            mensajesFusionados.sort((m1, m2) -> {
                if (m1.getTimestamp() == null && m2.getTimestamp() == null) return 0;
                if (m1.getTimestamp() == null) return 1;
                if (m2.getTimestamp() == null) return -1;
                return m1.getTimestamp().compareTo(m2.getTimestamp());
            });

            // Actualizar la lista solo si hay cambios
            if (!mensajesIguales(messagesList, mensajesFusionados)) {
                messagesList.clear();
                messagesList.addAll(mensajesFusionados);
                messagesAdapter.notifyDataSetChanged();

                if (!messagesList.isEmpty()) {
                    messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al fusionar mensajes: " + e.getMessage());
            // En caso de error, solo actualizar con mensajes de Firestore
            messagesList.clear();
            messagesList.addAll(firestoreMessages);
            messagesAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Compara si dos listas de mensajes son iguales
     */
    private boolean mensajesIguales(List<ChatMessage> lista1, List<ChatMessage> lista2) {
        try {
            if (lista1.size() != lista2.size()) return false;

            for (int i = 0; i < lista1.size(); i++) {
                ChatMessage msg1 = lista1.get(i);
                ChatMessage msg2 = lista2.get(i);

                if (msg1 == null || msg2 == null) return false;
                if (msg1.getContent() == null || msg2.getContent() == null) return false;
                if (msg1.getSenderId() == null || msg2.getSenderId() == null) return false;
                if (msg1.getTimestamp() == null || msg2.getTimestamp() == null) return false;

                if (!msg1.getContent().equals(msg2.getContent()) ||
                    !msg1.getSenderId().equals(msg2.getSenderId()) ||
                    !msg1.getTimestamp().equals(msg2.getTimestamp())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al comparar mensajes: " + e.getMessage());
            return false;
        }
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        if (adminName == null) {
            adminName = "Administrador del Hotel";
        }

        // Usar timestamp local para envío instantáneo
        Timestamp currentTimestamp = Timestamp.now();

        ChatMessage message = new ChatMessage(
                currentChatId,
                currentUserId,
                currentUserName,
                adminId,
                adminName,
                messageText,
                currentTimestamp,
                false
        );

        // ✅ MOSTRAR MENSAJE INSTANTÁNEAMENTE (Actualización optimista)
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

                    // ❌ Si falla, remover el mensaje de la lista y restaurar el texto
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
            if (!message.isRead() && message.getReceiverId().equals(currentUserId)) {
                db.collection("messages")
                        .document(message.getId())
                        .update("read", true);
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
