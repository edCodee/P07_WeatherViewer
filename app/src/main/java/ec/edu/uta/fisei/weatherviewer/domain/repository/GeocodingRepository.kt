package ec.edu.uta.fisei.weatherviewer.domain.repository

import ec.edu.uta.fisei.weatherviewer.domain.model.Location

interface GeocodingRepository {
    /**
     * @return la mejor coincidencia, o null si el servicio no encontro nada
     *         (responde 200 con [], CONTRATO.md archivo 04).
     * @throws ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
     */
    suspend fun findLocation(query: String): Location?
}