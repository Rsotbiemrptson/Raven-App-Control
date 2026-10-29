# Usage

**Enabled** lists currently enabled applications. Search by name or package, then switch an app off. A short, icon-free confirmation names the affected app. Search for another app normally: the first app remains disabled. Searches and section changes do not change any package state.

**Disabled** lists disabled applications and provides switches to restore them. Each app has its own original-state record. Re-enabling one app does not re-enable another. Installed APKs and private data remain on the device.

The three-dot menu contains **Show system apps**, **Settings** and **About**. There is no All apps section or Help item. Show system apps is saved and applies to both lists. Protected system apps can be viewed and, if disabled, re-enabled; protected enabled apps cannot be switched off.

Settings puts **Accent color** and **Light/Dark mode** on one row. Accent choices include a hue/saturation wheel, RGB sliders, #RRGGBB input, three other Raven-app colors (Backup green, System Control orange, Gamepad blue), and purple/yellow/white. **Default** restores App Control red. Appearance can be Light, Dark or Follow system. The sidebar/card colors follow the shared Raven palette and accent text adjusts for contrast.

Settings also explains always-on app safeguards and recovery/removal. About is specific to App Control, does not scroll, and includes version, build date, creator and the GPL license link.

Android stores disabled-user state across app exits and restarts. Raven has no boot receiver or watcher. The last enabled/disabled action and date remain in the footer; the switch-instruction footer has been removed.
