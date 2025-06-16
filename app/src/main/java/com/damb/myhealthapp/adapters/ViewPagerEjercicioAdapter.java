package com.damb.myhealthapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.views.TrainingPlanDetailsActivity;
import com.damb.myhealthapp.R;
import java.util.List;

public class ViewPagerEjercicioAdapter extends RecyclerView.Adapter<ViewPagerEjercicioAdapter.EjercicioViewHolder> {

    private List<String> tiposEjercicio;
    private Context context;

    public ViewPagerEjercicioAdapter(Context context, List<String> tiposEjercicio) {
        this.context = context;
        this.tiposEjercicio = tiposEjercicio;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_viewpager_ejercicio, parent, false);
        return new EjercicioViewHolder(view, context, tiposEjercicio); // Pasa el contexto y la lista
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        String nombre = tiposEjercicio.get(position);
        holder.nombreEjercicio.setText(nombre);

        int imageResId = getImageResource(nombre);
        holder.imagenEjercicio.setImageResource(imageResId);

        // Si tienes una ImageView en tu layout item_viewpager_ejercicio.xml,
        // aquí es donde cargarías la imagen.
        // Ejemplo usando Glide (si lo has añadido a tus dependencias):
        // int imageResId = getImageResource(nombre);
        // if (holder.imagenEjercicio != null && imageResId != 0) {
        //     Glide.with(holder.itemView.getContext())
        //          .load(imageResId)
        //          .centerCrop() // O scaleType que necesites
        //          .placeholder(R.drawable.ic_loading) // Opcional: drawable mientras carga
        //          .error(R.drawable.ic_error) // Opcional: drawable si hay error
        //          .into(holder.imagenEjercicio);
        // } else if (holder.imagenEjercicio != null) {
        //     holder.imagenEjercicio.setImageResource(R.drawable.ic_default_exercise); // Un default si no hay imagen específica
        // }

        // El OnClickListener ya está configurado en el ViewHolder y usa getAdapterPosition()
    }

    @Override
    public int getItemCount() {
        return tiposEjercicio.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nombreEjercicio;
        ImageView imagenEjercicio;

        private Context context;
        private List<String> tiposEjercicio;

        EjercicioViewHolder(View itemView, Context context, List<String> tiposEjercicio) {
            super(itemView);
            this.context = context;
            this.tiposEjercicio = tiposEjercicio;
            nombreEjercicio = itemView.findViewById(R.id.nombreEjercicioViewPager);
            imagenEjercicio = itemView.findViewById(R.id.imagenEjercicio);
            // imagenEjercicio = itemView.findViewById(R.id.imagenEjercicio); // Descomentar si añades un ImageView

            itemView.setOnClickListener(this); // Establecer el listener una sola vez
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition(); // Obtener la posición del elemento cliqueado
            if (position != RecyclerView.NO_POSITION) {
                String nombre = tiposEjercicio.get(position);
                Intent intent = new Intent(context, TrainingPlanDetailsActivity.class);
                intent.putExtra("nombre_ejercicio", nombre);
                if (!(context instanceof android.app.Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            }
        }
    }

    private int getImageResource(String nombreEjercicio) {
        if (nombreEjercicio.equalsIgnoreCase("Pérdida de Peso (Quema de Grasa)")) {
            return R.drawable.perdida_de_grasa;
        } else if (nombreEjercicio.equalsIgnoreCase("Ganancia de Masa Muscular (Hipertrofia)")) {
            return R.drawable.ganancia_de_masa_muscular;
        } else if (nombreEjercicio.equalsIgnoreCase("Flexibilidad y Movilidad")) {
            return R.drawable.flexibilidad_y_movilidad;
        } else if (nombreEjercicio.equalsIgnoreCase("Salud General y Bienestar")) {
            return R.drawable.salud_general_y_bienestar;
        } else if (nombreEjercicio.equalsIgnoreCase("Resistencia y Cardio")) {
            return R.drawable.resistencia_y_cardio;
        } else if (nombreEjercicio.equalsIgnoreCase("Entrenamiento Funcional")) {
            return R.drawable.entrenamiento_funcional;
        } else if (nombreEjercicio.equalsIgnoreCase("Plan Rápido para Tonificación")) {
            return R.drawable.plan_rapido_para_tonificacion;
        } else if (nombreEjercicio.equalsIgnoreCase("Plan para Principiantes")) {
            return R.drawable.plan_para_principiantes;
        } else {
            return R.drawable.ic_face_female;
        }
    }

}