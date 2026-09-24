package ec.edu.uta.fisei.weatherviewer.ui.forecast

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Convierte los numeros del dominio en texto segun el locale.
 * El locale se recibe por parametro (se evalua al construir, como el
 * MoneyFormatter del TipCalculator): si el usuario cambia de idioma,
 * la Activity se recrea y se construye un formatter nuevo.
 * SimpleDateFormat no es thread-safe: usar solo desde el hilo principal.
 */
class ForecastRowFormatter(private val locale: Locale = Locale.getDefault()) {

    // UTC a proposito: epochDay YA es el dia local de la ciudad (Fase 2).
    // Aplicar la zona del telefono encima repetiria el bug #12 del PDF.
    private val nombreDelDia = SimpleDateFormat("EEEE", locale).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val entero = NumberFormat.getIntegerInstance(locale)
    private val porcentaje = NumberFormat.getPercentInstance(locale)

    fun dayName(epochDay: Long): String =
        nombreDelDia.format(Date(epochDay * MILLIS_POR_DIA))
            .replaceFirstChar { it.titlecase(locale) }

    /** Redondea ANTES de formatear: evita el "-0" y el HALF_EVEN de NumberFormat. */
    fun temperature(celsius: Double): String = "${entero.format(celsius.roundToInt())}°C"

    fun humidity(percent: Int): String = porcentaje.format(percent / 100.0)

    private companion object {
        const val MILLIS_POR_DIA = 86_400_000L
    }
}