package ec.edu.uta.fisei.weatherviewer.ui.forecast

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ec.edu.uta.fisei.weatherviewer.AppContainer
import ec.edu.uta.fisei.weatherviewer.R
import ec.edu.uta.fisei.weatherviewer.WeatherViewerApp
import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmUrls
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastEntry
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import ec.edu.uta.fisei.weatherviewer.domain.model.Location
import ec.edu.uta.fisei.weatherviewer.domain.repository.ForecastRepository
import ec.edu.uta.fisei.weatherviewer.domain.repository.GeocodingRepository
import ec.edu.uta.fisei.weatherviewer.domain.usecase.GetDailyForecastUseCase
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith

/**
 * La app completa en el emulador, SIN red ni API key: el AppContainer
 * se reemplaza por uno con repositorios falsos antes de abrir la Activity.
 * Nombres con guion bajo: DEX no admite espacios con minSdk < 30.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private val app = ApplicationProvider.getApplicationContext<WeatherViewerApp>()

    /** ciudad = null simula el [] del geocoding (CONTRATO.md, archivo 04). */
    private fun instalarContenedorFalso(ciudad: Location?) {
        app.container = AppContainer(
            fetcher = HttpJsonFetcher(),
            urls = OwmUrls(apiKey = "sin-uso-en-tests"),
            getDailyForecast = GetDailyForecastUseCase(
                geocoding = object : GeocodingRepository {
                    override suspend fun findLocation(query: String) = ciudad
                },
                forecast = object : ForecastRepository {
                    override suspend fun getForecast(latitude: Double, longitude: Double) = SERIE
                }
            )
        )
    }

    private fun buscar(texto: String) {
        onView(withId(R.id.locationEditText)).perform(replaceText(texto), closeSoftKeyboard())
        onView(withId(R.id.fab)).perform(click())
    }

    private fun verificarPrimeraFila(contiene: String) {
        onData(anything())
            .inAdapterView(withId(R.id.weatherListView))
            .atPosition(0)
            .onChildView(withId(R.id.dayTextView))
            .check(matches(withText(containsString(contiene))))
    }

    @Test
    fun buscar_una_ciudad_muestra_la_ciudad_resuelta_y_sus_dias() {
        instalarContenedorFalso(QUITO)
        ActivityScenario.launch(MainActivity::class.java).use {
            buscar("quito")

            // "quito" en el campo, "Quito, EC" en la toolbar: el texto es unico
            onView(withText("Quito, EC")).check(matches(isDisplayed()))
            verificarPrimeraFila("lluvia ligera")
        }
    }

    @Test
    fun una_ciudad_inexistente_muestra_el_snackbar_de_error() {
        instalarContenedorFalso(null)
        ActivityScenario.launch(MainActivity::class.java).use {
            buscar("Ciudadquenoexiste123")

            onView(withText(R.string.error_city_not_found)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun recrear_la_activity_conserva_el_pronostico() {
        instalarContenedorFalso(QUITO)
        ActivityScenario.launch(MainActivity::class.java).use { escenario ->
            buscar("quito")

            escenario.recreate()   // lo mismo que rotar o cambiar el tema: bug #15 del PDF

            onView(withText("Quito, EC")).check(matches(isDisplayed()))
            verificarPrimeraFila("lluvia ligera")
        }
    }

    private companion object {
        val QUITO = Location("Quito", "EC", -0.2, -78.5)

        val SERIE = ForecastSeries(0, listOf(
            ForecastEntry(20_720L * 86_400L + 12 * 3_600L, 9.0, 14.0, 89, "lluvia ligera", "10d")
        ))
    }
}