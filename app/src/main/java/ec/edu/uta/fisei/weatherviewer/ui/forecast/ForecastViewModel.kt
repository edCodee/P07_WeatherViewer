package ec.edu.uta.fisei.weatherviewer.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult
import ec.edu.uta.fisei.weatherviewer.domain.usecase.GetDailyForecastUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sobrevive a la recreacion de la Activity (rotacion, tema, idioma),
 * a diferencia del weatherList y el GetWeatherTask del PDF (bug #15).
 */
class ForecastViewModel(
    private val getDailyForecast: GetDailyForecastUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForecastUiState())
    val state: StateFlow<ForecastUiState> = _state.asStateFlow()

    private var busqueda: Job? = null

    fun onSearch(query: String) {
        // Una busqueda nueva invalida la anterior (misma idea que el ImageLoader)
        busqueda?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }

        busqueda = viewModelScope.launch {
            when (val resultado = getDailyForecast(query)) {
                is ForecastResult.Success -> _state.update {
                    it.copy(isLoading = false, location = resultado.location, days = resultado.days)
                }
                // Un error NO borra el pronostico que ya se mostraba
                is ForecastResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = resultado)
                }
            }
        }
    }

    fun onErrorShown() {
        _state.update { it.copy(error = null) }
    }

    companion object {
        fun factory(getDailyForecast: GetDailyForecastUseCase): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { ForecastViewModel(getDailyForecast) }
            }
    }
}