package ec.edu.uta.fisei.weatherviewer.ui.forecast

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastEntry
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import ec.edu.uta.fisei.weatherviewer.domain.model.Location
import ec.edu.uta.fisei.weatherviewer.domain.repository.GeocodingRepository
import ec.edu.uta.fisei.weatherviewer.domain.usecase.GetDailyForecastUseCase
import ec.edu.uta.fisei.weatherviewer.fakes.FakeForecastRepository
import ec.edu.uta.fisei.weatherviewer.fakes.FakeGeocodingRepository
import ec.edu.uta.fisei.weatherviewer.fakes.LONDRES
import ec.edu.uta.fisei.weatherviewer.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val serieDeUnDia = ForecastSeries(0, listOf(
        ForecastEntry(20_720L * 86_400L + 12 * 3_600L, 10.0, 20.0, 50, "clear sky", "01d")
    ))

    /** Geocoding cuyas respuestas llegan cuando el test decide. */
    private class GeocodingControlado : GeocodingRepository {
        val pendientes = mutableMapOf<String, CompletableDeferred<Location?>>()

        override suspend fun findLocation(query: String): Location? =
            pendientes.getOrPut(query) { CompletableDeferred() }.await()
    }

    private fun viewModel(geocoding: GeocodingRepository = FakeGeocodingRepository()) =
        ForecastViewModel(
            GetDailyForecastUseCase(geocoding, FakeForecastRepository(resultado = serieDeUnDia))
        )

    @Test
    fun `al iniciar no hay datos, ni carga, ni error`() {
        assertEquals(ForecastUiState(), viewModel().state.value)
    }

    @Test
    fun `mientras busca muestra la carga y al terminar la quita`() = runTest {
        val geo = GeocodingControlado()
        val vm = viewModel(geo)

        vm.onSearch("London,GB"); runCurrent()
        assertTrue(vm.state.value.isLoading)

        geo.pendientes.getValue("London,GB").complete(LONDRES); runCurrent()
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `una busqueda exitosa entrega la ciudad y sus dias`() = runTest {
        val vm = viewModel()

        vm.onSearch("London,GB"); advanceUntilIdle()

        assertEquals(LONDRES, vm.state.value.location)
        assertEquals(1, vm.state.value.days.size)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `una ciudad inexistente queda como error pendiente`() = runTest {
        val vm = viewModel(FakeGeocodingRepository(resultado = null))

        vm.onSearch("Ciudadquenoexiste123"); advanceUntilIdle()

        assertEquals(ForecastResult.CityNotFound, vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `un error no borra el pronostico que ya se mostraba`() = runTest {
        val geo = GeocodingControlado()
        val vm = viewModel(geo)

        vm.onSearch("London,GB"); runCurrent()
        geo.pendientes.getValue("London,GB").complete(LONDRES); runCurrent()

        vm.onSearch("Nada"); runCurrent()
        geo.pendientes.getValue("Nada").complete(null); runCurrent()

        assertEquals(ForecastResult.CityNotFound, vm.state.value.error)
        assertEquals(LONDRES, vm.state.value.location)
        assertEquals(1, vm.state.value.days.size)
    }

    @Test
    fun `el error se consume una sola vez`() = runTest {
        val vm = viewModel(FakeGeocodingRepository(resultado = null))

        vm.onSearch("x"); advanceUntilIdle()
        vm.onErrorShown()

        assertNull(vm.state.value.error)
    }

    @Test
    fun `una busqueda nueva cancela la anterior - el resultado viejo no pisa al nuevo`() = runTest {
        val geo = GeocodingControlado()
        val vm = viewModel(geo)

        vm.onSearch("Quito,EC"); runCurrent()
        vm.onSearch("London,GB"); runCurrent()   // el usuario cambia de idea

        geo.pendientes.getValue("London,GB").complete(LONDRES); runCurrent()
        geo.pendientes.getValue("Quito,EC").complete(QUITO); runCurrent()   // Quito llega TARDE

        assertEquals(LONDRES, vm.state.value.location)
    }

    private companion object {
        /** Coordenadas aproximadas: en este test no importan. */
        val QUITO = Location("Quito", "EC", -0.2, -78.5)
    }
}