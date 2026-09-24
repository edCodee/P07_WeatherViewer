package ec.edu.uta.fisei.weatherviewer.ui.forecast

import androidx.annotation.StringRes
import ec.edu.uta.fisei.weatherviewer.R
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult

/** Sin else: un fallo nuevo en el dominio no compila hasta tener su mensaje. */
@StringRes
fun ForecastResult.Failure.messageRes(): Int = when (this) {
    ForecastResult.EmptyQuery -> R.string.error_empty_query
    ForecastResult.CityNotFound -> R.string.error_city_not_found
    ForecastResult.Unauthorized -> R.string.error_unauthorized
    ForecastResult.NoConnection -> R.string.error_no_connection
    ForecastResult.ServiceUnavailable -> R.string.error_service_unavailable
    ForecastResult.UnexpectedResponse -> R.string.error_unexpected_response
}