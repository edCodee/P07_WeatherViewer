package ec.edu.uta.fisei.weatherviewer.data.remote

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class OwmUrlsTest {

    private val urls = OwmUrls(apiKey = "clave-de-prueba")
    private val localeOriginal = Locale.getDefault()

    @After
    fun restaurarLocale() = Locale.setDefault(localeOriginal)

    @Test
    fun `por defecto apunta al servidor oficial por https`() {
        assertTrue(urls.geocoding("London").startsWith("https://api.openweathermap.org/"))
    }

    @Test
    fun `geocoding codifica espacios y comas`() {
        assertTrue(urls.geocoding("New York,US").contains("q=New+York%2CUS&"))
    }

    @Test
    fun `geocoding codifica acentos en UTF-8`() {
        assertTrue(urls.geocoding("Bogotá").contains("q=Bogot%C3%A1&"))
    }

    @Test
    fun `geocoding pide un solo resultado`() {
        assertTrue(urls.geocoding("London").contains("limit=1"))
    }

    @Test
    fun `forecast usa punto decimal aunque el telefono este en es-EC`() {
        Locale.setDefault(Locale.forLanguageTag("es-EC"))
        val url = urls.forecast(51.5074456, -0.1277653)
        assertTrue(url, url.contains("lat=51.5074456&lon=-0.1277653"))
    }

    @Test
    fun `forecast pide unidades metricas`() {
        assertTrue(urls.forecast(0.0, 0.0).contains("units=metric"))
    }

    @Test
    fun `el icono se pide por https en tamano 2x`() {
        assertEquals("https://openweathermap.org/img/wn/04d@2x.png", urls.icon("04d"))
    }
}