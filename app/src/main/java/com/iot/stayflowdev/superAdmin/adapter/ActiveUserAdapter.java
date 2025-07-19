package com.iot.stayflowdev.superAdmin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.model.User;
import java.util.List;

public class ActiveUserAdapter extends RecyclerView.Adapter<ActiveUserAdapter.ViewHolder> {

    private List<User> userList;

    public ActiveUserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Nombre completo
        String nombreCompleto = user.getNombres() + " " + user.getApellidos();
        holder.tvUserName.setText(nombreCompleto);

        // Rol del usuario
        String rolTexto = getRoleDisplayText(user.getRol());
        holder.tvUserRole.setText(rolTexto);

        // Tiempo conectado (calculado desde ultimaConexion)
        String tiempoConectado = calculateConnectionTime(user.getUltimaConexion());
        holder.tvConnectionTime.setText("Conectado hace " + tiempoConectado);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private String getRoleDisplayText(String rol) {
        if (rol == null) return "Usuario";

        switch (rol.toLowerCase()) {
            case "cliente":
            case "usuario":
                return "Cliente";
            case "driver":
                return "Driver";
            case "adminhotel":
                return "Admin Hotel";
            case "superadmin":
                return "Super Admin";
            default:
                return "Usuario";
        }
    }

    private String calculateConnectionTime(long ultimaConexion) {
        if (ultimaConexion == 0) return "tiempo desconocido";

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - ultimaConexion;

        long minutes = timeDiff / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " día" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " min";
        } else {
            return "ahora mismo";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvUserRole;
        TextView tvConnectionTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvConnectionTime = itemView.findViewById(R.id.tvConnectionTime);
        }
    }
}
