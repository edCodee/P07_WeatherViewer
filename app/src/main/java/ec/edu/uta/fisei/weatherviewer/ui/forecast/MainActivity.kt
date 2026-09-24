package ec.edu.uta.fisei.weatherviewer.ui.forecast

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import ec.edu.uta.fisei.weatherviewer.BuildConfig
import ec.edu.uta.fisei.weatherviewer.core.IconCache
import ec.edu.uta.fisei.weatherviewer.core.ImageLoader
import ec.edu.uta.fisei.weatherviewer.data.remote.HttpJsonFetcher
import ec.edu.uta.fisei.weatherviewer.data.remote.OwmUrls
import ec.edu.uta.fisei.weatherviewer.databinding.ActivityMainBinding
import ec.edu.uta.fisei.weatherviewer.domain.model.DailyForecast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WeatherArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        configurarLista()
        aplicarInsets()
        mostrarDatosDeMuestra() // TEMPORAL: se borra en la Fase 6
    }

    private fun configurarLista() {
        val fetcher = HttpJsonFetcher()
        val urls = OwmUrls(BuildConfig.OWM_API_KEY)
        val descargar = BitmapDownloader(fetcher)

        val iconLoader = ImageLoader<ImageView, Bitmap>(
            scope = lifecycleScope,               // se cancela solo al destruir la Activity
            cache = IconCache(maxEntries = 32),
            download = { url -> descargar(url) },
            show = { imageView, bitmap -> imageView.setImageBitmap(bitmap) }
        )

        adapter = WeatherArrayAdapter(this, ForecastRowFormatter(), iconLoader, urls::icon)
        binding.weatherListView.adapter = adapter
        binding.weatherListView.emptyView = binding.emptyTextView
    }

    /** TEMPORAL (Fase 5): 30 filas para forzar el reciclado del ListView. */
    private fun mostrarDatosDeMuestra() {
        val climas = listOf(
            "01d" to "clear sky", "02d" to "few clouds", "03d" to "scattered clouds",
            "04d" to "broken clouds", "09d" to "shower rain", "10d" to "rain",
            "11d" to "thunderstorm", "13d" to "snow", "50d" to "mist"
        )
        val dias = (0 until 30).map { i ->
            val (icono, descripcion) = climas[i % climas.size]
            DailyForecast(20_720L + i, 10.0 + i, 20.0 + i, 40 + i, descripcion, icono, 8)
        }
        adapter.submit(dias)
    }

    private fun aplicarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinatorLayout) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            binding.topAppBar.updatePadding(top = barras.top)
            binding.contentLayout.updatePadding(left = barras.left, right = barras.right)
            binding.weatherListView.updatePadding(bottom = maxOf(barras.bottom, teclado.bottom))
            insets
        }
    }
}