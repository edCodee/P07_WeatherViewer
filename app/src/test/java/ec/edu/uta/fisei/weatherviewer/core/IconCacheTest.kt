package ec.edu.uta.fisei.weatherviewer.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IconCacheTest {

    @Test
    fun `guarda y recupera por clave`() {
        val cache = IconCache<String>(maxEntries = 2)
        cache.put("01d", "sol")
        assertEquals("sol", cache["01d"])
        assertNull(cache["02d"])
    }

    @Test
    fun `nunca supera el maximo`() {
        val cache = IconCache<String>(maxEntries = 2)
        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")
        assertEquals(2, cache.size)
    }

    @Test
    fun `expulsa el menos usado recientemente`() {
        val cache = IconCache<String>(maxEntries = 2)
        cache.put("a", "1")
        cache.put("b", "2")
        cache["a"]            // "a" pasa a ser el mas reciente
        cache.put("c", "3")   // sale "b", no "a"
        assertNull(cache["b"])
        assertEquals("1", cache["a"])
    }
}