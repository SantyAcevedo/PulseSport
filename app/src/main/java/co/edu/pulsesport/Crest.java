package co.edu.pulsesport;
import android.content.*;
import android.graphics.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
public final class Crest {
 private static final ExecutorService pool=Executors.newFixedThreadPool(3);
 public static void into(Context context,ImageView view,String url){
  view.setImageResource(R.drawable.ic_pulse);
  if(url==null||!url.startsWith("https://a.espncdn.com/"))return;
  File cache=new File(context.getCacheDir(),"crest-"+Integer.toHexString(url.hashCode())+".png");
  pool.execute(()->{try{
   if(!cache.exists()){HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(5000);c.setReadTimeout(5000);try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(cache)){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);}finally{c.disconnect();}}
   Bitmap bitmap=BitmapFactory.decodeFile(cache.getAbsolutePath());if(bitmap!=null)view.post(()->view.setImageBitmap(bitmap));
  }catch(Exception e){android.util.Log.w("PulseSport","Escudo no disponible");}});
 }
}
