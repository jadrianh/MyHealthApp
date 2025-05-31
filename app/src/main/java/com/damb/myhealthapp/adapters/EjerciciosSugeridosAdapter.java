package com.damb.myhealthapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.models.EjercicioSugerido;
import java.util.List;

public class EjerciciosSugeridosAdapter extends RecyclerView.Adapter<EjerciciosSugeridosAdapter.EjercicioViewHolder> {
    private List<EjercicioSugerido> ejercicios;

    public EjerciciosSugeridosAdapter(List<EjercicioSugerido> ejercicios) {
        this.ejercicios = ejercicios;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio_sugerido, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        EjercicioSugerido ejercicio = ejercicios.get(position);
        holder.nombre.setText(ejercicio.getNombre());
        holder.seriesReps.setText(ejercicio.getSeriesReps());
        holder.imagen.setImageResource(ejercicio.getImagenResId());
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre;
        TextView seriesReps;

        EjercicioViewHolder(View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagenEjercicio);
            nombre = itemView.findViewById(R.id.nombreEjercicio);
            seriesReps = itemView.findViewById(R.id.seriesRepsEjercicio);
        }
    }
} 