package ec.edu.uta.fisei.weatherviewer.domain.repository

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries

interface ForecastRepository {
    /** @throws ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException */
    suspend fun getForecast(latitude: Double, longitude: Double): ForecastSeries
}