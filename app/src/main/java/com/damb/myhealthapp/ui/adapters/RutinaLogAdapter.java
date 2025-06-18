package com.damb.myhealthapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.R;
import java.util.List;

public class RutinaLogAdapter extends RecyclerView.Adapter<RutinaLogAdapter.RutinaViewHolder> {
    public interface OnRutinaClickListener {
        void onRutinaClick(String rutinaId, String nombreRutina);
    }

    private List<RutinaLogItem> rutinas;
    private OnRutinaClickListener listener;

    public RutinaLogAdapter(List<RutinaLogItem> rutinas, OnRutinaClickListener listener) {
        this.rutinas = rutinas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina_log, parent, false);
        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        RutinaLogItem rutina = rutinas.get(position);
        holder.tvNombreRutina.setText(rutina.nombreRutina);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRutinaClick(rutina.rutinaId, rutina.nombreRutina);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rutinas.size();
    }

    public static class RutinaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreRutina;
        ImageView ivDetalle;
        public RutinaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreRutina = itemView.findViewById(R.id.tvNombreRutina);
            ivDetalle = itemView.findViewById(R.id.ivDetalle);
        }
    }

    // Clase para representar cada rutina
    public static class RutinaLogItem {
        public String rutinaId;
        public String nombreRutina;
        public RutinaLogItem(String rutinaId, String nombreRutina) {
            this.rutinaId = rutinaId;
            this.nombreRutina = nombreRutina;
        }
    }
} 