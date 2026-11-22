# PulseSport para Android

App nativa académica escrita en Java. Consulta partidos, calendarios por fecha, eventos, clasificaciones y equipos favoritos. Incluye fútbol (Premier League, LaLiga y Liga Colombia) y baloncesto (NBA).

## Abrir en Android Studio

1. Descomprime el ZIP.
2. En Android Studio selecciona **Open** y abre la carpeta **PulseSport**, donde está `settings.gradle`.
3. Espera a que termine **Gradle Sync**. Instala SDK Platform 35 y Build Tools 35.0.0 si los solicita. Usa el JDK integrado, versión 17 o superior compatible.
4. Selecciona un emulador o un teléfono conectado con depuración USB y pulsa **Run ▶**.

No necesitas clave de API, cuenta ni servidor propio. Sí necesitas Internet para descargar Gradle, el plugin y el SDK la primera vez.

## Comandos en Windows

En la pestaña **Terminal** de Android Studio, ubicada en la carpeta PulseSport:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

APK generado: `app\build\outputs\apk\debug\app-debug.apk`.
Reporte de pruebas: `app\build\reports\tests\testDebugUnitTest\index.html`.
En Linux/macOS usa `./gradlew`.

El APK entregado se compiló con AAPT2, ECJ, D8, zipalign y apksigner del SDK 35, porque no se pudo descargar la distribución completa de Gradle en el entorno de construcción. Se verificó la firma v2/v3. La importación y compilación mediante Android Studio/Gradle quedan por verificar en tu equipo. No se ejecutaron emuladores desde este entorno.

## Instalar el APK entregado

Copia `PulseSport-debug.apk` al teléfono, ábrelo desde Archivos y permite instalar desde esa aplicación cuando Android lo solicite. Requiere Android 8 o posterior. Es una compilación de depuración para la evaluación académica. Si instalas después otra compilación firmada con una clave distinta, desinstala primero la anterior; esto borra favoritos.

## Uso

- Elige disciplina, liga y fecha. Las flechas recorren días; tocar la fecha abre un calendario.
- `En vivo` filtra partidos de la fecha elegida. `Hoy` vuelve al día actual.
- Toca la estrella de un equipo real para guardarlo. Los favoritos persisten localmente.
- Abre un partido para ver las incidencias disponibles. El detalle actualiza el marcador cada 60 segundos mientras está visible y permite actualizar manualmente.
- En **Favoritos** puedes consultar la liga del equipo, quitarlo o activar las alertas.
- Desde **Ajustes** puedes activar una demostración claramente identificada con equipos ficticios. Ese modo no guarda favoritos ni emite alertas y no ofrece tablas ficticias.

## API y límites

Fuente: endpoints públicos de ESPN, sin autenticación ni garantía de disponibilidad o de uso como API comercial. App independiente sin afiliación con ESPN. Los datos no son generados por IA. La cobertura, los idiomas y la demora dependen del proveedor.

| Función | Base y recurso |
| --- | --- |
| Marcadores y calendario | `https://site.api.espn.com/apis/site/v2/sports/{sport}/{league}/scoreboard?dates=AAAAMMDD&limit=100` |
| Tablas | `https://site.web.api.espn.com/apis/v2/sports/{sport}/{league}/standings` |
| Jugadas | `https://site.api.espn.com/apis/site/v2/sports/{sport}/{league}/summary?event={id}` |

| Liga | sport | league |
| --- | --- | --- |
| Premier League | soccer | eng.1 |
| LaLiga | soccer | esp.1 |
| Liga Colombia | soccer | col.1 |
| NBA | basketball | nba |

Se comprobaron ocho rutas el 21 de noviembre de 2026; ver `docs/evidencias/comprobacion_api.json`. HTTP 200 no garantiza precisión ni disponibilidad futura. TheSportsDB no se utiliza porque su documentación reserva los livescores a premium.

Marcadores: caché de 60 s. Tablas: 15 min. Tras errores, las pantallas de consulta reintentan cada 120 s. La selección de fecha consulta cada uno de los tres días por separado, combina eventos por ID y filtra por día local; máximo 100 eventos por consulta diaria. La caché puede ser eliminada por Android. Los datos guardados siempre muestran fecha y advertencia cuando no se pudieron actualizar.

## Alertas

Se usa una sesión de servicio foreground `dataSync`, iniciada por el usuario, de hasta dos horas, con consulta cada 60 s y notificación persistente para detenerla. Solo se consultan ligas con favoritos. Android 13+ pide permiso de notificaciones.

La primera lectura establece una referencia y no avisa. Las siguientes comparan el marcador por liga y partido. Un aviso puede agrupar varias anotaciones o reflejar una corrección. No hay backend ni FCM. No se garantizan alertas instantáneas, continuidad tras cierre forzado ni recuperación de avisos perdidos. Las restricciones de batería y conectividad pueden interrumpir el seguimiento. Reiniciar la sesión reinicia las referencias.

## Estructura

- `MainActivity`: pantallas y ciclo de vida.
- `Repository`: HTTPS, validación y caché.
- `Models`, `League`: interpretación de datos y ligas.
- `Store`: favoritos y preferencias privadas.
- `ScoreChanges`: reglas de comparación de marcadores.
- `TrackingService`: sesión de seguimiento y notificaciones.
- `app/src/test`: pruebas de procesamiento y alertas.
- `docs`: informe Word, diagramas, mockups, protocolo y registros.

## Estado real de validación

Ver `SEGUIMIENTO.md`. Se ejecutaron 22 pruebas JUnit en JVM y comprobaciones de endpoints; se compiló y verificó un APK. Quedan pendientes la ejecución en tres emuladores, la instalación física, la revisión de Android Studio/Gradle y la publicación en GitHub. Los mockups no son capturas de ejecución.

## Publicar en GitHub

Crea en tu cuenta un repositorio vacío llamado `PulseSport`, sin README inicial. En Terminal, dentro de esta carpeta, ejecuta y reemplaza `TU_USUARIO`:

```powershell
git init
git add .
git commit -m "Implementa PulseSport Android y documentacion"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/PulseSport.git
git push -u origin main
```

Autentícate con GitHub cuando lo solicite. No subas claves de firma, `local.properties`, contraseñas ni carpetas de compilación. Adjunta el APK a una Release si el docente lo requiere. Añade aquí la URL y capturas reales de tus pruebas.

## Corrección 1.0.1 del calendario y la conexión

La primera versión enviaba intervalos en `dates` (por ejemplo `20261120-20261122`), que el proveedor rechazó con HTTP 400. Ahora cada petición utiliza una fecha `AAAAMMDD`. Se conservan la ventana de tres días, el filtro de fecha local y la caché diaria. El servicio de favoritos también usa consultas diarias.

Reintentar muestra el estado de carga y fuerza nuevas peticiones. Los errores HTTP, DNS, espera y formato reciben explicaciones distintas; **Ver detalle** permite consultar la causa exacta. Si solo parte de la consulta falla, se conservan los partidos disponibles con advertencia y opción de reintento. Un fallo al escribir la caché no descarta los datos recién descargados.

Se comprobaron 12 consultas diarias reales y se ejecutaron 22 pruebas JUnit, incluidas regresiones de fechas, duplicados, caché, fallos parciales y reintentos. Falta confirmar la nueva versión en el dispositivo del estudiante. Para actualizar el código, abrir esta versión en Android Studio y volver a pulsar Run. Si se instala el APK por separado y aparece conflicto de firma con la compilación de Android Studio, desinstalar la anterior primero; se pierden sus favoritos.

## Versión 1.1 · portada y personalización

Al abrir por primera vez se eligen equipos de las cuatro ligas disponibles mediante búsqueda, seguidos de las ligas favoritas. En Inicio aparecen los partidos en vivo y los próximos seis días de las ligas elegidas, con opción de cargar los seis días siguientes. Los equipos seguidos tienen prioridad entre encuentros del mismo estado. Un mensaje al final permite añadir más ligas. Las ligas disponibles siguen siendo Premier League, LaLiga, Liga Colombia y NBA; la búsqueda de equipos se limita a esas competiciones.

Los escudos se descargan mediante HTTPS desde el dominio de imágenes indicado por la fuente y se guardan en la caché privada. Un escudo que el proveedor no entregue se sustituye por el icono de PulseSport. Las fichas de equipo muestran los datos públicos, la posición en la liga seleccionada y el calendario que publica ESPN. Desde un partido se puede tocar el escudo o el nombre para abrir la ficha. La ficha de liga muestra la tabla actual.

Durante una sesión de seguimiento activada por el usuario, la aplicación compara lecturas de los partidos de ligas o equipos favoritos. Puede avisar del inicio, final, cambio de marcador y de ciertas faltas o tarjetas cuando aparecen en las incidencias publicadas. La primera lectura establece el estado inicial. No hay notificaciones push ni garantía de todas las faltas, goles o eventos: dependen de la cobertura, la consulta periódica y Android. Historial completo de campeones, palmarés, datos curiosos y goleadores no se presentan como hechos porque el proveedor no devuelve una fuente uniforme y verificable para esas secciones; la ficha indica su ausencia.

Esta versión compila con las herramientas SDK y pasa 24 pruebas JVM. Todavía requiere ejecución y revisión visual en un teléfono y emuladores. Si Android Studio ha instalado antes una compilación propia firmada con otra clave, usar Run para instalar su nueva compilación o desinstalar la anterior antes de instalar el APK descargado.

## Corrección 1.1.1

Se corrigió el bucle del asistente inicial. Al pulsar **Siguiente · ligas**, la aplicación abre la selección de ligas; **Terminar y ver partidos** guarda la configuración y abre Inicio. El botón de Ajustes funciona durante el asistente. Abre el proyecto actualizado en Android Studio y pulsa **Run** para instalar la nueva compilación.

## Versión 1.2 · rediseño de lectura rápida

La portada usa fondo `#0B1220`, tarjetas `#172536`, texto `#F4F7FB` y acentos `#55E6B1` para partidos en vivo y `#54B8FF` para encuentros próximos. Cada tarjeta compacta muestra la liga, estado, escudos, equipos, marcador u hora y fecha; los equipos favoritos se identifican con una estrella. Se puede tocar toda la tarjeta para abrir el partido.

La barra inferior tiene accesos a Inicio, En vivo, Favoritos y Ligas. Los filtros de la portada permiten consultar los partidos próximos, los encuentros en vivo y una liga favorita concreta. La ficha del partido incluye marcador central, escudos que abren las fichas de equipo y pestañas Resumen, Cronología y Tabla. La tabla presenta posición, equipo, partidos jugados y puntos en columnas compactas. No se muestran posesión, momentum ni goleadores si la fuente no proporciona esos datos.

La APK de esta entrega usa `versionCode 5` y `versionName 1.2.0`. Compila y conserva las 24 pruebas JVM aprobadas. La apariencia y las interacciones aún deben comprobarse en tres emuladores y en un teléfono físico para la evidencia académica.


## Sincronización de Gradle 8.14.7

El wrapper de este proyecto usa Gradle **8.14.7**, fijado en `gradle/wrapper/gradle-wrapper.properties`, con verificación SHA-256 de la distribución. Android Studio descargará esa versión la primera vez que abra el proyecto; requiere conexión a Internet. Usa el JDK integrado de Android Studio (17 o superior). El plugin Android (`com.android.application` 8.9.1) mantiene su versión; la versión de Gradle que pedía actualizar Android Studio es la del wrapper. El APK de esta entrega no cambia porque este ajuste solo afecta la sincronización y construcción del proyecto.

## Versión 1.3 · Inicio y tablet

La cabecera ya no tiene engranaje de configuración. **Ajustes** es el quinto destino fijo de la barra inferior, con una zona táctil de 64 dp de alto. También está accesible durante el asistente inicial. La barra inferior tiene Inicio, En vivo, Favoritos, Ligas y Ajustes.

La pantalla de Inicio se inspira en la jerarquía de la referencia: pestañas **Favoritos / Resultados**, filtros rápidos, contador de partidos, encabezados de liga y tarjetas con escudos, estado y marcador u horario centrado. Favoritos recoge las ligas seguidas y los partidos de equipos seguidos; Resultados consulta las cuatro ligas disponibles. No incluye apuestas, cuotas, publicidad ni predicciones. La vista limita el contenido a 720 dp de ancho y lo centra en tablets para evitar tarjetas excesivamente anchas.

La versión 1.3.0 (versionCode 6) compila y pasa 24 pruebas JVM. Las dimensiones y el comportamiento táctil todavía deben verificarse visualmente en el dispositivo real y emuladores del estudiante.
