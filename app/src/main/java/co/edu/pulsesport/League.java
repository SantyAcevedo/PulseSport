package co.edu.pulsesport;
public enum League {
 COLOMBIA("Fútbol", "Liga Colombia", "soccer", "col.1", "Colombia", "leaguelogos/soccer/500/1543.png"),
 COLOMBIA_B("Fútbol", "Primera B Colombia", "soccer", "col.2", "Colombia", "leaguelogos/soccer/500/2295.png"),
 SUPERLIGA_COLOMBIA("Fútbol", "Superliga de Colombia", "soccer", "col.superliga", "Colombia", "leaguelogos/soccer/500-dark/2405.png"),
 COPA_COLOMBIA("Fútbol", "Copa Colombia", "soccer", "col.copa", "Colombia", "leaguelogos/soccer/500/2332.png"),
 LIBERTADORES("Fútbol", "CONMEBOL Libertadores", "soccer", "conmebol.libertadores", "Sudamérica", "leaguelogos/soccer/500/58.png"),
 SUDAMERICANA("Fútbol", "CONMEBOL Sudamericana", "soccer", "conmebol.sudamericana", "Sudamérica", "leaguelogos/soccer/500/1208.png"),
 RECOPA("Fútbol", "CONMEBOL Recopa", "soccer", "conmebol.recopa", "Sudamérica", "leaguelogos/soccer/500/2335.png"),
 PREMIER("Fútbol", "Premier League", "soccer", "eng.1", "Europa", "leaguelogos/soccer/500/23.png"),
 LALIGA("Fútbol", "LaLiga", "soccer", "esp.1", "Europa", "leaguelogos/soccer/500/15.png"),
 NBA("Baloncesto", "NBA", "basketball", "nba", "Baloncesto", "teamlogos/leagues/500/nba.png");
 public final String sport, title, apiSport, code, group, logo;
 League(String sport,String title,String apiSport,String code,String group,String logoPath){
  this.sport=sport;this.title=title;this.apiSport=apiSport;this.code=code;this.group=group;this.logo="https://a.espncdn.com/i/"+logoPath;
 }
 public String path(){return apiSport+"/"+code;}
 @Override public String toString(){return title;}
}
