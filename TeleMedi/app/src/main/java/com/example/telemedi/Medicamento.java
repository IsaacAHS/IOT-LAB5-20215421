package com.example.telemedi;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Medicamento {
    private String id;
    private String nombre;
    private String tipo;
    private String dosis;
    private int frecuenciaHoras;
    private long fechaInicioMillis;

    // Constructor completo
    public Medicamento(String nombre, String tipo, String dosis, int frecuenciaHoras, long fechaInicioMillis) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.tipo = tipo;
        this.dosis = dosis;
        this.frecuenciaHoras = frecuenciaHoras;
        this.fechaInicioMillis = fechaInicioMillis;
    }

    // Constructor con ID (para cargar desde almacenamiento)
    public Medicamento(String id, String nombre, String tipo, String dosis, int frecuenciaHoras, long fechaInicioMillis) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.dosis = dosis;
        this.frecuenciaHoras = frecuenciaHoras;
        this.fechaInicioMillis = fechaInicioMillis;
    }

    // Constructor desde JSON
    public Medicamento(JSONObject json) throws JSONException {
        this.id = json.getString("id");
        this.nombre = json.getString("nombre");
        this.tipo = json.getString("tipo");
        this.dosis = json.getString("dosis");
        this.frecuenciaHoras = json.getInt("frecuenciaHoras");
        this.fechaInicioMillis = json.getLong("fechaInicioMillis");
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDosis() {
        return dosis;
    }

    public int getFrecuenciaHoras() {
        return frecuenciaHoras;
    }

    public long getFechaInicioMillis() {
        return fechaInicioMillis;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public void setFrecuenciaHoras(int frecuenciaHoras) {
        this.frecuenciaHoras = frecuenciaHoras;
    }

    public void setFechaInicioMillis(long fechaInicioMillis) {
        this.fechaInicioMillis = fechaInicioMillis;
    }

    // Métodos de utilidad
    public String getFechaInicioFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(fechaInicioMillis));
    }

    public String getHoraInicioFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(fechaInicioMillis));
    }

    public String getFechaHoraInicioFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(fechaInicioMillis));
    }

    public String getFrecuenciaTexto() {
        if (frecuenciaHoras == 1) {
            return "Cada hora";
        } else if (frecuenciaHoras < 24) {
            return "Cada " + frecuenciaHoras + " horas";
        } else if (frecuenciaHoras == 24) {
            return "Una vez al día";
        } else {
            int dias = frecuenciaHoras / 24;
            return "Cada " + dias + " días";
        }
    }

    // Método para calcular próxima toma
    public long calcularProximaToma() {
        long ahora = System.currentTimeMillis();
        long intervaloMillis = frecuenciaHoras * 60 * 60 * 1000L; // Convertir horas a milisegundos

        if (fechaInicioMillis > ahora) {
            // Si la fecha de inicio es futura, la próxima toma es la fecha de inicio
            return fechaInicioMillis;
        } else {
            // Calcular cuántas tomas han pasado desde el inicio
            long tiempoTranscurrido = ahora - fechaInicioMillis;
            long tomasTranscurridas = tiempoTranscurrido / intervaloMillis;

            // Calcular la próxima toma
            return fechaInicioMillis + ((tomasTranscurridas + 1) * intervaloMillis);
        }
    }

    public String getProximaTomaFormateada() {
        long proximaToma = calcularProximaToma();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(proximaToma));
    }

    // Serialización a JSON
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("nombre", nombre);
        json.put("tipo", tipo);
        json.put("dosis", dosis);
        json.put("frecuenciaHoras", frecuenciaHoras);
        json.put("fechaInicioMillis", fechaInicioMillis);
        return json;
    }

    // Método toString para debugging
    @Override
    public String toString() {
        return "Medicamento{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", dosis='" + dosis + '\'' +
                ", frecuenciaHoras=" + frecuenciaHoras +
                ", fechaInicioMillis=" + fechaInicioMillis +
                '}';
    }

    // Método equals para comparación
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Medicamento that = (Medicamento) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Método para verificar si es hora de tomar el medicamento
    public boolean esHoraDeTomar() {
        long ahora = System.currentTimeMillis();
        long proximaToma = calcularProximaToma();

        // Considerar un margen de 5 minutos
        long margenMillis = 5 * 60 * 1000L;

        return Math.abs(ahora - proximaToma) <= margenMillis;
    }

    // Método para obtener el emoji según el tipo de medicamento
    public String getEmojiTipo() {
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
}