package ec.edu.uta.fisei.weatherviewer.data.remote

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastEntry
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import ec.edu.uta.fisei.weatherviewer.domain.model.Location
import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Traduce las respuestas de OpenWeatherMap a modelos de dominio.
 * No lee el campo "cod" (CONTRATO.md, hallazgo 2): el estado lo decide el codigo HTTP.
 */
class OwmJsonParser {

    fun parseGeocoding(json: String): Location? = conFormatoEsperado {
        val resultados = JSONArray(json)
        if (resultados.length() == 0) return@conFormatoEsperado null

        val ciudad = resultados.getJSONObject(0)
        Location(
            name = ciudad.getString("name"),
            country = ciudad.optString("country"),
            latitude = ciudad.getDouble("lat"),
            longitude = ciudad.getDouble("lon")
        )
    }

    fun parseForecast(json: String): ForecastSeries = conFormatoEsperado {
        val raiz = JSONObject(json)
        val lista = raiz.getJSONArray("list")

        val lecturas = (0 until lista.length()).map { i ->
            val item = lista.getJSONObject(i)
            val main = item.getJSONObject("main")
            val clima = item.getJSONArray("weather").getJSONObject(0)
            ForecastEntry(
                epochSeconds = item.getLong("dt"),
                tempMin = main.getDouble("temp_min"),
                tempMax = main.getDouble("temp_max"),
                humidity = main.getInt("humidity"),
                description = clima.getString("description"),
                iconCode = clima.getString("icon")
            )
        }

        ForecastSeries(
            timezoneOffsetSeconds = raiz.getJSONObject("city").getInt("timezone"),
            entries = lecturas
        )
    }

    /** Cualquier sorpresa de formato se reporta en terminos del dominio. */
    private inline fun <T> conFormatoEsperado(bloque: () -> T): T =
        try {
            bloque()
        } catch (e: JSONException) {
            throw WeatherDataException.UnexpectedResponse(e)
        }
}