package ec.edu.uta.fisei.weatherviewer.core

/**
 * LRU por cantidad de entradas. OpenWeatherMap tiene menos de 20 codigos de
 * icono, asi que contar bytes seria complejidad sin beneficio.
 * NO es thread-safe a proposito: solo se usa desde el hilo principal
 * (el HashMap del PDF se escribia desde hilos de fondo: bug #13).
 */
class IconCache<V : Any>(private val maxEntries: Int) {

    // accessOrder = true: cada get() mueve la entrada al final (la mas reciente)
    private val entradas = object : LinkedHashMap<String, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, V>?): Boolean =
            size > maxEntries
    }

    operator fun get(key: String): V? = entradas[key]

    fun put(key: String, value: V) {
        entradas[key] = value
    }

    val size: Int get() = entradas.size
}