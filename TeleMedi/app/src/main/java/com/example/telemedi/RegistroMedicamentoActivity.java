package com.example.telemedi;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistroMedicamentoActivity extends AppCompatActivity {

    private EditText etNombreMedicamento;
    private Spinner spinnerTipo;
    private EditText etDosis;
    private NumberPicker npFrecuencia;
    private TextView tvFechaSeleccionada;
    private TextView tvHoraSeleccionada;
    private Button btnSeleccionarFecha;
    private Button btnSeleccionarHora;
    private Button btnGuardar;

    private Calendar fechaHoraInicio;
    private SharedPreferencesManager prefsManager;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_medicamento);

        initComponents();
        setupSpinner();
        setupNumberPicker();
        setupClickListeners();
        initManagers();
    }

    private void initComponents() {
        etNombreMedicamento = findViewById(R.id.etNombreMedicamento);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        etDosis = findViewById(R.id.etDosis);
        npFrecuencia = findViewById(R.id.npFrecuencia);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        tvHoraSeleccionada = findViewById(R.id.tvHoraSeleccionada);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnSeleccionarHora = findViewById(R.id.btnSeleccionarHora);
        btnGuardar = findViewById(R.id.btnGuardar);

        fechaHoraInicio = Calendar.getInstance();
    }

    private void setupSpinner() {
        String[] tiposMedicamento = {"Pastilla", "Jarabe", "Ampolla", "Cápsula"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tiposMedicamento
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);
    }

    private void setupNumberPicker() {
        npFrecuencia.setMinValue(1);
        npFrecuencia.setMaxValue(24);
        npFrecuencia.setValue(8); // Default: cada 8 horas
        npFrecuencia.setWrapSelectorWheel(false);
    }

    private void setupClickListeners() {
        btnSeleccionarFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePicker();
            }
        });

        btnSeleccionarHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarTimePicker();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarMedicamento();
            }
        });
    }

    private void initManagers() {
        prefsManager = new SharedPreferencesManager(this);
        notificationHelper = new NotificationHelper(this);
    }

    private void mostrarDatePicker() {
        Calendar calendario = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fechaHoraInicio.set(Calendar.YEAR, year);
                        fechaHoraInicio.set(Calendar.MONTH, month);
                        fechaHoraInicio.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        actualizarFechaTexto();
                    }
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        // No permitir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        Calendar calendario = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        fechaHoraInicio.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        fechaHoraInicio.set(Calendar.MINUTE, minute);
                        fechaHoraInicio.set(Calendar.SECOND, 0);
                        actualizarHoraTexto();
                    }
                },
                calendario.get(Calendar.HOUR_OF_DAY),
                calendario.get(Calendar.MINUTE),
                true // Formato 24 horas
        );

        timePickerDialog.show();
    }

    private void actualizarFechaTexto() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvFechaSeleccionada.setText("Fecha: " + sdf.format(fechaHoraInicio.getTime()));
    }

    private void actualizarHoraTexto() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvHoraSeleccionada.setText("Hora: " + sdf.format(fechaHoraInicio.getTime()));
    }

    private void guardarMedicamento() {
        if (!validarDatos()) {
            return;
        }

        // Crear objeto medicamento
        Medicamento medicamento = new Medicamento(
                etNombreMedicamento.getText().toString().trim(),
                spinnerTipo.getSelectedItem().toString(),
                etDosis.getText().toString().trim(),
                npFrecuencia.getValue(),
                fechaHoraInicio.getTimeInMillis()
        );

        // Guardar en SharedPreferences
        boolean guardado = prefsManager.guardarMedicamento(medicamento);

        if (guardado) {
            // Programar notificaciones
            notificationHelper.programarNotificacionesMedicamento(medicamento);

            Toast.makeText(this, "Medicamento guardado exitosamente", Toast.LENGTH_SHORT).show();
            finish(); // Regresar a la actividad anterior
        } else {
            Toast.makeText(this, "Error al guardar medicamento", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarDatos() {
        // Validar nombre
        if (TextUtils.isEmpty(etNombreMedicamento.getText().toString().trim())) {
            etNombreMedicamento.setError("Ingrese el nombre del medicamento");
            etNombreMedicamento.requestFocus();
            return false;
        }

        // Validar dosis
        if (TextUtils.isEmpty(etDosis.getText().toString().trim())) {
            etDosis.setError("Ingrese la dosis");
            etDosis.requestFocus();
            return false;
        }

        // Validar que se haya seleccionado fecha
        if (tvFechaSeleccionada.getText().toString().equals("Fecha: No seleccionada")) {
            Toast.makeText(this, "Seleccione una fecha de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que se haya seleccionado hora
        if (tvHoraSeleccionada.getText().toString().equals("Hora: No seleccionada")) {
            Toast.makeText(this, "Seleccione una hora de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que la fecha/hora no sea en el pasado
        if (fechaHoraInicio.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "La fecha y hora deben ser futuras", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Inicializar textos si es necesario
        if (tvFechaSeleccionada.getText().toString().isEmpty()) {
            tvFechaSeleccionada.setText("Fecha: No seleccionada");
        }
        if (tvHoraSeleccionada.getText().toString().isEmpty()) {
            tvHoraSeleccionada.setText("Hora: No seleccionada");
        }
    }
}