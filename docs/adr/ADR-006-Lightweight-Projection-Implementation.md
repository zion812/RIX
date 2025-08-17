# ADR-006: Lightweight Projection Implementation for Fowl Records

## Status

Accepted

## Context

The ROSTRY platform needs to handle large datasets efficiently, especially for fowl timeline records. As farmers track their fowls over time, the number of records can grow significantly, potentially impacting performance and memory usage on low-end devices in rural areas.

The existing FowlRecordEntity contains complex data types like Map and List which require significant processing power and memory to deserialize. When displaying a list of records in the timeline view, we don't need all the detailed information immediately. This leads to:

1. Slow UI rendering due to deserializing complex data types
2. High memory consumption on low-end devices
3. Poor user experience when scrolling through long lists
4. Network bandwidth issues when syncing with the cloud

We need to implement a lightweight projection to ensure smooth performance and a good user experience on low-end devices.

## Decision

We will implement a lightweight projection for fowl records with the following approach:

1. **Database Layer**:
   - Create FowlRecordListItem entity as a projection of FowlRecordEntity
   - Add a method to FowlRecordDao to query this projection with LIMIT and OFFSET
   - Maintain proper indexing on (fowl_id, record_date DESC) for efficient pagination

2. **Repository Layer**:
   - Add `getRecordListItemsByFowlIdPaged` method to FowlRecordRepository
   - Implement the method in FowlRecordRepositoryImpl with proper error handling

3. **Domain Layer**:
   - Create FowlRecordListItem domain model
   - Add conversion functions between entity and domain models

4. **ViewModel Layer**:
   - Add state management for lightweight projection data in SimpleFowlViewModel
   - Allow switching between full records and lightweight projections

5. **UI Layer**:
   - Update FowlDetailScreen to use lightweight projections for list views
   - Create separate composables for displaying full records vs. lightweight projections
   - Maintain the ability to switch between modes for debugging/testing

6. **Performance Considerations**:
   - FowlRecordListItem excludes complex Map and List fields (metrics, proofUrls)
   - FowlRecordListItem includes only essential fields for list display
   - Use the lightweight projection by default for list views
   - Load full records only when detailed view is needed

## Consequences

### Positive

1. Improved performance when displaying large timelines
2. Reduced memory consumption on low-end devices
3. Faster scrolling through long lists
4. Reduced network bandwidth usage when syncing with the cloud
5. Better user experience on constrained devices
6. Efficient database queries with proper LIMIT and OFFSET usage

### Negative

1. Increased complexity in data handling (two representations of the same data)
2. Additional code to maintain
3. Need for proper testing of both full and lightweight modes
4. Slight increase in database query complexity

### Neutral

1. Follows existing patterns in the codebase
2. Maintains consistency with offline-first approach
3. Provides flexibility to switch between modes

## Implementation Plan

1. Create FowlRecordListItem entity and domain model
2. Update FowlRecordDao with projection query method
3. Update FowlRecordRepository with projection methods
4. Implement projection methods in FowlRecordRepositoryImpl
5. Add state management in SimpleFowlViewModel
6. Update UI to use lightweight projections
7. Test both full and lightweight modes
8. Update documentation

## Related Issues

- Performance optimization for low-end devices
- Memory usage optimization
- Pagination implementation
- Offline-first design patterns