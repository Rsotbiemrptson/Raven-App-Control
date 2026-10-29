# Raven App Control 1.2.0

- Add Settings to the three-dot menu and remove Help.
- Add light/dark/system appearance with its button beside Accent color.
- Add the shared Raven color wheel, RGB/hex editing and six dots: Backup green, System Control orange, Gamepad blue, purple, yellow and white. Default restores App Control red.
- Use the same Raven sidebar/card palettes in both themes.
- Compact About contains only App Control information, with no scrolling and an embedded GPL license link.
- Center overflow dots. Remove the switch-instruction footer and All apps tab; navigation is Enabled and Disabled.
- Replace icon-bearing Android toasts with short icon-free confirmations. Keep the last enabled/disabled action and date.
- Verify app state is independent across successive searches, disables, reopening and restoration.
- Serialize mutations across controller instances and strengthen always-on protection for platform-signed/non-launchable system components, shared identities, home handlers and active services.
- Verify the icon has the same fully opaque charcoal background as Raven Backup.

207 device checks passed on Odin 2 / Android 13 using two disposable fixtures. The first app stayed disabled when searching for and disabling the second; both states persisted through Activity recreation; restoring one did not restore the other; private data markers remained intact. Owner app states and appearance preferences were restored. The curated public-source build matched all nine APK payload entries of the tested release.

Requires compatible AYN factory PServerBinder access in the owner profile. Other hardware/firmware is unverified. Protection reduces risk but cannot guarantee every vendor dependency or prevent all possible device problems. Re-enable controlled apps before uninstalling Raven.

Install the signed APK over the previous release. Verify SHA256SUMS.txt. Licensed under GPL-3.0-only; corresponding source and build instructions are included in the version tag.
