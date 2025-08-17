# ADR-004: Fowl Record Implementation for Timeline Functionality

## Status

Accepted

## Context

The ROSTRY platform requires a comprehensive timeline functionality to track important events in a fowl's life such as vaccinations, growth milestones, quarantine periods, and mortality events. This functionality is critical for traceability and breeder management.

The existing TimelineEntity is limited in scope and doesn't provide the detailed tracking required for poultry management. We need a more robust solution that can handle various types of records with metrics and proof documentation.

## Decision

We will implement a FowlRecordEntity with the following characteristics:

1. **Entity Design**:
   - Dedicated FowlRecordEntity for tracking fowl lifecycle events
   - Support for various record types (VACCINATION, GROWTH, QUARANTINE, MORTALITY, etc.)
   - Flexible metrics storage using key-value pairs
   - Proof documentation support with URLs and count tracking
   - Complete audit trail with createdBy, createdAt, updatedAt, and version fields
   - Proper indexing for performance optimization including (fowlId, recordDate DESC) for pagination

2. **Database Integration**:
   - Add FowlRecordEntity to the Room database schema
   - Create proper indices for performance optimization
   - Implement database migration from version 2 to 3
   - Add TypeConverters for handling Map and List types

3. **Data Access Layer**:
   - Create FowlRecordDao with queries for common operations
   - Implement Flow-based reactive queries for UI updates
   - Add paging support for efficient timeline display
   - Add proper foreign key constraints to maintain data integrity

4. **Repository Layer**:
   - Create FowlRecordRepository interface
   - Implement FowlRecordRepositoryImpl with offline-first support
   - Integrate with outbox pattern for synchronization

5. **Use Cases**:
   - Create AddFowlRecordUseCase for adding new records
   - Add validation for milestone records (5w, 20w, weekly updates)
   - Design FowlRecordCreationData for clean data input

6. **Reminder System**:
   - Implement WorkManager workers for milestone reminders (5w, 20w, weekly)
   - Implement WorkManager workers for quarantine reminders (12h cadence)

7. **Testing**:
   - Add unit tests for entities and converters
   - Ensure proper coverage for repository and use case logic

## Consequences

### Positive

1. Enhanced traceability for fowl lifecycle events
2. Better support for breeder management with milestone tracking
3. Improved data integrity with proper audit fields
4. Offline-first design enables rural usage
5. Flexible metrics system allows for extensibility
6. Proof documentation support for verification
7. Proper indexing for performance optimization
8. Automated reminders for important milestones

### Negative

1. Increased database complexity with additional entity
2. More synchronization points to manage
3. Additional code to maintain

### Neutral

1. Follows existing patterns in the codebase
2. Maintains consistency with offline-first approach

## Implementation Plan

1. Create FowlRecordEntity with proper annotations and indices
2. Implement TypeConverters for complex data types
3. Add database migration
4. Create FowlRecordDao with required queries
5. Implement repository and use case
6. Add validation for milestone records
7. Implement WorkManager workers for reminders
8. Add unit tests
9. Update documentation

## Related Issues

- Enhanced traceability for verified transfers
- Breeder threshold monitoring
- Marketplace listing verification