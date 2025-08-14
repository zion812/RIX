# Production Readiness

Status: Partial (Core foundation ready; advanced modules gated)
Gate: Pre-Beta

## Readiness Checklist

- Authentication (Firebase Auth): PASS
- Navigation (Compose): PASS
- Room Wiring: NEEDS ACTION — app references DatabaseProvider from :core:database-simple; ensure dependency in app/build.gradle
- Firestore Offline Persistence: PASS
- FCM Notifications: PASS (SimpleFCMService)
- Background Sync: PARTIAL — Basic SyncWorker; :core:sync not integrated
- Payments: DEMO ONLY — module not included in app build
- Feature Modules: PRESENT BUT DISABLED — staged enablement planned
- Security: FAIL — keystore committed; rotate and purge history

## Actions to Reach Beta

1) Fix Room wiring mismatch
2) Remove keystore from VCS, rotate keys, update SECURITY.md
3) Integrate :core:data with Hilt providers and add tests
4) Optional: Integrate :core:sync or elevate SyncWorker to cover critical sync flows with network heuristics
