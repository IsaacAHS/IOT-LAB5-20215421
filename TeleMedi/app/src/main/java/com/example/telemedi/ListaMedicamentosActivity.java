package com.example.telemedi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ListaMedicamentosActivity extends AppCompatActivity implements MedicamentoAdapter.OnMedicamentoClickListener {

    private RecyclerView recyclerViewMedicamentos;
    private FloatingActionButton fabAgregarMedicamento;
    private MedicamentoAdapter medicamentoAdapter;
    private List<Medicamento> listaMedicamentos;
    private SharedPreferencesManager preferencesManager;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_medicamentos);

        initViews();
        initPreferences();
        setupRecyclerView();
        setupFloatingActionButton();
        cargarMedicamentos();
    }

    private void initViews() {
        recyclerViewMedicamentos = findViewById(R.id.recyclerViewMedicamentos);
        fabAgregarMedicamento = findViewById(R.id.fabAgregarMedicamento);
    }

    private void initPreferences() {
        preferencesManager = new SharedPreferencesManager(this);
        notificationHelper = new NotificationHelper(this);
    }

    private void setupRecyclerView() {
        listaMedicamentos = new ArrayList<>();
        medicamentoAdapter = new MedicamentoAdapter(listaMedicamentos, this);
        recyclerViewMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMedicamentos.setAdapter(medicamentoAdapter);
    }

    private void setupFloatingActionButton() {
        fabAgregarMedicamento.setOnClickListener(v -> {
            Intent intent = new Intent(ListaMedicamentosActivity.this, RegistroMedicamentoActivity.class);
            startActivity(intent);
        });
    }

    private void cargarMedicamentos() {
        listaMedicamentos.clear();
        List<Medicamento> medicamentos = preferencesManager.obtenerMedicamentos();
        if (medicamentos != null && !medicamentos.isEmpty()) {
            listaMedicamentos.addAll(medicamentos);
            medicamentoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar medicamentos cuando regresamos de agregar uno nuevo
        cargarMedicamentos();
    }

    @Override
    public void onEliminarClick(int position) {
        if (position >= 0 && position < listaMedicamentos.size()) {
            Medicamento medicamento = listaMedicamentos.get(position);
            mostrarDialogoConfirmacion(medicamento, position);
        }
    }

    private void mostrarDialogoConfirmacion(Medicamento medicamento, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Medicamento")
                .setMessage("¿Estás seguro de que deseas eliminar " + medicamento.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarMedicamento(medicamento, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarMedicamento(Medicamento medicamento, int position) {
        // Cancelar notificaciones programadas para este medicamento
        notificationHelper.cancelarNotificacionesMedicamento(medicamento.getId()); // ← Aquí está el cambio

        // Eliminar usando el ID del medicamento
        boolean eliminado = preferencesManager.eliminarMedicamento(medicamento.getId());

        if (eliminado) {
            // Eliminar de la lista visual
            listaMedicamentos.remove(position);
            medicamentoAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Medicamento eliminado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al eliminar medicamento", Toast.LENGTH_SHORT).show();
        }
    }
}