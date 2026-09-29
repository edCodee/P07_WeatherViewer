# Weather Viewer — Análisis de diseño (PDF Deitel 2016 → Kotlin / API 36)

Complementa a [COMPATIBILIDAD.md](COMPATIBILIDAD.md) (catálogo #1–#31) y a [api/CONTRATO.md](api/CONTRATO.md).

> Rutas relativas a `app/src/main/java/ec/edu/uta/fisei/weatherviewer/`. **Código congelado desde la etiqueta `v1.0`.**

---

## 1. Resumen

| Métrica               | PDF                                       | Proyecto                                                  |
| --------------------- | ----------------------------------------- | --------------------------------------------------------- |
| Clases                | 3 (+2 `AsyncTask` anidadas)               | 25 archivos Kotlin en 4 capas + composición               |
| Lógica sin Android    | Ninguna                                   | `domain` completo, `core`, `data/remote` (JVM + org.json) |
| Errores               | `catch (Exception) { printStackTrace() }` | 3 niveles tipados: HTTP → dominio → UI                    |
| Pruebas               | 0                                         | 79 unitarias + 3 instrumentadas                           |
| Contrato del servicio | Asumido                                   | Medido con key real y guardado como fixtures              |

---

## 2. ¿Se aplicó arquitectura limpia?

**Sí.**

| Aspecto                                      | Evidencia                                                                                                                       |
| -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| Dominio puro                                 | `domain/` solo usa Kotlin y `java.lang.Math` (verificable, sección 8)                                                           |
| Inversión de dependencias                    | `GeocodingRepository` y `ForecastRepository` (dominio) ← `Owm*Repository` (datos)                                               |
| Errores del negocio definidos por el dominio | `WeatherDataException` sellada: los repositorios hablan en términos del negocio, no de HTTP ni JSON                             |
| Resultados, no excepciones, hacia la UI      | `ForecastResult` sellado; la `CancellationException` sí se propaga (probado)                                                    |
| Punto de composición                         | `AppContainer.production(key)`; `WeatherViewerApp` lo crea de forma diferida y las pruebas lo reemplazan (`@VisibleForTesting`) |
| Configuración fuera del código               | Key en `local.properties`; `baseUrl` e idioma inyectables en `OwmUrls`                                                          |

**Filtración:** la UI toma `container.fetcher` y `container.urls` (clases de `data`) para los íconos, y `BitmapDownloader` (en `ui`) depende de `HttpJsonFetcher`. `AppContainer` dice ser "el único lugar donde se unen las capas", pero no lo es del todo (§7 #2).

---

## 3. Malos olores del PDF (STUPID)

| Letra | ¿En el PDF? | Evidencia                                                                                                                                                                     | Corrección                                                                                    |
| ----- | ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| **S** | No aplica   | —                                                                                                                                                                             | —                                                                                             |
| **T** | **Sí**      | `GetWeatherTask` y `LoadImageTask` son clases internas: tocan la Activity, su lista y el adapter                                                                              | Capas; el ViewModel no conoce vistas                                                          |
| **U** | **Sí**      | Red, JSON, formato y vistas en la misma clase                                                                                                                                 | 79 pruebas en la JVM                                                                          |
| **P** | No aplica   | —                                                                                                                                                                             | —                                                                                             |
| **I** | **Sí**      | `Weather` guarda **textos ya formateados** con nombres de dato (`minTemp: String`); `day` es un `Weather`; el comentario "16-**dayOfWeek** weather forecast" (Fig. 7.14 l. 2) | Dominio numérico (`DailyForecast.minTemp: Double`); el formato vive en `ForecastRowFormatter` |
| **D** | **Sí**      | Dos implementaciones de `HttpURLConnection` (JSON e imágenes) con el mismo `try`/`finally`                                                                                    | Un solo `HttpJsonFetcher` con `get` (UTF-8) y `getBytes` (binario)                            |

---

## 4. Principios SOLID

| Principio | PDF                                                                                   | Proyecto                                                                                                                                                                                                                                 |
| --------- | ------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **S**     | `MainActivity` arma la URL, llama al servicio, parsea, maneja errores y dibuja        | URL → `OwmUrls`; HTTP → `HttpJsonFetcher`; JSON → `OwmJsonParser`; flujo → `GetDailyForecastUseCase`; agregación → `AggregateDailyForecastUseCase`; estado → `ForecastViewModel`; texto → `ForecastRowFormatter`; íconos → `ImageLoader` |
| **O**     | Cambiar de endpoint obliga a reescribir la Activity                                   | Otro proveedor del clima es otra implementación de las interfaces. Cuando el endpoint de 16 días desapareció, el cambio quedó en `data` y en un caso de uso nuevo                                                                        |
| **L**     | No aplica                                                                             | Repositorios falsos (`FakeGeocodingRepository`, `GeocodingControlado`) y reales cumplen el mismo contrato, documentado con `@throws`                                                                                                     |
| **I**     | `AsyncTask<Params, Progress, Result>` obliga a declarar tipos que no se usan (`Void`) | Interfaces de un método; `ImageLoader` recibe solo dos funciones (`download`, `show`)                                                                                                                                                    |
| **D**     | La Activity depende de `HttpURLConnection` y `JSONObject`                             | El caso de uso depende de interfaces; `ImageLoader` es genérico en destino e imagen, así que se prueba sin `ImageView` ni `Bitmap`                                                                                                       |

---

## 5. Otros principios aplicados

| Principio                                 | Evidencia                                                                                                                                                                                                         |
| ----------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Contrato medido antes de programar**    | `docs/api/CONTRATO.md` y sus fixtures: el diseño parte de lo que el servicio responde de verdad (`cod` es texto en éxito y número en error; `[]` para una ciudad inexistente; 7/8/8/8/8/1 lecturas por día local) |
| **La fuente de verdad es el código HTTP** | El parser no lee `cod` (hallazgo 2 del contrato)                                                                                                                                                                  |
| **Cancelación estructurada**              | Una búsqueda nueva cancela la anterior; un resultado viejo nunca pisa al nuevo (probado)                                                                                                                          |
| **Un error no destruye datos**            | Un fallo conserva el pronóstico que ya se mostraba (probado)                                                                                                                                                      |
| **Estado único para la UI**               | `ForecastUiState`; `render()` no calcula nada; el error se consume una vez                                                                                                                                        |
| **Independencia del locale**              | Coordenadas con plantilla de Kotlin, no `String.format` (en es-EC la coma rompería la URL; probado); JSON siempre como UTF-8                                                                                      |
| **Inyectable para probar**                | `baseUrl`, idioma (función que se evalúa en cada llamada), dispatcher de IO, timeouts, `Locale` y unidad del formatter                                                                                            |

---

## 6. Errores e inconsistencias del PDF

### 6.1 Errores en el texto

| #   | Ubicación             | Qué dice                                                                                                                           | Lo correcto                                                                                                |
| --- | --------------------- | ---------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| 1   | Fig. 7.14 l. 2        | "Displays a 16-**dayOfWeek** weather forecast"                                                                                     | "16-day": resto de un buscar y reemplazar de `day` por `dayOfWeek`                                         |
| 2   | Fig. 7.18 ll. 103–104 | "…and **saves the data to a local HTML file**"                                                                                     | El código no guarda ningún archivo                                                                         |
| 3   | PDF 7.3.2             | `"weather"` es "un objeto JSON"                                                                                                    | Es un **arreglo** de objetos (Fig. 7.3 l. 27; el código usa `getJSONArray("weather").getJSONObject(0)`)    |
| 4   | PDF 7.3.4             | "**Activity** Not Responding (ANR)"                                                                                                | **Application** Not Responding                                                                             |
| 5   | PDF 7.6.3             | El `-1` del constructor "indica que usamos un layout propio"                                                                       | Es un truco: pasa un id de recurso inválido que solo funciona porque `getView` se sobrescribe por completo |
| 6   | PDF 7.7.5             | `doInBackground` en "lines 108–51"                                                                                                 | 108–151                                                                                                    |
| 7   | PDF 7.3.3 y otros     | `HttpUrlConnection`                                                                                                                | `HttpURLConnection`                                                                                        |
| 8   | PDF 7.3.1             | Límites de 1200 peticiones por minuto y 1,7 millones por día                                                                       | Datos de 2016; el plan gratuito actual es otro (consultar la página de precios)                            |
| 9   | Varias                | "get's" (7.3.5), "it's original size" (7.3.7), "most new template include" (7.3.6), "reference's" (Fig. 7.12), "Library's—a" (7.8) | Erratas                                                                                                    |

### 6.2 Problemas de diseño en el código

| #   | Ubicación         | Problema                                                                                                | En el proyecto                                                                  |
| --- | ----------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| A   | Fig. 7.7          | El modelo `Weather` guarda textos formateados y arma la URL del ícono                                   | Modelo numérico; formato en la UI; URL en `OwmUrls`                             |
| B   | Figs. 7.13 y 7.18 | `catch (Exception e) { e.printStackTrace(); }` en todas partes                                          | Solo se capturan los errores esperados; `BitmapDownloader` registra con `Log.w` |
| C   | Fig. 7.17         | `createURL` devuelve `null` si `URLEncoder` falla con "UTF-8" (no puede fallar) y muestra "Invalid URL" | URL construida sin caminos imposibles                                           |
| D   | Fig. 7.18         | El cuerpo se lee línea por línea y se vuelve a unir sin saltos, con el charset por defecto              | `readBytes()` + UTF-8 explícito (probado con "Zürich")                          |

---

## 7. Autocrítica: deuda técnica conocida

| #   | Hallazgo                                                                                                                                                                                                                                                                                        | Riesgo                                                                                                | Acción propuesta                                                         | Estado                                                      |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ | ----------------------------------------------------------- |
| 1   | Historial: varios commits anuncian cambios que no incluyen (`c4567c1`, `2ffaa47`, `179716c`, `b80902b`, `c665a7d`, `79162e1`, `e2d0801`, `30e3eac`, `462dafa`); `c665a7d` declaró 67 pruebas que **no compilaban**; `e86588f` subió una **mutación** del `ImageLoader` (revertida en `79162e1`) | La regla "gate verde antes del commit" se rompió; el historial no es confiable commit a commit        | Revisar `git show --stat` y ejecutar `gradlew test` antes de cada commit | Aceptado (el historial no se reescribe); registrado en PLAN |
| 2   | La UI usa `container.fetcher` y `container.urls` (capa `data`) para los íconos; `BitmapDownloader` (en `ui`) depende de `HttpJsonFetcher`                                                                                                                                                       | Filtración de capas; el punto de composición no es el único lugar de unión                            | Un puerto `IconSource` y una implementación en `data`                    | Pendiente                                                   |
| 3   | `GetDailyForecastUseCase` solo captura `WeatherDataException` (a propósito). `openConnection()` está **fuera** del `try` de `HttpJsonFetcher`                                                                                                                                                   | Una `IOException` ahí, u otra excepción no prevista, cierra la app desde `viewModelScope`             | Mover `openConnection` dentro del `try`                                  | Sin verificar (poco probable)                               |
| 4   | `forecast()` escribe las coordenadas con `Double.toString`: si \|x\| < 0,001, sale en notación científica (`1.0E-4`)                                                                                                                                                                            | Una ciudad a menos de ~110 m del ecuador o de Greenwich podría recibir una URL que el API no entienda | `BigDecimal(x).toPlainString()`                                          | Sin verificar (caso raro)                                   |
| 5   | Al cambiar el idioma, los días se traducen (formatter nuevo), pero las **descripciones** vienen del API y siguen en el idioma anterior hasta la próxima búsqueda                                                                                                                                | Pantalla mezclada ("Jueves: overcast clouds")                                                         | Repetir la última búsqueda si cambió el idioma                           | Confirmado 06                                               |
| 6   | `IconCache` vive en la Activity                                                                                                                                                                                                                                                                 | Tras rotar o cambiar el tema se vuelven a descargar los íconos                                        | Llevar la caché a la aplicación o al contenedor                          | Pendiente                                                   |
| 7   | La key queda dentro del APK (`BuildConfig`)                                                                                                                                                                                                                                                     | Se puede extraer del APK                                                                              | Backend propio que firme las peticiones                                  | Aceptado para el curso                                      |
| 8   | `constraintlayout` sin uso; Lifecycle (`ViewModel`, `viewModelScope`, `repeatOnLifecycle`) se usa sin declararse (llega por dependencias transitivas); en `build.gradle.kts` quedó un `// ...` y `coroutines-android` está después del bloque de pruebas                                        | Build frágil ante cambios de versión; ruido                                                           | Declarar `lifecycle-*` explícitamente, quitar lo que sobra, ordenar      | Pendiente                                                   |
| 9   | Hasta 5 días (menos si el último está incompleto) frente a los 16 del PDF                                                                                                                                                                                                                       | Funcionalidad reducida                                                                                | Plan pago o agregar otro proveedor                                       | Aceptado (CONTRATO, archivo 01)                             |
| 10  | `ListView` + `ArrayAdapter`, como el PDF                                                                                                                                                                                                                                                        | Menos eficiente que `RecyclerView`                                                                    | Migrar a `RecyclerView` + `ListAdapter`                                  | Aceptado (fidelidad; P08 usa `RecyclerView`)                |
| 11  | Ícono de plantilla                                                                                                                                                                                                                                                                              | Paso 2.5.2 no aplicado                                                                                | Image Asset                                                              | Pendiente · captura 10                                      |
| 12  | `ApiKeyConfigTest` se ignora si no hay key                                                                                                                                                                                                                                                      | El total en verde varía (79, o 78 + 1 ignorada)                                                       | —                                                                        | Documentado                                                 |
| 13  | Las instrumentadas usan un contenedor sin red: los íconos fallan en silencio (`Log.w`) y no se prueban; no se ejecutaron en tablet                                                                                                                                                              | La carga real de íconos solo se verifica a mano                                                       | Un `ImageLoader` falso en la prueba                                      | Pendiente                                                   |
| 14  | `ImageLoader.enCurso` guarda el último `Job` de cada `ImageView`, aunque ya haya terminado                                                                                                                                                                                                      | Referencias retenidas (acotadas al número de filas visibles; el scope es `lifecycleScope`)            | Quitar la entrada al terminar                                            | Aceptado                                                    |
| 15  | Capturas en dos carpetas: `docs/evidencia/` (fase 6) y `docs/img/` (esta documentación)                                                                                                                                                                                                         | Convención mixta                                                                                      | —                                                                        | Documentado                                                 |

---

## 8. Cómo verificar este documento

```bat
gradlew test lint
gradlew connectedDebugAndroidTest

:: Dominio sin Android (no debe imprimir nada)
git grep -l "import android" -- app/src/main/java/ec/edu/uta/fisei/weatherviewer/domain

:: La key no está en Git (no debe imprimir nada)
git grep -n "OWM_API_KEY=" -- .

:: §7 #2: la UI usa clases de data
git grep -n "container.fetcher\|container.urls\|HttpJsonFetcher" -- app/src/main/java/ec/edu/uta/fisei/weatherviewer/ui

:: §7 #1: historial
git log --oneline
```
