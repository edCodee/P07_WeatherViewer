package ec.edu.uta.fisei.weatherviewer.domain.model

/** Ciudad resuelta por el geocoding. */
data class Location(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

/** Una lectura del pronostico cada 3 horas. Temperaturas en grados Celsius. */
data class ForecastEntry(
    val epochSeconds: Long,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val description: String,
    val iconCode: String
)

/** Lecturas tal como las entrega el servicio, mas el desfase horario de la CIUDAD. */
data class ForecastSeries(
    val timezoneOffsetSeconds: Int,
    val entries: List<ForecastEntry>
)

/** Resumen de un dia local de la ciudad. */
data class DailyForecast(
    /** Dia local en dias desde 1970-01-01. La UI lo convierte en "Monday", "lunes"... */
    val epochDay: Long,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Int,
    val description: String,
    val iconCode: String,
    val readingsCount: Int
)