package co.edu.pulsesport;
import java.util.*;
/** Compara lecturas durante una sesión foreground. La primera lectura fija el punto de partida. */
public final class MatchAlerts {
 private final Map<String,Models.Match> before=new HashMap<>();
 public List<String> update(League league,Models.Match now,boolean watched){
  String key=league.name()+":"+now.id();Models.Match old=before.put(key,now);List<String> alerts=new ArrayList<>();
  if(old==null||!watched)return alerts;
  if(old.state().equals("pre")&&now.live())alerts.add("Comenzó: "+now.home().name()+" vs "+now.away().name());
  if(!old.state().equals("post")&&now.state().equals("post"))alerts.add("Terminó: "+now.home().name()+" "+now.score()+" "+now.away().name());
  if(!old.score().equals(now.score())&&!now.state().equals("pre"))alerts.add("Cambio de marcador: "+now.home().name()+" "+now.score()+" "+now.away().name());
  if(now.live()&&now.timeline().size()>old.timeline().size())for(int i=old.timeline().size();i<now.timeline().size();i++){
   String event=now.timeline().get(i);if(event.toLowerCase(Locale.ROOT).matches(".*(foul|falta|yellow|red|tarjeta).*"))alerts.add("Incidencia: "+event);
  }
  return alerts;
 }
}
