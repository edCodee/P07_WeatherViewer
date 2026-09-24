package ec.edu.uta.fisei.weatherviewer.domain.usecase

import ec.edu.uta.fisei.weatherviewer.domain.model.DailyForecast
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastEntry
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Convierte las lecturas cada 3 h del plan gratuito en un resumen por dia.
 * Reemplaza al endpoint forecast/daily del PDF, que ya no esta disponible
 * en el plan gratuito (CONTRATO.md, archivo 01).
 */
class AggregateDailyForecastUseCase {

    operator fun invoke(series: ForecastSeries): List<DailyForecast> {
        val offset = series.timezoneOffsetSeconds.toLong()

        val dias = series.entries
            .sortedBy { it.epochSeconds }
            .groupBy { Math.floorDiv(it.epochSeconds + offset, SEGUNDOS_POR_DIA) }
            .map { (epochDay, lecturas) -> resumir(epochDay, lecturas, offset) }

        // Hoy siempre se muestra; los demas dias solo si estan razonablemente completos
        return dias.filterIndexed { indice, dia ->
            indice == 0 || dia.readingsCount >= MIN_LECTURAS_POR_DIA
        }
    }

    private fun resumir(
        epochDay: Long,
        lecturas: List<ForecastEntry>,
        offset: Long
    ): DailyForecast {
        // minBy devuelve el PRIMER minimo: como la lista viene ordenada, un empate gana la mas temprana
        val representativa = lecturas.minBy { distanciaAlMediodia(it.epochSeconds + offset) }

        return DailyForecast(
            epochDay = epochDay,
            minTemp = lecturas.minOf { it.tempMin },
            maxTemp = lecturas.maxOf { it.tempMax },
            humidity = lecturas.map { it.humidity }.average().roundToInt(),
            description = representativa.description,
            iconCode = representativa.iconCode,
            readingsCount = lecturas.size
        )
    }

    private fun distanciaAlMediodia(segundosLocales: Long): Long =
        abs(Math.floorMod(segundosLocales, SEGUNDOS_POR_DIA) - MEDIODIA)

    companion object {
        const val SEGUNDOS_POR_DIA = 86_400L
        const val MEDIODIA = 43_200L
        /** Medio dia de lecturas cada 3 h. El primer dia (hoy) esta exento. */
        const val MIN_LECTURAS_POR_DIA = 4
    }
}