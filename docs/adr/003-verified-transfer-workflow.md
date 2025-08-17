# 3. Verified Transfer Workflow

Date: 2025-08-16

## Status

Accepted

## Context

The ROSTRY platform requires a trust-critical verified transfer workflow for fowl ownership changes. This is essential to prevent fraud and ensure legitimate transfers between users. The system must support offline operations while maintaining an immutable audit trail.

Key requirements:
- Transfers must be verified by the receiving party
- All transfer operations must be logged immutably
- The system must work offline and sync when connectivity is restored
- Verification must include matching descriptors (price, color, age/weight, photo)

## Decision

We will implement a verified transfer workflow with the following components:

1. **TransferLogEntity** - An immutable log of all transfer operations with detailed verification information
2. **Outbox Pattern** - For offline-first operations with sync capability
3. **Atomic Local Transactions** - Ensuring all related updates happen together (TransferLog, Fowl.ownerUserId, MarketListing.status)
4. **Receiver Verification** - Mandatory verification step by the receiving user before ownership changes

### Workflow Steps

1. **Initiation** - Giver initiates transfer with verification details
2. **Offline Support** - All operations queued in outbox for sync
3. **Notification** - Receiver notified of pending transfer
4. **Verification** - Receiver verifies transfer details match expectations
5. **Completion** - Atomic update of all related entities
6. **Notification** - Both parties notified of completion

### Data Model

The TransferLog entity contains:
- All parties involved (from/to users)
- Fowl being transferred
- Verification details (expected descriptors)
- Status tracking (pending, verified, rejected)
- Timeline information (initiated, verified, completed timestamps)
- Dispute handling fields

### Security Considerations

- Only the intended receiver can verify a transfer
- All transfer logs are immutable after terminal state (verified/rejected)
- Disputes can be raised for verified transfers if needed
- Verification documents can be attached for evidence

## Consequences

### Positive
- Strong audit trail for all transfers
- Support for offline operations
- Trust-enhancing verification process
- Dispute resolution capability

### Negative
- Increased complexity in transfer process
- Additional storage requirements for logs
- Need for robust sync mechanism

## Implementation Plan

1. Create TransferLog entity and DAO
2. Implement Outbox pattern for sync operations
3. Build TransferRepository with verified workflow
4. Create WorkManager workers for background sync
5. Implement UI for transfer initiation and verification
6. Add notification system for transfer events
7. Comprehensive testing of offline scenarios