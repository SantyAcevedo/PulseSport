package co.edu.pulsesport;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class RepositoryTest {
 private static final String DATA="{\"events\":[{\"id\":\"live\",\"competitions\":[{\"competitors\":[{\"homeAway\":\"home\",\"score\":\"2\",\"team\":{\"id\":\"1\"}},{\"homeAway\":\"away\",\"score\":\"1\",\"team\":{\"id\":\"2\"}}]}]}]}";
 private static HttpURLConnection ok(String url) throws Exception {
  return new HttpURLConnection(new URL(url)){
   @Override public void connect(){} @Override public void disconnect(){} @Override public boolean usingProxy(){return false;}
   @Override public int getResponseCode(){return 200;}
   @Override public InputStream getInputStream(){return new ByteArrayInputStream(DATA.getBytes(StandardCharsets.UTF_8));}
  };
 }
 private Repository repo(File cache,Repository.Connections factory){return new Repository(cache,name->{throw new FileNotFoundException(name);},factory);}
 private static HttpURLConnection response(String url) throws IOException {try{return ok(url);}catch(Exception e){throw new IOException(e);}}
 @Test public void actualRepositoryRequestsSingleDaysAndDeduplicates() throws Exception {
  List<String> urls=new ArrayList<>();Repository r=repo(Files.createTempDirectory("pulse-test").toFile(),url->{urls.add(url);return response(url);});
  Repository.Result result=r.scores(League.PREMIER,"20261120-20261122",true);
  assertEquals(3,urls.size());for(String url:urls)assertFalse(new URI(url).getQuery().contains("-"));
  assertEquals(1,Models.matches(result.json()).size());assertEquals("2 - 1",Models.matches(result.json()).get(0).score());assertEquals("",result.warning());
 }
 @Test public void retryForcesANewRequestAndKeepsStaleTimestampOnFailure() throws Exception {
  AtomicInteger calls=new AtomicInteger();Repository r=repo(Files.createTempDirectory("pulse-test").toFile(),url->{if(calls.incrementAndGet()>1)throw new SocketTimeoutException("timeout");return response(url);});
  Repository.Result first=r.scores(League.PREMIER,"20261121",false);
  assertTrue(r.scores(League.PREMIER,"20261121",false).cached());assertEquals(1,calls.get());
  Repository.Result retried=r.scores(League.PREMIER,"20261121",true);
  assertEquals(2,calls.get());assertEquals(first.time(),retried.time());assertTrue(retried.warning().contains("Sin actualizar"));
 }
 @Test public void partialDayFailureKeepsAvailableGamesWithAWarning() throws Exception {
  Repository r=repo(Files.createTempDirectory("pulse-test").toFile(),url->{if(url.contains("20261120"))throw new UnknownHostException("test");return response(url);});
  Repository.Result result=r.scores(League.NBA,"20261120-20261121",true);
  assertEquals(1,Models.matches(result.json()).size());assertTrue(result.warning().contains("Consulta incompleta"));
 }
 @Test(expected=UnknownHostException.class) public void allFailedDaysRemainAnError() throws Exception {
  repo(Files.createTempDirectory("pulse-test").toFile(),url->{throw new UnknownHostException("test");}).scores(League.PREMIER,"20261120-20261122",true);
 }
 @Test public void localCacheWriteFailureDoesNotDiscardDownloadedGames() throws Exception {
  File cacheFile=File.createTempFile("pulse-test", ".file");
  Repository.Result result=repo(cacheFile,RepositoryTest::response).scores(League.PREMIER,"20261121",true);
  assertEquals(1,Models.matches(result.json()).size());assertFalse(result.cached());
 }
}
