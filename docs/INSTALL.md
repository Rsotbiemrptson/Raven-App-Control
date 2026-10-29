# Installation

Download the signed APK from [GitHub Releases](https://github.com/Rsotbiemrptson/Raven-App-Control/releases) or `releases/v1.2.0/` and compare it with the accompanying SHA256SUMS.txt.

Open the APK in a device file manager. Allow installation from that file manager if Android asks. An update signed with the existing release identity can be installed over the previous release without uninstalling or resetting app preferences.

Open Raven and wait for **AYN root connected**. This release is hardware-tested on Odin 2 / Android 13 in the owner profile. A compatible, accessible AYN factory root service is required. The declared Android minimum is API 30; Android version alone does not prove root compatibility.

No storage, internet or background-service permission is requested. A local development build has a different signature and cannot update the published release. Re-enable controlled apps before uninstalling the release to try a differently signed build.
