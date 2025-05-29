package com.example.telemedi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_GALLERY = 1001;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1002;
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1003;

    // Referencias a las vistas
    private TextView textViewSaludo;
    private TextView textViewMensajeMotivacional;
    private TextView textViewEstadisticas;
    private ImageView imageViewFotoPerfil;
    private Button buttonVerMedicamentos;
    private Button buttonConfiguraciones;

    // Instancias de clases auxiliares
    private SharedPreferencesManager preferencesManager;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar clases auxiliares
        preferencesManager = new SharedPreferencesManager(this);
        notificationHelper = new NotificationHelper(this);

        // Inicializar vistas
        inicializarVistas();

        // Configurar listeners
        configurarListeners();

        // Verificar permisos y configurar notificaciones
        verificarYSolicitarPermisos();

        Log.d(TAG, "MainActivity creada");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Actualizar contenido dinámico cada vez que se resume la actividad
        actualizarContenidoDinamico();

        // Programar notificaciones de medicamentos si hay medicamentos registrados
        programarNotificacionesMedicamentos();
    }

    /**
     * Inicializa todas las vistas de la actividad
     */
    private void inicializarVistas() {
        textViewSaludo = findViewById(R.id.textViewSaludo);
        textViewMensajeMotivacional = findViewById(R.id.textViewMensajeMotivacional);
        textViewEstadisticas = findViewById(R.id.textViewEstadisticas);
        imageViewFotoPerfil = findViewById(R.id.imageViewFotoPerfil);
        buttonVerMedicamentos = findViewById(R.id.buttonVerMedicamentos);
        buttonConfiguraciones = findViewById(R.id.buttonConfiguraciones);

        Log.d(TAG, "Vistas inicializadas");
    }

    /**
     * Configura todos los listeners de la actividad
     */
    private void configurarListeners() {
        // Listener para ver medicamentos
        buttonVerMedicamentos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirListaMedicamentos();
            }
        });

        // Listener para configuraciones
        buttonConfiguraciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirConfiguraciones();
            }
        });

        // Listener para cambiar foto de perfil
        imageViewFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarFotoPerfil();
            }
        });

        Log.d(TAG, "Listeners configurados");
    }

    /**
     * Actualiza el contenido dinámico de la pantalla principal
     */
    private void actualizarContenidoDinamico() {
        try {
            // Actualizar saludo dinámico
            actualizarSaludo();

            // Actualizar mensaje motivacional
            actualizarMensajeMotivacional();

            // Actualizar estadísticas
            actualizarEstadisticas();

            // Cargar foto de perfil
            cargarFotoPerfil();

            Log.d(TAG, "Contenido dinámico actualizado");

        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar contenido dinámico", e);
        }
    }

    /**
     * Actualiza el saludo dinámico basado en la hora del día
     */
    private void actualizarSaludo() {
        String nombreUsuario = preferencesManager.obtenerNombreUsuario();
        String saludo = obtenerSaludoSegunHora();

        String saludoCompleto = saludo + ", " + nombreUsuario + "!";
        textViewSaludo.setText(saludoCompleto);

        Log.d(TAG, "Saludo actualizado: " + saludoCompleto);
    }

    /**
     * Obtiene el saludo apropiado según la hora del día
     */
    private String obtenerSaludoSegunHora() {
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);

        if (hora >= 5 && hora < 12) {
            return "Buenos días";
        } else if (hora >= 12 && hora < 18) {
            return "Buenas tardes";
        } else {
            return "Buenas noches";
        }
    }

    /**
     * Actualiza el mensaje motivacional
     */
    private void actualizarMensajeMotivacional() {
        String mensaje = preferencesManager.obtenerMensajeMotivacional();
        textViewMensajeMotivacional.setText(mensaje);

        Log.d(TAG, "Mensaje motivacional actualizado");
    }

    /**
     * Actualiza las estadísticas básicas
     */
    private void actualizarEstadisticas() {
        int totalMedicamentos = preferencesManager.obtenerTotalMedicamentos();

        String estadisticas;
        if (totalMedicamentos == 0) {
            estadisticas = "No tienes medicamentos registrados";
            buttonVerMedicamentos.setText("Agregar mi primer medicamento");
        } else if (totalMedicamentos == 1) {
            estadisticas = "Tienes 1 medicamento registrado";
            buttonVerMedicamentos.setText("Ver mis medicamentos");
        } else {
            estadisticas = "Tienes " + totalMedicamentos + " medicamentos registrados";
            buttonVerMedicamentos.setText("Ver mis medicamentos");

            // Agregar información de próxima toma si hay medicamentos
            String proximaToma = obtenerProximaTomaInfo();
            if (proximaToma != null) {
                estadisticas += "\n" + proximaToma;
            }
        }

        textViewEstadisticas.setText(estadisticas);

        Log.d(TAG, "Estadísticas actualizadas: " + totalMedicamentos + " medicamentos");
    }

    /**
     * Obtiene información sobre la próxima toma de medicamento
     */
    private String obtenerProximaTomaInfo() {
        try {
            List<Medicamento> medicamentos = preferencesManager.obtenerMedicamentos();
            if (medicamentos.isEmpty()) {
                return null;
            }

            // Encontrar el medicamento con la próxima toma más cercana
            Medicamento proximoMedicamento = null;
            long tiempoMasCercano = Long.MAX_VALUE;
            long ahora = System.currentTimeMillis();

            for (Medicamento medicamento : medicamentos) {
                long inicioMillis = medicamento.getFechaInicioMillis();
                long intervaloMillis = medicamento.getFrecuenciaHoras() * 60 * 60 * 1000L;

                // Calcular próxima toma
                long proximaToma = inicioMillis;
                while (proximaToma <= ahora) {
                    proximaToma += intervaloMillis;
                }

                if (proximaToma < tiempoMasCercano) {
                    tiempoMasCercano = proximaToma;
                    proximoMedicamento = medicamento;
                }
            }

            if (proximoMedicamento != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String horaProximaToma = sdf.format(tiempoMasCercano);

                return "Próxima toma: " + proximoMedicamento.getNombre() + " a las " + horaProximaToma;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo información de próxima toma", e);
        }

        return null;
    }

    /**
     * Carga la foto de perfil desde SharedPreferences
     */
    private void cargarFotoPerfil() {
        String fotoUri = preferencesManager.obtenerFotoPerfilUri();

        if (fotoUri != null && !fotoUri.isEmpty()) {
            try {
                // Usar Glide para cargar la imagen con opciones de redimensionamiento
                Glide.with(this)
                        .load(Uri.parse(fotoUri))
                        .apply(new RequestOptions()
                                .centerCrop()
                                .placeholder(R.drawable.ic_person_placeholder)
                                .error(R.drawable.ic_person_placeholder))
                        .into(imageViewFotoPerfil);

                Log.d(TAG, "Foto de perfil cargada desde URI");

            } catch (Exception e) {
                Log.e(TAG, "Error cargando foto de perfil", e);
                imageViewFotoPerfil.setImageResource(R.drawable.ic_person_placeholder);
            }
        } else {
            // Mostrar imagen por defecto
            imageViewFotoPerfil.setImageResource(R.drawable.ic_person_placeholder);
            Log.d(TAG, "Usando imagen de perfil por defecto");
        }
    }

    /**
     * Abre la actividad de lista de medicamentos
     */
    private void abrirListaMedicamentos() {
        Intent intent = new Intent(this, ListaMedicamentosActivity.class);
        startActivity(intent);
        Log.d(TAG, "Navegando a ListaMedicamentosActivity");
    }

    /**
     * Abre la actividad de configuraciones
     */
    private void abrirConfiguraciones() {
        Intent intent = new Intent(this, ConfiguracionActivity.class);
        startActivity(intent);
        Log.d(TAG, "Navegando a ConfiguracionActivity");
    }

    /**
     * Inicia el proceso de selección de foto de perfil
     */
    private void seleccionarFotoPerfil() {
        // Verificar permisos de almacenamiento
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ usa permisos granulares
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_STORAGE_PERMISSION);
                return;
            }
        } else {
            // Android 12 y anteriores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                return;
            }
        }

        // Si ya tenemos permisos, abrir galería
        abrirGaleria();
    }

    /**
     * Abre la galería para seleccionar una imagen
     */
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
        Log.d(TAG, "Abriendo galería para seleccionar foto");
    }

    /**
     * Verifica y solicita todos los permisos necesarios
     */
    private void verificarYSolicitarPermisos() {
        // Verificar permisos de notificación para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationHelper.tienePermisosNotificacion()) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
            }
        }

        // Verificar permisos de alarmas exactas
        verificarPermisosAlarmasExactas();
    }

    /**
     * Verifica los permisos de alarmas exactas y solicita al usuario habilitarlos si es necesario
     */
    private void verificarPermisosAlarmasExactas() {
        if (!notificationHelper.tienePermisosAlarmasExactas()) {
            new AlertDialog.Builder(this)
                    .setTitle("Permisos de Alarmas")
                    .setMessage("Para que las notificaciones de medicamentos funcionen correctamente, necesitas habilitar las alarmas exactas en la configuración del sistema.")
                    .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Más tarde", null)
                    .show();
        }
    }

    /**
     * Programa las notificaciones para todos los medicamentos registrados
     */
    private void programarNotificacionesMedicamentos() {
        try {
            // Verificar permisos antes de programar
            if (!notificationHelper.tienePermisosNotificacion()) {
                Log.w(TAG, "No hay permisos de notificación, saltando programación");
                return;
            }

            List<Medicamento> medicamentos = preferencesManager.obtenerMedicamentos();

            for (Medicamento medicamento : medicamentos) {
                notificationHelper.programarNotificacionesMedicamento(medicamento);
            }

            // También programar notificaciones motivacionales
            int frecuencia = preferencesManager.obtenerFrecuenciaNotificaciones();
            String mensaje = preferencesManager.obtenerMensajeMotivacional();
            notificationHelper.programarNotificacionesMotivacionales(frecuencia, mensaje);

            Log.d(TAG, "Notificaciones programadas para " + medicamentos.size() + " medicamentos");

        } catch (Exception e) {
            Log.e(TAG, "Error programando notificaciones", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                // Guardar URI en SharedPreferences
                preferencesManager.guardarFotoPerfilUri(selectedImageUri.toString());

                // Cargar la nueva imagen
                cargarFotoPerfil();

                Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Foto de perfil seleccionada y guardada");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirGaleria();
                } else {
                    Toast.makeText(this, "Permiso requerido para acceder a las fotos", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_NOTIFICATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permisos de notificación concedidos");
                    programarNotificacionesMedicamentos();
                } else {
                    Toast.makeText(this, "Las notificaciones están deshabilitadas. Puedes habilitarlas en Configuración.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destruida");
    }
}