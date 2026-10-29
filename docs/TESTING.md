# Testing

Version 1.2.0 passed 207 device checks on Odin 2 / Android 13. The app's own UID used AYN factory root; privileged app changes were limited to two disposable fixtures. A separate visual run checked light/dark About and Settings, the six-dot color picker and Enabled navigation. The curated source rebuild matched all nine APK payload entries of the signed release.

The checks cover sequential search/disabling of two apps, persistence across Activity recreation, independent restoration and retained private data; self/Settings/provider and non-launchable system safeguards; centered overflow and icon-free confirmations; the removed All apps/Help/footer; exact Raven surface colors; light/dark modes; one-row appearance controls; wheel/RGB/hex editing and all six dots; invalid hex/cancel/default behavior; non-scrolling App Control-only About and license; and an opaque icon background matching Backup.

Build app and both fixtures/tests with one signing identity:

```powershell
& .\scripts\test.ps1
```

Install all four packages on the intended unlocked test device, seed both fixtures and run:

```text
adb -s SERIAL install -r dist/Raven-App-Control-1.2.0-local.apk
adb -s SERIAL install -r dist/Raven-App-Control-fixture.apk
adb -s SERIAL install -r dist/Raven-App-Control-fixture2.apk
adb -s SERIAL install -r dist/Raven-App-Control-tests.apk
adb -s SERIAL shell am start -W -n dev.raven.control.fixture/.SeedActivity
adb -s SERIAL shell am start -W -n dev.raven.control.fixture.two/dev.raven.control.fixture.SeedActivity
adb -s SERIAL shell am instrument -w dev.raven.control.tests/dev.raven.control.SmokeTest
```

Both fixtures must start enabled. The test requires Raven Backup installed for native icon-background comparison. It restores appearance/display preferences and prior last-activity text, and attempts to restore both fixtures on failure. Remove both fixtures and the instrumentation APK afterward. No personal app data or real system-app disabling is needed.

Local development builds cannot instrument the published release because signing identities differ. Do not substitute personal packages for the fixtures. Raw device evidence stays private. No reboot was performed; Android's disabled-user state was checked directly. Other hardware/firmware needs independent validation.