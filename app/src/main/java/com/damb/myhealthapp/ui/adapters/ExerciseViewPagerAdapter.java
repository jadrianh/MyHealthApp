package com.damb.myhealthapp.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.ui.views.TrainingPlanDetailsActivity;
import com.damb.myhealthapp.R;
import java.util.List;

public class ExerciseViewPagerAdapter extends RecyclerView.Adapter<ExerciseViewPagerAdapter.EjercicioViewHolder> {

    // Cambiado de List<String> a List<EjercicioData>
    private List<EjercicioData> ejercicios;
    private Context context;

    // Constructor modificado para aceptar List<EjercicioData>
    public ExerciseViewPagerAdapter(Context context, List<EjercicioData> ejercicios) {
        this.context = context;
        this.ejercicios = ejercicios;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_viewpager_ejercicio, parent, false);
        // Pasa el contexto y la lista de objetos de datos al ViewHolder
        return new EjercicioViewHolder(view, context, ejercicios);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        // Obtener el objeto EjercicioData para la posición actual
        EjercicioData ejercicio = ejercicios.get(position);

        // Asignar los datos a los TextViews
        holder.nombreEjercicio.setText(ejercicio.getNombre());
        holder.subtituloEjercicio.setText(ejercicio.getTipoEntrenamiento()); // Usa el nuevo campo
        holder.tiempoEjercicio.setText(ejercicio.getTiempo());               // Usa el nuevo campo
        holder.caloriasEjercicio.setText(ejercicio.getCalorias());           // Usa el nuevo campo

        // Cargar la imagen
        int imageResId = getImageResource(ejercicio.getNombre());
        holder.imagenEjercicio.setImageResource(imageResId);
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    // El ViewHolder ahora contendrá los nuevos TextViews
    static class EjercicioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nombreEjercicio;
        TextView subtituloEjercicio; // Nuevo
        TextView tiempoEjercicio;    // Nuevo
        TextView caloriasEjercicio;  // Nuevo
        ImageView imagenEjercicio;

        private Context context;
        // La lista de objetos de datos se pasa al ViewHolder
        private List<EjercicioData> ejercicios;

        // Constructor modificado para inicializar los nuevos TextViews
        EjercicioViewHolder(View itemView, Context context, List<EjercicioData> ejercicios) {
            super(itemView);
            this.context = context;
            this.ejercicios = ejercicios; // Almacena la lista
            nombreEjercicio = itemView.findViewById(R.id.nombreEjercicioViewPager);
            subtituloEjercicio = itemView.findViewById(R.id.subtituloEjercicio); // Inicializar nuevo TextView
            tiempoEjercicio = itemView.findViewById(R.id.tiempoEjercicio);       // Inicializar nuevo TextView
            caloriasEjercicio = itemView.findViewById(R.id.caloriasEjercicio);   // Inicializar nuevo TextView
            imagenEjercicio = itemView.findViewById(R.id.imagenEjercicio);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Obtener el objeto EjercicioData para la posición cliqueada
                EjercicioData ejercicioSeleccionado = ejercicios.get(position);
                Intent intent = new Intent(context, TrainingPlanDetailsActivity.class);
                // Pasa el nombre del ejercicio u otros datos relevantes
                intent.putExtra("nombre_ejercicio", ejercicioSeleccionado.getNombre());
                // Si TrainingPlanDetailsActivity necesita más datos, añádelos aquí:
                // intent.putExtra("subtitulo_ejercicio", ejercicioSeleccionado.getTipoEntrenamiento());
                // intent.putExtra("tiempo_ejercicio", ejercicioSeleccionado.getTiempo());
                // intent.putExtra("calorias_ejercicio", ejercicioSeleccionado.getCalorias());

                if (!(context instanceof android.app.Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            }
        }
    }

    // Esta función mapea el nombre del ejercicio a su recurso de imagen
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
            return R.drawable.ic_face_female; // Recurso de imagen por defecto
        }
    }

    // *** CLASE DE DATOS PARA EL EJERCICIO (Importante: define esta clase) ***
    // Puedes definirla aquí como una clase interna estática o en un archivo EjercicioData.java separado.
    // Para evitar más problemas de rutas, la mantenemos aquí como interna estática.
    public static class EjercicioData {
        private String nombre;
        private String tipoEntrenamiento; // Nuevo campo para el subtítulo
        private String tiempo;            // Nuevo campo para la duración
        private String calorias;          // Nuevo campo para las calorías

        public EjercicioData(String nombre, String tipoEntrenamiento, String tiempo, String calorias) {
            this.nombre = nombre;
            this.tipoEntrenamiento = tipoEntrenamiento;
            this.tiempo = tiempo;
            this.calorias = calorias;
        }

        public String getNombre() {
            return nombre;
        }

        public String getTipoEntrenamiento() {
            return tipoEntrenamiento;
        }

        public String getTiempo() {
            return tiempo;
        }

        public String getCalorias() {
            return calorias;
        }
    }
}