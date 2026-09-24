package ec.edu.uta.fisei.weatherviewer.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi



/**
 * Carga imagenes en destinos que se RECICLAN (filas de un ListView).
 *
 * Corrige el bug #14 del PDF: si un destino pide una imagen nueva, la carga
 * anterior se cancela y nunca llega a mostrarse sobre el destino reciclado.
 *
 * Generico en T (destino) e I (imagen) para probar la logica en la JVM,
 * sin ImageView ni Bitmap. Debe usarse desde el hilo principal.
 */

@OptIn(ExperimentalCoroutinesApi::class)
class ImageLoader<T : Any, I : Any>(
    private val scope: CoroutineScope,
    private val cache: IconCache<I>,
    private val download: suspend (String) -> I?,
    private val show: (T, I?) -> Unit
) {

    private val enCurso = HashMap<T, Job>()

    fun load(target: T, url: String) {
        // 1. El destino se recicla: lo que estuviera bajando para el ya no sirve
        enCurso.remove(target)?.cancel()

        // 2. Del cache: al instante, sin parpadeo
        cache[url]?.let { imagen ->
            show(target, imagen)
            return
        }

        // 3. Limpiar el icono del dia anterior mientras llega el nuevo
        show(target, null)

        enCurso[target] = scope.launch {
            // Si este Job se cancela durante la descarga, download lanza
            // CancellationException al volver y las lineas siguientes NO corren
            val imagen = download(url)
            if (imagen != null) cache.put(url, imagen)
            show(target, imagen)
        }
    }
}