package ec.edu.uta.fisei.weatherviewer.data.remote

import java.net.URLEncoder

/**
 * Arma las URLs del API. baseUrl es inyectable para que los tests apunten
 * a un servidor local (MockWebServer) en vez de a internet.
 */
class OwmUrls(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openweathermap.org"
) {

    fun geocoding(query: String): String =
        "$baseUrl/geo/1.0/direct?q=${URLEncoder.encode(query, "UTF-8")}&limit=1&appid=$apiKey"

    /**
     * Plantilla de Kotlin ($latitude usa Double.toString), NUNCA String.format:
     * la plantilla siempre escribe punto decimal; String.format usaria la coma
     * del locale es-EC y romperia la URL.
     */
    fun forecast(latitude: Double, longitude: Double): String =
        "$baseUrl/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey"


    /** https: el http del PDF esta bloqueado por defecto desde API 28 (incompatibilidad #10). */
    fun icon(iconCode: String): String = "https://openweathermap.org/img/wn/$iconCode@2x.png"
}