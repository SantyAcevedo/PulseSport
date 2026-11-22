package co.edu.pulsesport;

import java.net.*;
import java.util.*;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScoreboardQueryTest {
 @Test public void rangeBecomesThreeSingleDayRequests() throws Exception {
  List<String> urls=ScoreboardQuery.urls(League.PREMIER,"20261120-20261122");
  assertEquals(3,urls.size());
  for(int i=0;i<urls.size();i++){
   URI uri=new URI(urls.get(i));assertEquals("site.api.espn.com",uri.getHost());
   assertEquals("dates=202611"+(20+i)+"&limit=100",uri.getQuery());
   assertFalse("ESPN rechaza intervalos en dates",uri.getQuery().contains("-"));
  }
 }
 @Test public void singleDayRemainsSingleRequest(){assertEquals(1,ScoreboardQuery.urls(League.NBA,"20261121").size());}
 @Test public void midnightWindowCanCrossTheYear(){List<String> urls=ScoreboardQuery.urls(League.NBA,"20261231-20270102");assertEquals(3,urls.size());assertTrue(urls.get(1).contains("dates=20270101&"));}
 @Test(expected=IllegalArgumentException.class) public void reversedWindowIsRejected(){ScoreboardQuery.urls(League.PREMIER,"20261122-20261120");}
 @Test(expected=IllegalArgumentException.class) public void largeWindowIsRejected(){ScoreboardQuery.urls(League.PREMIER,"20261120-20261130");}
 @Test public void overlappingDaysDoNotDuplicateMatches() throws Exception {
  JSONObject first=new JSONObject("{\"events\":[{\"id\":\"1\",\"value\":\"old\"}]}");
  JSONObject next=new JSONObject("{\"events\":[{\"id\":\"1\",\"value\":\"updated\"},{\"id\":\"2\"}]}");
  JSONArray events=ScoreboardQuery.merge(Arrays.asList(first,next)).getJSONArray("events");
  assertEquals(2,events.length());assertEquals("updated",events.getJSONObject(0).getString("value"));
 }
 @Test public void anEmptyDayDoesNotHideOtherDays() throws Exception {
  JSONArray events=ScoreboardQuery.merge(Arrays.asList(new JSONObject("{\"events\":[]}"),new JSONObject("{\"events\":[{\"id\":\"match\"}]}"))).getJSONArray("events");
  assertEquals(1,events.length());
 }
 @Test public void rejectedQueryAndDnsFailureHaveDifferentExplanations(){
  assertTrue(DataErrors.describe(new DataErrors.HttpFailure(400)).contains("rechazó"));
  assertTrue(DataErrors.describe(new UnknownHostException()).contains("DNS"));
  assertTrue(DataErrors.describe(new SocketTimeoutException()).contains("tardó"));
 }
}
