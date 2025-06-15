package com.example.workflowly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class adapter_miembro_crear_tarea extends RecyclerView.Adapter<adapter_miembro_crear_tarea.ViewHolder> {

    private List<miembro_crear_tarea> listaMiembrosAgregados;
    private OnRemoveClickListener removeClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public adapter_miembro_crear_tarea(List<miembro_crear_tarea> listaMiembrosAgregados, OnRemoveClickListener listener) {
        this.listaMiembrosAgregados = listaMiembrosAgregados;
        this.removeClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_miembro_registro_tarea, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        miembro_crear_tarea miembro = listaMiembrosAgregados.get(position);
        holder.usernameTextView.setText(miembro.getUsername());

        holder.removeButton.setOnClickListener(v -> {
            if (removeClickListener != null) {
                removeClickListener.onRemoveClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMiembrosAgregados.size();
    }

    public List<miembro_crear_tarea> getMiembros() {
        return this.listaMiembrosAgregados;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        ImageButton removeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textViewUsername);
            removeButton = itemView.findViewById(R.id.buttonRemove);
        }
    }
}
