package com.damb.myhealthapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.models.SuggestedExcercise;
import java.util.List;
import java.util.Locale;

public class SuggestedExercisesAdapter extends RecyclerView.Adapter<SuggestedExercisesAdapter.EjercicioViewHolder> {
    private List<SuggestedExcercise> ejercicios;

    public SuggestedExercisesAdapter(List<SuggestedExcercise> ejercicios) {
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
        SuggestedExcercise ejercicio = ejercicios.get(position);
        holder.nombre.setText(ejercicio.getNombre());

        String textoPrincipal = String.format(Locale.getDefault(), "%s series de %d reps",
                ejercicio.getSeries(),
                ejercicio.getRepeticiones());

        if (ejercicio.getDetalle() != null && !ejercicio.getDetalle().isEmpty()) {
            textoPrincipal += " (" + ejercicio.getDetalle() + ")";
        }
        holder.seriesReps.setText(textoPrincipal);

        // Usamos Glide para cargar la imagen local (puede ser PNG, JPG o GIF)
        Glide.with(holder.itemView.getContext())
                .load(ejercicio.getImagenResId())
                .placeholder(R.drawable.exercise_placeholder)
                .into(holder.imagen);
    }

    @Override
    public int getItemCount() { return ejercicios.size(); }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre, seriesReps;
        EjercicioViewHolder(View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagenEjercicio);
            nombre = itemView.findViewById(R.id.nombreEjercicio);
            seriesReps = itemView.findViewById(R.id.seriesRepsEjercicio);
        }
    }
}