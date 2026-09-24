package ec.edu.uta.fisei.weatherviewer.ui.forecast

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.domain.model.WeatherDataException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Descarga y decodifica fuera del hilo principal. Un icono que falla = fila sin icono, no crash. */
class BitmapDownloader(
    private val fetcher: HttpJsonFetcher,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(url: String): Bitmap? = withContext(io) {
        try {
            val bytes = fetcher.getBytes(url)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: WeatherDataException) {
            // Una fila sin icono no debe cerrar la app, pero el fallo debe quedar registrado
            Log.w(TAG, "No se pudo cargar el icono $url", e)
            null
        }
    }

    private companion object {
        const val TAG = "BitmapDownloader"
    }
}