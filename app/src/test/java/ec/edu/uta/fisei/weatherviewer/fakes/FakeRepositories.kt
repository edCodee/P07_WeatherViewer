package ec.edu.uta.fisei.weatherviewer.fakes

import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import ec.edu.uta.fisei.weatherviewer.domain.model.Location
import ec.edu.uta.fisei.weatherviewer.domain.repository.ForecastRepository
import ec.edu.uta.fisei.weatherviewer.domain.repository.GeocodingRepository

/** Coordenadas reales de docs/api/02-geocoding-ok.json */
val LONDRES = Location(
    name = "London",
    country = "GB",
    latitude = 51.5074456,
    longitude = -0.1277653
)

class FakeGeocodingRepository(
    private val resultado: Location? = LONDRES,
    private val error: Throwable? = null
) : GeocodingRepository {

    val consultas = mutableListOf<String>()

    override suspend fun findLocation(query: String): Location? {
        consultas += query
        error?.let { throw it }
        return resultado
    }
}

class FakeForecastRepository(
    private val resultado: ForecastSeries = ForecastSeries(0, emptyList()),
    private val error: Throwable? = null
) : ForecastRepository {

    var llamadas = 0
        private set
    var ultimaLatitud: Double? = null
        private set
    var ultimaLongitud: Double? = null
        private set

    override suspend fun getForecast(latitude: Double, longitude: Double): ForecastSeries {
        llamadas++
        ultimaLatitud = latitude
        ultimaLongitud = longitude
        error?.let { throw it }
        return resultado
    }
}