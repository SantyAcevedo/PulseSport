package co.edu.pulsesport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.json.*;

/** El proveedor acepta un día por consulta, no un intervalo en dates. */
public final class ScoreboardQuery {
 private ScoreboardQuery() {}
 public static List<String> urls(League league,String selection) {
  String[] bounds=selection.split("-",-1);
  if(bounds.length>2)throw new IllegalArgumentException("Fecha de consulta inválida");
  LocalDate first=LocalDate.parse(bounds[0],DateTimeFormatter.BASIC_ISO_DATE);
  LocalDate last=bounds.length==2?LocalDate.parse(bounds[1],DateTimeFormatter.BASIC_ISO_DATE):first;
  if(last.isBefore(first)||last.isAfter(first.plusDays(2)))throw new IllegalArgumentException("La ventana debe contener de uno a tres días");
  List<String> urls=new ArrayList<>();
  for(LocalDate day=first;!day.isAfter(last);day=day.plusDays(1)) {
   urls.add("https://site.api.espn.com/apis/site/v2/sports/"+league.path()+"/scoreboard?dates="+day.format(DateTimeFormatter.BASIC_ISO_DATE)+"&limit=100");
  }
  return urls;
 }
 public static JSONObject merge(List<JSONObject> responses) throws JSONException {
  Map<String,JSONObject> events=new LinkedHashMap<>();
  for(JSONObject response:responses){JSONArray items=response.getJSONArray("events");
   for(int i=0;i<items.length();i++){JSONObject event=items.getJSONObject(i);events.put(event.getString("id"),event);}
  }
  JSONArray combined=new JSONArray();for(JSONObject event:events.values())combined.put(event);
  return new JSONObject().put("events",combined);
 }
}
