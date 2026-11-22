package co.edu.pulsesport;
import java.util.*;
public final class ScoreChanges {
 private final Map<String,String> previous=new HashMap<>();
 public List<Models.Match> observe(List<Models.Match> matches,Set<String> favorites,League league){
  List<Models.Match> changed=new ArrayList<>();
  for(Models.Match m:matches){String key=league.name()+":"+m.id(),old=previous.put(key,m.score());
   if(old!=null&&!old.equals(m.score())&&!m.state().equals("pre") && (favorites.contains(league.name()+":"+m.home().id())||favorites.contains(league.name()+":"+m.away().id())))changed.add(m);
  }return changed;
 }
}
