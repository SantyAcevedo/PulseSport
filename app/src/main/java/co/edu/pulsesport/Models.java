package co.edu.pulsesport;
import org.json.*;
import java.util.*;
public final class Models {
 public record Team(String id,String name,String score,String logo) {}
 public record Match(String id,String date,String state,String status,Team home,Team away,List<String> timeline) {
  public boolean live(){return state.equals("in");}
  public String score(){return home.score()+" - "+away.score();}
 }
 public record Standing(String group,String rank,String team,String played,String wins,String losses,String points) {}
 public static String logo(JSONObject team) {
  String direct=team.optString("logo","");if(direct.startsWith("https://"))return direct;
  JSONArray logos=team.optJSONArray("logos");
  if(logos!=null)for(int i=0;i<logos.length();i++){
   JSONObject item=logos.optJSONObject(i);if(item!=null){String href=item.optString("href","");if(href.startsWith("https://"))return href;}
  }
  return "";
 }
 public static List<Match> matches(JSONObject root) throws JSONException {
  if(!root.has("events")) throw new JSONException("Respuesta sin events");
  List<Match> result=new ArrayList<>(); JSONArray es=root.getJSONArray("events");
  for(int i=0;i<es.length();i++) {
   JSONObject e=es.getJSONObject(i); JSONArray cs=e.optJSONArray("competitions"); if(cs==null||cs.length()==0)continue;
   JSONObject c=cs.getJSONObject(0); JSONArray rivals=c.optJSONArray("competitors"); if(rivals==null)continue;
   Team home=null,away=null;
   for(int j=0;j<rivals.length();j++) {JSONObject r=rivals.getJSONObject(j),t=r.optJSONObject("team");if(t==null)continue;
    Team team=new Team(t.optString("id"),t.optString("displayName","Equipo"),r.optString("score","—"),logo(t));
    if(r.optString("homeAway").equals("home"))home=team;else if(r.optString("homeAway").equals("away"))away=team;
   }
   if(home==null||away==null)continue;
   JSONObject status=c.optJSONObject("status");if(status==null)status=e.optJSONObject("status"); if(status==null)status=new JSONObject();
   JSONObject type=status.optJSONObject("type");if(type==null)type=new JSONObject();
   List<String> timeline=new ArrayList<>(); JSONArray ds=c.optJSONArray("details");
   if(ds!=null)for(int j=0;j<ds.length();j++) {JSONObject d=ds.getJSONObject(j);JSONObject clock=d.optJSONObject("clock"),dt=d.optJSONObject("type");
    String athlete="";JSONArray aa=d.optJSONArray("athletesInvolved");if(aa!=null&&aa.length()>0)athlete=aa.getJSONObject(0).optString("displayName");
    timeline.add((clock==null?"":clock.optString("displayValue"))+" · "+(dt==null?"Evento":dt.optString("text"))+" "+athlete);
   }
   result.add(new Match(e.getString("id"),e.optString("date"),type.optString("state"),type.optString("shortDetail",type.optString("description")),home,away,timeline));
  }return result;
 }
 public static List<Standing> standings(JSONObject root) throws JSONException {
  if(!root.has("children")&&!root.has("standings"))throw new JSONException("Respuesta sin clasificación");
  List<Standing> rows=new ArrayList<>();walk(root,root.optString("name","Clasificación"),rows);rows.sort(Comparator.comparing(Standing::group).thenComparingInt(s -> rankNumber(s.rank())));return rows;
 }
 private static int rankNumber(String rank){try{return Integer.parseInt(rank);}catch(Exception e){return Integer.MAX_VALUE;}}
 private static String played(Map<String,String> stats){if(stats.containsKey("gamesPlayed"))return stats.get("gamesPlayed");try{return String.valueOf(Integer.parseInt(stats.get("wins"))+Integer.parseInt(stats.get("losses")));}catch(Exception e){return "—";}}
 private static void walk(JSONObject node,String group,List<Standing> rows) throws JSONException {
  JSONObject st=node.optJSONObject("standings"); if(st!=null){JSONArray es=st.optJSONArray("entries");if(es!=null)for(int i=0;i<es.length();i++){
   JSONObject e=es.getJSONObject(i),t=e.optJSONObject("team");if(t==null)continue;Map<String,String> stats=new HashMap<>();JSONArray ss=e.optJSONArray("stats");
   if(ss!=null)for(int j=0;j<ss.length();j++){JSONObject s=ss.getJSONObject(j);stats.put(s.optString("name"),s.optString("displayValue",s.optString("value","—")));}
   rows.add(new Standing(group,stats.getOrDefault("rank",stats.getOrDefault("playoffSeed",String.valueOf(i+1))),t.optString("displayName"),played(stats),stats.getOrDefault("wins","—"),stats.getOrDefault("losses","—"),stats.getOrDefault("winPercent",stats.getOrDefault("points","—"))));
  }}
  JSONArray children=node.optJSONArray("children");if(children!=null)for(int i=0;i<children.length();i++){JSONObject c=children.getJSONObject(i);walk(c,c.optString("name",group),rows);}
 }
}
