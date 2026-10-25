# Build on Windows

Requires PowerShell, a JDK, Android SDK Platform 35 and Android SDK Build Tools 35.0.0. The script uses `javac`, D8, `aapt`, `zipalign` and `apksigner`; Gradle is not required.

Set `ANDROID_SDK_ROOT` and `JAVA_HOME`, then:

```powershell
& .\build.ps1
```

Or supply paths explicitly:

```powershell
& .\build.ps1 -AndroidSdk 'D:\Android\Sdk' -JavaHome 'D:\Java\jdk'
```

Output: `dist/Raven-App-Control-1.1.1-local.apk`, with a SHA-256 sidecar. Intermediates go in `.build/`; a development keystore is generated under `.local/`. These directories are ignored by Git.

The development key uses the conventional `android` password and is only for local builds. It is unrelated to the preserved private release identity. A differently signed APK cannot update the published app. Re-enable controlled apps before uninstalling the release to try a local build.

For your own signing identity, pass `-SigningKey` and `-KeyAlias`; set `RAVEN_CONTROL_STORE_PASS` and optionally `RAVEN_CONTROL_KEY_PASS` in the process environment. Do not commit keys or passwords.

An optional `-Toolchain` path supports this directory layout:

```text
Android-Build-Tools/  (executables and lib/)
Android-SDK/platforms/android-35/android.jar
Java/bin/
```

Build fixture and instrumentation targets with the same signing identity:

```powershell
& .\build.ps1 -Target fixture
& .\build.ps1 -Target tests
```

Device test instructions are in [TESTING.md](TESTING.md). Private release signing keys and automated publishing credentials are deliberately excluded.
