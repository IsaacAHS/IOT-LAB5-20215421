package com.example.telemedi;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    // IDs de canales de notificación
    public static final String CHANNEL_PASTILLA = "canal_pastilla";
    public static final String CHANNEL_JARABE = "canal_jarabe";
    public static final String CHANNEL_AMPOLLA = "canal_ampolla";
    public static final String CHANNEL_CAPSULA = "canal_capsula";
    public static final String CHANNEL_MOTIVACIONAL = "canal_motivacional";

    // Nombres de canales
    private static final String CHANNEL_PASTILLA_NAME = "Pastillas";
    private static final String CHANNEL_JARABE_NAME = "Jarabes";
    private static final String CHANNEL_AMPOLLA_NAME = "Ampollas";
    private static final String CHANNEL_CAPSULA_NAME = "Cápsulas";
    private static final String CHANNEL_MOTIVACIONAL_NAME = "Mensajes Motivacionales";

    // Descripciones de canales
    private static final String CHANNEL_PASTILLA_DESC = "Notificaciones para medicamentos en pastilla";
    private static final String CHANNEL_JARABE_DESC = "Notificaciones para medicamentos en jarabe";
    private static final String CHANNEL_AMPOLLA_DESC = "Notificaciones para medicamentos en ampolla";
    private static final String CHANNEL_CAPSULA_DESC = "Notificaciones para medicamentos en cápsula";
    private static final String CHANNEL_MOTIVACIONAL_DESC = "Mensajes motivacionales";

    private Context context;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        crearCanalesNotificacion();
    }

    /**
     * Crea todos los canales de notificación necesarios
     */
    private void crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Canal para Pastillas
            NotificationChannel canalPastilla = new NotificationChannel(
                    CHANNEL_PASTILLA,
                    CHANNEL_PASTILLA_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalPastilla.setDescription(CHANNEL_PASTILLA_DESC);
            canalPastilla.enableVibration(true);
            canalPastilla.setVibrationPattern(new long[]{0, 250, 250, 250});

            // Canal para Jarabes
            NotificationChannel canalJarabe = new NotificationChannel(
                    CHANNEL_JARABE,
                    CHANNEL_JARABE_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalJarabe.setDescription(CHANNEL_JARABE_DESC);
            canalJarabe.enableVibration(true);
            canalJarabe.setVibrationPattern(new long[]{0, 300, 300, 300});

            // Canal para Ampollas
            NotificationChannel canalAmpolla = new NotificationChannel(
                    CHANNEL_AMPOLLA,
                    CHANNEL_AMPOLLA_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalAmpolla.setDescription(CHANNEL_AMPOLLA_DESC);
            canalAmpolla.enableVibration(true);
            canalAmpolla.setVibrationPattern(new long[]{0, 500, 200, 500});

            // Canal para Cápsulas
            NotificationChannel canalCapsula = new NotificationChannel(
                    CHANNEL_CAPSULA,
                    CHANNEL_CAPSULA_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalCapsula.setDescription(CHANNEL_CAPSULA_DESC);
            canalCapsula.enableVibration(true);
            canalCapsula.setVibrationPattern(new long[]{0, 200, 200, 200});

            // Canal para Mensajes Motivacionales
            NotificationChannel canalMotivacional = new NotificationChannel(
                    CHANNEL_MOTIVACIONAL,
                    CHANNEL_MOTIVACIONAL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canalMotivacional.setDescription(CHANNEL_MOTIVACIONAL_DESC);
            canalMotivacional.enableVibration(false);

            // Registrar todos los canales
            notificationManager.createNotificationChannel(canalPastilla);
            notificationManager.createNotificationChannel(canalJarabe);
            notificationManager.createNotificationChannel(canalAmpolla);
            notificationManager.createNotificationChannel(canalCapsula);
            notificationManager.createNotificationChannel(canalMotivacional);

            Log.d(TAG, "Canales de notificación creados");
        }
    }

    /**
     * Programa todas las notificaciones para un medicamento
     */
    public void programarNotificacionesMedicamento(Medicamento medicamento) {
        Log.d(TAG, "Programando notificaciones para: " + medicamento.getNombre());

        long ahora = System.currentTimeMillis();
        long inicioMillis = medicamento.getFechaInicioMillis();
        long intervaloMillis = medicamento.getFrecuenciaHoras() * 60 * 60 * 1000L;

        // Programar las próximas 10 notificaciones
        for (int i = 0; i < 10; i++) {
            long tiempoNotificacion = inicioMillis + (i * intervaloMillis);

            // Solo programar notificaciones futuras
            if (tiempoNotificacion > ahora) {
                programarNotificacionIndividual(medicamento, tiempoNotificacion, i);
            }
        }
    }

    /**
     * Programa una notificación individual
     */
    private void programarNotificacionIndividual(Medicamento medicamento, long tiempoNotificacion, int numeroToma) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("medicamento_id", medicamento.getId());
        intent.putExtra("medicamento_nombre", medicamento.getNombre());
        intent.putExtra("medicamento_tipo", medicamento.getTipo());
        intent.putExtra("medicamento_dosis", medicamento.getDosis());
        intent.putExtra("numero_toma", numeroToma);

        // Crear un ID único para cada notificación
        int notificationId = (medicamento.getId() + "_" + numeroToma).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        tiempoNotificacion,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        tiempoNotificacion,
                        pendingIntent
                );
            }

            Log.d(TAG, "Notificación programada para: " + medicamento.getNombre() +
                    " - Toma #" + (numeroToma + 1) + " - ID: " + notificationId);

        } catch (SecurityException e) {
            Log.e(TAG, "Error de permisos al programar notificación", e);
        }
    }

    /**
     * Cancela todas las notificaciones de un medicamento
     */
    public void cancelarNotificacionesMedicamento(String medicamentoId) {
        Log.d(TAG, "Cancelando notificaciones para medicamento ID: " + medicamentoId);

        // Cancelar las 10 posibles notificaciones programadas
        for (int i = 0; i < 10; i++) {
            int notificationId = (medicamentoId + "_" + i).hashCode();

            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        Log.d(TAG, "Notificaciones canceladas para medicamento ID: " + medicamentoId);
    }

    /**
     * Programa notificaciones motivacionales
     */
    public void programarNotificacionesMotivacionales(int frecuenciaHoras, String mensaje) {
        Log.d(TAG, "Programando notificaciones motivacionales cada " + frecuenciaHoras + " horas");

        // Cancelar notificaciones motivacionales anteriores
        cancelarNotificacionesMotivacionales();

        long intervaloMillis = frecuenciaHoras * 60 * 60 * 1000L;
        long ahora = System.currentTimeMillis();

        // Programar las próximas 5 notificaciones motivacionales
        for (int i = 1; i <= 5; i++) {
            long tiempoNotificacion = ahora + (i * intervaloMillis);

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("es_motivacional", true);
            intent.putExtra("mensaje_motivacional", mensaje);
            intent.putExtra("numero_motivacional", i);

            int notificationId = ("motivacional_" + i).hashCode();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            tiempoNotificacion,
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            tiempoNotificacion,
                            pendingIntent
                    );
                }

                Log.d(TAG, "Notificación motivacional #" + i + " programada - ID: " + notificationId);

            } catch (SecurityException e) {
                Log.e(TAG, "Error de permisos al programar notificación motivacional", e);
            }
        }
    }

    /**
     * Cancela todas las notificaciones motivacionales
     */
    public void cancelarNotificacionesMotivacionales() {
        Log.d(TAG, "Cancelando notificaciones motivacionales");

        for (int i = 1; i <= 5; i++) {
            int notificationId = ("motivacional_" + i).hashCode();

            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    /**
     * Obtiene el canal correcto según el tipo de medicamento
     */
    public static String obtenerCanalPorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "pastilla":
                return CHANNEL_PASTILLA;
            case "jarabe":
                return CHANNEL_JARABE;
            case "ampolla":
                return CHANNEL_AMPOLLA;
            case "cápsula":
                return CHANNEL_CAPSULA;
            default:
                return CHANNEL_PASTILLA;
        }
    }

    /**
     * Verifica si los permisos de notificación están habilitados
     */
    public boolean tienePermisosNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return notificationManager.areNotificationsEnabled();
        }
        return true; // En versiones anteriores se asume que están habilitadas
    }

    /**
     * Verifica si los permisos de alarmas exactas están habilitados
     */
    public boolean tienePermisosAlarmasExactas() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true; // En versiones anteriores se asume que están habilitadas
    }
}