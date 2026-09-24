package ec.edu.uta.fisei.weatherviewer.core

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifica que org.json es la implementacion REAL en la JVM de tests,
 * no el stub de android.jar. Sin esto, el parser de la Fase 3 podria
 * "pasar" leyendo ceros.
 */
class OrgJsonHarnessTest {

    @Test
    fun `org_json parsea de verdad en la JVM`() {
        val json = JSONObject("""{"cod":"200","cnt":40}""")
        assertEquals(40, json.getInt("cnt"))
        assertEquals("200", json.getString("cod"))
    }
}