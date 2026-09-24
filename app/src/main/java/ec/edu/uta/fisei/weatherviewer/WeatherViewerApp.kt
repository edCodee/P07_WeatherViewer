package ec.edu.uta.fisei.weatherviewer

import android.app.Application

class WeatherViewerApp : Application() {
    val container: AppContainer by lazy { AppContainer(BuildConfig.OWM_API_KEY) }
}