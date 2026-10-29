# Security and safety

Raven App Control can request privileged package-manager changes through a compatible AYN factory service. Treat the ability to disable packages as device administration, and install releases only from a source you trust.

The app accepts no shell-command intent or exported control service. Package names are validated; root requests are bounded; owner-profile support and UID 0 are checked. The enabled state is verified after every mutation. Concurrent engine mutations are serialized across Activity/controller instances.

## Protected packages

Before each disable, the engine checks Raven itself, named Android essentials, system/shared identities, persistent services, all home-screen handlers, enabled keyboards, active accessibility services, active administrators and the current WebView provider. Platform-signed system packages and system packages without an enabled launcher are protected. This conservatively covers many providers, overlays, vendor/boot services and recovery paths. Protection is not optional in Settings. Re-enabling is allowed for protected apps.

These checks reduce risk; they cannot establish every vendor dependency or guarantee that disabling an otherwise allowed package will never break a feature. Other system apps require confirmation. Android, firmware, root-service access or package state may also change outside Raven.

Re-enable controlled apps before uninstalling Raven. See [Recovery](docs/RECOVERY.md). Do not disable system components through another tool to bypass Raven's safeguards.

For a suspected vulnerability, contact the repository owner through the contact method on their GitHub profile. Avoid posting credentials, private app data or an unredacted device inventory in a public issue. No reporting email address is assumed by this project.
