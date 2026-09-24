package ec.edu.uta.fisei.weatherviewer.testutil

/** Lee archivos de src/test/resources/fixtures SIEMPRE como UTF-8. */
object Fixtures {
    fun read(name: String): String {
        val url = requireNotNull(javaClass.classLoader?.getResource("fixtures/$name")) {
            "No existe el fixture $name en src/test/resources/fixtures"
        }
        return url.readText(Charsets.UTF_8)
    }
}