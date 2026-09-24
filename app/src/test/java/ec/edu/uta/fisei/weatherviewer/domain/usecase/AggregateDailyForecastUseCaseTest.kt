package ec.edu.uta.fisei.weatherviewer.domain.usecase

import ec.edu.uta.fisei.weatherviewer.domain.model.DailyForecast
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastEntry
import ec.edu.uta.fisei.weatherviewer.domain.model.ForecastSeries
import org.junit.Assert.assertEquals
import org.junit.Test

class AggregateDailyForecastUseCaseTest {

    private val agregar = AggregateDailyForecastUseCase()

    private fun lectura(
        epochSeconds: Long,
        min: Double = 10.0,
        max: Double = 20.0,
        humedad: Int = 50,
        descripcion: String = "clear sky",
        icono: String = "01d"
    ) = ForecastEntry(epochSeconds, min, max, humedad, descripcion, icono)

    private fun serie(offset: Int, vararg lecturas: ForecastEntry) =
        ForecastSeries(offset, lecturas.toList())

    // --- Casos base ---

    @Test
    fun `sin lecturas no hay dias`() {
        assertEquals(emptyList<DailyForecast>(), agregar(ForecastSeries(0, emptyList())))
    }

    // --- Agrupacion por dia LOCAL (bug #12 del PDF) ---

    @Test
    fun `una lectura de las 23 UTC pertenece al dia siguiente en UTC+1`() {
        val r = agregar(serie(3600, lectura(INICIO_24_UTC + 23 * HORA)))
        assertEquals(DIA_24 + 1, r.single().epochDay)
    }

    @Test
    fun `una lectura de las 03 UTC pertenece al dia anterior en UTC-5`() {
        val r = agregar(serie(-5 * 3600, lectura(INICIO_24_UTC + 3 * HORA)))
        assertEquals(DIA_24 - 1, r.single().epochDay)
    }

    // --- Resumen del dia ---

    @Test
    fun `minimo y maximo se calculan entre todas las lecturas del dia`() {
        // Valores reales de las primeras lecturas de docs/api/03
        val r = agregar(serie(3600,
            lectura(INICIO_24_UTC + 3 * HORA, min = 12.0, max = 12.55),
            lectura(INICIO_24_UTC + 6 * HORA, min = 11.47, max = 11.92),
            lectura(INICIO_24_UTC + 15 * HORA, min = 22.47, max = 22.47)
        ))
        assertEquals(11.47, r.single().minTemp, 0.0)
        assertEquals(22.47, r.single().maxTemp, 0.0)
    }

    @Test
    fun `la humedad es el promedio redondeado hacia arriba en el punto medio`() {
        val r = agregar(serie(0,
            lectura(INICIO_24_UTC + 3 * HORA, humedad = 81),
            lectura(INICIO_24_UTC + 6 * HORA, humedad = 84)
        ))
        assertEquals(83, r.single().humidity) // 82.5 -> 83
    }

    @Test
    fun `icono y descripcion salen de la lectura mas cercana al mediodia local`() {
        val r = agregar(serie(0,
            lectura(INICIO_24_UTC + 3 * HORA, descripcion = "overcast clouds", icono = "04n"),
            lectura(INICIO_24_UTC + 12 * HORA, descripcion = "clear sky", icono = "01d"),
            lectura(INICIO_24_UTC + 21 * HORA, descripcion = "light rain", icono = "10n")
        ))
        assertEquals("01d", r.single().iconCode)
        assertEquals("clear sky", r.single().description)
    }

    @Test
    fun `ante un empate gana la lectura mas temprana`() {
        // 10:30 y 13:30 estan ambas a 1.5 h del mediodia; llegan en orden inverso
        val r = agregar(serie(0,
            lectura(INICIO_24_UTC + 13 * HORA + 1800, icono = "tarde"),
            lectura(INICIO_24_UTC + 10 * HORA + 1800, icono = "manana")
        ))
        assertEquals("manana", r.single().iconCode)
    }

    // --- Regla del dia incompleto (CONTRATO.md, hallazgo 6) ---

    @Test
    fun `el primer dia se muestra aunque tenga una sola lectura`() {
        val r = agregar(serie(0, lectura(INICIO_24_UTC + 21 * HORA)))
        assertEquals(1, r.single().readingsCount)
    }

    @Test
    fun `un dia posterior con menos de 4 lecturas se descarta`() {
        val hoy = (0 until 8).map { lectura(INICIO_24_UTC + it * 3 * HORA) }
        val manana = (0 until 3).map { lectura(INICIO_24_UTC + 24 * HORA + it * 3 * HORA) }
        val r = agregar(ForecastSeries(0, hoy + manana))
        assertEquals(listOf(DIA_24), r.map { it.epochDay })
    }

    @Test
    fun `un dia posterior con exactamente 4 lecturas se conserva`() {
        val hoy = (0 until 8).map { lectura(INICIO_24_UTC + it * 3 * HORA) }
        val manana = (0 until 4).map { lectura(INICIO_24_UTC + 24 * HORA + it * 3 * HORA) }
        val r = agregar(ForecastSeries(0, hoy + manana))
        assertEquals(listOf(DIA_24, DIA_24 + 1), r.map { it.epochDay })
    }

    @Test
    fun `los dias salen ordenados aunque las lecturas lleguen desordenadas`() {
        val manana = (0 until 4).map { lectura(INICIO_24_UTC + 24 * HORA + it * 3 * HORA) }
        val hoy = listOf(lectura(INICIO_24_UTC + 12 * HORA))
        val r = agregar(ForecastSeries(0, manana + hoy))
        assertEquals(listOf(DIA_24, DIA_24 + 1), r.map { it.epochDay })
    }

    // --- Escenario real ---

    @Test
    fun `escenario real de Londres - 40 lecturas quedan en 5 dias`() {
        // docs/api/03: primera lectura 1790218800 (2026-09-24 03:00 UTC), paso 3 h, timezone 3600
        val lecturas = (0 until 40).map { lectura(1_790_218_800L + it * 3 * HORA) }
        val r = agregar(ForecastSeries(3600, lecturas))
        assertEquals(listOf(7, 8, 8, 8, 8), r.map { it.readingsCount })
        assertEquals((DIA_24..DIA_24 + 4).toList(), r.map { it.epochDay })
    }

    companion object {
        /** 2026-09-24 en dias desde 1970-01-01 */
        const val DIA_24 = 20_720L
        /** 2026-09-24 00:00 UTC en segundos */
        const val INICIO_24_UTC = DIA_24 * 86_400L
        const val HORA = 3_600L
    }
}