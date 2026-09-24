package ec.edu.uta.fisei.weatherviewer.ui.forecast

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ForecastErrorMessagesTest {

    @Test
    fun `cada fallo tiene su propio mensaje`() {
        val fallos = listOf(
            ForecastResult.EmptyQuery,
            ForecastResult.CityNotFound,
            ForecastResult.Unauthorized,
            ForecastResult.NoConnection,
            ForecastResult.ServiceUnavailable,
            ForecastResult.UnexpectedResponse
        )
        // El when sin else garantiza que TODOS tienen mensaje; esto, que no se repitan
        assertEquals(fallos.size, fallos.map { it.messageRes() }.toSet().size)
    }
}