package com.damb.myhealthapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.models.PlanEntrenamiento;
import java.util.List;

public class PlanesEntrenamientoAdapter extends RecyclerView.Adapter<PlanesEntrenamientoAdapter.PlanViewHolder> {
    private List<PlanEntrenamiento> planes;

    public PlanesEntrenamientoAdapter(List<PlanEntrenamiento> planes) {
        this.planes = planes;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_entrenamiento, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanEntrenamiento plan = planes.get(position);
        holder.tituloPlan.setText(plan.getTitulo());
        holder.duracionPlan.setText(plan.getDuracion());
        holder.modalidadPlan.setText(plan.getModalidad());
        holder.frecuenciaPlan.setText(plan.getFrecuencia());
        holder.ejerciciosPlan.setText(plan.getEjercicios());
        holder.idealParaPlan.setText(plan.getIdealPara());
    }

    @Override
    public int getItemCount() {
        return planes.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tituloPlan;
        TextView duracionPlan;
        TextView modalidadPlan;
        TextView frecuenciaPlan;
        TextView ejerciciosPlan;
        TextView idealParaPlan;

        PlanViewHolder(View itemView) {
            super(itemView);
            tituloPlan = itemView.findViewById(R.id.tituloPlan);
            duracionPlan = itemView.findViewById(R.id.duracionPlan);
            modalidadPlan = itemView.findViewById(R.id.modalidadPlan);
            frecuenciaPlan = itemView.findViewById(R.id.frecuenciaPlan);
            ejerciciosPlan = itemView.findViewById(R.id.ejerciciosPlan);
            idealParaPlan = itemView.findViewById(R.id.idealParaPlan);
        }
    }
} 