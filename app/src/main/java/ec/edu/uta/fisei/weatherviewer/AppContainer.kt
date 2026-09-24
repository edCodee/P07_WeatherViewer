package ec.edu.uta.fisei.weatherviewer

import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmUrls
import ec.edu.uta.fisei.weatherviewer.data.repository.OwmForecastRepository
import ec.edu.uta.fisei.weatherviewer.data.repository.OwmGeocodingRepository
import ec.edu.uta.fisei.weatherviewer.domain.usecase.GetDailyForecastUseCase

/**
 * Punto de composicion: el UNICO lugar donde se unen las capas.
 * Recibe sus piezas por constructor para que las pruebas instrumentadas
 * puedan armar uno con repositorios falsos.
 */
class AppContainer(
    val fetcher: HttpJsonFetcher,
    val urls: OwmUrls,
    val getDailyForecast: GetDailyForecastUseCase
) {
    companion object {
        fun production(apiKey: String): AppContainer {
            val fetcher = HttpJsonFetcher()
            val urls = OwmUrls(apiKey)
            return AppContainer(
                fetcher = fetcher,
                urls = urls,
                getDailyForecast = GetDailyForecastUseCase(
                    OwmGeocodingRepository(fetcher, urls),
                    OwmForecastRepository(fetcher, urls)
                )
            )
        }
    }
}