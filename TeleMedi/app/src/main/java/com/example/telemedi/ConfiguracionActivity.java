package com.example.telemedi;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ConfiguracionActivity extends AppCompatActivity {

    private static final String TAG = "ConfiguracionActivity";

    // Referencias a las vistas
    private EditText editTextNombreUsuario;
    private EditText editTextMensajeMotivacional;
    private Spinner spinnerFrecuenciaNotificaciones;
    private Button buttonGuardarConfiguracion;

    // Instancias de clases auxiliares
    private SharedPreferencesManager preferencesManager;
    private NotificationHelper notificationHelper;

    // Array con opciones de frecuencia de notificaciones
    private final String[] opcionesFrecuencia = {
            "Cada 1 hora",
            "Cada 2 horas",
            "Cada 3 horas",
            "Cada 4 horas",
            "Cada 6 horas",
            "Cada 8 horas",
            "Cada 12 horas",
            "Cada 24 horas (1 día)",
            "Cada 48 horas (2 días)"
    };

    // Valores correspondientes en horas
    private final int[] valoresFrecuencia = {1, 2, 3, 4, 6, 8, 12, 24, 48};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Inicializar clases auxiliares
        preferencesManager = new SharedPreferencesManager(this);
        notificationHelper = new NotificationHelper(this);

        // Configurar toolbar
        configurarToolbar();

        // Inicializar vistas
        inicializarVistas();

        // Configurar spinner
        configurarSpinner();

        // Cargar configuraciones guardadas
        cargarConfiguraciones();

        // Configurar listeners
        configurarListeners();

        Log.d(TAG, "ConfiguracionActivity creada");
    }

    /**
     * Configura el toolbar de la actividad
     */
    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Configuraciones");
        }

        // Manejar el botón de regreso
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * Inicializa todas las vistas de la actividad
     */
    private void inicializarVistas() {
        editTextNombreUsuario = findViewById(R.id.editTextNombreUsuario);
        editTextMensajeMotivacional = findViewById(R.id.editTextMensajeMotivacional);
        spinnerFrecuenciaNotificaciones = findViewById(R.id.spinnerFrecuenciaNotificaciones);
        buttonGuardarConfiguracion = findViewById(R.id.buttonGuardarConfiguracion);

        Log.d(TAG, "Vistas inicializadas");
    }

    /**
     * Configura el spinner de frecuencia de notificaciones
     */
    private void configurarSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcionesFrecuencia
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrecuenciaNotificaciones.setAdapter(adapter);

        Log.d(TAG, "Spinner configurado");
    }

    /**
     * Carga las configuraciones guardadas en SharedPreferences
     */
    private void cargarConfiguraciones() {
        try {
            // Cargar nombre de usuario
            String nombreUsuario = preferencesManager.obtenerNombreUsuario();
            editTextNombreUsuario.setText(nombreUsuario);

            // Cargar mensaje motivacional
            String mensajeMotivacional = preferencesManager.obtenerMensajeMotivacional();
            editTextMensajeMotivacional.setText(mensajeMotivacional);

            // Cargar frecuencia de notificaciones
            int frecuenciaActual = preferencesManager.obtenerFrecuenciaNotificaciones();
            int posicionSpinner = encontrarPosicionFrecuencia(frecuenciaActual);
            spinnerFrecuenciaNotificaciones.setSelection(posicionSpinner);

            Log.d(TAG, "Configuraciones cargadas - Usuario: " + nombreUsuario +
                    ", Frecuencia: " + frecuenciaActual + " horas");

        } catch (Exception e) {
            Log.e(TAG, "Error al cargar configuraciones", e);
            Toast.makeText(this, "Error al cargar configuraciones", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Encuentra la posición en el spinner para una frecuencia dada
     */
    private int encontrarPosicionFrecuencia(int frecuenciaHoras) {
        for (int i = 0; i < valoresFrecuencia.length; i++) {
            if (valoresFrecuencia[i] == frecuenciaHoras) {
                return i;
            }
        }
        // Si no se encuentra, retornar posición por defecto (12 horas)
        return 6; // Posición de "Cada 12 horas"
    }

    /**
     * Configura todos los listeners de la actividad
     */
    private void configurarListeners() {
        buttonGuardarConfiguracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarConfiguraciones();
            }
        });

        Log.d(TAG, "Listeners configurados");
    }

    /**
     * Guarda todas las configuraciones
     */
    private void guardarConfiguraciones() {
        try {
            // Obtener valores de los campos
            String nombreUsuario = editTextNombreUsuario.getText().toString().trim();
            String mensajeMotivacional = editTextMensajeMotivacional.getText().toString().trim();
            int posicionSpinner = spinnerFrecuenciaNotificaciones.getSelectedItemPosition();
            int frecuenciaHoras = valoresFrecuencia[posicionSpinner];

            // Validar campos
            if (!validarCampos(nombreUsuario, mensajeMotivacional)) {
                return;
            }

            // Mostrar progress (opcional - puedes implementar un ProgressBar)
            buttonGuardarConfiguracion.setEnabled(false);
            buttonGuardarConfiguracion.setText("Guardando...");

            // Guardar en SharedPreferences
            preferencesManager.guardarNombreUsuario(nombreUsuario);
            preferencesManager.guardarMensajeMotivacional(mensajeMotivacional);

            // Verificar si cambió la frecuencia de notificaciones
            int frecuenciaAnterior = preferencesManager.obtenerFrecuenciaNotificaciones();
            if (frecuenciaAnterior != frecuenciaHoras) {
                preferencesManager.guardarFrecuenciaNotificaciones(frecuenciaHoras);

                // Reprogramar notificaciones motivacionales con nueva frecuencia
                notificationHelper.programarNotificacionesMotivacionales(frecuenciaHoras, mensajeMotivacional);

                Log.d(TAG, "Frecuencia de notificaciones cambiada de " + frecuenciaAnterior +
                        " a " + frecuenciaHoras + " horas");
            }

            // Mostrar mensaje de éxito
            Toast.makeText(this, "Configuraciones guardadas exitosamente", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Configuraciones guardadas - Usuario: " + nombreUsuario +
                    ", Frecuencia: " + frecuenciaHoras + " horas");

            // Cerrar actividad después de un breve delay
            buttonGuardarConfiguracion.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);

        } catch (Exception e) {
            Log.e(TAG, "Error al guardar configuraciones", e);
            Toast.makeText(this, "Error al guardar configuraciones", Toast.LENGTH_SHORT).show();
        } finally {
            // Restaurar estado del botón
            buttonGuardarConfiguracion.setEnabled(true);
            buttonGuardarConfiguracion.setText("Guardar Configuración");
        }
    }

    /**
     * Valida los campos del formulario
     */
    private boolean validarCampos(String nombreUsuario, String mensajeMotivacional) {
        // Validar nombre de usuario
        if (TextUtils.isEmpty(nombreUsuario)) {
            editTextNombreUsuario.setError("El nombre de usuario es requerido");
            editTextNombreUsuario.requestFocus();
            return false;
        }

        if (nombreUsuario.length() < 2) {
            editTextNombreUsuario.setError("El nombre debe tener al menos 2 caracteres");
            editTextNombreUsuario.requestFocus();
            return false;
        }

        if (nombreUsuario.length() > 50) {
            editTextNombreUsuario.setError("El nombre no puede exceder 50 caracteres");
            editTextNombreUsuario.requestFocus();
            return false;
        }

        // Validar mensaje motivacional
        if (TextUtils.isEmpty(mensajeMotivacional)) {
            editTextMensajeMotivacional.setError("El mensaje motivacional es requerido");
            editTextMensajeMotivacional.requestFocus();
            return false;
        }

        if (mensajeMotivacional.length() < 10) {
            editTextMensajeMotivacional.setError("El mensaje debe tener al menos 10 caracteres");
            editTextMensajeMotivacional.requestFocus();
            return false;
        }

        if (mensajeMotivacional.length() > 200) {
            editTextMensajeMotivacional.setError("El mensaje no puede exceder 200 caracteres");
            editTextMensajeMotivacional.requestFocus();
            return false;
        }

        // Limpiar errores si todo está correcto
        editTextNombreUsuario.setError(null);
        editTextMensajeMotivacional.setError(null);

        return true;
    }

    /**
     * Verifica permisos de notificación al reanudar la actividad
     */
    @Override
    protected void onResume() {
        super.onResume();
        verificarPermisos();
    }

    /**
     * Verifica que los permisos necesarios estén habilitados
     */
    private void verificarPermisos() {
        if (!notificationHelper.tienePermisosNotificacion()) {
            Toast.makeText(this,
                    "Las notificaciones están deshabilitadas. Habilítalas en Configuración para recibir recordatorios.",
                    Toast.LENGTH_LONG).show();
        }

        if (!notificationHelper.tienePermisosAlarmasExactas()) {
            Toast.makeText(this,
                    "Los permisos de alarmas exactas están deshabilitados. Esto puede afectar la precisión de las notificaciones.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Maneja el botón de regreso del sistema
     */
    @Override
    public void onBackPressed() {
        // Verificar si hay cambios sin guardar
        if (hayCambiosSinGuardar()) {
            Toast.makeText(this, "Tienes cambios sin guardar", Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
    }

    /**
     * Verifica si hay cambios sin guardar
     */
    private boolean hayCambiosSinGuardar() {
        try {
            String nombreActual = editTextNombreUsuario.getText().toString().trim();
            String mensajeActual = editTextMensajeMotivacional.getText().toString().trim();
            int frecuenciaActual = valoresFrecuencia[spinnerFrecuenciaNotificaciones.getSelectedItemPosition()];

            String nombreGuardado = preferencesManager.obtenerNombreUsuario();
            String mensajeGuardado = preferencesManager.obtenerMensajeMotivacional();
            int frecuenciaGuardada = preferencesManager.obtenerFrecuenciaNotificaciones();

            return !nombreActual.equals(nombreGuardado) ||
                    !mensajeActual.equals(mensajeGuardado) ||
                    frecuenciaActual != frecuenciaGuardada;

        } catch (Exception e) {
            Log.e(TAG, "Error verificando cambios", e);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ConfiguracionActivity destruida");
    }
}