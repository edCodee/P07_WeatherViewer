package ec.edu.uta.fisei.weatherviewer

import android.app.Application
import androidx.annotation.VisibleForTesting

class WeatherViewerApp : Application() {

    private var contenedor: AppContainer? = null

    /**
     * Se crea la primera vez que se pide. El setter existe SOLO para que las
     * pruebas instrumentadas instalen uno falso antes de abrir la Activity;
     * lint marca como error cualquier uso desde codigo de produccion.
     */
    var container: AppContainer
        get() = contenedor ?: AppContainer.production(BuildConfig.OWM_API_KEY).also { contenedor = it }
        @VisibleForTesting set(value) {
            contenedor = value
        }
}