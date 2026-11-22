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

No convertir los estados pendientes en completados sin ejecutar las pruebas y adjuntar evidencia real. Los documentos no afirman que la app se haya instalado en un teléfono ni que GitHub esté publicado.

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
- Pendiente: verificar en dispositivo y emuladores el aspecto, la navegación, los datos recibidos y las notificaciones en un partido real. No se ha publicado el repositorio GitHub.

## Corrección 1.1.1 de navegación inicial

La versión 1.1 redirigía a la selección de equipos en cada redibujo mientras no se hubiera terminado el asistente. Por eso Siguiente, Ajustes y la selección de ligas no avanzaban. Ahora la redirección ocurre solo al abrir la aplicación por primera vez. Siguiente abre ligas, Terminar guarda el fin del asistente y Ajustes puede abrirse durante el proceso. APK compilado y 24 pruebas unitarias existentes aprobadas; falta comprobar las pulsaciones en emulador o teléfono.

## Rediseño 1.2.0

- Hecho: tarjetas compactas con escudos, marcador u horario, estados visibles y estrellas para equipos favoritos; paleta oscura de alto contraste.
- Hecho: barra inferior Inicio, En vivo, Favoritos y Ligas; filtros de portada, selector de liga favorita, detalle con Resumen, Cronología y Tabla; filas alineadas de clasificación.
- Verificado: APK versionCode 5 compilado y firma v2/v3 validada; 24 pruebas JVM satisfactorias.
- Pendiente: evaluación visual y funcional en tres emuladores y un teléfono físico; capturas reales y publicación en GitHub por el estudiante.


## Gradle 8.14.7

- Actualizado `gradle/wrapper/gradle-wrapper.properties` de 8.11.1 a 8.14.7 con checksum oficial SHA-256.
- Verificado el destino de descarga mediante `./gradlew --version`; la descarga y sincronización completas no pudieron ejecutarse aquí porque este entorno no tiene acceso a `services.gradle.org`.
- Pendiente de comprobación en Android Studio del estudiante: abrir este ZIP actualizado y esperar la sincronización con Internet.

## Interfaz 1.3.0

- Completado: Ajustes en la barra inferior con destino táctil de 64 dp; eliminación del engranaje pequeño.
- Completado: Inicio con pestañas Favoritos y Resultados, filtros, contador, ligas y tarjetas centradas. Ancho del contenido limitado a 720 dp para tablets. Ninguna sección de apuestas.
- Verificado: compilación de APK versionCode 6, firma validada y 24 pruebas JVM correctas.
- Pendiente: capturas y prueba de interacción en tablet, tres emuladores y teléfono físico; GitHub.

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
- Pendiente: comprobar respuestas de los seis endpoints en teléfono con conexión, tres emuladores y repositorio GitHub real.
