package co.edu.pulsesport;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formats ESPN kickoff times for match cards using the device clock. */
public final class MatchTime {
 private MatchTime() {}
 public static Instant parse(String raw) {
  if(raw==null||raw.isBlank())return null;
  try{return Instant.parse(raw);}catch(Exception ignored){}
  try{return OffsetDateTime.parse(raw).toInstant();}catch(Exception ignored){}
  return null;
 }
 public static String remaining(String raw,long nowMillis) {
  Instant start=parse(raw);if(start==null)return "Hora por confirmar";
  long millis=start.toEpochMilli()-nowMillis;
  if(millis<=0)return "Por comenzar";
  long seconds=(millis+999)/1000;
  if(seconds>=86400){long days=(seconds+86399)/86400;return days+" "+(days==1?"día":"días");}
  if(seconds>=3600)return String.format(Locale.ROOT,"%02d:%02d:%02d",seconds/3600,(seconds/60)%60,seconds%60);
  return String.format(Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);
 }
 public static String localDate(String raw,ZoneId zone) {
  Instant start=parse(raw);if(start==null)return "Hora por confirmar";
  return start.atZone(zone).format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm",new Locale("es","CO")));
 }
}
