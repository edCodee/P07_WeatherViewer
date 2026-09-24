package ec.edu.uta.fisei.weatherviewer.data.repository

import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmJsonParser
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmUrls
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import ec.edu.uta.fisei.weatherviewer.domain.repository.ForecastRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OwmForecastRepository(
    private val fetcher: HttpJsonFetcher,
    private val urls: OwmUrls,
    private val parser: OwmJsonParser = OwmJsonParser(),
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ForecastRepository {
    override suspend fun getForecast(latitude: Double, longitude: Double): ForecastSeries =
        withContext(io) {
            parser.parseForecast(fetcher.get(urls.forecast(latitude, longitude)))
        }
}