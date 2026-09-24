package ec.edu.uta.fisei.weatherviewer.ui.forecast

import ec.edu.uta.fisei.weatherviewer.domain.model.DailyForecast
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult
import ec.edu.uta.fisei.weatherviewer.domain.model.Location

/** Todo lo que la pantalla necesita para dibujarse, en un solo valor. */
data class ForecastUiState(
    val isLoading: Boolean = false,
    val location: Location? = null,
    val days: List<DailyForecast> = emptyList(),
    /** Error pendiente de mostrar. La UI lo muestra una vez y llama a onErrorShown(). */
    val error: ForecastResult.Failure? = null
)