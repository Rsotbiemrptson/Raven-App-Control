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

Output: `dist/Raven-App-Control-1.2.0-local.apk`, with a SHA-256 sidecar. Intermediates go in `.build/`; a development keystore is generated under `.local/`. These directories are ignored by Git.

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
& .\build.ps1 -Target fixture2
& .\build.ps1 -Target tests
```

Device test instructions are in [TESTING.md](TESTING.md). Private release signing keys and automated publishing credentials are deliberately excluded.

## Rebuild after moving or cleaning the checkout

Run the script from the repository root. The public build resolves source and default output paths from the script location, so the checkout can be moved, including into a folder whose name contains spaces.

To choose separate temporary folders:

```powershell
& .\build.ps1 -BuildDirectory '.\.build' -OutputDirectory '.\dist'
```

The optional parameters work with either the SDK/JDK configuration above or `-Toolchain`. For example, with the shared Workbench toolchain:

```powershell
& .\build.ps1 -Toolchain 'C:\APK-Workbench\Toolchain'
```

After verification, `.build/` and `dist/` can be moved out of the checkout; the script recreates them on the next build. Keep `.local/development.jks` to retain the signing identity of your local builds. Preserve any custom signing key separately as well. No previous build outputs are required to rebuild.

On October 29, 2026, the maintained workstation source rebuilt the signed 1.2.0 release byte-for-byte after relocation. The public build uses the portable script above and a separate development key; compare its uncompressed APK payload entries rather than expecting the release APK checksum. The app source, resources, manifest and test sources were checked against the maintained source and matched.
