package ec.edu.uta.fisei.weatherviewer.ui.forecast

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class ForecastRowFormatterTest {

    private val us = ForecastRowFormatter(Locale.US)
    private val zonaOriginal = TimeZone.getDefault()

    @After
    fun restaurarZona() = TimeZone.setDefault(zonaOriginal)

    // --- Dia de la semana (epochDay de la Fase 2) ---

    @Test
    fun `el dia 20720 es Thursday en ingles`() {
        assertEquals("Thursday", us.dayName(20_720L))
    }

    @Test
    fun `en espanol el dia empieza con mayuscula`() {
        assertEquals("Jueves", ForecastRowFormatter(Locale.forLanguageTag("es-EC")).dayName(20_720L))
    }

    @Test
    fun `el nombre del dia no depende de la zona horaria del telefono`() {
        // En UTC-5, aplicar la zona del telefono (bug #12 del PDF) daria Wednesday
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guayaquil"))
        assertEquals("Thursday", ForecastRowFormatter(Locale.US).dayName(20_720L))
    }

    // --- Temperatura (dominio en grados Celsius) ---

    @Test
    fun `la temperatura se redondea a entero`() {
        assertEquals("11°C", us.temperature(11.47))
    }

    @Test
    fun `el punto medio redondea hacia arriba`() {
        // NumberFormat usa HALF_EVEN por defecto: formatearia 22.5 como "22"
        assertEquals("23°C", us.temperature(22.5))
    }

    @Test
    fun `nunca muestra menos cero`() {
        // NumberFormat sin decimales formatea -0.4 como "-0"
        assertEquals("0°C", us.temperature(-0.4))
    }

    // --- Humedad ---

    @Test
    fun `la humedad se formatea como porcentaje`() {
        assertEquals("65%", us.humidity(65))
    }

    @Test
    fun `la humedad respeta el locale`() {
        // El frances SIEMPRE separa el % con espacio ("65 %").
        // es-EC no sirve de ejemplo: en CLDR escribe "65%" igual que el ingles.
        val francia = ForecastRowFormatter(Locale.FRANCE)
        assertNotEquals(us.humidity(65), francia.humidity(65))
    }
}