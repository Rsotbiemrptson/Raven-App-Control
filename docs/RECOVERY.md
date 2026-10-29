# Recovery

Open Disabled and switch the affected app on. If it is a system app, enable **Show system apps** in the three-dot menu first. Protection does not prevent re-enabling.

Android's App info screen may also offer **Enable**. With a previously authorized ADB connection, the package manager can restore a known package in the owner profile:

```text
adb -s SERIAL shell pm enable --user 0 PACKAGE
```

Replace SERIAL and PACKAGE with the intended device and actual affected package. Do not run unreviewed lists of system-package commands. This command is an explicit enable operation; Raven normally restores the prior default/enabled state it saved for that app.

Uninstalling or clearing Raven's data does not undo Android's package disabled state. Clearing data loses Raven's prior-state history and appearance preferences. Reinstalling the signed app can inventory Android's disabled packages again if factory root access remains available.

If the device cannot boot normally, recovery options depend on its firmware and whether ADB is available. Raven has no pre-boot rescue component and cannot promise recovery from arbitrary changes made by another tool. Preserve your firmware recovery instructions and independent backups.
