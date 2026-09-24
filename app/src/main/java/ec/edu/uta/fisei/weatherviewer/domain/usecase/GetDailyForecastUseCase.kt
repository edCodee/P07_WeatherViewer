package ec.edu.uta.fisei.weatherviewer.domain.usecase

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastResult
import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import ec.edu.uta.fisei.weatherviewer.domain.repository.ForecastRepository
import ec.edu.uta.fisei.weatherviewer.domain.repository.GeocodingRepository

/**
 * Ciudad -> coordenadas -> lecturas -> dias.
 * Nunca lanza errores de negocio: los devuelve como ForecastResult.Failure.
 */
class GetDailyForecastUseCase(
    private val geocoding: GeocodingRepository,
    private val forecast: ForecastRepository,
    private val aggregate: AggregateDailyForecastUseCase = AggregateDailyForecastUseCase()
) {

    suspend operator fun invoke(query: String): ForecastResult {
        val ciudad = query.trim()
        if (ciudad.isEmpty()) return ForecastResult.EmptyQuery

        return try {
            val location = geocoding.findLocation(ciudad)
                ?: return ForecastResult.CityNotFound
            val series = forecast.getForecast(location.latitude, location.longitude)
            ForecastResult.Success(location, aggregate(series))
        } catch (e: WeatherDataException) {
            // SOLO nuestro tipo: una CancellationException debe seguir de largo
            e.toFailure()
        }
    }

    private fun WeatherDataException.toFailure(): ForecastResult.Failure = when (this) {
        is WeatherDataException.Unauthorized -> ForecastResult.Unauthorized
        is WeatherDataException.NoConnection -> ForecastResult.NoConnection
        is WeatherDataException.ServiceUnavailable -> ForecastResult.ServiceUnavailable
        is WeatherDataException.UnexpectedResponse -> ForecastResult.UnexpectedResponse
    }
}