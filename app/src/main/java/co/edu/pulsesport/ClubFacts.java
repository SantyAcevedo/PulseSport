package co.edu.pulsesport;
import android.app.Activity;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;
/** Structured public facts from Wikidata. Missing claims are explicitly labelled. */
public final class ClubFacts {
 static JSONObject get(String url)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);c.setRequestProperty("User-Agent","PulseSport/1.8 educational");
  try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return new JSONObject(out.toString("UTF-8"));}finally{c.disconnect();}
 }
 static JSONObject entity(String id)throws Exception{return get("https://www.wikidata.org/wiki/Special:EntityData/"+id+".json").getJSONObject("entities").getJSONObject(id);}
 static List<JSONObject> values(JSONObject entity,String property){List<JSONObject> result=new ArrayList<>();JSONArray claims=entity.optJSONObject("claims")==null?null:entity.optJSONObject("claims").optJSONArray(property);if(claims!=null)for(int i=0;i<claims.length();i++){JSONObject claim=claims.optJSONObject(i);if(claim==null||"deprecated".equals(claim.optString("rank")))continue;JSONObject snak=claim.optJSONObject("mainsnak");JSONObject value=snak==null?null:snak.optJSONObject("datavalue");if(value!=null)result.add(value);}return result;}
 static String label(JSONObject e){JSONObject labels=e.optJSONObject("labels");if(labels==null)return "";JSONObject l=labels.optJSONObject("es");if(l==null)l=labels.optJSONObject("en");return l==null?"":l.optString("value");}
 static JSONObject find(Models.Team team)throws Exception{
  JSONObject response=get("https://www.wikidata.org/w/api.php?action=wbsearchentities&format=json&language=es&uselang=es&type=item&limit=8&search="+URLEncoder.encode(team.name(),"UTF-8"));
  JSONArray matches=response.getJSONArray("search");
  for(int i=0;i<matches.length();i++){
   JSONObject candidate=entity(matches.getJSONObject(i).getString("id"));
   if(values(candidate,"P13590").stream().anyMatch(value->team.id().equals(value.optString("value"))))
    return candidate;
  }
  throw new IOException("No se encontró una ficha con el mismo identificador ESPN");
 }
 static String describe(JSONObject e,String property)throws Exception{
  List<String> strings=new ArrayList<>();for(JSONObject v:values(e,property)){Object value=v.opt("value");if(value instanceof JSONObject){JSONObject obj=(JSONObject)value;
   if(obj.has("id"))strings.add(label(entity(obj.getString("id"))));else if(obj.has("time"))strings.add(obj.optString("time").replace("+","").split("T")[0]);else if(obj.has("amount"))strings.add(obj.optString("amount").replace("+",""));else if(obj.has("text"))strings.add(obj.optString("text"));
  }else if(value!=null)strings.add(String.valueOf(value));}
  return strings.isEmpty()?"":String.join(" · ",strings);
 }
 static JSONObject catalog(android.content.Context context,String name){
  try(InputStream in=context.getAssets().open(name);
      ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] buffer=new byte[4096];
   int count;
   while((count=in.read(buffer))!=-1)out.write(buffer,0,count);
   return new JSONObject(out.toString("UTF-8"));
  }catch(IOException|JSONException error){
   android.util.Log.w("PulseSport","No se pudo leer "+name,error);
   return new JSONObject();
  }
 }
 public static void load(Activity activity,LinearLayout parent,League league,Models.Team team,
                         java.util.function.Consumer<String> source){
  LinearLayout fields=new LinearLayout(activity);fields.setOrientation(1);parent.addView(fields);
  TextView loading=new TextView(activity);loading.setTextColor(0xffaabbcb);loading.setText("Consultando ficha del club…");fields.addView(loading);
  new Thread(()->{
   Map<String,String> facts=new LinkedHashMap<>();
   List<String> origins=new ArrayList<>();
   try{
    JSONObject entry=catalog(activity,"club_facts_catalog.json").optJSONObject(league.apiSport+":"+team.id());
    if(entry!=null){
     String date=entry.optString("foundation");
     if(!date.isBlank()){
      java.time.LocalDate day=java.time.LocalDate.parse(date);
      facts.put("Fundación", day.format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",new Locale("es"))));
     }
     putList(facts,"Apodos",entry.optJSONArray("nicknames"),", ");
     JSONArray titles=entry.optJSONArray("officialTitles");
     if(titles==null)titles=entry.optJSONArray("titles");
     putList(facts,"Palmarés",titles,"\n");
     for(String key:List.of("source","officialSource")){
      String url=entry.optString(key);
      if(!url.isEmpty())origins.add(url);
     }
    }else if(league!=League.NBA){
     JSONObject e=find(team);
     String foundation=describe(e,"P571"),nicknames=describe(e,"P1449");
     if(!foundation.isBlank())facts.put("Fundación",foundation);
     if(!nicknames.isBlank())facts.put("Apodos",nicknames);
     String titles=describe(e,"P2522");
     if(!titles.isBlank())facts.put("Palmarés",titles);
     origins.add("https://www.wikidata.org/wiki/"+e.getString("id"));
    }
   }catch(Exception error){
    android.util.Log.w("PulseSport","Ficha pendiente para "+team.id(),error);
   }
   if(facts.isEmpty())facts.put("Ficha del club","Datos pendientes de verificar.");
   facts.putIfAbsent("Palmarés","Títulos pendientes de verificar.");
   activity.runOnUiThread(()->{
    for(String link:origins)source.accept(link);
    if(activity.isFinishing()||activity.isDestroyed())return;
    fields.removeAllViews();
    for(Map.Entry<String,String> f:facts.entrySet()){
     TextView text=new TextView(activity);text.setText(f.getKey()+"\n"+f.getValue());text.setTextSize(15);
     text.setTextColor(0xfff4f7fb);text.setPadding(12,16,12,16);fields.addView(text);
    }
   });
  }).start();
 }
 private static void putList(Map<String,String> fields,String label,JSONArray values,String separator){
  if(values==null||values.length()==0)return;
  List<String> items=new ArrayList<>();
  for(int i=0;i<values.length();i++){
   String value=values.optString(i);
   if(!value.isBlank())items.add(value);
  }
  if(!items.isEmpty())fields.put(label,String.join(separator,items));
 }
}
