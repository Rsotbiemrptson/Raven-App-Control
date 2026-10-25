package dev.raven.control;

import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.*;
import android.view.inputmethod.*;
import java.util.*;

/** Rebuilt before every disable so changes to launcher/keyboard/admin are respected. */
public final class Protection {
 private final Map<String,String> reasons=new HashMap<>();
 private final PackageManager pm;
 public Protection(Context context) {
  pm=context.getPackageManager();
  reasons.put(context.getPackageName(),"Raven App Control must stay available to restore apps.");
  String[] core={"android","com.android.systemui","com.android.settings","com.android.shell","com.android.providers.settings","com.android.permissioncontroller","com.google.android.permissioncontroller","com.android.packageinstaller","com.google.android.packageinstaller","com.android.providers.downloads","com.android.providers.downloads.ui","com.android.providers.media","com.google.android.providers.media.module","com.android.externalstorage","com.android.documentsui","com.google.android.documentsui","com.android.networkstack","com.google.android.networkstack","com.android.bluetooth","com.android.phone","com.android.se","com.android.keychain","com.android.credentialmanager","com.google.android.gms","com.google.android.gsf","com.qualcomm.qti.services.systemhelper","com.ayn.pserver","com.android.pserver"};
  for(String pkg:core) reasons.put(pkg,"Essential system or device service.");
  protectHandler(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),"Current home screen.");
  protectHandler(new Intent(android.provider.Settings.ACTION_SETTINGS),"Device settings and recovery access.");
  try {
   InputMethodManager manager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
   for(InputMethodInfo input:manager.getEnabledInputMethodList()) reasons.put(input.getPackageName(),"Enabled keyboard. Choose another keyboard in Settings first.");
   DevicePolicyManager policy=(DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
   List<ComponentName> admins=policy.getActiveAdmins();
   if(admins!=null)for(ComponentName admin:admins) reasons.put(admin.getPackageName(),"Active device administrator.");
  } catch(RuntimeException error) {throw new IllegalStateException("Cannot check essential app protection. Try reopening Raven.",error);}
 }
 private void protectHandler(Intent intent,String reason){ResolveInfo r=pm.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);if(r!=null&&r.activityInfo!=null)reasons.put(r.activityInfo.packageName,reason);}
 public String reason(ApplicationInfo info) {
  String reason=reasons.get(info.packageName);if(reason!=null)return reason;
  if(info.uid%100000<10000)return "Shared system identity; required by Android or device services.";
  if((info.flags&ApplicationInfo.FLAG_PERSISTENT)!=0)return "Persistent system service.";
  return null;
 }
}
