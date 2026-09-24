package ec.edu.uta.fisei.weatherviewer.domain.usecase

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastEntry
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import ec.edu.uta.fisei.weatherviewer.fakes.FakeForecastRepository
import ec.edu.uta.fisei.weatherviewer.fakes.FakeGeocodingRepository
import ec.edu.uta.fisei.weatherviewer.fakes.LONDRES
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class GetDailyForecastUseCaseTest {

    @Test
    fun `consulta en blanco no llama a ningun repositorio`() = runTest {
        val geo = FakeGeocodingRepository()
        val pronostico = FakeForecastRepository()

        val r = GetDailyForecastUseCase(geo, pronostico)("   ")

        assertEquals(ForecastResult.EmptyQuery, r)
        assertTrue(geo.consultas.isEmpty())
        assertEquals(0, pronostico.llamadas)
    }

    @Test
    fun `la consulta llega sin espacios al repositorio`() = runTest {
        val geo = FakeGeocodingRepository()
        GetDailyForecastUseCase(geo, FakeForecastRepository())("  London,GB  ")
        assertEquals(listOf("London,GB"), geo.consultas)
    }

    @Test
    fun `ciudad no encontrada no consulta el pronostico`() = runTest {
        val pronostico = FakeForecastRepository()
        val r = GetDailyForecastUseCase(
            FakeGeocodingRepository(resultado = null), pronostico
        )("Ciudadquenoexiste123")

        assertEquals(ForecastResult.CityNotFound, r)
        assertEquals(0, pronostico.llamadas)
    }

    @Test
    fun `el pronostico se pide con las coordenadas del geocoding`() = runTest {
        val pronostico = FakeForecastRepository()
        GetDailyForecastUseCase(FakeGeocodingRepository(), pronostico)("London,GB")

        assertEquals(LONDRES.latitude, pronostico.ultimaLatitud!!, 0.0)
        assertEquals(LONDRES.longitude, pronostico.ultimaLongitud!!, 0.0)
    }

    @Test
    fun `exito devuelve la ubicacion y los dias agregados`() = runTest {
        val serie = ForecastSeries(0, listOf(
            ForecastEntry(20_720L * 86_400L + 12 * 3_600L, 10.0, 20.0, 50, "clear sky", "01d")
        ))
        val r = GetDailyForecastUseCase(
            FakeGeocodingRepository(), FakeForecastRepository(resultado = serie)
        )("London,GB")

        val exito = r as ForecastResult.Success
        assertEquals(LONDRES, exito.location)
        assertEquals(1, exito.days.size)
    }

    @Test
    fun `key rechazada se traduce a Unauthorized`() = runTest {
        val r = GetDailyForecastUseCase(
            FakeGeocodingRepository(error = WeatherDataException.Unauthorized()),
            FakeForecastRepository()
        )("London,GB")
        assertEquals(ForecastResult.Unauthorized, r)
    }

    @Test
    fun `sin conexion en el pronostico se traduce a NoConnection`() = runTest {
        val r = GetDailyForecastUseCase(
            FakeGeocodingRepository(),
            FakeForecastRepository(error = WeatherDataException.NoConnection())
        )("London,GB")
        assertEquals(ForecastResult.NoConnection, r)
    }

    @Test
    fun `la cancelacion NO se convierte en error de dominio`() = runTest {
        val caso = GetDailyForecastUseCase(
            FakeGeocodingRepository(),
            FakeForecastRepository(error = CancellationException("pantalla cerrada"))
        )
        val lanzada = runCatching { caso("London,GB") }.exceptionOrNull()
        assertTrue("Se esperaba CancellationException, fue $lanzada", lanzada is CancellationException)
    }
}