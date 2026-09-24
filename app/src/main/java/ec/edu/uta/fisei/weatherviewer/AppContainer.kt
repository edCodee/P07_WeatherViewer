package ec.edu.uta.fisei.weatherviewer

import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmUrls
import ec.edu.uta.fisei.weatherviewer.data.repository.OwmForecastRepository
import ec.edu.uta.fisei.weatherviewer.data.repository.OwmGeocodingRepository
import ec.edu.uta.fisei.weatherviewer.domain.usecase.GetDailyForecastUseCase

/**
 * Punto de composicion: el UNICO lugar donde se unen las capas.
 * La Fase 9 lo reemplazara por uno con repositorios falsos para Espresso.
 */
class AppContainer(apiKey: String) {
    val fetcher = HttpJsonFetcher()
    val urls = OwmUrls(apiKey)

    val getDailyForecast = GetDailyForecastUseCase(
        OwmGeocodingRepository(fetcher, urls),
        OwmForecastRepository(fetcher, urls)
    )
}