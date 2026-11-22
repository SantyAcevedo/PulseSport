package co.edu.pulsesport;
import android.app.*;
import android.content.*;
import android.os.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
public class TrackingService extends Service {
 public static final String CHANNEL="scores",SESSION="session";
 private final ScheduledExecutorService executor=Executors.newSingleThreadScheduledExecutor();
 private final MatchAlerts detector=new MatchAlerts(); private Store store; private boolean scheduled=false;
 public static void channels(Context c){NotificationManager n=c.getSystemService(NotificationManager.class);n.createNotificationChannel(new NotificationChannel(CHANNEL,"Cambios de marcador",NotificationManager.IMPORTANCE_DEFAULT));n.createNotificationChannel(new NotificationChannel(SESSION,"Sesión de seguimiento",NotificationManager.IMPORTANCE_LOW));}
 private PendingIntent open(){return PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);}
 @Override public void onCreate(){super.onCreate();store=new Store(this);channels(this);}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(intent!=null&&"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
  PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,TrackingService.class).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(this,SESSION).setSmallIcon(co.edu.pulsesport.R.drawable.ic_notification).setContentTitle("Seguimiento de favoritos activo").setContentText("Consulta cada 60 s · sesión máxima de 2 horas").setContentIntent(open()).setOngoing(true).addAction(new Notification.Action.Builder(null,"Detener",stop).build()).build();
  startForeground(1,n);
  if(scheduled)return START_NOT_STICKY;
  scheduled=true;
  store.trackingUntil(System.currentTimeMillis()+2*60*60*1000L);
  executor.scheduleWithFixedDelay(this::poll,0,60,TimeUnit.SECONDS);return START_NOT_STICKY;
 }
 private void poll(){
  if(System.currentTimeMillis()>store.trackingUntil()||store.demo()||(store.favorites().isEmpty()&&store.leagues().isEmpty())){stopSelf();return;}
  Repository repo=new Repository(this);
  for(League l:League.values()){
   if(Thread.currentThread().isInterrupted())return;
   Set<String> fav=store.favorites();boolean needed=false;for(String k:fav)if(k.startsWith(l.name()+":"))needed=true;if(!needed&&!store.follows(l))continue;
   try{
    // Ventana UTC de dos días para cubrir partidos nocturnos de la fecha local.
    LocalDate utc=LocalDate.now(ZoneOffset.UTC);String range=utc.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+utc.format(DateTimeFormatter.BASIC_ISO_DATE);
    Repository.Result r=repo.scores(l,range,false);if(!r.warning().isEmpty())continue;
    for(Models.Match m:Models.matches(r.json())){
     boolean watched=store.follows(l)||fav.contains(l.name()+":"+m.home().id())||fav.contains(l.name()+":"+m.away().id());
     for(String message:detector.update(l,m,watched)){
      Notification notification=new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_notification).setContentTitle(l.title).setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message)).setContentIntent(open()).setAutoCancel(true).build();
      getSystemService(NotificationManager.class).notify((l.name()+m.id()+message).hashCode(),notification);
     }
    }
   }catch(Exception e){android.util.Log.w("PulseSport","No se pudo actualizar "+l.code+": "+e.getClass().getSimpleName());}
  }
 }
 @Override public void onTimeout(int startId,int fgsType){stopSelf();}
 @Override public void onDestroy(){executor.shutdownNow();if(store!=null)store.trackingUntil(0);stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}
