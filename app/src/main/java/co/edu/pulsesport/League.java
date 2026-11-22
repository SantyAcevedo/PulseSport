package co.edu.pulsesport;
public enum League {
 PREMIER("Fútbol", "Premier League", "soccer", "eng.1"),
 LALIGA("Fútbol", "LaLiga", "soccer", "esp.1"),
 COLOMBIA("Fútbol", "Liga Colombia", "soccer", "col.1"),
 NBA("Baloncesto", "NBA", "basketball", "nba");
 public final String sport, title, apiSport, code;
 League(String s,String t,String a,String c){sport=s;title=t;apiSport=a;code=c;}
 public String path(){return apiSport+"/"+code;}
 @Override public String toString(){return title;}
}
