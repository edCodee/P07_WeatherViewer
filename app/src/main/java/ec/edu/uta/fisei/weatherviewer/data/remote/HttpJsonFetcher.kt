package ec.edu.uta.fisei.weatherviewer.data.remote

import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/**
 * GET bloqueante que devuelve el cuerpo como texto o lanza WeatherDataException.
 * Bloqueante a proposito: los repositorios lo ejecutan en Dispatchers.IO.
 */
class HttpJsonFetcher(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 10_000
) {

    fun get(url: String): String {
        val conexion = URI.create(url).toURL().openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = connectTimeoutMs
            conexion.readTimeout = readTimeoutMs
            conexion.setRequestProperty("Accept", "application/json")

            val codigo = conexion.responseCode
            return when {
                codigo == HttpURLConnection.HTTP_OK ->
                    conexion.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

                // CONTRATO.md, hallazgo 1: key invalida O plan sin acceso
                codigo == HttpURLConnection.HTTP_UNAUTHORIZED ->
                    throw WeatherDataException.Unauthorized()

                codigo == HTTP_TOO_MANY_REQUESTS || codigo >= 500 ->
                    throw WeatherDataException.ServiceUnavailable(codigo)

                else -> throw WeatherDataException.UnexpectedResponse()
            }
        } catch (e: IOException) {
            // Sin red, DNS que no resuelve, conexion rechazada, timeout...
            throw WeatherDataException.NoConnection(e)
        } finally {
            conexion.disconnect()
        }
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}