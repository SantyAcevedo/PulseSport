package co.edu.pulsesport;
public enum League {
 COLOMBIA("Fútbol", "Liga Colombia", "soccer", "col.1", "Colombia"),
 COLOMBIA_B("Fútbol", "Primera B Colombia", "soccer", "col.2", "Colombia"),
 SUPERLIGA_COLOMBIA("Fútbol", "Superliga de Colombia", "soccer", "col.superliga", "Colombia"),
 COPA_COLOMBIA("Fútbol", "Copa Colombia", "soccer", "col.copa", "Colombia"),
 LIBERTADORES("Fútbol", "CONMEBOL Libertadores", "soccer", "conmebol.libertadores", "Sudamérica"),
 SUDAMERICANA("Fútbol", "CONMEBOL Sudamericana", "soccer", "conmebol.sudamericana", "Sudamérica"),
 RECOPA("Fútbol", "CONMEBOL Recopa", "soccer", "conmebol.recopa", "Sudamérica"),
 PREMIER("Fútbol", "Premier League", "soccer", "eng.1", "Europa"),
 LALIGA("Fútbol", "LaLiga", "soccer", "esp.1", "Europa"),
 NBA("Baloncesto", "NBA", "basketball", "nba", "Baloncesto");
 public final String sport, title, apiSport, code, group;
 League(String sport,String title,String apiSport,String code,String group){
  this.sport=sport;this.title=title;this.apiSport=apiSport;this.code=code;this.group=group;
 }
 public String path(){return apiSport+"/"+code;}
 @Override public String toString(){return title;}
}
