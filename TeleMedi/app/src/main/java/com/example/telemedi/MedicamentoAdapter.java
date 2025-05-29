package com.example.telemedi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Locale;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> medicamentos;
    private OnMedicamentoClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnMedicamentoClickListener {
        void onEliminarClick(int position);
    }

    public MedicamentoAdapter(List<Medicamento> medicamentos, OnMedicamentoClickListener listener) {
        this.medicamentos = medicamentos;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = medicamentos.get(position);
        holder.bind(medicamento, position);
    }

    @Override
    public int getItemCount() {
        return medicamentos.size();
    }

    public class MedicamentoViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewNombre;
        private TextView textViewTipo;
        private TextView textViewDosis;
        private TextView textViewFrecuencia;
        private TextView textViewFechaInicio;
        private Button buttonEliminar;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewTipo = itemView.findViewById(R.id.textViewTipo);
            textViewDosis = itemView.findViewById(R.id.textViewDosis);
            textViewFrecuencia = itemView.findViewById(R.id.textViewFrecuencia);
            textViewFechaInicio = itemView.findViewById(R.id.textViewFechaInicio);
            buttonEliminar = itemView.findViewById(R.id.buttonEliminar);
        }

        public void bind(Medicamento medicamento, int position) {
            textViewNombre.setText(medicamento.getNombre());
            textViewTipo.setText("Tipo: " + medicamento.getTipo());
            textViewDosis.setText("Dosis: " + medicamento.getDosis());

            String frecuenciaTexto = "Cada " + medicamento.getFrecuenciaHoras() + " horas";
            textViewFrecuencia.setText(frecuenciaTexto);

            String fechaTexto = "Inicio: " + dateFormat.format(new Date(medicamento.getFechaInicioMillis()));
            textViewFechaInicio.setText(fechaTexto);

            buttonEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarClick(position);
                }
            });
        }
    }
}