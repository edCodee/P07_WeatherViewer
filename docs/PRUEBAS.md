# Weather Viewer — Pruebas

| Tipo           | Clases | Pruebas | Resultado                                         | Dónde                               |
| -------------- | ------ | ------- | ------------------------------------------------- | ----------------------------------- |
| Unitarias      | 12     | 79      | En verde (`ApiKeyConfigTest` se ignora sin key)   | JVM, **sin red**                    |
| Instrumentadas | 1      | 3       | En verde tras `gradlew connectedDebugAndroidTest` | Pixel 7, API 36, **sin red ni key** |

---

## 1. Cómo ejecutarlas

```bat
gradlew test
:: Reporte: app/build/reports/tests/testDebugUnitTest/index.html
gradlew connectedDebugAndroidTest
gradlew lint
```

---

## 2. Estrategia

| Nivel              | Qué verifica                                          | Cómo evita la red                                                                |
| ------------------ | ----------------------------------------------------- | -------------------------------------------------------------------------------- |
| **Dominio**        | Agregación y flujo con errores                        | Repositorios falsos                                                              |
| **Datos**          | HTTP, códigos, timeouts, UTF-8, binario, parser, URLs | **MockWebServer** + fixtures reales (`docs/api` → `src/test/resources/fixtures`) |
| **UI (JVM)**       | ViewModel, formato, mensajes, `ImageLoader`           | `MainDispatcherRule`, descargas controladas con `CompletableDeferred`            |
| **Instrumentadas** | App completa: búsqueda, error, recreación             | `AppContainer` falso instalado antes de abrir la Activity                        |
| **Manual**         | Red real, idioma, orientación, tablet, modo avión     | Sección 6                                                                        |

`isReturnDefaultValues = false` es explícito: una llamada a `android.*` en una prueba falla en vez de devolver `null` o `0`.

---

## 3. Pruebas unitarias

| Clase                               | #   | Qué protege                                                                                                                                                                                                                                                                                                                   | Relación      |
| ----------------------------------- | --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- |
| `AggregateDailyForecastUseCaseTest` | 12  | Vacío; **23:00 UTC es el día siguiente en UTC+1** y 03:00 UTC el anterior en UTC−5; mínima y máxima entre lecturas; humedad con redondeo hacia arriba; ícono del mediodía; empate → la más temprana; hoy con una lectura; día posterior con 3 lecturas descartado y con 4 conservado; orden; **Londres real: 40 → 7/8/8/8/8** | #8, #12       |
| `GetDailyForecastUseCaseTest`       | 8   | Consulta en blanco sin red; `trim`; ciudad inexistente sin pedir el pronóstico; coordenadas del geocoding; éxito; 401 → `Unauthorized`; `NoConnection`; **la cancelación no se convierte en error**                                                                                                                           | #16, #17      |
| `HttpJsonFetcherTest`               | 9   | 200; UTF-8 ("Zürich"); 401; 429 conserva el código; 503; 404; servidor apagado; **servidor que no responde → timeout**; **bytes PNG intactos**                                                                                                                                                                                | #13, #17      |
| `OwmJsonParserTest`                 | 9   | Geocoding real → Londres; `[]` → `null`; cuerpo de error → inesperado; 40 lecturas y timezone; primera y última lectura campo por campo (`temp_min` entero, campo opcional `rain`); JSON truncado; 401 sin `list`; **agregación sobre datos reales**                                                                          | CONTRATO      |
| `OwmUrlsTest`                       | 9   | https; espacios, comas y acentos codificados; `limit=1`; **punto decimal en es-EC**; métrico; ícono `@2x` por https; `lang`; **el idioma se lee en cada llamada**                                                                                                                                                             | #10, #28      |
| `OwmRepositoriesTest`               | 5   | Rutas y parámetros reales; flujo completo con **2 peticiones**; key rechazada o ciudad inexistente cortan en **1**                                                                                                                                                                                                            | #8, #9        |
| `ImageLoaderTest`                   | 5   | Mostrar; caché sin descargar; **la carrera del PDF: la fila reciclada nunca recibe el ícono viejo**; un fallo no se guarda en caché; filas independientes                                                                                                                                                                     | #13, #14      |
| `IconCacheTest`                     | 3   | Guardar y recuperar; tope; expulsa el menos usado                                                                                                                                                                                                                                                                             | #13           |
| `ForecastRowFormatterTest`          | 10  | Jueves (en/es, mayúscula); **no depende de la zona del teléfono**; redondeo; 22,5 → 23; **nunca "-0"**; humedad por locale; °F; °F sin "-0"                                                                                                                                                                                   | #11, #12, #25 |
| `ForecastViewModelTest`             | 7   | Estado inicial; carga visible y oculta; éxito; error pendiente; **un error no borra el pronóstico**; error consumido una vez; **una búsqueda nueva cancela la anterior**                                                                                                                                                      | #15           |
| `ForecastErrorMessagesTest`         | 1   | Seis fallos con seis mensajes distintos                                                                                                                                                                                                                                                                                       | #17           |
| `ApiKeyConfigTest`                  | 1   | Key de 32 caracteres hexadecimales sin comillas ni espacios; se ignora sin key                                                                                                                                                                                                                                                | #7            |

---

## 4. Pruebas instrumentadas (`MainActivityTest`)

| Prueba                                                    | Qué verifica                                                         |
| --------------------------------------------------------- | -------------------------------------------------------------------- |
| `buscar_una_ciudad_muestra_la_ciudad_resuelta_y_sus_dias` | "quito" → subtítulo "Quito, EC" y la primera fila con la descripción |
| `una_ciudad_inexistente_muestra_el_snackbar_de_error`     | Snackbar "City not found"                                            |
| `recrear_la_activity_conserva_el_pronostico`              | `recreate()` conserva la ciudad y la lista (#15)                     |

---

## 5. Técnicas usadas

| Técnica                                        | Dónde                    | Para qué                                          |
| ---------------------------------------------- | ------------------------ | ------------------------------------------------- |
| Fixtures reales medidos                        | `Fixtures.read` (UTF-8)  | Probar contra lo que el API responde de verdad    |
| MockWebServer                                  | Fetcher y repositorios   | Códigos HTTP, timeouts y rutas sin internet       |
| `SocketPolicy.NO_RESPONSE` + timeouts cortos   | `HttpJsonFetcherTest`    | Probar el timeout en milisegundos                 |
| Respuestas controladas (`CompletableDeferred`) | ViewModel, `ImageLoader` | Decidir el orden de llegada y reproducir carreras |
| `MainDispatcherRule` + `runTest`               | ViewModel                | `Dispatchers.Main` no existe en la JVM            |
| `backgroundScope`                              | `ImageLoaderTest`        | Corrutinas que no bloquean el fin de la prueba    |
| `Locale` y `TimeZone` cambiados y restaurados  | `OwmUrlsTest`, formatter | Probar es-EC y UTC−5 sin depender de la máquina   |
| `assumeTrue`                                   | `ApiKeyConfigTest`       | Ignorar, no fallar, sin key                       |
| Contenedor falso                               | `MainActivityTest`       | App completa sin red ni key                       |

---

## 6. Verificación manual

Requiere `OWM_API_KEY` en `local.properties`. Capturas en `docs/img/`; las de la fase 6 están en `docs/evidencia/` (`06-busqueda-claro.png`, `06-busqueda-oscuro.png`).

| Captura                 | Cómo                                            | Qué se comprueba                                          |
| ----------------------- | ----------------------------------------------- | --------------------------------------------------------- |
| `01_inicio.png`         | Arranque                                        | Estado vacío, foco y teclado                              |
| `02_pronostico.png`     | "Quito, EC" + FAB                               | Subtítulo "Quito, EC", días con ícono, °C                 |
| `03_error_ciudad.png`   | "Ciudadquenoexiste123"                          | Snackbar "City not found"                                 |
| `04_horizontal.png`     | Rotar                                           | Sin bloqueo; pronóstico conservado                        |
| `05_oscuro.png`         | `cmd uimode night yes`                          | Pronóstico conservado; íconos visibles sobre el círculo   |
| `06_espanol_cambio.png` | `set-app-locales es-EC` **sin** volver a buscar | Días en español, descripciones en inglés (ANALISIS §7 #5) |
| `07_espanol.png`        | Buscar de nuevo                                 | Descripciones en español ("nubes dispersas")              |
| `08_sin_conexion.png`   | Modo avión + FAB                                | "No se pudo conectar…"; la lista anterior se conserva     |
| `09_tablet.png`         | Pixel Tablet                                    | Diseño en pantalla grande                                 |
| `10_icono.png`          | Cajón de apps                                   | Ícono de plantilla                                        |
| `11_pruebas.png`        | Reporte de pruebas unitarias                    | 79                                                        |
| `12_lint.png`           | Reporte de Lint                                 | 0 errores                                                 |

---

## 7. Límites conocidos

| Qué no se verifica automáticamente  | Mitigación                                      |
| ----------------------------------- | ----------------------------------------------- |
| Carga real de íconos en `ImageView` | Lógica probada en `ImageLoaderTest`; captura 02 |
| Red real                            | Contrato medido y fixtures; verificación manual |
| Tablet en instrumentadas            | Captura 09                                      |
| Coordenadas cercanas a 0            | ANALISIS §7 #4                                  |

---

## 8. Evolución

| Fase | Commit                | Unitarias                     | Instrumentadas |
| ---- | --------------------- | ----------------------------- | -------------- |
| 2    | `5a0e65e`             | 20                            | —              |
| 3    | `b5d0def`             | 49                            | —              |
| 5    | `c665a7d` → `e86588f` | 67 (compilan desde `e86588f`) | —              |
| 6    | `5add884`             | 75                            | —              |
| 8    | `a324ab6`             | 79                            | —              |
| 9    | `c62b727`             | 79                            | 3              |
