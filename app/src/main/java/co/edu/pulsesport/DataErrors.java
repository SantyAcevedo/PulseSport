package co.edu.pulsesport;

import java.io.IOException;
import java.net.*;
import javax.net.ssl.SSLException;
import org.json.JSONException;

public final class DataErrors {
 private DataErrors() {}
 public static final class HttpFailure extends IOException {
  public final int status;
  public HttpFailure(int code){super("HTTP "+code);status=code;}
 }
 public static String describe(Exception e){
  if(e instanceof HttpFailure){int status=((HttpFailure)e).status;
   if(status==429)return "La fuente limitó las consultas. Espera un minuto y vuelve a intentar.";
   if(status==400)return "La fuente rechazó la consulta (HTTP 400).";
   if(status==403)return "La fuente bloqueó el acceso desde esta red (HTTP 403).";
   if(status>=500)return "La fuente deportiva tiene un problema temporal (HTTP "+status+").";
   return "La fuente no pudo responder (HTTP "+status+").";
  }
  if(e instanceof UnknownHostException)return "No se pudo encontrar el servidor. Comprueba el acceso a Internet y el DNS del emulador o teléfono.";
  if(e instanceof SocketTimeoutException)return "La consulta tardó demasiado. Comprueba tu conexión y vuelve a intentar.";
  if(e instanceof SSLException)return "No se pudo establecer la conexión segura. Comprueba la fecha y hora de Android y la red que utilizas.";
  if(e instanceof ConnectException)return "No se pudo conectar con la fuente deportiva. Comprueba Internet y vuelve a intentar.";
  if(e instanceof JSONException)return "La fuente respondió con un formato de datos inesperado. Vuelve a intentar más tarde.";
  return "No se pudieron cargar los datos. Pulsa Ver detalle para consultar la causa.";
 }
}
