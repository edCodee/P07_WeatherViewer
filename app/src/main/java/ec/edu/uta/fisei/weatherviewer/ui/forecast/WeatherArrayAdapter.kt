package ec.edu.uta.fisei.weatherviewer.ui.forecast

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import ec.edu.uta.fisei.weatherviewer.R
import ec.edu.uta.fisei.weatherviewer.core.ImageLoader
import ec.edu.uta.fisei.weatherviewer.databinding.ListItemBinding
import ec.edu.uta.fisei.weatherviewer.domain.model.DailyForecast

/**
 * PDF 7.6: ArrayAdapter con el patron ViewHolder.
 * El ArrayAdapter guarda su propia lista; se actualiza con submit().
 */
class WeatherArrayAdapter(
    context: Context,
    private val formatter: ForecastRowFormatter,
    private val iconLoader: ImageLoader<ImageView, Bitmap>,
    private val iconUrl: (String) -> String
) : ArrayAdapter<DailyForecast>(context, 0) {

    /** PDF 7.6.2. ViewBinding ya cachea las referencias: el holder lo envuelve. */
    private class ViewHolder(val binding: ListItemBinding)

    fun submit(days: List<DailyForecast>) {
        setNotifyOnChange(false)
        clear()
        addAll(days)
        notifyDataSetChanged()   // una sola notificacion, no dos
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            // No hay fila para reciclar: inflar una y guardar su holder como tag
            val binding = ListItemBinding.inflate(LayoutInflater.from(context), parent, false)
            holder = ViewHolder(binding)
            view = binding.root
            view.tag = holder
        } else {
            // Fila reciclada: su holder ya tiene las referencias
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        bind(holder.binding, getItem(position)!!)
        return view
    }

    private fun bind(fila: ListItemBinding, dia: DailyForecast) {
        fila.dayTextView.text = context.getString(
            R.string.day_description, formatter.dayName(dia.epochDay), dia.description
        )
        fila.lowTextView.text = context.getString(R.string.low_temp, formatter.temperature(dia.minTemp))
        fila.hiTextView.text = context.getString(R.string.high_temp, formatter.temperature(dia.maxTemp))
        fila.humidityTextView.text = context.getString(R.string.humidity, formatter.humidity(dia.humidity))
        iconLoader.load(fila.conditionImageView, iconUrl(dia.iconCode))
    }
}