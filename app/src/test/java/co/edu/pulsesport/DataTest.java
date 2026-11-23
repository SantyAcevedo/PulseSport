package co.edu.pulsesport;
import org.junit.Test;
import org.json.*;
import java.util.*;
import static org.junit.Assert.*;
public class DataTest {
 private Models.Match match(String score,String state){return new Models.Match("42","2026-11-21T20:00Z",state,"",new Models.Team("1","Local",score,""),new Models.Team("2","Visitante","0",""),List.of());}
 @Test public void firstObservationAndIdenticalScoreDoNotNotify(){ScoreChanges d=new ScoreChanges();Set<String> f=Set.of("PREMIER:1");assertTrue(d.observe(List.of(match("0","in")),f,League.PREMIER).isEmpty());assertTrue(d.observe(List.of(match("0","in")),f,League.PREMIER).isEmpty());assertEquals(1,d.observe(List.of(match("1","in")),f,League.PREMIER).size());assertTrue(d.observe(List.of(match("1","in")),f,League.PREMIER).isEmpty());}
 @Test public void leaguesDoNotShareBaselines(){ScoreChanges d=new ScoreChanges();Set<String> f=Set.of("PREMIER:1","NBA:1");d.observe(List.of(match("0","in")),f,League.PREMIER);assertTrue(d.observe(List.of(match("80","in")),f,League.NBA).isEmpty());}
 @Test public void correctionsAndFinalChangesAreReported(){ScoreChanges d=new ScoreChanges();Set<String> f=Set.of("PREMIER:2");d.observe(List.of(match("2","in")),f,League.PREMIER);assertEquals(1,d.observe(List.of(match("1","post")),f,League.PREMIER).size());}
 @Test public void nonFavoritesAndPreGameDoNotNotify(){ScoreChanges d=new ScoreChanges();d.observe(List.of(match("0","pre")),Set.of(),League.PREMIER);assertTrue(d.observe(List.of(match("1","in")),Set.of("NBA:1"),League.PREMIER).isEmpty());assertTrue(d.observe(List.of(match("2","pre")),Set.of("PREMIER:1"),League.PREMIER).isEmpty());}
 @Test public void reversedCompetitorsKeepHomeAndAway() throws Exception {JSONObject root=new JSONObject("{\"events\":[{\"id\":\"e\",\"competitions\":[{\"competitors\":[{\"homeAway\":\"away\",\"team\":{\"id\":\"2\"}},{\"homeAway\":\"home\",\"team\":{\"id\":\"1\"}}]}]}]}");Models.Match m=Models.matches(root).get(0);assertEquals("1",m.home().id());assertEquals("2",m.away().id());assertEquals("—",m.home().score());}
 @Test public void teamLogoUsesLogosArrayWhenLogoFieldMissing() throws Exception {
  JSONObject team=new JSONObject("{\"id\":\"359\",\"logos\":[{\"href\":\"https://a.espncdn.com/i/teamlogos/soccer/500/359.png\"}]}");
  assertEquals("https://a.espncdn.com/i/teamlogos/soccer/500/359.png",Models.logo(team));
 }
 @Test public void scoreboardTeamLogoUsesLogosArray() throws Exception {
  JSONObject root=new JSONObject("{\"events\":[{\"id\":\"e\",\"competitions\":[{\"competitors\":[{\"homeAway\":\"away\",\"team\":{\"id\":\"2\",\"logos\":[{\"href\":\"https://a.espncdn.com/away.png\"}]}},{\"homeAway\":\"home\",\"team\":{\"id\":\"1\",\"logos\":[{\"href\":\"https://a.espncdn.com/home.png\"}]}}]}]}]}");
  assertEquals("https://a.espncdn.com/home.png",Models.matches(root).get(0).home().logo());
 }
 @Test public void emptyCalendarIsValid() throws Exception {assertTrue(Models.matches(new JSONObject("{\"events\":[]}")).isEmpty());}
 @Test(expected=JSONException.class) public void malformedPayloadIsRejected() throws Exception {Models.matches(new JSONObject("{\"error\":true}"));}
 @Test public void nestedConferencesAndPercentageAreKept() throws Exception {JSONObject root=new JSONObject("{\"children\":[{\"name\":\"Este\",\"children\":[{\"name\":\"Atlántico\",\"standings\":{\"entries\":[{\"team\":{\"displayName\":\"Equipo A\"},\"stats\":[{\"name\":\"winPercent\",\"displayValue\":\".750\"}]}]}}]}]}");List<Models.Standing> rows=Models.standings(root);assertEquals(1,rows.size());assertEquals("Atlántico",rows.get(0).group());assertEquals(".750",rows.get(0).points());}
 @Test public void nbaUsesWinPercentAndSeedInsteadOfPoints() throws Exception {
 JSONObject root=new JSONObject("{\"standings\":{\"entries\":[{\"team\":{\"displayName\":\"NBA\"},\"stats\":[{\"name\":\"points\",\"displayValue\":\"-7.0\"},{\"name\":\"winPercent\",\"displayValue\":\".750\"},{\"name\":\"wins\",\"displayValue\":\"12\"},{\"name\":\"losses\",\"displayValue\":\"4\"},{\"name\":\"playoffSeed\",\"displayValue\":\"2\"}]}]}}");
 Models.Standing row=Models.standings(root).get(0);assertEquals(".750",row.points());assertEquals("16",row.played());assertEquals("2",row.rank());
 }
}
