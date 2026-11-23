package co.edu.pulsesport;

import java.util.List;

record ClubProfile(String name, String stadium, String asset, String credit, String photoSource,
                   String license, String facts, List<String> sources) {
 static List<String> honors(String id){
  return switch(id){
   case "83" -> List.of("LaLiga · 29 títulos", "Copa del Rey · 32 títulos",
    "Supercopa de España · 16 títulos", "Copa de Europa / Champions League · 1991-92, 2005-06, 2008-09, 2010-11, 2014-15",
    "Mundial de Clubes · 2009, 2011, 2015", "Recopa de Europa · 1978-79, 1981-82, 1988-89, 1996-97",
    "Copa de Ferias · 1957-58, 1959-60, 1965-66");
   case "5484" -> List.of("Liga colombiana · 1949, 1951, 1952, 1953, 1959, 1961, 1962, 1963, 1964, 1972, 1978, 1987, 1988, 2012-II, 2017-II, 2023-I",
    "Copa Colombia · 1953, 1963, 2011, 2022", "Superliga de Colombia · 2018, 2024", "Copa Merconorte · 2001");
   case "6137" -> List.of("Liga colombiana · 2024-I");
   case "2919" -> List.of("Liga colombiana · 1950, 2003-I, 2009-I, 2010-II", "Copa Libertadores · 2004");
   case "2029" -> List.of("Copa Libertadores · 1999, 2020, 2021", "Recopa Sudamericana · 2022",
    "Copa do Brasil · 1998, 2012, 2015, 2020", "Campeonato Brasileño · 1960, 1967 (dos torneos), 1969, 1972, 1973, 1993, 1994, 2016, 2018, 2022, 2023");
   default -> List.of();
  };
 }
 static ClubProfile find(League league, String id) {
  if (league == League.NBA) return null;
  return switch (id) {
   case "83" -> new ClubProfile(
    "Fútbol Club Barcelona", "Spotify Camp Nou", "", "", "", "",
    "Fundación: 29 de noviembre de 1899\nApodos: Barça, azulgranas",
    List.of("https://www.fcbarcelona.es/es/ficha/643865/1899-09-nacimiento-y-",
     "https://www.fcbarcelona.es/es/football/primer-equipo/honours"));
   case "5484" -> new ClubProfile(
    "Millonarios Fútbol Club", "Estadio Nemesio Camacho El Campín", "", "", "", "",
    "Fundación: 18 de junio de 1946\nApodos: El Embajador, Ballet Azul",
    List.of("https://millonarios.com.co/nuestra-historia/",
     "https://iemp.gov.co/wp-content/uploads/2022/05/Revista-Rostros-y-Rastros-9.pdf",
     "https://millonarios.com.co/wp-content/uploads/2021/09/COMUNICADO-BOLETERI%CC%81A.pdf"));
   case "6137" -> new ClubProfile(
    "Atlético Bucaramanga", "Estadio Américo Montanini", "bucaramanga.jpg",
    "Ángel Páez, 2024 · CC BY 4.0",
    "https://commons.wikimedia.org/wiki/File:Estadio_Alfonso_L%C3%B3pez_-_2024.jpg",
    "https://creativecommons.org/licenses/by/4.0/",
    "Fundación: 11 de mayo de 1949\nApodo: Leopardo\nAforo de referencia: aproximadamente 25.000 (ENTerritorio, 2025)",
    List.of("https://www.dimayor.com.co/bucaramanga-2/",
      "https://ediciones.uis.edu.co/index.php/publicacionesuis/catalog/book/570",
      "https://dimoficial.com/wp-content/uploads/2026/03/DIM-vs-Bucaramanga.pdf",
      "https://www.enterritorio.gov.co/web/sites/default/files/2026-03/documentos/INFORMER%20DE%20GESTION%202025%20140326.pdf"));
   case "2919" -> new ClubProfile(
    "Once Caldas", "Estadio Palogrande", "once_caldas.jpg",
    "Alcaldía de Manizales, 2024 · CC BY-SA 4.0",
    "https://commons.wikimedia.org/wiki/File:Estadio_Palogrande_vista_a%C3%A9rea.jpg",
    "https://creativecommons.org/licenses/by-sa/4.0/",
    "Debut en la Dimayor: 1961",
    List.of("https://www.oncecaldas.com.co/nuestro-club",
      "https://dimayor.com.co/2020/05/14/historia-del-estadio-palogrande/"));
   case "2029" -> new ClubProfile(
    "Sociedade Esportiva Palmeiras", "Allianz Parque", "palmeiras.jpg",
    "Roberto Sabino, 2015 · CC BY 2.0",
    "https://commons.wikimedia.org/wiki/File:AllianzParquePanor%C3%A2mica.jpg",
    "https://creativecommons.org/licenses/by/2.0/",
    "Fundación: 26 de agosto de 1914\nApodo: Verdão\nRival: Corinthians (Derby Paulista)",
    List.of("https://www.palmeiras.com.br/memoria-alviverde/",
      "https://palmeiras.com.br/derby/recordes-e-estreias/",
      "https://www.palmeiras.com.br/galeria-de-titulos/"));
   default -> null;
  };
 }
}
