package com.damb.myhealthapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.views.TrainingPlanDetailsActivity;
import java.util.List;

public class SliderEjerciciosAdapter extends RecyclerView.Adapter<SliderEjerciciosAdapter.EjercicioViewHolder> {
    private List<String> tiposEjercicio;
    private Context context;

    public SliderEjerciciosAdapter(Context context, List<String> tiposEjercicio) {
        this.context = context;
        this.tiposEjercicio = tiposEjercicio;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider_ejercicio, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        String nombre = tiposEjercicio.get(position);
        holder.nombreEjercicio.setText(nombre);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TrainingPlanDetailsActivity.class);
            intent.putExtra("nombre_ejercicio", nombre);
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
            nombreEjercicio = itemView.findViewById(R.id.nombreEjercicio);
        }
    }
} 