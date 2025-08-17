# ROSTRY MVP Scope

This document defines the Minimum Viable Product (MVP) scope for the ROSTRY poultry platform. All features not explicitly listed here should be hidden or feature-flagged as non-MVP.

## ✅ MVP Features

### Authentication
- Firebase Auth (phone/email)
- Minimal profile (name, region, type)

### Fowl Management
- Create fowl with basic information
- Parent linkage (2-generation lineage)
- Date of birth tracking
- Timeline with:
  - Vaccination records
  - Growth tracking (5w/20w/weekly)
  - Quarantine events
  - Mortality records
- Proof uploads (deferred with offline support)
- Cover thumbnail for fowl profile
- Reminders (5w/20w/weekly, quarantine 12h)

### Verified Transfer
- Giver/receiver initiation
- Receiver verification with descriptors
- Atomic local transaction:
  - TransferLog creation (VERIFIED/REJECTED)
  - Fowl.owner update
  - Listing.status update
- FCM notifications
- Immutable logs
- Dispute stub

### Marketplace
- Listing creation from owned inventory
- Filters (purpose, price, location)
- Link to fowl profile
- Message seller shortcut

### Offline-First Support
- Outbox pattern for offline operations
- WorkManager sync with retry/backoff
- Idempotency for all operations

### Localization & UX
- English + one local language support
- Large touch targets
- Offline states clearly indicated

## ❌ Non-MVP Features (Must be hidden/flagged)

These features must be hidden or placed behind feature flags with default OFF values:

- Broadcasting/calls
- Groups beyond basics
- Complex chat features
- Admin dashboards
- Advanced analytics
- Full coin UX
- IoT/QR/NFC integration

## Feature Flag Matrix

| Feature Flag | Default Value | MVP Status |
|--------------|---------------|------------|
| `fowl_records_enabled` | `true` | ✅ |
| `upload_proof_worker_enabled` | `true` | ✅ |
| `export_sharing_enabled` | `false` | ❌ |
| `thumbnail_caching_enabled` | `true` | ✅ |
| `cover_thumbnails_enabled` | `true` | ✅ |
| `smart_suggestions_enabled` | `true` | ✅ |

## Technical Requirements

### Security
- Ownership-based writes enforced
- Immutable TransferLog (no edits post VERIFIED/REJECTED)
- Proof media rules scoped to owners
- Public reads denied for proof media
- Export stays image-free

### Performance
- Timeline pagination via indexed queries
- Projections for list views
- Thumbnail caching
- Resilient proof uploads (resume, per-asset completion)
- Device-aware cache quotas

### Resilience
- Idempotent sync operations
- Rollback on sync failure
- Offline-first semantics preserved
- Memory optimized for 1-2GB devices

## Guardrails

1. Never mutate VERIFIED/REJECTED TransferLog; use Dispute records
2. Don't block UI on network; show pending states and retries
3. Preserve offline-first semantics and idempotent sync
4. Feature-flag risky changes and document defaults