package co.edu.pulsesport;

import org.junit.Test;
import java.time.Instant;
import java.time.ZoneId;
import static org.junit.Assert.*;

public class MatchTimeTest {
 private final long now=Instant.parse("2026-11-21T12:00:00Z").toEpochMilli();
 private String after(long seconds){return Instant.ofEpochMilli(now+seconds*1000).toString();}
 @Test public void daysAreShownAtOrBeyondTwentyFourHours(){
  assertEquals("2 días",MatchTime.remaining(after(86400+1),now));
  assertEquals("1 día",MatchTime.remaining(after(86400),now));
 }
 @Test public void hoursMinutesAndSecondsWithinDay(){
  assertEquals("23:54:42",MatchTime.remaining(after(23*3600+54*60+42),now));
  assertEquals("01:00:00",MatchTime.remaining(after(3600),now));
 }
 @Test public void minutesAndSecondsWithinLastHour(){
  assertEquals("43:21",MatchTime.remaining(after(43*60+21),now));
  assertEquals("00:01",MatchTime.remaining(after(1),now));
 }
 @Test public void kickoffAndMissingDateAreClear(){
  assertEquals("Por comenzar",MatchTime.remaining(after(0),now));
  assertEquals("Hora por confirmar",MatchTime.remaining("fecha inválida",now));
  assertEquals("Hora por confirmar",MatchTime.localDate("",ZoneId.of("America/Bogota")));
 }
 @Test public void kickoffDateUsesDeviceZone(){
  assertTrue(MatchTime.localDate("2026-11-24T23:20Z",ZoneId.of("America/Bogota")).endsWith("18:20"));
 }
}
