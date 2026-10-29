package dev.raven.control;

import android.content.*;
import android.content.pm.*;
import java.util.*;

/** No watcher or boot job is needed: Android stores disabled-user persistently. */
public final class ControlEngine {
 private final Context context;
 private final PackageManager pm;
 private final android.content.SharedPreferences history;
 public ControlEngine(Context context){this.context=context.getApplicationContext();pm=context.getPackageManager();history=context.getSharedPreferences("app-control",0);}
 public static void validatePackage(String pkg){if(pkg==null||!pkg.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+"))throw new IllegalArgumentException("Invalid app package name.");}
 public boolean isEnabled(String pkg) throws Exception {
  ApplicationInfo info=pm.getApplicationInfo(pkg,PackageManager.MATCH_DISABLED_COMPONENTS);
  int state=pm.getApplicationEnabledSetting(pkg);
  return state==PackageManager.COMPONENT_ENABLED_STATE_ENABLED||(state==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT&&info.enabled);
 }
 public List<AppEntry> list() throws Exception {
  Protection guard=new Protection(context);List<AppEntry> apps=new ArrayList<>();
  for(ApplicationInfo info:pm.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS|PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS)) {
   if((info.flags&ApplicationInfo.FLAG_INSTALLED)==0)continue;
   try {apps.add(new AppEntry(info,info.loadLabel(pm).toString(),isEnabled(info.packageName),guard.reason(info)));}catch(PackageManager.NameNotFoundException gone){}
  }
  apps.sort((a,b)->{int c=a.label.compareToIgnoreCase(b.label);return c==0?a.packageName.compareTo(b.packageName):c;});return apps;
 }
 public String setEnabled(String pkg,boolean enable) throws Exception {
  synchronized(ControlEngine.class){return applyEnabled(pkg,enable);}
 }
 private String applyEnabled(String pkg,boolean enable) throws Exception {
  validatePackage(pkg);
  ApplicationInfo info=pm.getApplicationInfo(pkg,PackageManager.MATCH_DISABLED_COMPONENTS);
  if(!enable){String reason=new Protection(context).reason(info);if(reason!=null)throw new SecurityException(reason);}
  AynRoot.requireRoot();
  boolean before=isEnabled(pkg);
  if(before==enable)return "Already "+(enable?"enabled":"disabled")+".";
  int prior=pm.getApplicationEnabledSetting(pkg);
  String key="prior:"+pkg;
  if(!enable&&!history.contains(key)&&!history.edit().putInt(key,prior).commit())throw new IllegalStateException("Cannot save the app's original state. No change was made.");
  int restore=history.getInt(key,PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
  String action=enable?(restore==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT?"default-state":"enable"):"disable-user";
  String output=AynRoot.execute("timeout 12 pm "+action+" --user 0 '"+pkg+"' 2>&1");
  // Verify Android's state, never trust a switch tap or shell response alone.
  boolean actual=isEnabled(pkg);
  int actualState=pm.getApplicationEnabledSetting(pkg);
  if(actual!=enable||(!enable&&actualState!=PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER))throw new IllegalStateException("Android did not confirm the change. "+output);
  if(enable)history.edit().remove(key).commit();
  history.edit().putString("last",(enable?"Enabled ":"Disabled ")+info.loadLabel(pm)+" • "+new java.text.SimpleDateFormat("MMM d, HH:mm",Locale.getDefault()).format(new Date())).apply();
  return info.loadLabel(pm)+(enable?" is enabled.":" is disabled.");
 }
 public String lastChange(){return history.getString("last","");}
}
