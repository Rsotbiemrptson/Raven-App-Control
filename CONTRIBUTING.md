# Contributing

Keep changes focused and describe the device, Android version and firmware used for testing. Use a separate local signing key; never commit release keys, credentials, personal backups, device inventories or generated test packages.

Read [Building](docs/BUILD.md) and [Testing](docs/TESTING.md). App-state changes must be verified with Android, serialized, and limited to the selected package. Search and UI state must never implicitly enable another app. Protection belongs in the engine as well as the interface. Do not introduce a way around protected-package checks without explicit design review.

Use disposable fixtures for disable/enable tests. Preserve existing state and preferences after testing. Keep the shared Raven light/dark surface palette and readable accent contrast. Do not add screenshots or personal device evidence to the repository without the owner's request.

Contributions are distributed under GPL-3.0-only, the project's existing license.
