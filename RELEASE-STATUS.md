# Raven App Control 1.2.1

- The System apps switch sits beside Search and applies to both Enabled and Disabled. Its setting is saved.
- Report an issue in the three-dot menu opens an editable GitHub form with the app version and prompts filled in.

The exact signed APK passed an in-place upgrade from 1.2.0 on AYN Odin 3, Android 15, firmware Odin3_V1.0.0.187_20260616_193307_user. The disposable app stayed disabled during the upgrade. Raven restored its original enabled setting and retained its private data. Theme, accent and system filter settings persisted. The switch worked in portrait and landscape, and Chrome opened the correct issue form with the 1.2.1 prompts.

On 1.2.0, normal power-menu restart and shutdown preserved disabled state. One immediate ADB reboot did not. Version 1.2.1 does not change package persistence behavior.

[Signed APK](https://github.com/Rsotbiemrptson/Raven-App-Control/releases/tag/v1.2.1) · [Checksum](releases/v1.2.1/SHA256SUMS.txt)
