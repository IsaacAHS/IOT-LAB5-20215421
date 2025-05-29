package com.example.telemedi;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {
    private static final String PREFS_NAME = "MedicamentoAppPrefs";
    private static final String KEY_MEDICAMENTOS = "medicamentos";
    private static final String KEY_USUARIO_NOMBRE = "usuario_nombre";
    private static final String KEY_MENSAJE_MOTIVACIONAL = "mensaje_motivacional";
    private static final String KEY_FRECUENCIA_NOTIFICACIONES = "frecuencia_notificaciones";
    private static final String KEY_FOTO_PERFIL_URI = "foto_perfil_uri";

    private static final String TAG = "SharedPrefsManager";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // === MÉTODOS PARA MEDICAMENTOS ===

    /**
     * Guarda un nuevo medicamento en SharedPreferences
     */
    public boolean guardarMedicamento(Medicamento medicamento) {
        try {
            List<Medicamento> medicamentos = obtenerMedicamentos();
            medicamentos.add(medicamento);

            JSONArray jsonArray = new JSONArray();
            for (Medicamento med : medicamentos) {
                jsonArray.put(med.toJSON());
            }

            editor.putString(KEY_MEDICAMENTOS, jsonArray.toString());
            boolean guardado = editor.commit();

            Log.d(TAG, "Medicamento guardado: " + medicamento.getNombre() + " - Éxito: " + guardado);
            return guardado;

        } catch (JSONException e) {
            Log.e(TAG, "Error al guardar medicamento", e);
            return false;
        }
    }

    /**
     * Obtiene todos los medicamentos guardados
     */
    public List<Medicamento> obtenerMedicamentos() {
        List<Medicamento> medicamentos = new ArrayList<>();

        try {
            String medicamentosJson = sharedPreferences.getString(KEY_MEDICAMENTOS, "[]");
            JSONArray jsonArray = new JSONArray(medicamentosJson);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonMedicamento = jsonArray.getJSONObject(i);
                Medicamento medicamento = new Medicamento(jsonMedicamento);
                medicamentos.add(medicamento);
            }

            Log.d(TAG, "Medicamentos cargados: " + medicamentos.size());

        } catch (JSONException e) {
            Log.e(TAG, "Error al cargar medicamentos", e);
        }

        return medicamentos;
    }

    /**
     * Elimina un medicamento por ID
     */
    public boolean eliminarMedicamento(String medicamentoId) {
        try {
            List<Medicamento> medicamentos = obtenerMedicamentos();
            boolean eliminado = false;

            for (int i = 0; i < medicamentos.size(); i++) {
                if (medicamentos.get(i).getId().equals(medicamentoId)) {
                    medicamentos.remove(i);
                    eliminado = true;
                    break;
                }
            }

            if (eliminado) {
                JSONArray jsonArray = new JSONArray();
                for (Medicamento medicamento : medicamentos) {
                    jsonArray.put(medicamento.toJSON());
                }

                editor.putString(KEY_MEDICAMENTOS, jsonArray.toString());
                boolean guardado = editor.commit();

                Log.d(TAG, "Medicamento eliminado - ID: " + medicamentoId + " - Éxito: " + guardado);
                return guardado;
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error al eliminar medicamento", e);
        }

        return false;
    }

    /**
     * Actualiza un medicamento existente
     */
    public boolean actualizarMedicamento(Medicamento medicamentoActualizado) {
        try {
            List<Medicamento> medicamentos = obtenerMedicamentos();
            boolean actualizado = false;

            for (int i = 0; i < medicamentos.size(); i++) {
                if (medicamentos.get(i).getId().equals(medicamentoActualizado.getId())) {
                    medicamentos.set(i, medicamentoActualizado);
                    actualizado = true;
                    break;
                }
            }

            if (actualizado) {
                JSONArray jsonArray = new JSONArray();
                for (Medicamento medicamento : medicamentos) {
                    jsonArray.put(medicamento.toJSON());
                }

                editor.putString(KEY_MEDICAMENTOS, jsonArray.toString());
                boolean guardado = editor.commit();

                Log.d(TAG, "Medicamento actualizado: " + medicamentoActualizado.getNombre() + " - Éxito: " + guardado);
                return guardado;
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error al actualizar medicamento", e);
        }

        return false;
    }

    /**
     * Busca un medicamento por ID
     */
    public Medicamento obtenerMedicamentoPorId(String medicamentoId) {
        List<Medicamento> medicamentos = obtenerMedicamentos();

        for (Medicamento medicamento : medicamentos) {
            if (medicamento.getId().equals(medicamentoId)) {
                return medicamento;
            }
        }

        return null;
    }

    /**
     * Elimina todos los medicamentos
     */
    public boolean limpiarMedicamentos() {
        editor.remove(KEY_MEDICAMENTOS);
        boolean limpiado = editor.commit();
        Log.d(TAG, "Medicamentos limpiados - Éxito: " + limpiado);
        return limpiado;
    }

    // === MÉTODOS PARA CONFIGURACIÓN DE USUARIO ===

    /**
     * Guarda el nombre del usuario
     */
    public void guardarNombreUsuario(String nombre) {
        editor.putString(KEY_USUARIO_NOMBRE, nombre);
        editor.apply();
        Log.d(TAG, "Nombre de usuario guardado: " + nombre);
    }

    /**
     * Obtiene el nombre del usuario
     */
    public String obtenerNombreUsuario() {
        return sharedPreferences.getString(KEY_USUARIO_NOMBRE, "Usuario");
    }

    /**
     * Guarda el mensaje motivacional
     */
    public void guardarMensajeMotivacional(String mensaje) {
        editor.putString(KEY_MENSAJE_MOTIVACIONAL, mensaje);
        editor.apply();
        Log.d(TAG, "Mensaje motivacional guardado");
    }

    /**
     * Obtiene el mensaje motivacional
     */
    public String obtenerMensajeMotivacional() {
        return sharedPreferences.getString(KEY_MENSAJE_MOTIVACIONAL,
                "¡Recuerda tomar tus medicamentos para mantener tu salud!");
    }

    /**
     * Guarda la frecuencia de notificaciones motivacionales (en horas)
     */
    public void guardarFrecuenciaNotificaciones(int frecuenciaHoras) {
        editor.putInt(KEY_FRECUENCIA_NOTIFICACIONES, frecuenciaHoras);
        editor.apply();
        Log.d(TAG, "Frecuencia de notificaciones guardada: " + frecuenciaHoras + " horas");
    }

    /**
     * Obtiene la frecuencia de notificaciones motivacionales
     */
    public int obtenerFrecuenciaNotificaciones() {
        return sharedPreferences.getInt(KEY_FRECUENCIA_NOTIFICACIONES, 12); // Default: cada 12 horas
    }

    /**
     * Guarda la URI de la foto de perfil
     */
    public void guardarFotoPerfilUri(String uri) {
        editor.putString(KEY_FOTO_PERFIL_URI, uri);
        editor.apply();
        Log.d(TAG, "URI de foto de perfil guardada");
    }

    /**
     * Obtiene la URI de la foto de perfil
     */
    public String obtenerFotoPerfilUri() {
        return sharedPreferences.getString(KEY_FOTO_PERFIL_URI, null);
    }

    // === MÉTODOS DE UTILIDAD ===

    /**
     * Verifica si es la primera vez que se abre la app
     */
    public boolean esPrimeraVez() {
        return sharedPreferences.getBoolean("primera_vez", true);
    }

    /**
     * Marca que ya no es la primera vez
     */
    public void marcarComoVisto() {
        editor.putBoolean("primera_vez", false);
        editor.apply();
    }

    /**
     * Obtiene estadísticas básicas
     */
    public int obtenerTotalMedicamentos() {
        return obtenerMedicamentos().size();
    }

    /**
     * Limpia todas las preferencias
     */
    public boolean limpiarTodo() {
        boolean limpiado = editor.clear().commit();
        Log.d(TAG, "Todas las preferencias limpiadas - Éxito: " + limpiado);
        return limpiado;
    }

    /**
     * Exporta todas las preferencias como JSON (para backup)
     */
    public String exportarDatos() {
        try {
            JSONObject export = new JSONObject();
            export.put(KEY_MEDICAMENTOS, sharedPreferences.getString(KEY_MEDICAMENTOS, "[]"));
            export.put(KEY_USUARIO_NOMBRE, obtenerNombreUsuario());
            export.put(KEY_MENSAJE_MOTIVACIONAL, obtenerMensajeMotivacional());
            export.put(KEY_FRECUENCIA_NOTIFICACIONES, obtenerFrecuenciaNotificaciones());

            return export.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error al exportar datos", e);
            return null;
        }
    }
}