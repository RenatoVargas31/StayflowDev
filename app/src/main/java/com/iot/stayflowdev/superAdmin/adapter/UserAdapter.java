package com.iot.stayflowdev.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private List<User> userListFull;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onDetailsClick(User user);
        void onStatusChanged(User user, boolean isEnabled, String reason);
        void onVerifyTaxista(User user); // Nueva interfaz para verificar taxistas
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = new ArrayList<>(userList);
        this.userListFull = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.superadmin_item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Usar el nombre completo o construirlo a partir de nombres y apellidos
        holder.textViewUserName.setText(user.getName());

        // Mostrar el rol con descripción legible
        holder.textViewUserRole.setText(user.getRoleDescription());

        // Mostrar el estado
        holder.textViewUserStatus.setText(user.isEnabled() ? "Habilitado" : "Deshabilitado");

        // Cargar la imagen del usuario con Glide
        String fotoUrl = user.getFotoPerfilUrl();

        // Debug: Log para ver las URLs
        android.util.Log.d("UserAdapter", "Usuario: " + user.getName() + ", FotoURL: " + fotoUrl);

        // Limpiar configuraciones previas
        holder.imageViewUserAvatar.setColorFilter(null);
        holder.imageViewUserAvatar.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        if (fotoUrl != null && !fotoUrl.isEmpty() && !fotoUrl.equals("null")) {
            // Debug: Confirmar que se intenta cargar la imagen
            android.util.Log.d("UserAdapter", "Cargando imagen para: " + user.getName());

            // Cargar la foto del usuario desde Firebase Storage
            Glide.with(holder.itemView.getContext())
                    .load(fotoUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(200, 200)) // Redimensionar para mejor rendimiento
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            android.util.Log.e("UserAdapter", "Error cargando imagen para " + user.getName() + ": " + e.getMessage());
                            // Si falla la carga, mostrar ícono por defecto
                            holder.imageViewUserAvatar.setImageResource(R.drawable.ic_person);
                            holder.imageViewUserAvatar.setBackgroundColor(holder.itemView.getContext().getColor(R.color.md_theme_primaryContainer));
                            holder.imageViewUserAvatar.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_onPrimaryContainer));
                            holder.imageViewUserAvatar.setPadding(16, 16, 16, 16);
                            holder.imageViewUserAvatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            android.util.Log.d("UserAdapter", "Imagen cargada exitosamente para: " + user.getName());
                            // Configuración para foto real
                            holder.imageViewUserAvatar.setPadding(0, 0, 0, 0);
                            holder.imageViewUserAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            return false;
                        }
                    })
                    .into(holder.imageViewUserAvatar);

        } else {
            // Debug: Usuario sin foto
            android.util.Log.d("UserAdapter", "Usuario sin foto: " + user.getName());

            // No hay foto, mostrar ícono por defecto con fondo de color
            Glide.with(holder.itemView.getContext()).clear(holder.imageViewUserAvatar);
            holder.imageViewUserAvatar.setImageResource(R.drawable.ic_person);

            // Configuración para ícono por defecto
            holder.imageViewUserAvatar.setBackgroundColor(holder.itemView.getContext().getColor(R.color.md_theme_primaryContainer));
            holder.imageViewUserAvatar.setColorFilter(holder.itemView.getContext().getColor(R.color.md_theme_onPrimaryContainer));
            holder.imageViewUserAvatar.setPadding(16, 16, 16, 16);
            holder.imageViewUserAvatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        // 🔴 Evitar que el listener se dispare al hacer setChecked
        holder.switchUserStatus.setOnCheckedChangeListener(null);
        holder.switchUserStatus.setChecked(user.isEnabled());

        // 🟢 Volver a asignar el listener después de setChecked
        holder.switchUserStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onStatusChanged(user, isChecked, null);
                // Actualizar el texto del estado
                holder.textViewUserStatus.setText(isChecked ? "Habilitado" : "Deshabilitado");
            }
        });

        // Hacer que toda la tarjeta sea clickeable
        holder.cardUser.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(user);
            }
        });

        // Nuevo: Listener para verificación de taxistas
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onVerifyTaxista(user);
                return true; // Indicar que el evento fue manejado
            }
            return false;
        });

        // Nuevo: Mostrar botón de verificación solo para taxistas no verificados
        if ("driver".equals(user.getRol()) && !user.isVerificado()) {
            holder.btnVerifyTaxista.setVisibility(View.VISIBLE);
            holder.btnVerifyTaxista.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerifyTaxista(user);
                }
            });
        } else {
            holder.btnVerifyTaxista.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Agregar nuevo usuario estructuradamente
    public void addUser(User user) {
        userList.add(0, user);
        userListFull.add(0, user);
        notifyItemInserted(0);
    }

    // Actualizar la lista completa
    public void updateUserList(List<User> newUserList) {
        try {
            // Verificar que userList esté inicializada
            if (userList == null) {
                userList = new ArrayList<>();
            }

            // Verificar que la nueva lista no sea null
            if (newUserList == null) {
                newUserList = new ArrayList<>();
            }

            userList.clear();
            userList.addAll(newUserList);
            notifyDataSetChanged();
        } catch (Exception e) {
            android.util.Log.e("UserAdapter", "Error en updateUserList: " + e.getMessage());
            // Si hay error, inicializar lista vacía
            if (userList == null) userList = new ArrayList<>();
            notifyDataSetChanged();
        }
    }

    // Actualizar tanto la lista completa como la lista original (para filtros)
    public void updateFullList(List<User> newUserList) {
        try {
            // Verificar que las listas estén inicializadas
            if (userList == null) {
                userList = new ArrayList<>();
            }
            if (userListFull == null) {
                userListFull = new ArrayList<>();
            }

            // Verificar que la nueva lista no sea null
            if (newUserList == null) {
                newUserList = new ArrayList<>();
            }

            userList.clear();
            userList.addAll(newUserList);
            userListFull.clear();
            userListFull.addAll(newUserList);

            // Notificar cambios de forma segura
            notifyDataSetChanged();
        } catch (Exception e) {
            android.util.Log.e("UserAdapter", "Error en updateFullList: " + e.getMessage());
            // Si hay error, inicializar listas vacías
            if (userList == null) userList = new ArrayList<>();
            if (userListFull == null) userListFull = new ArrayList<>();
            notifyDataSetChanged();
        }
    }

    public void filterByType(String userType) {
        List<User> filteredList = new ArrayList<>();
        
        if (userType.equals("Todos")) {
            filteredList.addAll(userListFull);
        } else {
            for (User user : userListFull) {
                if (user.getRol() != null && user.getRol().equals(userType)) {
                    filteredList.add(user);
                }
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void filterByText(String searchText) {
        List<User> filteredList = new ArrayList<>();
        
        if (searchText.isEmpty()) {
            filteredList.addAll(userListFull);
        } else {
            String searchTextLower = searchText.toLowerCase();
            for (User user : userListFull) {
                if ((user.getNombres() != null && user.getNombres().toLowerCase().contains(searchTextLower)) ||
                    (user.getApellidos() != null && user.getApellidos().toLowerCase().contains(searchTextLower)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTextLower))) {
                    filteredList.add(user);
                }
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void filterTaxistasByStatus(String status) {
        List<User> filteredList = new ArrayList<>();
        
        // Convertir el tipo de estado (string) a un valor booleano
        boolean estadoValue = "activo".equals(status);

        for (User user : userListFull) {
            if ("driver".equals(user.getRol())) { // Actualizado de "taxista" a "driver"
                if ("Todos".equals(status)) {
                    filteredList.add(user);
                } else if ("pendiente".equals(status) && !user.isEstado()) {
                    // Estado pendiente = false
                    filteredList.add(user);
                } else if ("activo".equals(status) && user.isEstado()) {
                    // Estado activo = true
                    filteredList.add(user);
                }
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardUser;
        ShapeableImageView imageViewUserAvatar;
        TextView textViewUserName, textViewUserRole, textViewUserStatus;
        SwitchCompat switchUserStatus;
        View btnVerifyTaxista; // Botón para verificar taxista

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUser = itemView.findViewById(R.id.cardUser);
            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserRole = itemView.findViewById(R.id.textViewUserRole);
            textViewUserStatus = itemView.findViewById(R.id.textViewUserStatus);
            switchUserStatus = itemView.findViewById(R.id.switchUserStatus);
            btnVerifyTaxista = itemView.findViewById(R.id.btnVerifyTaxista); // Inicializar botón
        }
    }
}
