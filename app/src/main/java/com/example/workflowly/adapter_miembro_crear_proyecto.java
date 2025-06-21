package com.example.workflowly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class adapter_miembro_crear_proyecto extends RecyclerView.Adapter<adapter_miembro_crear_proyecto.ViewHolder> {

    private List<miembro_crear_proyecto> listaMiembros;
    private OnRemoveClickListener removeClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }

    public adapter_miembro_crear_proyecto(List<miembro_crear_proyecto> listaMiembros, OnRemoveClickListener listener) {
        this.listaMiembros = listaMiembros;
        this.removeClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_miembro_registro_proyecto, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        miembro_crear_proyecto miembro = listaMiembros.get(position);
        holder.emailTextView.setText(miembro.getEmail());

        if (miembro.isMostrarEliminar()) {
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(v -> {
                if (removeClickListener != null) {
                    removeClickListener.onRemoveClick(holder.getAdapterPosition());
                }
            });
        } else {
            holder.removeButton.setVisibility(View.GONE); // Oculta el botón completamente
            holder.removeButton.setOnClickListener(null); // Evita referencias innecesarias
        }
    }


    @Override
    public int getItemCount() {
        return listaMiembros.size();
    }

    public List<miembro_crear_proyecto> getMiembros() {
        return this.listaMiembros;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;
        ImageButton removeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
            removeButton = itemView.findViewById(R.id.buttonRemove);
        }
    }
}
