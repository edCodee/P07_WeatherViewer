package ec.edu.uta.fisei.weatherviewer.ui.forecast

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import ec.edu.uta.fisei.weatherviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // dark() = iconos claros sobre el indigo de la toolbar
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        binding.weatherListView.emptyView = binding.emptyTextView
        aplicarInsets()
    }

    /**
     * Arriba: la toolbar se extiende detras de la barra de estado.
     * Lados: el contenido completo.
     * Abajo: SOLO la lista, para que haga scroll por detras de la barra de
     * navegacion (clipToPadding=false) y no quede tapada por el teclado.
     */
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