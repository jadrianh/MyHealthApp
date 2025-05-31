package com.damb.myhealthapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.DetalleEjercicioActivity;
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
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        String nombre = tiposEjercicio.get(position);
        holder.nombreEjercicio.setText(nombre);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.damb.myhealthapp.DetalleEjercicioActivity.class);
            intent.putExtra("nombre_ejercicio", nombre);
            if (!(context instanceof android.app.Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tiposEjercicio.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreEjercicio;

        EjercicioViewHolder(View itemView) {
            super(itemView);
            nombreEjercicio = itemView.findViewById(R.id.nombreEjercicioViewPager);
        }
    }
    
    // TODO: Add a helper method to get image resource based on exercise name
    // private int getImageResource(String exerciseName) {
    //     switch (exerciseName) {
    //         case "Pérdida de Peso (Quema de Grasa)":
    //             return R.drawable.ic_weight_loss; // replace with actual drawable
    //         case "Ganancia de Masa Muscular (Hipertrofia)":
    //             return R.drawable.ic_muscle_gain; // replace with actual drawable
    //         // ... more cases
    //         default:
    //             return R.drawable.ic_default_exercise; // replace with a default drawable
    //     }
    // }
} 