# Weather Viewer — Plan de fases (Práctica 07, Deitel Cap. 7, API 36)

| Fase | Contenido                                                                                                                           | Pruebas         | Commits                                       |
| ---- | ----------------------------------------------------------------------------------------------------------------------------------- | --------------- | --------------------------------------------- |
| 1    | Proyecto base: SDK 37/36/24, viewBinding, buildConfig, key fuera de Git, org.json real en pruebas                                   | —               | da10039 · fix c4567c1, 2ffaa47, dece5db       |
| —    | docs: contrato del API medido con key real                                                                                          |                 | 77e9028 · 3225bd9 · 179716c · e11f205         |
| 2    | Dominio con TDD: agregación de 3 h a días locales, `GetDailyForecast` con errores tipados                                           | 20              | 5a0e65e                                       |
| 3    | Capa de datos: `HttpURLConnection` con timeouts, parser org.json sobre fixtures reales, errores HTTP tipados                        | 49              | b5d0def                                       |
| 4    | GUI: CoordinatorLayout, TextInputLayout, FAB sin margen mágico, ListView con `list_item` (Fig. 7.5), tema M3                        | 49              | b80902b · fix 12fd307                         |
| 5    | Adapter con ViewHolder, `ImageLoader` que cancela cargas de filas recicladas (#14), `IconCache` LRU, formatter por locale           | 67 (declaradas) | c665a7d · fix e86588f, 79162e1, 7482da7       |
| 6    | `ForecastViewModel` con `StateFlow`, cancelación de búsquedas, errores con mensaje propio, `AppContainer`, datos reales en pantalla | 75              | 5add884                                       |
| 7    | Manifiesto sin bloqueo de orientación, `usesCleartextTraffic` explícito, teclado al iniciar                                         | 75              | e2d0801 · docs 661dbcd · fix 30e3eac, 76504a5 |
| 8    | Español, descripciones del API en el idioma del teléfono, °C/°F según preferencia, fondo para íconos, estado vacío                  | 79              | a324ab6 · fix 462dafa, 1685fc2                |
| 9    | Espresso sin red ni API key: `AppContainer` reemplazable, 3 pruebas instrumentadas, animaciones apagadas                            | 79 + 3          | c62b727 · tag `v1.0`                          |
| 10   | Documentación y capturas; código congelado                                                                                          |                 | v1.0-practica07                               |

Regla: no se pasa de fase sin pruebas y Lint en verde y sin commit, revisando `git status` antes y después.

**Excepciones registradas** (ANALISIS §7 #1):

- `c665a7d` (fase 5) anunciaba 67 pruebas que no compilaban; `e86588f` las hizo compilar.
- `e86588f` subió una mutación del `ImageLoader`, que `79162e1` revirtió.
- Varios `fix:` corrigen commits que anunciaban cambios sin incluirlos. La eliminación del color `black` se anunció en cuatro commits y llegó en `1685fc2`.
