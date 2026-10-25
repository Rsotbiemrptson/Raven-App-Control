package dev.raven.control.fixture;
public final class SeedActivity extends android.app.Activity {
 @Override public void onCreate(android.os.Bundle state){super.onCreate(state);try(java.io.FileOutputStream out=openFileOutput("keep.txt",0)){out.write("raven-control-data-preserved".getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception error){throw new RuntimeException(error);}android.widget.TextView label=new android.widget.TextView(this);label.setText("Disposable Raven Control test app — data seeded");setContentView(label);}
}
