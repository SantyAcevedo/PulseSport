package co.edu.pulsesport;
import android.content.Context;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import org.json.*;
/** Wikimedia article thumbnails: never substitutes another team's stadium. */
public final class StadiumPhoto {
 private static final ExecutorService pool=Executors.newFixedThreadPool(2);
 static void fromAsset(Context context,ImageView image,ClubProfile profile){
  pool.execute(()->{
   try(InputStream in=context.getAssets().open("stadiums/"+profile.asset())){
    android.graphics.Bitmap bitmap=BitmapFactory.decodeStream(in);
    if(bitmap==null)throw new IOException("Imagen de estadio no válida");
    image.post(()->{
     image.setImageBitmap(bitmap);
     image.setContentDescription(profile.stadium());
    });
   }catch(IOException error){
    android.util.Log.w("PulseSport","No se pudo abrir la foto: "+profile.asset(),error);
   }
  });
 }
 record Photo(String caption,String source) {}
 private record Reference(String stadium,String image,String source) {}
 static void into(Context context,ImageView view,League league,Models.Team team,Repository api,
                  java.util.function.Consumer<Photo> status){
  Context app=context.getApplicationContext();
  String id=team.id();
  pool.execute(()->{
   String stadium="";
   try{
    Reference reference=catalog(app,league,id);
    if(reference==null){
     JSONObject venue=api.homeVenue(league,id);
     if(venue!=null){
      stadium=venue.optString("fullName");
      reference=espn(venue,league,id);
     }
     if(stadium.isBlank()&&league!=League.NBA)stadium=wikidataVenue(team);
     if(reference==null)reference=wikimedia(stadium);
    }
    stadium=reference.stadium();
    android.graphics.Bitmap bitmap=reference.image().startsWith("asset:")
      ?asset(app,reference.image().substring(6)):download(app,reference.image());
    Photo photo=new Photo(stadium,reference.source());
    view.post(()->{
     view.setImageBitmap(bitmap);
     view.setContentDescription(photo.caption());
     status.accept(photo);
    });
   }catch(Exception error){
    android.util.Log.w("PulseSport","Foto no disponible para "+league.code+"/"+id,error);
    String message=stadium.isBlank()?"Foto del estadio no disponible":stadium+" · Foto no disponible";
    view.post(()->status.accept(new Photo(message,"")));
   }
  });
 }
 private static volatile JSONObject stadiumCatalog;
 private static Reference catalog(Context context,League league,String id)throws Exception{
  JSONObject catalog=stadiumCatalog;
  if(catalog==null){
   try(InputStream in=context.getAssets().open("stadium_catalog.json")){
    catalog=new JSONObject(new String(readCatalog(in),java.nio.charset.StandardCharsets.UTF_8));
    stadiumCatalog=catalog;
   }catch(FileNotFoundException missing){return null;}
  }
  JSONObject entry=catalog.optJSONObject(league.apiSport+":"+id);
  if(entry==null)return null;
  if(entry.optBoolean("blocked"))throw new IOException("La sede de ESPN requiere verificación");
  String file=entry.optString("asset");
  String image=file.isEmpty()?entry.getString("image"):"asset:"+file;
  return new Reference(entry.getString("stadium"),image,entry.getString("source"));
 }
 private static byte[] readCatalog(InputStream in)throws IOException{
  ByteArrayOutputStream buffer=new ByteArrayOutputStream();
  byte[] chunk=new byte[4096];
  int size;
  while((size=in.read(chunk))!=-1)buffer.write(chunk,0,size);
  return buffer.toByteArray();
 }
 private static Reference espn(JSONObject venue,League league,String id){
  JSONArray images=venue.optJSONArray("images");
  if(images==null)return null;
  for(int i=0;i<images.length();i++){
   JSONObject image=images.optJSONObject(i);
   if(image==null)continue;
   String href=image.optString("href");
   if(href.startsWith("https://a.espncdn.com/i/venues/"))
    return new Reference(venue.optString("fullName"),href,
      "https://www.espn.com/"+(league==League.NBA?"nba":"soccer")+"/team/_/id/"+id);
  }
  return null;
 }
 private static String wikidataVenue(Models.Team team)throws Exception{
  JSONObject club=ClubFacts.find(team);
  java.util.List<JSONObject> venues=ClubFacts.values(club,"P115");
  if(venues.size()!=1)throw new IOException("Wikidata no identifica una sede única");
  return ClubFacts.label(ClubFacts.entity(venues.get(0).getJSONObject("value").getString("id")));
 }
 private static Reference wikimedia(String stadium)throws Exception{
  if(stadium.isBlank())throw new IOException("Estadio sin nombre");
  for(String language:new String[]{"es","en","pt"}){
   try{
    String query="https://"+language+".wikipedia.org/w/api.php?action=query&format=json&formatversion=2"
      +"&prop=pageimages%7Cpageprops&piprop=name%7Cthumbnail&pilicense=free&pithumbsize=800&redirects=1&titles="
      +URLEncoder.encode(stadium,"UTF-8");
    JSONObject page=json(query).getJSONObject("query").getJSONArray("pages").getJSONObject(0);
    JSONObject props=page.optJSONObject("pageprops"), thumbnail=page.optJSONObject("thumbnail");
    if(props==null||props.has("disambiguation")||thumbnail==null)continue;
    String description=props.optString("wikibase-shortdesc").toLowerCase(java.util.Locale.ROOT);
    if(!description.matches(".*(stadi|estadi|estádi|arena|ballpark|football ground|cancha|sports venue).*"))continue;
    String filename=page.optString("pageimage");
    String image=thumbnail.optString("source");
    if(filename.isBlank()||!image.contains("/wikipedia/commons/")
        ||!new URI(image).getPath().toLowerCase(java.util.Locale.ROOT).matches(".*\\.(jpg|jpeg|webp)$"))continue;
    return new Reference(stadium,image,"https://commons.wikimedia.org/wiki/File:"
      +URLEncoder.encode(filename.replace(' ','_'),"UTF-8").replace("+","%20"));
   }catch(IOException|JSONException error){
    android.util.Log.d("PulseSport","Consulta Wikimedia fallida para "+stadium+" ("+language+")",error);
   }
  }
  throw new IOException("Sin fotografía de Wikimedia para el estadio identificado");
 }
 private static JSONObject json(String url)throws IOException,JSONException{
  return new JSONObject(new String(read(url,512_000),java.nio.charset.StandardCharsets.UTF_8));
 }
 private static android.graphics.Bitmap asset(Context context,String file)throws IOException{
  try(InputStream in=context.getAssets().open("stadiums/"+file)){
   android.graphics.Bitmap bitmap=BitmapFactory.decodeStream(in);
   if(bitmap==null)throw new IOException("Imagen no válida");
   return bitmap;
  }
 }
 private static android.graphics.Bitmap download(Context context,String url)throws Exception{
  String host=new URI(url).getHost();
  if(!url.startsWith("https://")||!("a.espncdn.com".equals(host)||"upload.wikimedia.org".equals(host)
      ||"thumb.wikimedia.org".equals(host)))throw new IOException("Origen de imagen inesperado");
  File cache=new File(context.getCacheDir(),"stadium-"+java.util.UUID.nameUUIDFromBytes(
    url.getBytes(java.nio.charset.StandardCharsets.UTF_8))+".img");
  android.graphics.Bitmap bitmap=cache.isFile()?decode(java.nio.file.Files.readAllBytes(cache.toPath())):null;
  if(bitmap!=null)return bitmap;
  byte[] data=read(url,8_000_000);
  bitmap=decode(data);
  if(bitmap==null)throw new IOException("La respuesta no contiene una imagen válida");
  try{
   File temp=File.createTempFile("stadium-",".tmp",context.getCacheDir());
   try{
    java.nio.file.Files.write(temp.toPath(),data);
    java.nio.file.Files.move(temp.toPath(),cache.toPath(),
      java.nio.file.StandardCopyOption.REPLACE_EXISTING);
   }finally{temp.delete();}
  }catch(IOException error){
   android.util.Log.w("PulseSport","No se pudo guardar la foto en caché",error);
  }
  return bitmap;
 }
 private static android.graphics.Bitmap decode(byte[] data){
  BitmapFactory.Options options=new BitmapFactory.Options();
  options.inJustDecodeBounds=true;
  BitmapFactory.decodeByteArray(data,0,data.length,options);
  options.inSampleSize=1;
  while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>1600)options.inSampleSize*=2;
  options.inJustDecodeBounds=false;
  return BitmapFactory.decodeByteArray(data,0,data.length,options);
 }
 private static byte[] read(String url,int maxBytes)throws IOException{
  HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
  c.setConnectTimeout(5000);c.setReadTimeout(5000);
  c.setRequestProperty("User-Agent","PulseSport/1.8.1 (educational Android project)");
  try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] buffer=new byte[8192];
   int count;
   while((count=in.read(buffer))!=-1){
    if(out.size()+count>maxBytes)throw new IOException("Respuesta demasiado grande");
    out.write(buffer,0,count);
   }
   return out.toByteArray();
  }finally{c.disconnect();}
 }
}
