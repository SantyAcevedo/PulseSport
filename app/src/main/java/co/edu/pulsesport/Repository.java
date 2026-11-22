package co.edu.pulsesport;
import android.content.Context;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public final class Repository {
 public record Result(JSONObject json,boolean cached,long time,String warning) {}
 interface AssetReader {InputStream open(String name) throws IOException;}
 interface Connections {HttpURLConnection open(String url) throws IOException;}
 private final File cacheDir;private final AssetReader assets;private final Connections connections;
 public Repository(Context c){this(c.getApplicationContext().getCacheDir(),name->c.getApplicationContext().getAssets().open(name),url->(HttpURLConnection)new URL(url).openConnection());}
 Repository(File cacheDir,AssetReader assets,Connections connections){this.cacheDir=cacheDir;this.assets=assets;this.connections=connections;}
 public Result scores(League l,String date,boolean force) throws Exception {
  java.util.List<JSONObject> responses=new java.util.ArrayList<>();
  java.util.Set<String> warnings=new java.util.LinkedHashSet<>();
  long oldest=Long.MAX_VALUE;boolean allCached=true;Exception failed=null;
  for(String url:ScoreboardQuery.urls(l,date)){
   if(Thread.currentThread().isInterrupted())throw new java.io.InterruptedIOException("Consulta cancelada");
   try{Result day=fetch(url,force,60000,false);responses.add(day.json());oldest=Math.min(oldest,day.time());allCached&=day.cached();if(!day.warning().isEmpty())warnings.add(day.warning());}
   catch(Exception e){failed=e;warnings.add("Consulta incompleta: "+DataErrors.describe(e));}
  }
  if(responses.isEmpty())throw failed==null?new IOException("Sin respuestas del proveedor"):failed;
  return new Result(ScoreboardQuery.merge(responses),allCached,oldest,String.join(" ",warnings));
 }
 public Result teams(League l) throws Exception{return fetch("https://site.api.espn.com/apis/site/v2/sports/"+l.path()+"/teams?limit=100",false,86400000,false);}
 public Result team(League l,String id) throws Exception{return fetch("https://site.api.espn.com/apis/site/v2/sports/"+l.path()+"/teams/"+id,false,86400000,false);}
 public Result schedule(League l,String id) throws Exception{return fetch("https://site.api.espn.com/apis/site/v2/sports/"+l.path()+"/teams/"+id+"/schedule",false,900000,false);}
 public Result standings(League l,boolean force) throws Exception {return fetch("https://site.web.api.espn.com/apis/v2/sports/"+l.path()+"/standings",force,900000,true);}
 public Result summary(League l,String id) throws Exception {return fetch("https://site.api.espn.com/apis/site/v2/sports/"+l.path()+"/summary?event="+id,false,60000,false);}
 private Result fetch(String url,boolean force,long ttl,boolean standings) throws Exception {
  File f=new File(cacheDir,"api-"+Integer.toHexString(url.hashCode())+".json");JSONObject saved=null;long time=0;
  if(f.exists())try{JSONObject envelope=new JSONObject(new String(java.nio.file.Files.readAllBytes(f.toPath()),StandardCharsets.UTF_8));saved=envelope.getJSONObject("data");time=envelope.getLong("time");}catch(Exception ignored){}
  if(!force&&saved!=null&&System.currentTimeMillis()-time<ttl)return new Result(saved,true,time,"");
  HttpURLConnection conn=null;
  try {
   conn=connections.open(url);conn.setConnectTimeout(10000);conn.setReadTimeout(12000);conn.setRequestProperty("Accept","application/json");conn.setRequestProperty("User-Agent","PulseSport-Educational/1.0");
   int code=conn.getResponseCode();if(code!=200)throw new DataErrors.HttpFailure(code);
   byte[] bytes;try(InputStream in=conn.getInputStream()){bytes=readBytes(in);}
   JSONObject data=new JSONObject(new String(bytes,StandardCharsets.UTF_8));
   if(url.contains("scoreboard"))Models.matches(data);else if(standings)Models.standings(data);
   long now=System.currentTimeMillis();JSONObject env=new JSONObject().put("time",now).put("data",data);
   // Cada solicitud escribe un temporal propio; el reemplazo es atómico entre UI y servicio.
   try {File tmp=File.createTempFile("api-", ".tmp",cacheDir);
   try{java.nio.file.Files.write(tmp.toPath(),env.toString().getBytes(StandardCharsets.UTF_8));java.nio.file.Files.move(tmp.toPath(),f.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);}finally{tmp.delete();}}catch(IOException|SecurityException cacheError){java.util.logging.Logger.getLogger("PulseSport").log(java.util.logging.Level.WARNING,"No se pudo guardar la caché",cacheError);}
   return new Result(data,false,now,"");
  }catch(Exception e){if(saved!=null)return new Result(saved,true,time,"Sin actualizar: "+DataErrors.describe(e));throw e;}
  finally{if(conn!=null)conn.disconnect();}
 }
 private static byte[] readBytes(InputStream in) throws IOException {ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);return out.toByteArray();}
 public Result demo(League l) throws Exception {
  try(InputStream in=assets.open(l==League.NBA?"demo_nba.json":"demo_soccer.json")){return new Result(new JSONObject(new String(readBytes(in),StandardCharsets.UTF_8)),false,0,"DEMOSTRACIÓN · datos ficticios");}
 }
}
