package ec.edu.uta.fisei.weatherviewer.core

import ec.edu.uta.fisei.weatherviewer.BuildConfig
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Detecta errores de configuracion de la key (comillas, espacios, key
 * incompleta) sin imprimirla. Si no hay key (maquina del docente, CI),
 * el test se marca como IGNORADO, no como fallido.
 */
class ApiKeyConfigTest {

    @Test
    fun `la key tiene el formato de OpenWeatherMap`() {
        val key = BuildConfig.OWM_API_KEY
        assumeTrue("Sin OWM_API_KEY en local.properties", key.isNotBlank())
        assertTrue(
            "La key debe ser 32 caracteres hex sin comillas ni espacios",
            key.matches(Regex("^[0-9a-f]{32}$"))
        )
    }
}