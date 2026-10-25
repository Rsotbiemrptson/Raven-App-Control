package dev.raven.control;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
 private static final int BG=0xff131519,SURFACE=0xff1f2228,CARD=0xff272b32,TEXT=0xfff1f2f6,MUTED=0xffa9b0bc;
 static final int ACCENT=0xfffb0b24,ACCENT_TEXT=AppearanceColors.readable(ACCENT,CARD),INK=AppearanceColors.ink(ACCENT);
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private ControlEngine engine;
 private List<AppEntry> all=new ArrayList<>(),shown=new ArrayList<>();
 private TextView rootTitle,rootMessage,summary,last,title,subtitle;
 private Button refresh,more;
 private EditText search;
 private ListView list;
 private AppAdapter adapter;
 private final ArrayList<Button> tabs=new ArrayList<>();
 private int filter=0;
 private boolean busy=false,rootReady=false,disposed=false,showSystem=false,pendingLayout=false;
 private String rootError="";
 private boolean landscape;
 private String query="";

 @Override public void onCreate(Bundle state){super.onCreate(state);engine=new ControlEngine(this);showSystem=getSharedPreferences("display",0).getBoolean("showSystem",false);if(state!=null){filter=state.getInt("filter",0);query=state.getString("query","");}build();}
 @Override protected void onResume(){super.onResume();reload();}
 @Override protected void onDestroy(){disposed=true;worker.shutdown();super.onDestroy();}
 @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putInt("filter",filter);state.putString("query",query);}
 @Override public void onConfigurationChanged(android.content.res.Configuration config){super.onConfigurationChanged(config);if(busy)pendingLayout=true;else build();}
 private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
 private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);return t;}
 private Button button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setTextSize(14);b.setAllCaps(false);b.setTextColor(TEXT);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setStateListAnimator(null);b.setElevation(0);b.setPadding(dp(16),dp(9),dp(16),dp(9));b.setBackground(shape(CARD,24));b.setOnClickListener(v->action.run());return b;}
 private void add(LinearLayout parent,View view,int width,int height){parent.addView(view,new LinearLayout.LayoutParams(width<0?width:dp(width),height<0?height:dp(height)));}
 private void gap(LinearLayout parent,int height){add(parent,new View(this),1,height);}
 private LinearLayout column(){LinearLayout layout=new LinearLayout(this);layout.setOrientation(1);return layout;}
 private LinearLayout row(){LinearLayout layout=new LinearLayout(this);layout.setGravity(Gravity.CENTER_VERTICAL);return layout;}
 private ImageView appIcon(){ImageView image=new ImageView(this);try{image.setImageDrawable(getPackageManager().getApplicationIcon(getPackageName()));}catch(android.content.pm.PackageManager.NameNotFoundException error){throw new IllegalStateException("App icon unavailable",error);}image.setContentDescription("Raven App Control");return image;}
 private void build(){
  tabs.clear();pendingLayout=false;
  landscape=getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE;
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);getWindow().setDecorFitsSystemWindows(false);
  LinearLayout root=new LinearLayout(this);root.setOrientation(landscape?0:1);root.setBackgroundColor(BG);
  root.setOnApplyWindowInsetsListener((view,insets)->{android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());view.setPadding(safe.left,safe.top,safe.right,safe.bottom);return insets;});setContentView(root);
  LinearLayout nav=landscape?column():row();nav.setTag("app-navigation");nav.setContentDescription("App navigation");nav.setPadding(dp(12),dp(16),dp(12),dp(12));nav.setBackgroundColor(SURFACE);
  if(landscape){LinearLayout brand=row();add(brand,appIcon(),40,40);TextView name=text("Raven App Control",17,TEXT);name.setTypeface(null,Typeface.BOLD);name.setPadding(dp(6),0,0,0);brand.addView(name,new LinearLayout.LayoutParams(0,-2,1));add(nav,brand,-1,-2);gap(nav,24);add(root,nav,148,-1);}
  LinearLayout page=column();page.setPadding(dp(22),dp(16),dp(22),dp(12));root.addView(page,new LinearLayout.LayoutParams(landscape?0:-1,landscape?-1:0,1));
  LinearLayout header=row();
  if(!landscape){LinearLayout.LayoutParams iconSpace=new LinearLayout.LayoutParams(dp(42),dp(42));iconSpace.setMarginEnd(dp(10));header.addView(appIcon(),iconSpace);}
  LinearLayout names=column();if(!landscape)add(names,text("Raven App Control",12,MUTED),-1,-2);
  title=text("All apps",25,TEXT);title.setTypeface(null,Typeface.BOLD);subtitle=text("Choose which apps can run",12,MUTED);add(names,title,-1,-2);add(names,subtitle,-1,-2);header.addView(names,new LinearLayout.LayoutParams(0,-2,1));
  refresh=button("Refresh",this::reload);refresh.setEnabled(!busy);add(header,refresh,-2,40);
  more=button("⋮",()->showMenu(more));more.setTextSize(27);more.setContentDescription("More options");more.setPadding(0,0,0,0);LinearLayout.LayoutParams menuSpace=new LinearLayout.LayoutParams(dp(44),dp(44));menuSpace.setMarginStart(dp(8));header.addView(more,menuSpace);add(page,header,-1,-2);gap(page,12);
  search=new EditText(this);search.setSingleLine(true);search.setTextSize(14);search.setTextColor(TEXT);search.setHintTextColor(MUTED);search.setHint("Search app name or package");search.setContentDescription("Search apps or package names");search.setPadding(dp(16),dp(8),dp(16),dp(8));search.setBackground(shape(SURFACE,24));search.setHighlightColor(0x55fb0b24);GradientDrawable cursor=shape(ACCENT_TEXT,1);cursor.setSize(dp(2),dp(22));search.setTextCursorDrawable(cursor);search.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);search.setText(query);search.setOnEditorActionListener((view,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);search.clearFocus();return true;}return false;});add(page,search,-1,44);
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();applyFilter();}public void afterTextChanged(Editable e){}});gap(page,10);
  summary=text("Loading apps…",12,MUTED);add(page,summary,-1,-2);gap(page,8);
  FrameLayout body=new FrameLayout(this);list=new ListView(this);list.setDivider(null);list.setDividerHeight(dp(8));list.setClipToPadding(false);list.setPadding(0,0,0,dp(4));list.setItemsCanFocus(true);list.setCacheColorHint(Color.TRANSPARENT);list.setSelector(android.R.color.transparent);adapter=new AppAdapter();list.setAdapter(adapter);body.addView(list,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout empty=column();empty.setPadding(dp(18),dp(14),dp(18),dp(14));empty.setBackground(shape(CARD,18));add(empty,text("No matching apps",19,TEXT),-1,-2);gap(empty,8);add(empty,text("Try another search or choose a different section.",13,MUTED),-1,-2);body.addView(empty,new FrameLayout.LayoutParams(-1,-2));list.setEmptyView(empty);page.addView(body,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout status=landscape?row():column();status.setPadding(0,dp(8),0,0);
  rootTitle=text("Checking AYN root service…",12,MUTED);rootTitle.setTypeface(null,Typeface.BOLD);add(status,rootTitle,-2,-2);
  rootMessage=text("Apps stay disabled until you enable them again.",12,MUTED);rootMessage.setMaxLines(1);rootMessage.setEllipsize(TextUtils.TruncateAt.END);if(landscape){rootMessage.setPadding(dp(10),0,0,0);status.addView(rootMessage,new LinearLayout.LayoutParams(0,-2,1));}else add(status,rootMessage,-1,-2);add(page,status,-1,-2);
  last=text("Switch on = enabled · Switch off = disabled",11,MUTED);last.setMaxLines(1);last.setEllipsize(TextUtils.TruncateAt.END);last.setPadding(0,dp(3),0,0);last.setOnClickListener(view->new AlertDialog.Builder(this).setTitle("Latest activity").setMessage(engine.lastChange().isEmpty()?"No app changes yet.":engine.lastChange()).setPositiveButton("Close",null).show());add(page,last,-1,-2);
  String[] labels={"All apps","Enabled","Disabled"};
  for(int i=0;i<labels.length;i++){final int index=i;Button section=button(labels[i],()->{filter=index;applyFilter();});section.setContentDescription("Show "+labels[i]);tabs.add(section);if(landscape){add(nav,section,-1,48);gap(nav,8);}else{section.setTextSize(13);section.setPadding(dp(6),dp(9),dp(6),dp(9));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(48),1);p.setMargins(dp(3),0,dp(3),0);nav.addView(section,p);}}
  if(landscape)nav.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));else{nav.setPadding(dp(9),dp(10),dp(9),dp(12));add(root,nav,-1,70);}
  root.setFocusableInTouchMode(true);root.requestFocus();applyFilter();updateLast();rootTitle.setText(rootReady?"AYN root connected":(rootError.isEmpty()?"Checking AYN root service…":"App control unavailable"));rootTitle.setTextColor(rootReady?MUTED:ACCENT_TEXT);if(!rootError.isEmpty())rootMessage.setText(rootError);
 }
 private void applyFilter(){
  String needle=query.toLowerCase(Locale.ROOT).trim();shown=new ArrayList<>();int disabled=0;
  for(AppEntry e:all){if(!showSystem&&e.system)continue;if(!e.enabled)disabled++;boolean matches=filter==0||filter==1&&e.enabled||filter==2&&!e.enabled;if(matches&&(e.label.toLowerCase(Locale.ROOT).contains(needle)||e.packageName.toLowerCase(Locale.ROOT).contains(needle)))shown.add(e);}
  for(int i=0;i<tabs.size();i++){Button b=tabs.get(i);b.setBackground(shape(filter==i?ACCENT:CARD,24));b.setTextColor(filter==i?INK:TEXT);b.setSelected(filter==i);}
  title.setText(new String[]{"All apps","Enabled apps","Disabled apps"}[filter]);subtitle.setText(filter==2?"Turn apps back on whenever you need them":"Choose which apps can run");summary.setText(shown.size()+" shown · "+disabled+" disabled · "+(showSystem?"System apps shown":"System apps hidden")+(busy?" · Working…":""));if(adapter!=null)adapter.notifyDataSetChanged();
 }
 private void reload(){
  if(busy||disposed)return;busy=true;refresh.setEnabled(false);rootTitle.setText("Checking AYN root service…");rootTitle.setTextColor(ACCENT_TEXT);applyFilter();
  worker.execute(()->{String failure=null;List<AppEntry> apps=null;try{AynRoot.requireRoot();}catch(Exception e){failure=message(e);}try{apps=engine.list();}catch(Exception e){failure="Could not read apps: "+message(e);}final String error=failure;final List<AppEntry> result=apps;
   runOnUiThread(()->{if(disposed)return;rootReady=error==null;rootError=error==null?"":error;busy=false;refresh.setEnabled(true);if(result!=null)all=result;rootTitle.setText(rootReady?"AYN root connected":"App control unavailable");rootTitle.setTextColor(rootReady?MUTED:ACCENT_TEXT);rootMessage.setText(rootReady?"Apps stay disabled until you enable them again.":error);applyFilter();updateLast();if(pendingLayout)build();});});
 }
 private void updateLast(){String value=engine.lastChange();last.setText(value.isEmpty()?"Switch on = enabled · Switch off = disabled":value);}
 private static String message(Throwable e){while(e.getCause()!=null)e=e.getCause();String m=e.getMessage();return m==null?e.getClass().getSimpleName():m;}
 private void requestChange(AppEntry entry){
  if(busy||!rootReady)return;
  boolean enable=!entry.enabled;
  if(!enable&&entry.protection!=null){new AlertDialog.Builder(this).setTitle("Protected app").setMessage(entry.protection).setPositiveButton("OK",null).show();return;}
  if(!enable&&entry.system){new AlertDialog.Builder(this).setTitle("Disable "+entry.label+"?").setMessage("This is a system app. Features that depend on it may stop working. Its data will be kept, and you can enable it again from Disabled.").setNegativeButton("Cancel",null).setPositiveButton("Disable",(d,w)->change(entry,false)).show();}else change(entry,enable);
 }
 private void change(AppEntry entry,boolean enable){
  busy=true;refresh.setEnabled(false);rootMessage.setText((enable?"Enabling ":"Disabling ")+entry.label+"…");applyFilter();
  worker.execute(()->{String outcome;boolean success;try{outcome=engine.setEnabled(entry.packageName,enable);success=true;}catch(Exception error){outcome=message(error);success=false;}List<AppEntry> apps=null;try{apps=engine.list();}catch(Exception ignored){}final List<AppEntry> result=apps;final String report=outcome;final boolean passed=success;
   runOnUiThread(()->{if(disposed)return;busy=false;refresh.setEnabled(true);if(result!=null)all=result;else rootReady=false;rootMessage.setText(result==null?"Refresh to verify app states before making another change.":"Apps stay disabled until you enable them again.");applyFilter();updateLast();if(pendingLayout)build();if(passed)Toast.makeText(this,report,Toast.LENGTH_LONG).show();else new AlertDialog.Builder(this).setTitle("Could not confirm the change").setMessage(report+"\n\nThe list shows the latest state Raven could read. Refresh to check again.").setPositiveButton("OK",null).show();});});
 }
 private void details(AppEntry e){new AlertDialog.Builder(this).setTitle(e.label).setMessage(e.packageName+"\n\n"+(e.enabled?"Enabled":"Disabled")+(e.system?" · System app":" · Installed app")+(e.protection==null?"":"\n\nProtected: "+e.protection)).setNegativeButton("Close",null).setPositiveButton("App info",(d,w)->{try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+e.packageName)));}catch(Exception error){Toast.makeText(this,"App settings unavailable.",Toast.LENGTH_SHORT).show();}}).show();}
 private void showMenu(View anchor){
  PopupMenu popup=new PopupMenu(this,anchor,Gravity.END);
  popup.getMenu().add(0,1,0,"Show system apps").setCheckable(true).setChecked(showSystem);
  popup.getMenu().add(0,2,1,"Help");popup.getMenu().add(0,3,2,"About");
  popup.setOnMenuItemClickListener(item->{if(item.getItemId()==1){showSystem=!showSystem;getSharedPreferences("display",0).edit().putBoolean("showSystem",showSystem).apply();applyFilter();return true;}if(item.getItemId()==2){help();return true;}if(item.getItemId()==3){about();return true;}return false;});popup.show();
 }
 private void about(){
  LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(24),dp(12),dp(24),dp(8));
  ImageView icon=appIcon();icon.setContentDescription("Raven App Control icon");LinearLayout.LayoutParams iconSpace=new LinearLayout.LayoutParams(dp(80),dp(80));iconSpace.gravity=Gravity.CENTER_HORIZONTAL;content.addView(icon,iconSpace);gap(content,12);
  TextView name=text("Raven App Control",22,TEXT);name.setGravity(Gravity.CENTER);name.setTypeface(null,Typeface.BOLD);add(content,name,-1,-2);
  String version="1.1.1";try{version=getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception ignored){}
  TextView release=text("Version "+version,13,ACCENT_TEXT);release.setGravity(Gravity.CENTER);add(content,release,-1,-2);gap(content,16);
  add(content,text("Your apps. Your control.",16,ACCENT_TEXT),-1,-2);gap(content,8);
  add(content,text("Disable the apps you don't need and turn them back on whenever you choose. Part of the Raven app family, alongside Raven Backup.",14,TEXT),-1,-2);gap(content,14);
  add(content,text("Built for AYN",15,TEXT),-1,-2);gap(content,4);add(content,text("Uses the device's factory root service. Verified on Odin 2; other devices and firmware require compatibility testing.",13,MUTED),-1,-2);gap(content,14);
  add(content,text("Private by design",15,TEXT),-1,-2);gap(content,4);add(content,text("Works offline, with no ads, analytics or internet permission. App lists and preferences stay on your device.",13,MUTED),-1,-2);
  ScrollView scroll=new ScrollView(this);scroll.addView(content);new AlertDialog.Builder(this).setTitle("About").setView(scroll).setPositiveButton("Close",null).show();
 }
 private void help(){new AlertDialog.Builder(this).setTitle("Raven App Control").setMessage("Turn a switch off to fully disable an app. Android stops it and removes it from the launcher. Its installed files and data stay on the device.\n\nOpen Disabled to turn it back on. Use the three-dot menu to show or hide system apps; this setting also applies to Disabled. Disabled apps stay off across restarts; Raven does not need to run in the background.\n\nProtected apps keep essential device controls available. Showing system apps includes other built-in apps; disabling them may affect related features.\n\nDisabling is stored by Android. Uninstalling Raven does not re-enable apps. Re-enable them first, or use Android's App info screen where Enable is available.\n\nUses AYN's factory root service in the owner profile. Other firmware needs compatibility testing.\n\nVersion 1.1.1").setPositiveButton("Got it",null).show();}
 private final class AppAdapter extends BaseAdapter {
  public int getCount(){return shown.size();}public Object getItem(int position){return shown.get(position);}public long getItemId(int position){return position;}
  public View getView(int position,View old,ViewGroup parent){
   AppEntry entry=shown.get(position);LinearLayout card=column();card.setTag("app-card");card.setPadding(dp(landscape?16:18),dp(landscape?10:14),dp(landscape?16:18),dp(landscape?10:14));card.setBackground(shape(CARD,18));LinearLayout line=row();
   ImageView icon=new ImageView(MainActivity.this);try{icon.setImageDrawable(entry.info.loadIcon(getPackageManager()));}catch(Exception ignored){}icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);icon.setOnClickListener(view->details(entry));add(line,icon,40,40);
   LinearLayout labels=column();labels.setPadding(dp(14),0,dp(8),0);TextView name=text(entry.label,16,TEXT);name.setMaxLines(1);name.setEllipsize(TextUtils.TruncateAt.END);name.setTypeface(null,Typeface.BOLD);add(labels,name,-1,-2);TextView pkg=text(entry.packageName,11,MUTED);pkg.setMaxLines(1);pkg.setEllipsize(TextUtils.TruncateAt.END);add(labels,pkg,-1,-2);line.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
   String state=entry.enabled?"Enabled":"Disabled";if(entry.protection!=null)state+=" · Protected";
   if(landscape){LinearLayout metadata=column();TextView kind=text(entry.system?"System app":"User app",11,MUTED);add(metadata,kind,-1,-2);TextView current=text(state,11,entry.protection==null?ACCENT_TEXT:MUTED);current.setMaxLines(1);current.setEllipsize(TextUtils.TruncateAt.END);add(metadata,current,-1,-2);add(line,metadata,166,-2);}
   labels.setOnClickListener(view->details(entry));labels.setContentDescription(entry.label+", "+state+". App details");labels.setFocusable(true);
   Switch toggle=new Switch(MainActivity.this);toggle.setChecked(entry.enabled);toggle.setShowText(false);toggle.setMinWidth(dp(48));toggle.setMinimumHeight(dp(48));toggle.setThumbTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{ACCENT,MUTED}));toggle.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{0xff831525,0xff505660}));toggle.setEnabled(!busy&&rootReady&&(!entry.enabled||entry.protection==null));toggle.setAlpha(toggle.isEnabled()?1f:0.42f);toggle.setContentDescription(entry.label+" — "+(entry.enabled?"enabled":"disabled")+(entry.protection!=null?". "+entry.protection:". Turn "+(entry.enabled?"off to disable":"on to enable")));toggle.setOnClickListener(view->{toggle.setChecked(entry.enabled);requestChange(entry);});add(line,toggle,48,48);add(card,line,-1,-2);
   if(!landscape){gap(card,8);add(card,text((entry.system?"System app":"User app")+" · "+state,11,entry.protection==null?ACCENT_TEXT:MUTED),-1,-2);}return card;
  }
 }
}
