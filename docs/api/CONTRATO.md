\# Contrato de OpenWeatherMap medido el 2026-09-23 (plan gratuito)



| Archivo | Endpoint | Resultado |

|---|---|---|

| 01 | /data/2.5/forecast/daily (el del PDF) | 401 "Invalid API key" con key VALIDA -> no disponible en el plan |

| 02 | /geo/1.0/direct?q=London,GB | 200, arreglo con name, lat, lon, country, local\_names |

| 03 | /data/2.5/forecast?lat\&lon (5 dias / 3 h) | 200, cnt 40 |

| 04 | /geo/1.0/direct?q=<ciudad inexistente> | 200 con \[] (NO es un error HTTP) |

| 05 | cualquier endpoint con key invalida | 401 |



\## Hallazgos que afectan el diseno

1\. El mensaje del 401 es ambiguo: key invalida o plan sin acceso.

2\. "cod" es texto ("200") en exito y numero (401) en error.

&#x20;  La fuente de verdad es el codigo HTTP, no el campo cod.

3\. dt y dt\_txt estan en UTC. city.timezone da el desfase en segundos

&#x20;  (3600 para Londres en septiembre, horario de verano).

4\. En cada entrada de 3 h, temp\_min y temp\_max son casi iguales:

&#x20;  el minimo y maximo del DIA se calculan entre entradas.

5\. "rain" aparece solo en algunas entradas: hay campos opcionales.

6\. Agrupando por dia local, las 40 entradas quedan 7 / 8 / 8 / 8 / 8 / 1:

&#x20;  el primer dia es parcial y el ultimo tiene UNA sola lectura.

