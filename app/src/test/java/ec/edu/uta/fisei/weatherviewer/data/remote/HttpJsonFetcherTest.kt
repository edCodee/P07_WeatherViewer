package ec.edu.uta.fisei.weatherviewer.data.remote

import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import  okio.buffer
import org.junit.Assert.assertArrayEquals

class HttpJsonFetcherTest {

    private val server = MockWebServer()
    // Timeouts cortos para que el test de "no responde" dure milisegundos, no 10 s
    private val fetcher = HttpJsonFetcher(connectTimeoutMs = 1_000, readTimeoutMs = 300)

    @Before
    fun iniciar() = server.start()

    @After
    fun detener() = server.shutdown()

    private fun url() = server.url("/recurso").toString()

    @Test
    fun `200 devuelve el cuerpo tal cual`() {
        server.enqueue(MockResponse().setBody("""{"ok":true}"""))
        assertEquals("""{"ok":true}""", fetcher.get(url()))
    }

    @Test
    fun `el cuerpo se decodifica como UTF-8`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("""[{"name":"Zürich"}]""")
        )
        assertEquals("""[{"name":"Zürich"}]""", fetcher.get(url()))
    }

    @Test
    fun `401 es Unauthorized`() {
        server.enqueue(MockResponse().setResponseCode(401))
        assertThrows(WeatherDataException.Unauthorized::class.java) { fetcher.get(url()) }
    }

    @Test
    fun `429 es servicio no disponible y conserva el codigo`() {
        server.enqueue(MockResponse().setResponseCode(429))
        val e = assertThrows(WeatherDataException.ServiceUnavailable::class.java) { fetcher.get(url()) }
        assertEquals(429, e.httpCode)
    }

    @Test
    fun `503 es servicio no disponible`() {
        server.enqueue(MockResponse().setResponseCode(503))
        assertThrows(WeatherDataException.ServiceUnavailable::class.java) { fetcher.get(url()) }
    }

    @Test
    fun `404 es respuesta inesperada`() {
        server.enqueue(MockResponse().setResponseCode(404))
        assertThrows(WeatherDataException.UnexpectedResponse::class.java) { fetcher.get(url()) }
    }

    @Test
    fun `servidor apagado es sin conexion`() {
        val apagado = MockWebServer().apply { start() }
        val urlMuerta = apagado.url("/").toString()
        apagado.shutdown()
        assertThrows(WeatherDataException.NoConnection::class.java) { fetcher.get(urlMuerta) }
    }

    @Test
    fun `servidor que no responde agota el timeout y es sin conexion`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        assertThrows(WeatherDataException.NoConnection::class.java) { fetcher.get(url()) }
    }

    @Test
    fun `getBytes devuelve los bytes binarios intactos`() {
        // Firma real de un PNG: 0x89 no es UTF-8 valido; leerlo como texto lo corromperia
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        server.enqueue(MockResponse().setBody(Buffer().write(png)))
        assertArrayEquals(png, fetcher.getBytes(url()))
    }
}