package ec.edu.uta.fisei.weatherviewer.ui.forecast

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import ec.edu.uta.fisei.weatherviewer.R
import ec.edu.uta.fisei.weatherviewer.WeatherViewerApp
import ec.edu.uta.fisei.weatherviewer.core.IconCache
import ec.edu.uta.fisei.weatherviewer.core.ImageLoader
import ec.edu.uta.fisei.weatherviewer.databinding.ActivityMainBinding
import ec.edu.uta.fisei.weatherviewer.domain.model.DailyForecast
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WeatherArrayAdapter

    private val container by lazy { (application as WeatherViewerApp).container }
    private val viewModel: ForecastViewModel by viewModels {
        ForecastViewModel.factory(container.getDailyForecast)
    }

    /** Ultima lista entregada al adapter: evita re-enviarla en cada cambio de estado. */
    private var diasMostrados: List<DailyForecast>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        configurarLista()
        configurarBusqueda()
        aplicarInsets()
        observarEstado()
        // PDF Fig. 7.2: foco y teclado al abrir, pero NO al rotar ni al cambiar de tema
        if (savedInstanceState == null) mostrarTecladoInicial()
    }

    private fun configurarLista() {
        val descargar = BitmapDownloader(container.fetcher)
        val iconLoader = ImageLoader<ImageView, Bitmap>(
            scope = lifecycleScope,
            cache = IconCache(maxEntries = 32),
            download = { url -> descargar(url) },
            show = { imageView, bitmap -> imageView.setImageBitmap(bitmap) }
        )
        adapter = WeatherArrayAdapter(this, ForecastRowFormatter(), iconLoader, container.urls::icon)
        binding.weatherListView.adapter = adapter
        binding.weatherListView.emptyView = binding.emptyTextView
    }

    /** El FAB del PDF y, ademas, la tecla de busqueda del teclado. */
    private fun configurarBusqueda() {
        binding.fab.setOnClickListener { buscar() }
        binding.locationEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscar()
                true
            } else {
                false
            }
        }
    }

    private fun buscar() {
        // PDF 7.7.4 usaba InputMethodManager con cast; hoy se pide al controlador de insets
        WindowCompat.getInsetsController(window, binding.root).hide(WindowInsetsCompat.Type.ime())
        binding.locationEditText.clearFocus()
        viewModel.onSearch(binding.locationEditText.text?.toString().orEmpty())
    }

    /** STARTED: no se pinta ni se muestran Snackbars con la app en segundo plano. */
    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    /** Todo lo visible sale del estado. Aqui no se calcula nada. */
    private fun render(state: ForecastUiState) {
        binding.loadingIndicator.isVisible = state.isLoading
        binding.fab.isEnabled = !state.isLoading
        supportActionBar?.subtitle = state.location?.let {
            getString(R.string.location_label, it.name, it.country)
        }

        if (state.days !== diasMostrados) {
            diasMostrados = state.days
            adapter.submit(state.days)
            binding.weatherListView.smoothScrollToPosition(0)   // PDF 7.7.5, linea 158
        }

        state.error?.let { error ->
            // Siempre en el hilo principal: corrige el bug #6 del PDF
            Snackbar.make(binding.coordinatorLayout, error.messageRes(), Snackbar.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
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

    private fun mostrarTecladoInicial() {
        binding.locationEditText.requestFocus()
        WindowCompat.getInsetsController(window, binding.locationEditText)
            .show(WindowInsetsCompat.Type.ime())
    }
}