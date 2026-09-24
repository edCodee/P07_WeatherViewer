package ec.edu.uta.fisei.weatherviewer.data.remote

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastEntry
import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import ec.edu.uta.fisei.weatherviewer.domain.usecase.AggregateDailyForecastUseCase
import ec.edu.uta.fisei.weatherviewer.fakes.LONDRES
import ec.edu.uta.fisei.weatherviewer.testutil.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class OwmJsonParserTest {

    private val parser = OwmJsonParser()
    private val forecastReal get() = Fixtures.read("03-forecast-5d.json")

    // --- Geocoding (CONTRATO.md archivos 02, 04 y 05) ---

    @Test
    fun `geocoding real devuelve Londres con sus coordenadas exactas`() {
        assertEquals(LONDRES, parser.parseGeocoding(Fixtures.read("02-geocoding-ok.json")))
    }

    @Test
    fun `geocoding vacio devuelve null y no un error`() {
        assertNull(parser.parseGeocoding(Fixtures.read("04-geocoding-no-existe.json")))
    }

    @Test
    fun `un cuerpo de error en lugar del arreglo es respuesta inesperada`() {
        assertThrows(WeatherDataException.UnexpectedResponse::class.java) {
            parser.parseGeocoding(Fixtures.read("05-key-invalida.json"))
        }
    }

    // --- Forecast (CONTRATO.md archivo 03) ---

    @Test
    fun `forecast real trae 40 lecturas y el timezone de la ciudad`() {
        val serie = parser.parseForecast(forecastReal)
        assertEquals(40, serie.entries.size)
        assertEquals(3600, serie.timezoneOffsetSeconds)
    }

    @Test
    fun `la primera lectura real se mapea campo por campo`() {
        // "temp_min":12 viene como entero en el JSON: getDouble debe tolerarlo
        assertEquals(
            ForecastEntry(1_790_218_800L, 12.0, 12.55, 81, "overcast clouds", "04n"),
            parser.parseForecast(forecastReal).entries.first()
        )
    }

    @Test
    fun `la ultima lectura real con lluvia tambien se mapea`() {
        // Esta lectura trae el campo opcional "rain" (hallazgo 5): no debe estorbar
        assertEquals(
            ForecastEntry(1_790_640_000L, 16.96, 16.96, 97, "moderate rain", "10n"),
            parser.parseForecast(forecastReal).entries.last()
        )
    }

    @Test
    fun `JSON truncado es respuesta inesperada`() {
        assertThrows(WeatherDataException.UnexpectedResponse::class.java) {
            parser.parseForecast(forecastReal.take(500))
        }
    }

    @Test
    fun `el cuerpo del 401 no trae list y es respuesta inesperada`() {
        assertThrows(WeatherDataException.UnexpectedResponse::class.java) {
            parser.parseForecast(Fixtures.read("05-key-invalida.json"))
        }
    }

    // --- El dominio de la Fase 2 sobre datos reales ---

    @Test
    fun `los datos reales de Londres se agregan en 5 dias`() {
        val dias = AggregateDailyForecastUseCase()(parser.parseForecast(forecastReal))

        assertEquals(listOf(7, 8, 8, 8, 8), dias.map { it.readingsCount })

        val hoy = dias.first()
        assertEquals(11.47, hoy.minTemp, 0.0)   // lectura de las 06:00 UTC
        assertEquals(22.47, hoy.maxTemp, 0.0)   // lectura de las 15:00 UTC
        assertEquals(65, hoy.humidity)          // (81+83+63+48+45+58+76)/7 = 64.86
        assertEquals("04d", hoy.iconCode)       // 12:00 UTC = 13:00 local, la mas cercana al mediodia
    }
}