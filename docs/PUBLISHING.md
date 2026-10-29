# Publishing

Repository: https://github.com/Rsotbiemrptson/Raven-App-Control

Follow the Raven Backup repository structure: source/resources and portable build script at the root; public guides in docs; helpers in scripts; selected signed releases under releases/vVERSION.

Update version metadata, changelog, release status, build/install/testing documentation and matching source. Build with the preserved release identity privately, verify signing/alignment, and test on supported hardware using disposable fixtures. Include the signed APK, SHA256SUMS.txt, LICENSE, SIGNING-CERTIFICATE.txt and RELEASE-NOTES.md in the versioned release folder.

Commit the matching source and documentation, push a version tag, and attach the approved APK/checksum/license to GitHub Releases. Preserve existing public tags and old release assets; investigate differences before replacing anything. The signed release's certificate must remain compatible with earlier installs.

Never publish private keys, credentials, firmware/decompiled OEM APKs, device inventories, screenshots, raw device-test output, personal backups, generated fixture APKs or the general local Outputs tree. Only the existing app-icon artwork belongs in docs/images unless the owner asks for more.
