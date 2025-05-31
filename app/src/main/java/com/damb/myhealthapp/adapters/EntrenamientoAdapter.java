package com.damb.myhealthapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.models.EjercicioSugerido;
import java.util.List;

public class EntrenamientoAdapter extends RecyclerView.Adapter<EntrenamientoAdapter.ViewHolder> {
    private List<EjercicioSugerido> ejercicios;
    private List<Boolean> completados;

    public EntrenamientoAdapter(List<EjercicioSugerido> ejercicios, List<Boolean> completados) {
        this.ejercicios = ejercicios;
        this.completados = completados;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ejercicio_check, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EjercicioSugerido ejercicio = ejercicios.get(position);
        holder.nombre.setText(ejercicio.getNombre());
        holder.series.setText(ejercicio.getSeriesReps());
        holder.imagen.setImageResource(ejercicio.getImagenResId());
        holder.checkBox.setChecked(completados.get(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> completados.set(position, isChecked));
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, series;
        ImageView imagen;
        CheckBox checkBox;
        ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombreEjercicioCheck);
            series = itemView.findViewById(R.id.seriesEjercicioCheck);
            imagen = itemView.findViewById(R.id.imagenEjercicioCheck);
            checkBox = itemView.findViewById(R.id.checkboxEjercicio);
        }
    }
} 