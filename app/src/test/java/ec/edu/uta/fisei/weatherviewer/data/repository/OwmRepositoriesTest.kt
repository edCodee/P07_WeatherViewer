package ec.edu.uta.fisei.weatherviewer.data.repository

import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmUrls
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult
import ec.edu.uta.fisei.weatherviewer.domain.usecase.GetDailyForecastUseCase
import ec.edu.uta.fisei.weatherviewer.fakes.LONDRES
import ec.edu.uta.fisei.weatherviewer.testutil.Fixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OwmRepositoriesTest {

    private val server = MockWebServer()
    private val fetcher = HttpJsonFetcher()
    private lateinit var urls: OwmUrls

    @Before
    fun iniciar() {
        server.start()
        urls = OwmUrls(
            apiKey = "clave-de-prueba",
            baseUrl = server.url("/").toString().removeSuffix("/")
        )
    }

    @After
    fun detener() = server.shutdown()

    private fun responder(fixture: String, codigo: Int = 200) =
        server.enqueue(MockResponse().setResponseCode(codigo).setBody(Fixtures.read(fixture)))

    private fun casoDeUso() = GetDailyForecastUseCase(
        OwmGeocodingRepository(fetcher, urls),
        OwmForecastRepository(fetcher, urls)
    )

    // --- Cada repositorio por separado ---

    @Test
    fun `geocoding pide la ruta correcta y devuelve Londres`() = runTest {
        responder("02-geocoding-ok.json")

        val location = OwmGeocodingRepository(fetcher, urls).findLocation("London,GB")

        assertEquals(LONDRES, location)
        val path = server.takeRequest().path!!
        assertTrue(path, path.startsWith("/geo/1.0/direct?q=London%2CGB&limit=1&appid=clave-de-prueba"))
    }

    @Test
    fun `forecast pide las coordenadas en metrico y trae 40 lecturas`() = runTest {
        responder("03-forecast-5d.json")

        val serie = OwmForecastRepository(fetcher, urls).getForecast(51.5074456, -0.1277653)

        assertEquals(40, serie.entries.size)
        val path = server.takeRequest().path!!
        assertTrue(path, path.startsWith("/data/2.5/forecast?lat=51.5074456&lon=-0.1277653&units=metric"))
    }

    // --- Flujo completo: dominio + datos, sin internet ---

    @Test
    fun `flujo completo - ciudad a 5 dias con dos peticiones`() = runTest {
        responder("02-geocoding-ok.json")
        responder("03-forecast-5d.json")

        val r = casoDeUso()("London,GB") as ForecastResult.Success

        assertEquals(LONDRES, r.location)
        assertEquals(5, r.days.size)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `flujo completo - key rechazada corta en la primera peticion`() = runTest {
        responder("05-key-invalida.json", codigo = 401)

        assertEquals(ForecastResult.Unauthorized, casoDeUso()("London,GB"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `flujo completo - ciudad inexistente corta en la primera peticion`() = runTest {
        responder("04-geocoding-no-existe.json")

        assertEquals(ForecastResult.CityNotFound, casoDeUso()("Ciudadquenoexiste123"))
        assertEquals(1, server.requestCount)
    }
}