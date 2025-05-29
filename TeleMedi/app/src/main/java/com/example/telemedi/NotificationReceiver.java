package com.example.telemedi;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notificación recibida");

        boolean esMotivacional = intent.getBooleanExtra("es_motivacional", false);

        if (esMotivacional) {
            mostrarNotificacionMotivacional(context, intent);
        } else {
            mostrarNotificacionMedicamento(context, intent);
        }
    }

    /**
     * Muestra una notificación de medicamento
     */
    private void mostrarNotificacionMedicamento(Context context, Intent intent) {
        String medicamentoId = intent.getStringExtra("medicamento_id");
        String nombre = intent.getStringExtra("medicamento_nombre");
        String tipo = intent.getStringExtra("medicamento_tipo");
        String dosis = intent.getStringExtra("medicamento_dosis");
        int numeroToma = intent.getIntExtra("numero_toma", 0);

        Log.d(TAG, "Mostrando notificación de medicamento: " + nombre);

        // Crear intent para abrir la app al tocar la notificación
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Crear intent para marcar como tomado
        Intent tomarIntent = new Intent(context, NotificationActionReceiver.class);
        tomarIntent.setAction("ACCION_TOMAR");
        tomarIntent.putExtra("medicamento_id", medicamentoId);
        tomarIntent.putExtra("numero_toma", numeroToma);

        PendingIntent tomarPendingIntent = PendingIntent.getBroadcast(
                context,
                (medicamentoId + "_tomar_" + numeroToma).hashCode(),
                tomarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Crear intent para posponer
        Intent posponerIntent = new Intent(context, NotificationActionReceiver.class);
        posponerIntent.setAction("ACCION_POSPONER");
        posponerIntent.putExtra("medicamento_id", medicamentoId);
        posponerIntent.putExtra("medicamento_nombre", nombre);
        posponerIntent.putExtra("medicamento_tipo", tipo);
        posponerIntent.putExtra("medicamento_dosis", dosis);
        posponerIntent.putExtra("numero_toma", numeroToma);

        PendingIntent posponerPendingIntent = PendingIntent.getBroadcast(
                context,
                (medicamentoId + "_posponer_" + numeroToma).hashCode(),
                posponerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Obtener el canal correcto según el tipo
        String canalId = NotificationHelper.obtenerCanalPorTipo(tipo);

        // Obtener emoji según el tipo
        String emoji = obtenerEmojiPorTipo(tipo);

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Usando ícono del sistema por ahora
                .setContentTitle(emoji + " Es hora de tu medicamento")
                .setContentText(nombre + " - " + dosis)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Es hora de tomar tu medicamento:\n\n" +
                                "💊 " + nombre + "\n" +
                                "📏 Dosis: " + dosis + "\n" +
                                "🕐 Toma #" + (numeroToma + 1)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_agenda, "Tomado", tomarPendingIntent)
                .addAction(android.R.drawable.ic_menu_recent_history, "Posponer 10 min", posponerPendingIntent)
                .setVibrate(new long[]{0, 250, 250, 250})
                .setLights(0xFF4CAF50, 1000, 1000);

        // Mostrar la notificación
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (medicamentoId + "_" + numeroToma).hashCode();
        notificationManager.notify(notificationId, builder.build());

        Log.d(TAG, "Notificación de medicamento mostrada - ID: " + notificationId);
    }

    /**
     * Muestra una notificación motivacional
     */
    private void mostrarNotificacionMotivacional(Context context, Intent intent) {
        String mensaje = intent.getStringExtra("mensaje_motivacional");
        int numeroMotivacional = intent.getIntExtra("numero_motivacional", 1);

        Log.d(TAG, "Mostrando notificación motivacional #" + numeroMotivacional);

        // Crear intent para abrir la app al tocar la notificación
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Crear intent para marcar como visto
        Intent vistoIntent = new Intent(context, NotificationActionReceiver.class);
        vistoIntent.setAction("ACCION_MOTIVACIONAL_VISTO");
        vistoIntent.putExtra("numero_motivacional", numeroMotivacional);

        PendingIntent vistoPendingIntent = PendingIntent.getBroadcast(
                context,
                ("motivacional_visto_" + numeroMotivacional).hashCode(),
                vistoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Array de mensajes motivacionales adicionales si no se proporciona uno
        String[] mensajesMotivacionales = {
                "¡Recuerda tomar tus medicamentos para mantener tu salud!",
                "Tu salud es lo más importante. ¡Sigue tu tratamiento!",
                "Cada medicamento tomado es un paso hacia tu bienestar",
                "¡Eres fuerte! Continúa cuidando tu salud",
                "Tu constancia en el tratamiento marca la diferencia"
        };

        if (mensaje == null || mensaje.isEmpty()) {
            mensaje = mensajesMotivacionales[numeroMotivacional % mensajesMotivacionales.length];
        }

        // Construir la notificación motivacional
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_MOTIVACIONAL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🌟 Mensaje Motivacional")
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("🌟 " + mensaje + "\n\n" +
                                "¡Mantén una actitud positiva hacia tu salud!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_view, "¡Gracias!", vistoPendingIntent)
                .setLights(0xFF2196F3, 1000, 1000);

        // Mostrar la notificación
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = ("motivacional_" + numeroMotivacional).hashCode();
        notificationManager.notify(notificationId, builder.build());

        Log.d(TAG, "Notificación motivacional mostrada - ID: " + notificationId);
    }

    /**
     * Obtiene el emoji correspondiente al tipo de medicamento
     */
    private String obtenerEmojiPorTipo(String tipo) {
        if (tipo == null) return "💊";

        switch (tipo.toLowerCase()) {
            case "pastilla":
            case "cápsula":
                return "💊";
            case "jarabe":
                return "🍯";
            case "ampolla":
                return "💉";
            default:
                return "💊";
        }
    }

    /**
     * Clase interna para manejar las acciones de las notificaciones
     */
    public static class NotificationActionReceiver extends BroadcastReceiver {
        private static final String TAG = "NotificationAction";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Acción recibida: " + action);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            switch (action) {
                case "ACCION_TOMAR":
                    manejarAccionTomar(context, intent, notificationManager);
                    break;

                case "ACCION_POSPONER":
                    manejarAccionPosponer(context, intent, notificationManager);
                    break;

                case "ACCION_MOTIVACIONAL_VISTO":
                    manejarAccionMotivacionalVisto(context, intent, notificationManager);
                    break;
            }
        }

        private void manejarAccionTomar(Context context, Intent intent, NotificationManager notificationManager) {
            String medicamentoId = intent.getStringExtra("medicamento_id");
            int numeroToma = intent.getIntExtra("numero_toma", 0);

            // Cancelar la notificación actual
            int notificationId = (medicamentoId + "_" + numeroToma).hashCode();
            notificationManager.cancel(notificationId);

            // Mostrar notificación de confirmación
            NotificationCompat.Builder confirmBuilder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_MOTIVACIONAL)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("✅ ¡Perfecto!")
                    .setContentText("Medicamento marcado como tomado")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .setTimeoutAfter(3000); // Auto-eliminar después de 3 segundos

            notificationManager.notify(("confirmacion_" + medicamentoId).hashCode(), confirmBuilder.build());

            Log.d(TAG, "Medicamento marcado como tomado - ID: " + medicamentoId);
        }

        private void manejarAccionPosponer(Context context, Intent intent, NotificationManager notificationManager) {
            String medicamentoId = intent.getStringExtra("medicamento_id");
            String nombre = intent.getStringExtra("medicamento_nombre");
            String tipo = intent.getStringExtra("medicamento_tipo");
            String dosis = intent.getStringExtra("medicamento_dosis");
            int numeroToma = intent.getIntExtra("numero_toma", 0);

            // Cancelar la notificación actual
            int notificationId = (medicamentoId + "_" + numeroToma).hashCode();
            notificationManager.cancel(notificationId);

            // Programar nueva notificación en 10 minutos
            NotificationHelper notificationHelper = new NotificationHelper(context);
            long tiempoPospuesto = System.currentTimeMillis() + (10 * 60 * 1000L); // 10 minutos

            Intent nuevoIntent = new Intent(context, NotificationReceiver.class);
            nuevoIntent.putExtra("medicamento_id", medicamentoId);
            nuevoIntent.putExtra("medicamento_nombre", nombre);
            nuevoIntent.putExtra("medicamento_tipo", tipo);
            nuevoIntent.putExtra("medicamento_dosis", dosis);
            nuevoIntent.putExtra("numero_toma", numeroToma);

            // Crear notificación de confirmación
            NotificationCompat.Builder confirmBuilder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_MOTIVACIONAL)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("⏰ Pospuesto")
                    .setContentText("Te recordaré en 10 minutos")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .setTimeoutAfter(3000);

            notificationManager.notify(("posponer_" + medicamentoId).hashCode(), confirmBuilder.build());

            Log.d(TAG, "Medicamento pospuesto 10 minutos - ID: " + medicamentoId);
        }

        private void manejarAccionMotivacionalVisto(Context context, Intent intent, NotificationManager notificationManager) {
            int numeroMotivacional = intent.getIntExtra("numero_motivacional", 1);

            // Cancelar la notificación motivacional
            int notificationId = ("motivacional_" + numeroMotivacional).hashCode();
            notificationManager.cancel(notificationId);

            Log.d(TAG, "Mensaje motivacional marcado como visto #" + numeroMotivacional);
        }
    }
}