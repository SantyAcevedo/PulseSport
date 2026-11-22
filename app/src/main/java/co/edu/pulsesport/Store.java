package co.edu.pulsesport;
import android.content.*;
import java.util.*;
public final class Store {
 private final android.content.SharedPreferences p;
 public Store(Context c){p=c.getSharedPreferences("pulse",Context.MODE_PRIVATE);}
 public Set<String> favorites(){return new HashSet<>(p.getStringSet("favorites",Collections.emptySet()));}
 public boolean favorite(League l,String id){return favorites().contains(l.name()+":"+id);}
 public void toggle(League l,Models.Team t){Set<String>s=favorites();String k=l.name()+":"+t.id();if(!s.remove(k))s.add(k);p.edit().putStringSet("favorites",s).putString("team:"+k,t.name()).apply();}
 public String teamName(String key){return p.getString("team:"+key,key);}
 public void remove(String key){Set<String>s=favorites();s.remove(key);p.edit().putStringSet("favorites",s).apply();}
 public boolean onboarded(){return p.getBoolean("onboarded",false);}
 public void completeOnboarding(){p.edit().putBoolean("onboarded",true).apply();}
 public Set<String> leagues(){return new HashSet<>(p.getStringSet("leagues",Collections.emptySet()));}
 public boolean follows(League l){return leagues().contains(l.name());}
 public void toggleLeague(League l){Set<String> s=leagues();if(!s.remove(l.name()))s.add(l.name());p.edit().putStringSet("leagues",s).apply();}
 public boolean demo(){return p.getBoolean("demo",false);}
 public void demo(boolean v){p.edit().putBoolean("demo",v).apply();}
 public long trackingUntil(){return p.getLong("trackingUntil",0);}
 public void trackingUntil(long v){p.edit().putLong("trackingUntil",v).apply();}
}
