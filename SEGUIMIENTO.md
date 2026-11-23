# Seguimiento de la entrega

Fecha: 21 de noviembre de 2026.

| Paso | Entregable | Estado |
| --- | --- | --- |
| 1 | Revisar propuesta y rúbrica | Completado |
| 2 | Comprobar marcadores y tablas de las cuatro ligas | Ocho respuestas HTTP 200 registradas |
| 3 | Crear proyecto Android nativo en Java | Completado |
| 4 | Implementar calendario, filtro, detalle y clasificaciones | Implementado; falta validar interfaz en dispositivos |
| 5 | Guardar favoritos y activar seguimiento foreground | Implementado; falta validar notificaciones en dispositivos |
| 6 | Pruebas del procesamiento y de cambios de marcador | 22 pruebas JUnit ejecutadas y aprobadas en JVM |
| 7 | Compilar y firmar APK de depuración | Completado con herramientas SDK; firma verificada |
| 8 | Documento, diagramas, mockups y protocolo | Completado; campos académicos por llenar |
| 9 | Importación y build Android Studio con Gradle | Pendiente en el equipo del estudiante |
| 10 | Instalar en teléfono y adjuntar capturas | Pendiente |
| 11 | Ejecutar casos en tres emuladores | Pendiente |
| 12 | Publicar repositorio y añadir URL y commit | Pendiente |

## Continuar desde el paso 9

Abrir el proyecto en Android Studio, ejecutar Gradle Sync y Run. Si aparece un error, conservar el texto completo de Build Output y la versión de Android Studio. Registrar los resultados de la matriz en `docs/evidencias/matriz_dispositivos.csv`.

El ZIP contiene un APK de depuración en `apk/`. La clave privada de construcción no se entrega. Si Android Studio firma una nueva compilación con otra clave, desinstalar la anterior antes de instalarla.

No convertir los estados pendientes en completados sin ejecutar las pruebas y adjuntar evidencia real. Los documentos no afirman que la app se haya instalado en un teléfono.

## Corrección 1.0.1

- Causa reproducida: HTTP 400 al enviar un intervalo en el parámetro dates.
- Cambio: consultas de un día para pantalla inicial y servicio de seguimiento, combinación de eventos sin duplicados y caché independiente por día.
- Reintento: fuerza una descarga y muestra el estado de carga; errores diferenciados y botón Ver detalle.
- Verificación: 12 respuestas HTTP 200 para las fechas 20 a 22 de noviembre de 2026 en las cuatro ligas; 22 pruebas JUnit aprobadas; APK 1.0.1 compilado y firma verificada.
- Pendiente: repetir Run en Android Studio y confirmar los partidos en el emulador del estudiante.

## Rediseño 1.1

- Completado: selección inicial de equipos y ligas; búsqueda dentro de las cuatro ligas; escudos remotos; portada sin selector de fecha; navegación a partido, equipo y liga; tabla y calendario de equipos; alertas de inicio, final, marcador y ciertas incidencias publicadas.
- Verificado: compilación del APK con herramientas SDK 35 y firma; 24 pruebas JVM.
- Parcial por límites de datos: campeones históricos, títulos, goleadores y curiosidades. Se indica ausencia en las fichas; falta contratar o identificar una fuente verificable para cada competición.
- Pendiente: verificar en dispositivo y emuladores el aspecto, la navegación, los datos recibidos y las notificaciones en un partido real. Repositorio del proyecto: [SantyAcevedo/PulseSport](https://github.com/SantyAcevedo/PulseSport).

## Corrección 1.1.1 de navegación inicial

La versión 1.1 redirigía a la selección de equipos en cada redibujo mientras no se hubiera terminado el asistente. Por eso Siguiente, Ajustes y la selección de ligas no avanzaban. Ahora la redirección ocurre solo al abrir la aplicación por primera vez. Siguiente abre ligas, Terminar guarda el fin del asistente y Ajustes puede abrirse durante el proceso. APK compilado y 24 pruebas unitarias existentes aprobadas; falta comprobar las pulsaciones en emulador o teléfono.

## Rediseño 1.2.0

- Hecho: tarjetas compactas con escudos, marcador u horario, estados visibles y estrellas para equipos favoritos; paleta oscura de alto contraste.
- Hecho: barra inferior Inicio, En vivo, Favoritos y Ligas; filtros de portada, selector de liga favorita, detalle con Resumen, Cronología y Tabla; filas alineadas de clasificación.
- Verificado: APK versionCode 5 compilado y firma v2/v3 validada; 24 pruebas JVM satisfactorias.
- Pendiente: evaluación visual y funcional en tres emuladores y un teléfono físico; capturas reales.


## Gradle 8.14.7

- Actualizado `gradle/wrapper/gradle-wrapper.properties` de 8.11.1 a 8.14.7 con checksum oficial SHA-256.
- Verificado el destino de descarga mediante `./gradlew --version`; la descarga y sincronización completas no pudieron ejecutarse aquí porque este entorno no tiene acceso a `services.gradle.org`.
- Pendiente de comprobación en Android Studio del estudiante: abrir este ZIP actualizado y esperar la sincronización con Internet.

## Interfaz 1.3.0

- Completado: Ajustes en la barra inferior con destino táctil de 64 dp; eliminación del engranaje pequeño.
- Completado: Inicio con pestañas Favoritos y Resultados, filtros, contador, ligas y tarjetas centradas. Ancho del contenido limitado a 720 dp para tablets. Ninguna sección de apuestas.
- Verificado: compilación de APK versionCode 6, firma validada y 24 pruebas JVM correctas.
- Pendiente: capturas y prueba de interacción en tablet, tres emuladores y teléfono físico.

## Favoritos 1.4.0

- Completado: búsqueda táctil de 56 dp, cuadrícula de tres equipos, escudos de 54 dp y estrella de 54 dp.
- Corregido: uso de `team.logos[].href` si el API no devuelve `team.logo`.
- Completado: búsqueda local tras una sola consulta de equipos; ZIP pequeño de actualización sobre el proyecto abierto.
- Verificado: APK versionCode 7 firmada y 26 pruebas JVM. Pendiente de comprobar escudos en red y la distribución visual en dispositivo físico.

## Corrección 1.4.1

- Lupa de cabecera y selector reemplazada por icono nativo visible con superficie táctil de 56 × 56 dp.
- Retirado el botón de actualización contiguo de la cabecera.
- APK versionCode 8 compilada y firmada; 26 pruebas JVM satisfactorias. Pendiente: comprobar tamaño y pulsación en móvil real.

## Torneos 1.5.0

- Agregados: Primera B, Superliga y Copa Colombia; Libertadores, Sudamericana y Recopa.
- Agrupación de ligas; calendario reciente y próximo en la ficha de competición; mensaje cuando no hay tabla.
- Optimizado: Resultados carga dos días por página y hasta cuatro ligas simultáneas; equipos y Favoritos mantienen sus rutas.
- Verificado: 27 pruebas JVM, compilación y firma de APK versionCode 9.
- Pendiente: comprobar respuestas de los seis endpoints en teléfono con conexión, y tres emuladores.

## Cuenta regresiva 1.5.1

- Hecho: días, HH:mm:ss y MM:ss para partidos próximos, con un único temporizador por pantalla que se detiene en segundo plano.
- Hecho: fecha de inicio en hora local y texto claro cuando no hay hora válida.
- Verificado: APK versionCode 10 compilada y firmada; 32 pruebas JVM.
- Pendiente: comprobar visualmente una cuenta regresiva en un dispositivo, especialmente el ancho de tarjetas pequeñas y la transición a partido en vivo.

## Referencia de video 1.6.0 — avance parcial
Perfil reorganizado con hero fotográfico, escudo y pestañas Resumen, Partidos, Posiciones y Club. Paleta y superficies ajustadas. Se intenta obtener fotografía del estadio en Wikipedia (Palogrande para Once Caldas, venue para otros equipos). Cobertura, licencias y atribuciones de fotos pendientes de revisión; no se garantiza foto para todos los clubes. Pendiente: rediseño completo del partido, alineaciones, estadísticas reales y revisión visual en Android. No se considera completada la equivalencia con el video.

## Interfaz global 1.7.0
Cambios en controles globales, paleta, márgenes y respuesta táctil; filtros horizontales en Inicio; Favoritos reorganizado con accesos reales al perfil; ficha de partido con Resumen, Alineaciones, Estadísticas, Posiciones y Minuto a minuto. Alineaciones y estadísticas se consultan en summary y se muestran solo cuando están publicadas. Se filtran métricas de goles esperados y predicción. Los perfiles mantienen la cabecera con intento de foto del estadio. Compilación y firma correctas, 32 pruebas JVM. Pendiente: revisión visual de todas las pantallas en Android y cobertura de fotos por equipo. No se ha verificado equivalencia visual completa con el video.


## PulseSport 1.8.0 (13)

- El perfil separa resultados (recientes primero), partidos en vivo y próximos (ascendente). El historial se limita al calendario que devuelva ESPN para el equipo y la competición.
- El selector consulta el catálogo de temporadas de ESPN. Cada resultado verifica el año devuelto y sustituye la tabla histórica anterior. Una temporada en el catálogo puede carecer de tabla.
- Las tablas de fútbol muestran PTS, PJ, G, E, P, GF, GC y DG. NBA muestra porcentaje de victorias, PJ, G, P, PF, PC y diferencia. Se conservan escudos, grupos y desplazamiento horizontal.
- Se comprobaron metadatos del logo y el catálogo de temporadas para las diez competiciones. La muestra de 2023 devolvió filas agrupadas para Liga Colombia, Primera B, Libertadores, Sudamericana, Premier, LaLiga y NBA. Para Superliga, Copa Colombia y Recopa no hubo grupos en esa muestra. Queda pendiente documentar temporada por temporada y cubrir los años fuera de ESPN y las fases eliminatorias.
- Bucaramanga (6137), Once Caldas (2919) y Palmeiras (2029) tienen una ficha parcial con fuentes y una foto local vinculada al ID de ESPN. Quedan pendientes los campos faltantes de esas fichas, el palmarés completo y la verificación de los demás clubes. Para esos otros clubes todavía se consulta ESPN y Wikimedia.
- Quedan pendientes las curiosidades verificadas de las competiciones y el rediseño integral para teléfono y tablet.

### Créditos fotográficos

- Américo Montanini (antes Alfonso López). Ángel Páez, 2024. Versión reducida, con encuadre adaptado a la pantalla. [Ficha de Commons](https://commons.wikimedia.org/wiki/File:Estadio_Alfonso_L%C3%B3pez_-_2024.jpg) y [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
- Palogrande. Alcaldía de Manizales, 2024. Archivo original, con encuadre adaptado a la pantalla. [Ficha de Commons](https://commons.wikimedia.org/wiki/File:Estadio_Palogrande_vista_a%C3%A9rea.jpg) y [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/).
- Allianz Parque. Roberto Sabino, 2015. Versión reducida, con encuadre adaptado a la pantalla. [Ficha de Commons](https://commons.wikimedia.org/wiki/File:AllianzParquePanor%C3%A2mica.jpg) y [CC BY 2.0](https://creativecommons.org/licenses/by/2.0/).

### Validación de la entrega

Gradle ejecutó 32 pruebas unitarias sin fallos y compiló el APK 1.8.0 (13). Se verificó la firma v2. En el emulador se abrió el perfil de Bucaramanga con foto, crédito, ficha y resultados. En Liga Colombia se consultaron 2023 y 2024 y la segunda tabla reemplazó la anterior. Queda pendiente la validación en tablet, teléfono físico y otros perfiles.

## PulseSport 1.8.1 (14)

El perfil de cualquier equipo busca la foto por deporte e ID ESPN. El catálogo consulta las fotos de ESPN y Wikimedia, conserva las tres fotos locales y guarda en caché las descargas correctas. El pie de foto enlaza a la fuente. Para fútbol, la búsqueda en Wikidata exige que coincida el ID ESPN antes de usar el estadio.

El [inventario](docs/evidencias/cobertura_estadios_espn.json) registra los 186 equipos distintos observados en ESPN, las fuentes y el resultado de cada consulta. Se comprobaron 124 respuestas remotas de imagen y tres fotos locales. De los 59 restantes, cuatro URLs devolvieron límites HTTP 429 en las comprobaciones y 55 carecen de una referencia confirmada. Algunas sedes que ESPN devuelve corresponden a partidos puntuales; cuando se detectó esa discrepancia, se corrigió mediante una fuente del club o se dejó pendiente.

En el emulador se abrió Millonarios y se comprobó que se cargó la foto de El Campín. La comprobación remota distingue si la URL entrega una imagen; la inspección visual se hizo en el perfil de Millonarios.

## PulseSport 1.8.2 (15)

- El FC Barcelona figura con fundación del 29 de noviembre de 1899. Millonarios figura con fundación del 18 de junio de 1946, El Embajador y Ballet Azul como apodos, y el estadio Nemesio Camacho El Campín.
- Las fichas automáticas de fútbol validan el identificador ESPN en Wikidata. Se retiró el recurso a la primera página de Wikipedia encontrada por nombre, que podía describir una ciudad u otra entidad.
- El historial lista las fases de la temporada. Las tablas se consultan por fase; las rondas eliminatorias muestran sus encuentros, agrupando los partidos de un mismo cruce. ESPN sigue determinando la cobertura de años, tablas y marcadores.
- Se comprobaron respuestas del proveedor para Apertura y Clausura de Liga Colombia 2023, octavos de Copa Colombia 2023 y octavos, cuartos, semifinal y final de Libertadores 2023.

## PulseSport 1.8.3 (16)

Los perfiles incluyen Palmarés. La cobertura comprobada comprende cinco fichas curadas, campeonatos de las 30 franquicias NBA hasta 2025-26, ediciones de Libertadores en registros de la CONMEBOL y una parte de los torneos cortos de Colombia con registro oficial. La lista de campeonatos de clubes de fútbol es parcial.

Los proveedores y las atribuciones se muestran en Ajustes → Créditos. Las fotos de Stamford Bridge y del estadio Santiago Bernabéu se incluyen en la app con su licencia. Las fotos remotas se decodifican a un tamaño acotado.

[Auditoría por equipo](docs/evidencias/auditoria_equipos.json) de los 186 equipos del inventario. Hay 127 equipos con imagen local o una URL que entregó una imagen en las comprobaciones. Quedan 59 fotos pendientes y 178 fichas sin descripción corroborada en esta auditoría. La consulta por lotes recibió HTTP 429; el índice alternativo solo devolvió tres clubes con el identificador coincidente.

Gradle compiló la versión en C:\Users\pinga\Downloads\PulseSport. Se comprobó instalación y arranque en el emulador. Logcat mostró fallos del servicio UWB del emulador; en la prueba de arranque no se reprodujo un cierre de PulseSport.
