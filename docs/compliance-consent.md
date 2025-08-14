# Consent and Data Retention (RBI/GDPR)

This app implements a minimal consent and data retention flow optimized for rural connectivity and
low-end devices.

Overview

- Users are presented with a consent dialog on first launch.
- Consent covers analytics/performance monitoring and service-critical processing.
- An explicit checkbox captures agreement to RBI/GDPR data retention policy.
- Choices are stored locally and can be synced to server in future phases.

Implementation

- UI: `app/src/main/java/com/rio/rostry/ui/compliance/ConsentDialog.kt` (English + Telugu strings)
- Storage: `core/common/.../ConsentManager.kt` (SharedPreferences)
- Entry point: Shown from `ROSTRYApp` when consent not yet recorded.

Future enhancements

- Server-side consent ledger in Firestore with audit log.
- In-app Settings screen to manage consent and retention choices.
- Locale-aware, offline-ready consent text with versioning.
- Export/delete requests handling to meet GDPR data subject requests.
