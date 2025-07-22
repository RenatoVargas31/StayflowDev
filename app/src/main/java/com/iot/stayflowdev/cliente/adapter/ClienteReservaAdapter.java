package com.iot.stayflowdev.cliente.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.cliente.ClienteChatActivity;
import com.iot.stayflowdev.cliente.ReservaResumenActivity;
import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.viewmodels.ReservaViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ClienteReservaAdapter extends RecyclerView.Adapter<ClienteReservaAdapter.ReservaViewHolder> {
    private final List<Reserva> reservas;
    private final Context context;
    private final ReservaViewModel viewModel;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final String TAG = "ClienteReservaAdapter";

    public ClienteReservaAdapter(Context context, ReservaViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.reservas = new ArrayList<>();
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cliente_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Configurar código de reserva usando el ID en lugar del código
        holder.codigoReserva.setText("Reserva #" + reserva.getId());

        // Configurar fechas de la reserva
        String fechasReserva = formatFechaReserva(reserva.getFechaInicio()) +
                               " - " +
                               formatFechaReserva(reserva.getFechaFin());
        holder.fechasReserva.setText(fechasReserva);

        // Configurar estado
        holder.estado.setText(reserva.getEstado());

        // Configurar visibilidad de taxi
        if (reserva.isQuieroTaxi()) {
            holder.iconoTaxi.setVisibility(View.VISIBLE);
            holder.textoTaxi.setVisibility(View.VISIBLE);
        } else {
            holder.iconoTaxi.setVisibility(View.GONE);
            holder.textoTaxi.setVisibility(View.GONE);
        }

        // Obtener y configurar nombre del hotel
        obtenerNombreHotel(reserva, hotel -> {
            if (hotel != null) {
                holder.nombreHotel.setText(hotel.getNombre());
            } else {
                holder.nombreHotel.setText("Hotel no disponible");
            }
        });

        // Configurar listener del botón de chat
        holder.buttonChat.setOnClickListener(v -> {
            Log.d(TAG, "Botón de chat presionado para reserva: " + reserva.getId());
            obtenerAdministradorHotel(reserva, hotel -> {
                if (hotel != null) {
                    Log.d(TAG, "Hotel obtenido, abriendo chat con admin: " + hotel.getAdminId());
                    abrirChat(reserva, hotel);
                } else {
                    Log.e(TAG, "No se pudo obtener el hotel o administrador");
                    Toast.makeText(context, "No se pudo obtener información del administrador", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Configurar listener del botón de detalles
        holder.buttonVerDetalles.setOnClickListener(v -> {
            Log.d(TAG, "Botón de detalles presionado para reserva: " + reserva.getId());
            mostrarDetallesReserva(reserva);
        });
    }

    /**
     * Muestra los detalles de la reserva en ReservaResumenActivity
     */
    private void mostrarDetallesReserva(Reserva reserva) {
        try {
            // Convertir el objeto Reserva a JSON usando Gson
            Gson gson = new Gson();
            String reservaJson = gson.toJson(reserva);
            Log.d(TAG, "Reserva serializada: " + reservaJson);

            // Crear intent para abrir ReservaResumenActivity
            Intent intent = new Intent(context, ReservaResumenActivity.class);
            intent.putExtra("reserva_data", reservaJson);
            intent.putExtra("modo_visualizacion", true); // Indicar que es modo visualización
            intent.putExtra("ocultar_boton_confirmar", true); // Ocultar el botón de confirmar reserva
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar detalles de reserva: " + e.getMessage());
            Toast.makeText(context, "Error al mostrar detalles de la reserva", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Obtiene el nombre del hotel asociado a una reserva
     */
    private void obtenerNombreHotel(Reserva reserva, OnHotelLoadedListener listener) {
        if (reserva.getIdHotel() == null) {
            Log.e(TAG, "ID del hotel es nulo para la reserva " + reserva.getId());
            listener.onHotelLoaded(null);
            return;
        }

        Log.d(TAG, "Obteniendo nombre del hotel con ID: " + reserva.getIdHotel() + " para reserva " + reserva.getId());

        // Consulta directa a Firestore para evitar problemas con LiveData compartido
        FirebaseFirestore.getInstance()
            .collection("hoteles")
            .document(reserva.getIdHotel())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Hotel hotel = documentSnapshot.toObject(Hotel.class);
                    if (hotel != null) {
                        Log.d(TAG, "Hotel obtenido correctamente: " + hotel.getNombre() + " para reserva " + reserva.getId());
                        listener.onHotelLoaded(hotel);
                    } else {
                        Log.e(TAG, "No se pudo convertir el documento a Hotel para reserva " + reserva.getId());
                        listener.onHotelLoaded(null);
                    }
                } else {
                    Log.e(TAG, "El documento del hotel no existe para reserva " + reserva.getId());
                    listener.onHotelLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener hotel para reserva " + reserva.getId() + ": " + e.getMessage());
                listener.onHotelLoaded(null);
            });
    }

    /**
     * Obtiene el administrador del hotel y abre el chat
     */
    private void obtenerAdministradorHotel(Reserva reserva, OnHotelLoadedListener listener) {
        if (reserva.getIdHotel() == null) {
            Log.e(TAG, "ID del hotel es nulo para la reserva " + reserva.getId());
            listener.onHotelLoaded(null);
            return;
        }

        Log.d(TAG, "Obteniendo administrador para hotel ID: " + reserva.getIdHotel());

        // Consulta directa a Firestore para evitar problemas con LiveData compartido
        FirebaseFirestore.getInstance()
            .collection("hoteles")
            .document(reserva.getIdHotel())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Hotel hotel = documentSnapshot.toObject(Hotel.class);
                    if (hotel != null) {
                        Log.d(TAG, "Hotel encontrado: " + hotel.getNombre());
                        Log.d(TAG, "Campo administradorAsignado: " + hotel.getAdministradorAsignado());

                        if (hotel.getAdministradorAsignado() != null && !hotel.getAdministradorAsignado().trim().isEmpty()) {
                            // El administradorAsignado debería ser el ID del documento en usuarios
                            String adminId = hotel.getAdministradorAsignado().trim();
                            Log.d(TAG, "Buscando administrador en colección usuarios con ID: " + adminId);

                            // Buscar en la colección usuarios (la colección correcta)
                            FirebaseFirestore.getInstance()
                                .collection("usuarios")
                                .document(adminId)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        Log.d(TAG, "Administrador encontrado en usuarios con ID: " + adminId);
                                        // Verificar que sea realmente un administrador de hotel
                                        String tipoUsuario = userSnapshot.getString("tipoUsuario");
                                        String rol = userSnapshot.getString("rol");
                                        Log.d(TAG, "Tipo usuario: " + tipoUsuario + ", Rol: " + rol);

                                        // Actualizar validación para incluir "adminhotel"
                                        if ("administrador".equals(tipoUsuario) || "admin".equals(tipoUsuario) ||
                                            "administrador de hotel".equals(tipoUsuario) ||
                                            "hotel admin".equals(rol) || "adminhotel".equals(rol)) {
                                            Log.d(TAG, "Usuario validado como administrador de hotel");
                                            hotel.setAdminId(adminId);
                                            listener.onHotelLoaded(hotel);
                                        } else {
                                            Log.e(TAG, "Usuario encontrado pero no es administrador de hotel");
                                            buscarCualquierAdministrador(hotel, listener);
                                        }
                                    } else {
                                        Log.e(TAG, "No existe documento usuarios con ID: " + adminId);
                                        buscarCualquierAdministrador(hotel, listener);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al buscar en usuarios: " + e.getMessage());
                                    buscarCualquierAdministrador(hotel, listener);
                                });
                        } else {
                            Log.e(TAG, "Campo administradorAsignado es nulo o vacío");
                            buscarCualquierAdministrador(hotel, listener);
                        }
                    } else {
                        Log.e(TAG, "No se pudo convertir el documento a Hotel");
                        listener.onHotelLoaded(null);
                    }
                } else {
                    Log.e(TAG, "El documento del hotel no existe");
                    listener.onHotelLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener hotel: " + e.getMessage());
                listener.onHotelLoaded(null);
            });
    }

    /**
     * Busca cualquier administrador de hotel disponible como fallback
     */
    private void buscarCualquierAdministrador(Hotel hotel, OnHotelLoadedListener listener) {
        Log.d(TAG, "Buscando cualquier administrador de hotel disponible...");

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereEqualTo("tipoUsuario", "administrador")
            .limit(5)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "Administradores encontrados: " + querySnapshot.size());

                if (!querySnapshot.isEmpty()) {
                    // Usar el primer administrador encontrado
                    String adminId = querySnapshot.getDocuments().get(0).getId();
                    String nombre = querySnapshot.getDocuments().get(0).getString("nombre");
                    Log.d(TAG, "Usando administrador: " + nombre + " (ID: " + adminId + ")");
                    hotel.setAdminId(adminId);
                    listener.onHotelLoaded(hotel);
                } else {
                    Log.d(TAG, "No se encontraron administradores, buscando por rol...");
                    // Intentar buscar por rol
                    buscarPorRol(hotel, listener);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al buscar administradores: " + e.getMessage());
                buscarPorRol(hotel, listener);
            });
    }

    /**
     * Busca administradores por rol como último recurso
     */
    private void buscarPorRol(Hotel hotel, OnHotelLoadedListener listener) {
        Log.d(TAG, "Buscando administradores por rol...");

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereEqualTo("rol", "hotel admin")
            .limit(5)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String adminId = querySnapshot.getDocuments().get(0).getId();
                    String nombre = querySnapshot.getDocuments().get(0).getString("nombre");
                    Log.d(TAG, "Administrador encontrado por rol: " + nombre + " (ID: " + adminId + ")");
                    hotel.setAdminId(adminId);
                    listener.onHotelLoaded(hotel);
                } else {
                    Log.d(TAG, "Listando todos los usuarios para debug...");
                    listarUsuariosParaDebug(hotel, listener);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al buscar por rol: " + e.getMessage());
                listarUsuariosParaDebug(hotel, listener);
            });
    }

    /**
     * Lista algunos usuarios para debug y encontrar la estructura correcta
     */
    private void listarUsuariosParaDebug(Hotel hotel, OnHotelLoadedListener listener) {
        Log.d(TAG, "Listando usuarios para debug...");

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .limit(5)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "Total usuarios encontrados: " + querySnapshot.size());

                for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                    var doc = querySnapshot.getDocuments().get(i);
                    Log.d(TAG, "Usuario " + i + " - ID: " + doc.getId());
                    Log.d(TAG, "Usuario " + i + " - Nombre: " + doc.getString("nombre"));
                    Log.d(TAG, "Usuario " + i + " - TipoUsuario: " + doc.getString("tipoUsuario"));
                    Log.d(TAG, "Usuario " + i + " - Rol: " + doc.getString("rol"));
                    Log.d(TAG, "Usuario " + i + " - Email: " + doc.getString("email"));
                }

                // Si hay usuarios, usar el primero disponible como fallback temporal
                if (!querySnapshot.isEmpty()) {
                    String firstUserId = querySnapshot.getDocuments().get(0).getId();
                    Log.d(TAG, "Usando primer usuario disponible como fallback: " + firstUserId);
                    hotel.setAdminId(firstUserId);
                    listener.onHotelLoaded(hotel);
                } else {
                    Log.e(TAG, "No hay usuarios disponibles en la colección");
                    listener.onHotelLoaded(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al listar usuarios: " + e.getMessage());
                listener.onHotelLoaded(null);
            });
    }

    /**
     * Abre la actividad de chat con el administrador del hotel
     */
    private void abrirChat(Reserva reserva, Hotel hotel) {
        Log.d(TAG, "Abriendo chat - Hotel: " + hotel.getNombre() + ", Admin: " + hotel.getAdminId());
        try {
            Intent intent = new Intent(context, ClienteChatActivity.class);
            intent.putExtra(ClienteChatActivity.EXTRA_HOTEL_ID, reserva.getIdHotel());
            intent.putExtra(ClienteChatActivity.EXTRA_HOTEL_NAME, hotel.getNombre());
            intent.putExtra(ClienteChatActivity.EXTRA_ADMIN_ID, hotel.getAdminId());
            context.startActivity(intent);
            Log.d(TAG, "Actividad de chat iniciada exitosamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir chat: " + e.getMessage());
            Toast.makeText(context, "Error al abrir el chat", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Actualiza la lista de reservas y notifica los cambios
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setReservas(List<Reserva> nuevasReservas) {
        this.reservas.clear();
        if (nuevasReservas != null) {
            this.reservas.addAll(nuevasReservas);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    /**
     * Formatea un timestamp de Firestore a un formato legible
     */
    private String formatFechaReserva(Timestamp timestamp) {
        if (timestamp == null) return "Fecha no disponible";
        Date date = timestamp.toDate();
        return sdf.format(date);
    }

    /**
     * ViewHolder para los items de reserva
     */
    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        final TextView codigoReserva;
        final TextView nombreHotel;
        final TextView fechasReserva;
        final TextView estado;
        final ImageView iconoTaxi;
        final TextView textoTaxi;
        final MaterialButton buttonChat;
        final MaterialButton buttonVerDetalles;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            codigoReserva = itemView.findViewById(R.id.textViewCodigoReserva);
            nombreHotel = itemView.findViewById(R.id.textViewNombreHotel);
            fechasReserva = itemView.findViewById(R.id.textViewFechasReserva);
            estado = itemView.findViewById(R.id.textViewEstado);
            iconoTaxi = itemView.findViewById(R.id.imageViewTaxi);
            textoTaxi = itemView.findViewById(R.id.textViewTaxi);
            buttonChat = itemView.findViewById(R.id.buttonChat);
            buttonVerDetalles = itemView.findViewById(R.id.buttonVerDetalles);
        }
    }

    /**
     * Interfaz para manejar la carga asíncrona de hoteles
     */
    interface OnHotelLoadedListener {
        void onHotelLoaded(Hotel hotel);
    }
}
