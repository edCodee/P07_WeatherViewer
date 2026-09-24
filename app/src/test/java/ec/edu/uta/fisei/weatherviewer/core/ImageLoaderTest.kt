package ec.edu.uta.fisei.weatherviewer.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageLoaderTest {

    /** Hace de ImageView: registra cada imagen que se le asigna, en orden. */
    private class Fila {
        val mostradas = mutableListOf<String?>()
    }

    /** Una descarga pendiente por URL; el test decide CUANDO termina cada una. */
    private val descargas = mutableMapOf<String, CompletableDeferred<String?>>()
    private var llamadasDeDescarga = 0

    private fun TestScope.nuevoLoader() = ImageLoader<Fila, String>(
        scope = backgroundScope,
        cache = IconCache(maxEntries = 10),
        download = { url ->
            llamadasDeDescarga++
            descargas.getOrPut(url) { CompletableDeferred() }.await()
        },
        show = { fila, imagen -> fila.mostradas += imagen }
    )

    @Test
    fun `la imagen descargada se muestra en su fila`() = runTest {
        val loader = nuevoLoader()
        val fila = Fila()

        loader.load(fila, "A"); runCurrent()
        descargas.getValue("A").complete("imagen-A"); runCurrent()

        // null primero: la fila se limpia mientras descarga
        assertEquals(listOf(null, "imagen-A"), fila.mostradas)
    }

    @Test
    fun `la segunda vez sale del cache sin descargar`() = runTest {
        val loader = nuevoLoader()
        loader.load(Fila(), "A"); runCurrent()
        descargas.getValue("A").complete("imagen-A"); runCurrent()

        val otraFila = Fila()
        loader.load(otraFila, "A")

        assertEquals(listOf("imagen-A"), otraFila.mostradas)
        assertEquals(1, llamadasDeDescarga)
    }

    @Test
    fun `la carrera del PDF - una fila reciclada nunca recibe el icono viejo`() = runTest {
        val loader = nuevoLoader()
        val fila = Fila()

        loader.load(fila, "A"); runCurrent()   // la fila muestra el lunes...
        loader.load(fila, "B"); runCurrent()   // ...y se recicla para el domingo antes de que A llegue

        descargas.getValue("B").complete("imagen-B"); runCurrent()
        descargas.getValue("A").complete("imagen-A"); runCurrent()  // A llega TARDE

        assertEquals(listOf(null, null, "imagen-B"), fila.mostradas)
    }

    @Test
    fun `una descarga fallida deja la fila sin icono y se reintenta la proxima vez`() = runTest {
        val loader = nuevoLoader()
        val fila = Fila()

        loader.load(fila, "A"); runCurrent()
        descargas.getValue("A").complete(null); runCurrent()
        assertEquals(listOf<String?>(null, null), fila.mostradas)

        loader.load(fila, "A"); runCurrent()
        assertEquals("Un fallo no se cachea", 2, llamadasDeDescarga)
    }

    @Test
    fun `filas distintas no se interfieren`() = runTest {
        val loader = nuevoLoader()
        val lunes = Fila()
        val martes = Fila()

        loader.load(lunes, "A")
        loader.load(martes, "B")
        runCurrent()
        descargas.getValue("B").complete("imagen-B")
        descargas.getValue("A").complete("imagen-A")
        runCurrent()

        assertEquals(listOf(null, "imagen-A"), lunes.mostradas)
        assertEquals(listOf(null, "imagen-B"), martes.mostradas)
    }
}