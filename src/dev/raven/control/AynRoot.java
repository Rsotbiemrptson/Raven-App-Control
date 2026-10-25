package dev.raven.control;

import android.os.IBinder;
import android.os.Parcel;
import java.nio.charset.StandardCharsets;

/** The observed AYN factory interface, deliberately without generic su fallbacks. */
public final class AynRoot {
 private AynRoot() {}
 public static String execute(String command) throws Exception {
  if (command.getBytes(StandardCharsets.UTF_8).length > 192) throw new IllegalArgumentException("Root request is too long.");
  IBinder service=(IBinder)Class.forName("android.os.ServiceManager").getDeclaredMethod("getService",String.class).invoke(null,"PServerBinder");
  if(service==null) throw new IllegalStateException("AYN root service is unavailable on this firmware.");
  Parcel request=Parcel.obtain(),reply=Parcel.obtain();
  try {
   request.writeStringArray(new String[]{command,"1"});
   if(!service.transact(0,request,reply,0)) throw new IllegalStateException("AYN root service rejected the request.");
   byte[] bytes=reply.createByteArray();
   if(bytes==null) throw new IllegalStateException("AYN root service returned no result.");
   return new String(bytes,StandardCharsets.UTF_8).trim();
  } finally {request.recycle();reply.recycle();}
 }
 public static void requireRoot() throws Exception {
  if(android.os.Process.myUid()>=100000) throw new IllegalStateException("Use Raven App Control in the device owner's profile.");
  if(!"0".equals(execute("id -u"))) throw new SecurityException("AYN service did not provide root access.");
 }
}
