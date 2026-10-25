# Testing

Hardware validation covers Odin 2 / Android 13. Other models and firmware are unverified.

For 1.1.0, 45 engine/view/layout checks and 25 unlocked live menu/About/Help checks passed. The icon update in 1.1.1 passed 31 targeted live checks, including adaptive icon loading, transparent corners, equal-size native icon rendering, root connection, system-app visibility persistence, core protection, and About/Help interactions. Native raven width/height differed from Raven Backup by one pixel at a 384px render size.

The repository's device tests use a disposable `dev.raven.control.fixture` app. Do not replace that package with an existing personal app. The test APK must share the tested app's signing certificate; local development tests cannot instrument the differently signed public release.

Build all three targets, install on the intended device and seed the fixture:

```text
adb -s SERIAL install -r dist/Raven-App-Control-1.1.1-local.apk
adb -s SERIAL install -r dist/Raven-App-Control-fixture.apk
adb -s SERIAL install -r dist/Raven-App-Control-tests.apk
adb -s SERIAL shell am start -W -n dev.raven.control.fixture/.SeedActivity
adb -s SERIAL shell am instrument -w dev.raven.control.tests/dev.raven.control.SmokeTest
```

The fixture must start enabled. Tests verify disabled-user state, inability to launch, data preservation, restored state and actual app-view switches. The locked-device branch renders only the app's own views without dismissing the lock screen or injecting lock-screen input. The unlocked branch uses live menu interactions.

A targeted menu/icon/About run is available with `-e menuOnly true`. That mode also requires Raven Backup installed for native icon comparison. The full test suite exercises live filters and may reset the Show system apps preference; use a test device and review its state afterward. The targeted run restores the original visibility preference.

After testing, re-enable the fixture if needed, uninstall both helper APKs, and remove test-generated images from Raven's external files folder. Test APKs are not part of the distributed app. No reboot validation was performed for this release; Android's persistent disabled-user state was verified directly.
