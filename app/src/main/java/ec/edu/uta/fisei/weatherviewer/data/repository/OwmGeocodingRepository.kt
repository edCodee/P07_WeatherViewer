package ec.edu.uta.fisei.weatherviewer.data.repository

import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmJsonParser
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmUrls
import ec.edu.uta.fisei.weatherviewer.domain.model.Location
import ec.edu.uta.fisei.weatherviewer.domain.repository.GeocodingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OwmGeocodingRepository(
    private val fetcher: HttpJsonFetcher,
    private val urls: OwmUrls,
    private val parser: OwmJsonParser = OwmJsonParser(),
    private val io: CoroutineDispatcher = Dispatchers.IO
) : GeocodingRepository {
    override suspend fun findLocation(query: String): Location? = withContext(io) {
        parser.parseGeocoding(fetcher.get(urls.geocoding(query)))
    }
}