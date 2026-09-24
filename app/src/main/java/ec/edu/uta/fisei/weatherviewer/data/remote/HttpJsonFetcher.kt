package ec.edu.uta.fisei.weatherviewer.data.remote

import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/**
 * GET bloqueante. Devuelve el cuerpo o lanza WeatherDataException.
 * Bloqueante a proposito: quien lo llama lo ejecuta en Dispatchers.IO.
 */
class HttpJsonFetcher(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 10_000
) {

    /** Texto (JSON), decodificado SIEMPRE como UTF-8. */
    fun get(url: String): String = String(getBytes(url), Charsets.UTF_8)

    /** Bytes crudos (imagenes): convertirlos a texto los corromperia. */
    fun getBytes(url: String): ByteArray {
        val conexion = URI.create(url).toURL().openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = connectTimeoutMs
            conexion.readTimeout = readTimeoutMs

            val codigo = conexion.responseCode
            return when {
                codigo == HttpURLConnection.HTTP_OK ->
                    conexion.inputStream.use { it.readBytes() }

                // CONTRATO.md, hallazgo 1: key invalida O plan sin acceso
                codigo == HttpURLConnection.HTTP_UNAUTHORIZED ->
                    throw WeatherDataException.Unauthorized()

                codigo == HTTP_TOO_MANY_REQUESTS || codigo >= 500 ->
                    throw WeatherDataException.ServiceUnavailable(codigo)

                else -> throw WeatherDataException.UnexpectedResponse()
            }
        } catch (e: IOException) {
            throw WeatherDataException.NoConnection(e)
        } finally {
            conexion.disconnect()
        }
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}