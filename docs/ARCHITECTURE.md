# Architecture

- `MainActivity`: native Android views, responsive sidebar/bottom navigation, app search, filters, verified switches, Help and About.
- `ControlEngine`: inventories installed/disabled applications, saves the prior enabled/default setting, uses `pm disable-user --user 0` to disable, restores state when re-enabled, and verifies Android's result.
- `Protection`: checks essential package names, system identities, persistent services, active administrators, enabled keyboards and current launcher immediately before a disable.
- `AynRoot`: invokes only the known factory `PServerBinder` transaction 0 string-array protocol. Requires `id -u` to return 0, validates supported profile use and bounds command size. No generic root fallback.
- `AppearanceColors`: contrast helpers shared in design with Raven Backup; icon-derived red accent.

The service protocol is an observed private OEM interface, not a public Android API. Factory-service availability and permissions may differ across firmware. The app has no exported root-control service or shell-command intent endpoint; only the launcher Activity is exported.

No boot receiver or watcher is needed because Android stores package enabled state. Local preferences store original app states and the visibility option. App data is not deleted by disabling.
