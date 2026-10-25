package dev.raven.control;
import android.content.pm.ApplicationInfo;
public final class AppEntry {
 public final ApplicationInfo info;
 public final String packageName,label,protection;
 public final boolean enabled,system;
 public AppEntry(ApplicationInfo info,String label,boolean enabled,String protection){this.info=info;this.packageName=info.packageName;this.label=label;this.enabled=enabled;this.protection=protection;this.system=(info.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0;}
}
