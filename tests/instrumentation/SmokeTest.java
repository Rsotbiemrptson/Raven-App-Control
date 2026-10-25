package dev.raven.control;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Runs in Raven's target UID. Only the disposable fixture is modified. */
public final class SmokeTest extends Instrumentation {
 private static final String FIXTURE="dev.raven.control.fixture";
 private final StringBuilder report=new StringBuilder();
 private int checks=0;
 private boolean deviceLocked;
 private Activity previewActivity;
 private int previewWidth,previewHeight;
 private boolean menuOnly;
 private String restoreShowSystem;
 private void check(boolean passed,String description){if(!passed)throw new AssertionError(description);checks++;report.append("PASS ").append(description).append('\n');}
 @Override public void onCreate(Bundle args){super.onCreate(args);menuOnly=args!=null&&"true".equals(args.getString("menuOnly"));restoreShowSystem=args==null?null:args.getString("restoreShowSystem");start();}
 @Override public void onStart(){
  if(menuOnly){liveMenuChecks();return;}
  ControlEngine engine=new ControlEngine(getTargetContext());boolean changed=false;int initial=0;
  try {
   check(getTargetContext().getPackageName().equals("dev.raven.control"),"Runs in Raven App Control context");
   check(android.os.Process.myUid()==getTargetContext().getApplicationInfo().uid,"Root calls originate from app UID");
   AynRoot.requireRoot();check(true,"AYN factory service proves UID 0");
   for(String bad:new String[]{"com.test;id","com.test'","../data","","com.test\nreboot"}){boolean rejected=false;try{ControlEngine.validatePackage(bad);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Reject invalid package "+bad.replace('\n',' '));}
   PackageManager pm=getTargetContext().getPackageManager();initial=pm.getApplicationEnabledSetting(FIXTURE);
   check(engine.isEnabled(FIXTURE),"Fixture initially enabled");
   Protection guard=new Protection(getTargetContext());
   check(guard.reason(pm.getApplicationInfo("dev.raven.control",0))!=null,"Raven protects itself");
   check(guard.reason(pm.getApplicationInfo("com.android.settings",0))!=null,"Device settings protected");
   boolean blocked=false;try{engine.setEnabled("dev.raven.control",false);}catch(SecurityException expected){blocked=true;}check(blocked,"Engine rejects self-disable before root mutation");
   check(guard.reason(pm.getApplicationInfo(FIXTURE,0))==null,"Ordinary app can be controlled");
   check(engine.list().stream().anyMatch(e->e.packageName.equals(FIXTURE)&&e.enabled),"Enabled fixture appears in app inventory");
   String payload=AynRoot.execute("cat /data/user/0/"+FIXTURE+"/files/keep.txt");check(payload.equals("raven-control-data-preserved"),"Fixture private data seeded");
   changed=true;engine.setEnabled(FIXTURE,false);
   check(!engine.isEnabled(FIXTURE),"Disable verified by Android");
   check(pm.getApplicationEnabledSetting(FIXTURE)==PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,"Uses persistent disabled-user state");
   check(pm.getLaunchIntentForPackage(FIXTURE)==null,"Disabled app cannot be launched");
   check(engine.list().stream().anyMatch(e->e.packageName.equals(FIXTURE)&&!e.enabled),"Disabled app remains in inventory");
   check(AynRoot.execute("cat /data/user/0/"+FIXTURE+"/files/keep.txt").equals(payload),"Disabling preserves private data");
   check(new ControlEngine(getTargetContext()).isEnabled(FIXTURE)==false,"Disabled state survives a fresh controller instance");
   engine.setEnabled(FIXTURE,true);changed=false;
   check(engine.isEnabled(FIXTURE),"Re-enable verified by Android");
   check(pm.getApplicationEnabledSetting(FIXTURE)==initial,"Original enabled/default setting restored");
   check(pm.getLaunchIntentForPackage(FIXTURE)!=null,"Re-enabled app is launchable");
   check(AynRoot.execute("cat /data/user/0/"+FIXTURE+"/files/keep.txt").equals(payload),"Re-enabling preserves private data");
   check(engine.setEnabled(FIXTURE,true).startsWith("Already"),"Already-enabled operation is harmless");
   boolean locked=((android.app.KeyguardManager)getTargetContext().getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked();deviceLocked=locked;
   getTargetContext().getSharedPreferences("display",0).edit().putBoolean("showSystem",false).commit();if(!locked)getUiAutomation();Thread.sleep(500);
   Activity activity=startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
   previewActivity=activity;
   Thread.sleep(2500);waitForIdleSync();
   onUi(()->{View decor=activity.getWindow().getDecorView();check(findText(decor,"Raven App Control")!=null,"App title visible");check(findText(decor,"AYN root connected")!=null,"UI root check succeeds");check(findText(decor,"All apps")!=null&&findText(decor,"Disabled")!=null&&findText(decor,"Enabled")!=null,"App filters visible");EditText search=findSearch(decor);check(search!=null,"Search field present");search.setText(FIXTURE);});
   waitForIdleSync();
   onUi(()->{View decor=activity.getWindow().getDecorView();check(findText(decor,"Raven Control Test App")!=null,"Search finds fixture by package");Switch toggle=findSwitch(decor);check(toggle!=null&&toggle.isChecked()&&toggle.isEnabled(),"Enabled app has active on-switch");toggle.performClick();});
   Thread.sleep(2200);waitForIdleSync();check(!engine.isEnabled(FIXTURE),"UI switch disables fixture");changed=true;
   onUi(()->{Switch toggle=findSwitch(activity.getWindow().getDecorView());check(toggle!=null&&!toggle.isChecked(),"UI off-switch matches verified disabled state");findText(activity.getWindow().getDecorView(),"Disabled").performClick();});waitForIdleSync();
   onUi(()->{Switch toggle=findSwitch(activity.getWindow().getDecorView());check(toggle!=null&&!toggle.isChecked()&&toggle.isEnabled(),"Disabled tab offers restoration switch");toggle.performClick();});
   Thread.sleep(2200);waitForIdleSync();check(engine.isEnabled(FIXTURE),"UI switch re-enables fixture");changed=false;
   onUi(()->{check(findText(activity.getWindow().getDecorView(),"No matching apps")!=null,"Restored app leaves Disabled results");findText(activity.getWindow().getDecorView(),"All apps").performClick();EditText search=findSearch(activity.getWindow().getDecorView());search.setText("");});waitForIdleSync();
   if(locked){
    report.append("Device locked: using only Raven's own views; no lock-screen input or screen capture.\n");
    onUi(()->{
     LinearLayout nav=activity.findViewById(android.R.id.content).findViewWithTag("app-navigation");check(nav!=null&&nav.getOrientation()==LinearLayout.VERTICAL,"Landscape uses vertical sidebar");check(nav.getWidth()==Math.round(148*activity.getResources().getDisplayMetrics().density),"Sidebar matches Raven Backup's 148dp width");
     check(AppearanceColors.contrast(MainActivity.ACCENT_TEXT,0xff272b32)>=4.5,"Accent text has readable contrast");check(AppearanceColors.contrast(MainActivity.INK,MainActivity.ACCENT)>=4.5,"Selected navigation text has readable contrast");
     findSearch(activity.getWindow().getDecorView()).setText(FIXTURE);
    });waitForIdleSync();
    onUi(()->{Switch toggle=findSwitch(activity.getWindow().getDecorView());check(toggle.getThumbTintList().getColorForState(new int[]{android.R.attr.state_checked},0)==0xfffb0b24,"Enabled switches use sampled icon red");findSearch(activity.getWindow().getDecorView()).setText("");});waitForIdleSync();captureOwnView(activity,"preview-landscape.png");
    android.content.res.Configuration originalConfig=new android.content.res.Configuration(activity.getResources().getConfiguration());android.util.DisplayMetrics originalMetrics=new android.util.DisplayMetrics();originalMetrics.setTo(activity.getResources().getDisplayMetrics());
    onUi(()->renderOrientation(activity,android.content.res.Configuration.ORIENTATION_PORTRAIT));waitForIdleSync();
    onUi(()->{LinearLayout nav=activity.findViewById(android.R.id.content).findViewWithTag("app-navigation");check(nav.getOrientation()==LinearLayout.HORIZONTAL,"Portrait uses bottom navigation");check(findText(activity.getWindow().getDecorView(),"AYN root connected")!=null,"Root connection status survives rotation");});captureOwnView(activity,"preview-portrait.png");
    onUi(()->{previewWidth=0;previewHeight=0;activity.getResources().updateConfiguration(originalConfig,originalMetrics);activity.onConfigurationChanged(originalConfig);});waitForIdleSync();
    check(((android.app.KeyguardManager)getTargetContext().getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked(),"Device remains locked after checks");
    getTargetContext().getSharedPreferences("app-control",0).edit().remove("last").commit();Bundle result=new Bundle();result.putString("stream",report+"\n"+checks+" checks passed. Popup menu and About screen interaction skipped while locked.\n");finish(Activity.RESULT_OK,result);return;
   }
   onUi(()->findSearch(activity.getWindow().getDecorView()).setText("com.android.settings"));waitForIdleSync();
   onUi(()->check(findText(activity.getWindow().getDecorView(),"No matching apps")!=null,"System app hidden by default"));
   onUi(()->findText(activity.getWindow().getDecorView(),"⋮").performClick());waitForIdleSync();
   Thread.sleep(300);android.view.accessibility.AccessibilityNodeInfo option=menuNode("Show system apps");check(option!=null,"Three-dot menu contains system-app option");check(!menuHasCheckedOption(),"System-app option initially unchecked");capture("menu.png");clickMenu("Show system apps");waitForIdleSync();
   onUi(()->{check(findText(activity.getWindow().getDecorView(),"com.android.settings")!=null,"Menu option reveals system app");Switch protectedSwitch=findAppSwitch(activity.getWindow().getDecorView(),"com.android.settings");check(protectedSwitch!=null&&!protectedSwitch.isEnabled(),"Protected system app switch is unavailable");});
   check(getTargetContext().getSharedPreferences("display",0).getBoolean("showSystem",false),"System-app visibility preference saved");
   onUi(()->findText(activity.getWindow().getDecorView(),"⋮").performClick());waitForIdleSync();Thread.sleep(200);check(menuHasCheckedOption(),"Menu reflects saved system-app visibility");clickMenu("Show system apps");waitForIdleSync();
   onUi(()->{check(findText(activity.getWindow().getDecorView(),"No matching apps")!=null,"Menu option hides system app again");findSearch(activity.getWindow().getDecorView()).setText("");findText(activity.getWindow().getDecorView(),"⋮").performClick();});waitForIdleSync();Thread.sleep(200);clickMenu("About");waitForIdleSync();
   check(menuNode("Raven App Control")!=null&&menuNode("Version 1.1.1")!=null,"About displays app name and version");check(menuNode("Private by design")!=null,"About describes offline privacy");capture("about.png");clickMenu("Close");waitForIdleSync();
   getTargetContext().getSharedPreferences("app-control",0).edit().remove("last").commit();
   Bundle result=new Bundle();result.putString("stream",report+"\n"+checks+" checks passed.\n");finish(Activity.RESULT_OK,result);
  } catch(Throwable error){
   if(changed)try{engine.setEnabled(FIXTURE,true);}catch(Exception recovery){report.append("RECOVERY FAILED: ").append(recovery).append('\n');}
   Bundle result=new Bundle();result.putString("stream",report+"\nFAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);
  }
 }
 private void liveMenuChecks(){
  android.content.SharedPreferences display=getTargetContext().getSharedPreferences("display",0);boolean original=restoreShowSystem==null?display.getBoolean("showSystem",false):Boolean.parseBoolean(restoreShowSystem);if(restoreShowSystem!=null)display.edit().putBoolean("showSystem",original).commit();
  try{
   check(!((android.app.KeyguardManager)getTargetContext().getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked(),"Odin is unlocked for live checks");getUiAutomation();
   Activity activity=startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Thread.sleep(2200);waitForIdleSync();
   onUi(()->verifyIconMatch(activity));
   onUi(()->{check(findText(activity.getWindow().getDecorView(),"AYN root connected")!=null,"Live app connects to AYN root");findText(activity.getWindow().getDecorView(),"All apps").performClick();findSearch(activity.getWindow().getDecorView()).setText("com.android.settings");});waitForIdleSync();
   openOverflow(activity);check(menuNode("Show system apps")!=null&&menuNode("Help")!=null&&menuNode("About")!=null,"Three-dot menu displays all three actions");check(menuHasCheckedOption()==original,"Menu checkmark matches saved preference");capture("live-menu.png");
   if(original){clickMenu("Show system apps");waitForIdleSync();}else{sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);waitForIdleSync();}
   onUi(()->check(findText(activity.getWindow().getDecorView(),"No matching apps")!=null,"Hiding system apps removes Settings from results"));
   openOverflow(activity);clickMenu("Show system apps");waitForIdleSync();
   onUi(()->{check(findText(activity.getWindow().getDecorView(),"com.android.settings")!=null,"Showing system apps reveals Settings");Switch control=findAppSwitch(activity.getWindow().getDecorView(),"com.android.settings");check(control!=null&&!control.isEnabled(),"Essential system app remains protected");});
   check(display.getBoolean("showSystem",false),"Show-system preference is saved");openOverflow(activity);check(menuHasCheckedOption(),"Reopened menu displays checked option");sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);waitForIdleSync();
   ActivityMonitor monitor=addMonitor(MainActivity.class.getName(),null,false);onUi(activity::recreate);Activity reopened=monitor.waitForActivityWithTimeout(8000);removeMonitor(monitor);check(reopened!=null,"Activity recreation completes");Thread.sleep(2200);waitForIdleSync();onUi(()->findSearch(reopened.getWindow().getDecorView()).setText("com.android.settings"));waitForIdleSync();
   onUi(()->check(findText(reopened.getWindow().getDecorView(),"com.android.settings")!=null,"System visibility persists in newly opened activity"));
   openOverflow(reopened);clickMenu("Show system apps");waitForIdleSync();onUi(()->check(findText(reopened.getWindow().getDecorView(),"No matching apps")!=null,"System apps hide again after menu toggle"));
   onUi(()->findSearch(reopened.getWindow().getDecorView()).setText(""));waitForIdleSync();openOverflow(reopened);clickMenu("About");waitForIdleSync();
   check(menuNode("Raven App Control")!=null,"About displays app name");check(menuNode("Version 1.1.1")!=null,"About displays installed release version");check(menuNode("Built for AYN")!=null,"About includes AYN description");capture("live-about.png");
   android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();scrollForward(root);waitForIdleSync();check(menuNode("Private by design")!=null,"About scroll exposes privacy information");capture("live-about-scrolled.png");clickMenu("Close");waitForIdleSync();
   onUi(()->check(findSearch(reopened.getWindow().getDecorView())!=null,"Closing About returns to app list"));
   openOverflow(reopened);clickMenu("Help");waitForIdleSync();check(menuNode("Got it")!=null,"Help opens from overflow menu");clickMenu("Got it");waitForIdleSync();
   if(original){openOverflow(reopened);clickMenu("Show system apps");waitForIdleSync();}check(display.getBoolean("showSystem",false)==original,"Original system-app preference restored");
   capture("live-layout.png");Bundle result=new Bundle();result.putString("stream",report+"\n"+checks+" live checks passed. No apps were disabled or enabled.\n");finish(Activity.RESULT_OK,result);
  }catch(Throwable error){display.edit().putBoolean("showSystem",original).commit();Bundle result=new Bundle();result.putString("stream",report+"\nFAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}
 }
 private void verifyIconMatch(Activity activity){try{
  android.graphics.drawable.Drawable backup=getTargetContext().getPackageManager().getApplicationIcon("dev.raven.backup"),control=getTargetContext().getPackageManager().getApplicationIcon("dev.raven.control");
  check(backup instanceof android.graphics.drawable.AdaptiveIconDrawable&&control instanceof android.graphics.drawable.AdaptiveIconDrawable,"Both apps load Android adaptive launcher icons");
  ImageView visible=findImage(activity.getWindow().getDecorView(),"Raven App Control");check(visible!=null&&visible.getDrawable() instanceof android.graphics.drawable.AdaptiveIconDrawable,"Header/sidebar uses Android launcher drawable");check(visible.getBackground()==null&&!visible.getClipToOutline(),"No custom background or clipping changes icon proportions");
  android.graphics.Bitmap source=android.graphics.BitmapFactory.decodeResource(getTargetContext().getResources(),getTargetContext().getResources().getIdentifier("raven_icon","drawable","dev.raven.control"));check(source.getWidth()==source.getHeight(),"Embedded artwork remains square");check(android.graphics.Color.alpha(source.getPixel(0,0))==0,"Embedded artwork outside corner is transparent");source.recycle();
  android.graphics.Bitmap a=android.graphics.Bitmap.createBitmap(384,384,android.graphics.Bitmap.Config.ARGB_8888),b=android.graphics.Bitmap.createBitmap(384,384,android.graphics.Bitmap.Config.ARGB_8888);backup.setBounds(0,0,384,384);control.setBounds(0,0,384,384);backup.draw(new android.graphics.Canvas(a));control.draw(new android.graphics.Canvas(b));
  android.graphics.Rect x=peachBounds(a),y=peachBounds(b);check(Math.abs(x.width()-y.width())<=3&&Math.abs(x.height()-y.height())<=3,"Rendered raven width and height match Backup within 3 pixels at 384px");report.append("Native raven bounds: Backup ").append(x).append("; Control ").append(y).append('\n');
  android.graphics.Bitmap sheet=android.graphics.Bitmap.createBitmap(1024,560,android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas canvas=new android.graphics.Canvas(sheet);canvas.drawColor(0xff131519);canvas.drawBitmap(a,64,64,null);canvas.drawBitmap(b,576,64,null);android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);paint.setColor(0xfff1f2f6);paint.setTextSize(30);canvas.drawText("Raven Backup",140,510,paint);canvas.drawText("Raven App Control",635,510,paint);try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),"icon-comparison.png"))){sheet.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}a.recycle();b.recycle();sheet.recycle();
 }catch(Exception error){throw new AssertionError(error);}}
 private android.graphics.Rect peachBounds(android.graphics.Bitmap image){int left=image.getWidth(),top=image.getHeight(),right=0,bottom=0;for(int row=0;row<image.getHeight();row++)for(int col=0;col<image.getWidth();col++){int c=image.getPixel(col,row),r=android.graphics.Color.red(c),g=android.graphics.Color.green(c),b=android.graphics.Color.blue(c);if(android.graphics.Color.alpha(c)>180&&r>150&&g>65&&g<210&&b<180&&r-g>25){left=Math.min(left,col);right=Math.max(right,col);top=Math.min(top,row);bottom=Math.max(bottom,row);}}return new android.graphics.Rect(left,top,right+1,bottom+1);}
 private ImageView findImage(View view,String description){if(view instanceof ImageView&&description.contentEquals(view.getContentDescription()==null?"":view.getContentDescription()))return (ImageView)view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){ImageView found=findImage(group.getChildAt(i),description);if(found!=null)return found;}}return null;}
 private void openOverflow(Activity activity){onUi(()->findText(activity.getWindow().getDecorView(),"⋮").performClick());waitForIdleSync();SystemClock.sleep(250);}
 private boolean scrollForward(android.view.accessibility.AccessibilityNodeInfo node){if(node==null)return false;if(node.isScrollable()&&node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)){SystemClock.sleep(300);return true;}for(int i=0;i<node.getChildCount();i++)if(scrollForward(node.getChild(i)))return true;return false;}
 private void onUi(Runnable action){java.util.concurrent.atomic.AtomicReference<Throwable> failure=new java.util.concurrent.atomic.AtomicReference<>();super.runOnMainSync(()->{try{layoutOwnView();action.run();layoutOwnView();}catch(Throwable error){failure.set(error);}});if(failure.get()!=null)throw new AssertionError(failure.get());}
 private void layoutOwnView(){if(deviceLocked&&previewActivity!=null){View decor=previewActivity.getWindow().getDecorView();android.util.DisplayMetrics metrics=previewActivity.getResources().getDisplayMetrics();int width=previewWidth==0?metrics.widthPixels:previewWidth,height=previewHeight==0?metrics.heightPixels:previewHeight;decor.forceLayout();decor.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));decor.layout(0,0,width,height);}}
 private void renderOrientation(Activity activity,int orientation){android.content.res.Configuration config=new android.content.res.Configuration(activity.getResources().getConfiguration());android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();metrics.setTo(activity.getResources().getDisplayMetrics());int shortSide=Math.min(metrics.widthPixels,metrics.heightPixels),longSide=Math.max(metrics.widthPixels,metrics.heightPixels);previewWidth=metrics.widthPixels=orientation==android.content.res.Configuration.ORIENTATION_PORTRAIT?shortSide:longSide;previewHeight=metrics.heightPixels=orientation==android.content.res.Configuration.ORIENTATION_PORTRAIT?longSide:shortSide;config.orientation=orientation;config.screenWidthDp=Math.round(metrics.widthPixels/metrics.density);config.screenHeightDp=Math.round(metrics.heightPixels/metrics.density);activity.getResources().updateConfiguration(config,metrics);activity.onConfigurationChanged(config);}
 private void captureOwnView(Activity activity,String name){onUi(()->{View decor=activity.getWindow().getDecorView();if(decor.getWidth()==0||decor.getHeight()==0)throw new AssertionError("App view has no measured bounds");android.graphics.Bitmap image=android.graphics.Bitmap.createBitmap(decor.getWidth(),decor.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);decor.draw(new android.graphics.Canvas(image));try(java.io.FileOutputStream stream=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),name))){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,stream);}catch(Exception error){throw new AssertionError(error);}finally{image.recycle();}});}
 private Switch findAppSwitch(View root,String pkg){TextView name=findText(root,pkg);return name==null?null:findSwitch((View)name.getParent().getParent());}
 private TextView findText(View view,String exact){if(view instanceof TextView&&!(view instanceof EditText)&&((TextView)view).getText().toString().equals(exact))return (TextView)view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){TextView found=findText(group.getChildAt(i),exact);if(found!=null)return found;}}return null;}
 private EditText findSearch(View view){if(view instanceof EditText)return (EditText)view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){EditText found=findSearch(group.getChildAt(i));if(found!=null)return found;}}return null;}
 private Switch findSwitch(View view){if(view instanceof Switch)return (Switch)view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){Switch found=findSwitch(group.getChildAt(i));if(found!=null)return found;}}return null;}
 private android.view.accessibility.AccessibilityNodeInfo menuNode(String label){for(int retry=0;retry<25;retry++){android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();if(root!=null)for(android.view.accessibility.AccessibilityNodeInfo node:root.findAccessibilityNodeInfosByText(label))if(node.getText()!=null&&node.getText().toString().equalsIgnoreCase(label))return node;SystemClock.sleep(100);}return null;}
 private boolean menuHasCheckedOption(){return treeHasChecked(getUiAutomation().getRootInActiveWindow());}
 private void capture(String name){try{android.graphics.Bitmap image=getUiAutomation().takeScreenshot();try(java.io.FileOutputStream stream=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),name))){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,stream);}finally{image.recycle();}}catch(Exception error){throw new AssertionError(error);}}
 private boolean treeHasChecked(android.view.accessibility.AccessibilityNodeInfo node){if(node==null)return false;if(node.isCheckable()&&node.isChecked())return true;for(int i=0;i<node.getChildCount();i++)if(treeHasChecked(node.getChild(i)))return true;return false;}
 private void clickMenu(String label){android.view.accessibility.AccessibilityNodeInfo node=menuNode(label);if(node==null)throw new AssertionError("Missing menu item: "+label);android.graphics.Rect bounds=new android.graphics.Rect();node.getBoundsInScreen(bounds);long now=SystemClock.uptimeMillis();android.view.MotionEvent down=android.view.MotionEvent.obtain(now,now,android.view.MotionEvent.ACTION_DOWN,bounds.centerX(),bounds.centerY(),0);android.view.MotionEvent up=android.view.MotionEvent.obtain(now,now+60,android.view.MotionEvent.ACTION_UP,bounds.centerX(),bounds.centerY(),0);down.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);up.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);try{check(getUiAutomation().injectInputEvent(down,true)&&getUiAutomation().injectInputEvent(up,true),"Tap "+label);SystemClock.sleep(350);}finally{down.recycle();up.recycle();}}
}
