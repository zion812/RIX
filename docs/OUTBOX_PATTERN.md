# ROSTRY Platform - Outbox Pattern Implementation

This document describes the implementation of the outbox pattern in the ROSTRY platform, which enables reliable offline-first operations with eventual consistency.

## Overview

The outbox pattern is a design pattern used to ensure reliable message delivery in distributed systems. In the context of the ROSTRY platform, it allows users to perform operations while offline, with those operations being reliably synchronized when connectivity is restored.

## Key Components

### 1. OutboxEntity

The [OutboxEntity](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\database\src\main\java\com\rio\rostry\core\database\entities\OutboxEntity.kt) represents an operation that needs to be synchronized with the server. It contains:

- `id`: Unique identifier for the outbox entry
- `entityType`: Type of entity being operated on (e.g., "FOWL", "TRANSFER_LOG")
- `entityId`: ID of the entity being operated on
- `operationType`: Type of operation (CREATE, UPDATE, DELETE)
- `entityData`: Serialized data of the entity (for CREATE/UPDATE operations)
- `createdAt`: Timestamp when the operation was created
- `updatedAt`: Timestamp when the operation was last updated
- `syncStatus`: Current synchronization status (PENDING, SUCCESS, FAILED)
- `priority`: Priority of the operation (1-5, with 5 being highest)
- `retryCount`: Number of times the operation has been retried
- `lastAttemptAt`: Timestamp of the last synchronization attempt
- `syncedAt`: Timestamp when the operation was successfully synced
- `errorMessage`: Error message if the operation failed

### 2. OutboxDao

The [OutboxDao](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\database\src\main\java\com\rio\rostry\core\database\dao\OutboxDao.kt) provides data access operations for outbox entries:

- `getById`: Retrieve an outbox entry by ID
- `getPendingOperations`: Retrieve pending operations ordered by priority
- `getFailedRetriableOperations`: Retrieve failed operations that can be retried
- `getPendingCount`: Get the count of pending operations
- `insert`: Insert a new outbox entry
- `insertAll`: Insert multiple outbox entries
- `update`: Update an outbox entry
- `updateSyncStatus`: Update the synchronization status of an outbox entry
- `delete`: Delete an outbox entry
- `deleteById`: Delete an outbox entry by ID

### 3. SyncRepository

The [SyncRepository](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\data\src\main\java\com\rio\rostry\core\data\repository\SyncRepository.kt) handles the synchronization logic:

- `syncPendingOperations`: Synchronize pending operations with the server
- `addToOutbox`: Add an operation to the outbox
- `getPendingOperationCount`: Get the count of pending operations

### 4. PeriodicSyncWorker

The [PeriodicSyncWorker](file://C:\Users\rowdy\AndroidStudioProjects\RIX\core\sync\src\main\java\com\rio\rostry\core\sync\workers\PeriodicSyncWorker.kt) is a WorkManager worker that periodically synchronizes outbox operations:

- Runs every 15 minutes when the app is in the background
- Attempts to sync up to 50 operations per run
- Handles network connectivity changes

## Workflow

### 1. Operation Initiation

When a user performs an operation (e.g., creates a fowl, initiates a transfer):

1. The operation is immediately executed on the local database
2. An outbox entry is created to represent the operation
3. The outbox entry is stored in the local database with status "PENDING"

### 2. Synchronization

When the app has network connectivity:

1. The PeriodicSyncWorker runs periodically
2. It retrieves pending operations from the outbox
3. For each operation:
   - It sends the operation to the server
   - If successful, it updates the outbox entry status to "SUCCESS"
   - If failed, it updates the outbox entry status to "FAILED" and increments the retry count
4. Failed operations with retry count < 3 are retried in subsequent sync cycles

### 3. Conflict Resolution

In case of conflicts during synchronization:

1. The server's version is considered the source of truth
2. Local changes are merged with server data when possible
3. Conflicts are logged for manual resolution when automatic merging is not possible

## Implementation Details

### Priority System

Operations have priorities from 1-5:

- Priority 1: Low priority operations (e.g., updating non-critical metadata)
- Priority 2: Medium-low priority operations
- Priority 3: Medium priority operations (default)
- Priority 4: Medium-high priority operations (e.g., updating sale status)
- Priority 5: High priority operations (e.g., transfer verification)

### Retry Mechanism

Failed operations are retried with exponential backoff:

- First retry: After 1 minute
- Second retry: After 5 minutes
- Third retry: After 25 minutes
- Operations with retry count >= 3 are not automatically retried

### Data Serialization

Entity data is serialized to JSON for storage in the outbox:

- CREATE/UPDATE operations include the full entity data
- DELETE operations only include the entity ID

## Benefits

1. **Offline Support**: Users can perform operations while offline
2. **Reliability**: Operations are not lost even if the app crashes
3. **Eventual Consistency**: Data is synchronized when connectivity is restored
4. **Audit Trail**: All operations are logged for debugging and auditing
5. **Priority Handling**: Critical operations are synchronized first

## Limitations

1. **Latency**: Operations may not be immediately visible to other users
2. **Conflict Resolution**: Complex conflicts may require manual resolution
3. **Storage**: Outbox entries consume local storage until synchronized

## Testing

The outbox pattern implementation includes:

1. Unit tests for the SyncRepository
2. Integration tests for the complete workflow
3. Manual testing of offline scenarios
4. Performance testing under various network conditions

## Future Improvements

1. **Enhanced Conflict Resolution**: Implement more sophisticated conflict resolution strategies
2. **Batch Operations**: Support batch synchronization of multiple operations
3. **Real-time Sync**: Implement real-time synchronization when the app is in foreground
4. **Compression**: Compress entity data to reduce storage and bandwidth usage