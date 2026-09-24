package ec.edu.uta.fisei.weatherviewer.domain.model

/**
 * Fallos que la capa de datos puede reportar. Los define el DOMINIO, asi que
 * los repositorios hablan en terminos del negocio, no de HTTP ni de JSON.
 */
sealed class WeatherDataException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    /** 401: key invalida o plan sin acceso (CONTRATO.md, hallazgo 1). */
    class Unauthorized : WeatherDataException("Acceso no autorizado al servicio")

    class NoConnection(cause: Throwable? = null) :
        WeatherDataException("Sin conexion con el servicio", cause)

    class ServiceUnavailable(val httpCode: Int) :
        WeatherDataException("Servicio no disponible (HTTP $httpCode)")

    class UnexpectedResponse(cause: Throwable? = null) :
        WeatherDataException("Respuesta con formato inesperado", cause)
}

/** Lo que la UI recibe: nunca una excepcion, siempre un resultado. */
sealed interface ForecastResult {

    data class Success(val location: Location, val days: List<DailyForecast>) : ForecastResult

    sealed interface Failure : ForecastResult

    data object EmptyQuery : Failure
    data object CityNotFound : Failure
    data object Unauthorized : Failure
    data object NoConnection : Failure
    data object ServiceUnavailable : Failure
    data object UnexpectedResponse : Failure
}