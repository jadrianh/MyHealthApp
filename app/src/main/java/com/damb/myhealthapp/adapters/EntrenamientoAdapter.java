package com.damb.myhealthapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Importamos Glide
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.models.EjercicioSugerido;
import java.util.List;
import java.util.Locale;

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

        // --- CORRECCIÓN 1: Construir el texto de series/reps ---
        // Usamos los nuevos métodos para crear la descripción.
        String textoPrincipal = String.format(Locale.getDefault(), "%s series de %d reps",
                ejercicio.getSeries(),
                ejercicio.getRepeticiones());

        String detalleAdicional = ejercicio.getDetalle();
        if (detalleAdicional != null && !detalleAdicional.isEmpty()) {
            textoPrincipal += " (" + detalleAdicional + ")";
        }

        // Asignamos el texto construido al TextView.
        holder.series.setText(textoPrincipal);

        // --- CORRECCIÓN 2: Cargar la imagen local con Glide ---
        // Usamos Glide para cargar el recurso de imagen local.
        Glide.with(holder.itemView.getContext())
                .load(ejercicio.getImagenResId())
                .placeholder(R.drawable.exercise_placeholder) // Muestra esto mientras carga
                .into(holder.imagen);

        // La lógica del CheckBox se mantiene igual.
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
