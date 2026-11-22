package co.edu.pulsesport;
import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class MatchAlertsTest {
 private Models.Match game(String state,String score,String... events){return new Models.Match("one","2026-11-22T20:00Z",state,"",new Models.Team("1","Home",score,""),new Models.Team("2","Away","0",""),Arrays.asList(events));}
 @Test public void noticesStartGoalFoulAndEndOnce(){MatchAlerts a=new MatchAlerts();assertTrue(a.update(League.PREMIER,game("pre","0"),true).isEmpty());assertEquals(1,a.update(League.PREMIER,game("in","0"),true).size());assertEquals(2,a.update(League.PREMIER,game("in","1","Falta 32'"),true).size());assertTrue(a.update(League.PREMIER,game("in","1","Falta 32'"),true).isEmpty());assertEquals(1,a.update(League.PREMIER,game("post","1","Falta 32'"),true).size());}
 @Test public void noAlertsForUnwatchedGame(){MatchAlerts a=new MatchAlerts();a.update(League.NBA,game("pre","0"),false);assertTrue(a.update(League.NBA,game("in","2"),false).isEmpty());}
}
