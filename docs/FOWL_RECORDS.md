# Fowl Records Documentation

## Overview

Fowl Records is a feature in the ROSTRY platform that allows tracking of important events in a fowl's life. This includes vaccinations, growth milestones, quarantine periods, and mortality events. The feature provides a comprehensive timeline view of a fowl's history, which is essential for traceability and breeder management.

## Entity Structure

### FowlRecordEntity

The FowlRecordEntity represents a single event in a fowl's life and contains the following fields:

- `id`: Unique identifier for the record
- `fowlId`: Reference to the fowl this record belongs to
- `recordType`: Type of record (VACCINATION, GROWTH, QUARANTINE, MORTALITY, etc.)
- `recordDate`: Date when the event occurred
- `description`: Optional description of the event
- `metrics`: Key-value pairs for storing event-specific data
- `proofUrls`: List of URLs pointing to proof documents/images
- `proofCount`: Number of proof documents
- `createdBy`: User who created the record
- `createdAt`: Timestamp when the record was created
- `updatedAt`: Timestamp when the record was last updated
- `version`: Version number for conflict resolution
- `isDeleted`: Flag indicating if the record has been deleted

### FowlRecordListItem

The FowlRecordListItem is a lightweight projection of FowlRecordEntity for timeline list display. It minimizes Map/List deserialization for better performance on low-end devices and contains the following fields:

- `id`: Unique identifier for the record
- `fowlId`: Reference to the fowl this record belongs to
- `recordType`: Type of record (VACCINATION, GROWTH, QUARANTINE, MORTALITY, etc.)
- `recordDate`: Date when the event occurred
- `description`: Optional description of the event
- `proofCount`: Number of proof documents
- `createdBy`: User who created the record
- `createdAt`: Timestamp when the record was created
- `updatedAt`: Timestamp when the record was last updated
- `version`: Version number for conflict resolution

### FowlEntity

The FowlEntity has been enhanced with a cover thumbnail field for improved UI performance:

- `coverThumbnailUrl`: URL of the cover thumbnail for list cards and profile headers

### FowlSummary

The FowlSummary is a compact DTO for marketplace and transfer integration:

- `id`: Unique identifier for the fowl
- `name`: Name of the fowl
- `breedPrimary`: Primary breed of the fowl
- `breedSecondary`: Secondary breed of the fowl
- `gender`: Gender of the fowl
- `color`: Color of the fowl
- `generation`: Generation of the fowl
- `dob`: Date of birth of the fowl
- `coverThumbnailUrl`: URL of the cover thumbnail
- `healthStatus`: Health status of the fowl
- `availabilityStatus`: Availability status of the fowl
- `region`: Region where the fowl is located
- `district`: District where the fowl is located
- `createdAt`: Timestamp when the fowl was created
- `updatedAt`: Timestamp when the fowl was last updated

### TimelineSummary

The TimelineSummary is a compact DTO for quick display during transfer flow:

- `recordType`: Type of record
- `recordDate`: Date when the event occurred
- `description`: Optional description of the event
- `proofCount`: Number of proof documents
- `isVerified`: Flag indicating if the record is verified

## Data Access

### FowlRecordDao

The FowlRecordDao provides the following methods for data access:

- `getRecordsByFowlId(fowlId: String)`: Get all records for a specific fowl ordered by date
- `getRecordsByType(fowlId: String, type: String)`: Get records by type for a specific fowl
- `getRecordById(id: String)`: Get a specific record by ID
- `getRecordsByDateRange(fowlId: String, startDate: Long, endDate: Long)`: Get records by date range
- `getRecordsByFowlIdPaged(fowlId: String)`: Get records with pagination support
- `getRecordsByFowlIdPaged(fowlId: String, limit: Int, offset: Int)`: Get records with pagination using limit and offset
- `getRecordListItemsByFowlIdPaged(fowlId: String, limit: Int, offset: Int)`: Get lightweight projection of records with pagination
- Standard CRUD operations (insert, update, delete)

## Repository Layer

### FowlRecordRepository

The FowlRecordRepository interface defines the contract for fowl record operations:

- `getRecordsByFowlId(fowlId: String)`: Observe records for a specific fowl
- `getRecordsByType(fowlId: String, type: String)`: Observe records by type
- `addRecord(record: FowlRecord)`: Add a new fowl record
- `updateRecord(record: FowlRecord)`: Update an existing fowl record
- `deleteRecord(recordId: String)`: Delete a fowl record
- `getRecordById(recordId: String)`: Get a specific record by ID
- `getRecordsByFowlIdPaged(fowlId: String, limit: Int, offset: Int)`: Get records with pagination support
- `getRecordListItemsByFowlIdPaged(fowlId: String, limit: Int, offset: Int)`: Get lightweight projection of records with pagination
- `getTimelineSummary(fowlId: String, limit: Int)`: Get compact timeline summary for a specific fowl

### FowlRepository

The FowlRepository interface defines the contract for fowl operations:

- `getFowlsByOwner(ownerId: String)`: Observe fowls for a specific owner
- `getFowlById(id: String)`: Get a specific fowl by ID
- `saveFowl(fowl: Fowl)`: Save a new fowl
- `updateFowl(fowl: Fowl)`: Update an existing fowl
- `deleteFowl(id: String)`: Delete a fowl
- `getFowlSummaries(ownerId: String)`: Get fowl summaries for marketplace integration

### FowlRecordRepositoryImpl

The FowlRecordRepositoryImpl implements the repository interface with offline-first support:

- Uses FowlRecordDao for local data access
- Integrates with the outbox pattern for synchronization
- Handles error cases with Result types
- Implements pagination for efficient loading of large timelines
- Provides lightweight projection for list views to reduce memory usage
- Provides timeline summary for transfer flow integration

### FowlRepositoryImpl

The FowlRepositoryImpl implements the repository interface with offline-first support:

- Uses FowlDao for local data access
- Integrates with the outbox pattern for synchronization
- Handles error cases with Result types
- Provides fowl summaries for marketplace integration

## Use Cases

### AddFowlRecordUseCase

The AddFowlRecordUseCase provides a clean interface for adding new fowl records:

- Takes FowlRecordCreationData as input
- Validates milestone records (5w, 20w, weekly updates)
- Validates record types based on fowl age (lifecycle gating)
- Validates proof requirements for different record types
- Creates a FowlRecord domain model
- Calls the repository to add the record
- Returns a Result indicating success or failure

### FowlRecordCreationData

Data class representing the creation data for a fowl record:

- `fowlId`: Reference to the fowl
- `recordType`: Type of record
- `recordDate`: Date when the event occurred
- `description`: Optional description
- `metrics`: Key-value pairs for event-specific data
- `proofUrls`: List of proof document URLs
- `proofCount`: Number of proof documents
- `createdBy`: User who created the record
- `createdAt`: Timestamp when the record was created
- `updatedAt`: Timestamp when the record was last updated
- `version`: Version number for conflict resolution

## UI Components

### Fowl Detail Screen

The Fowl Detail Screen displays comprehensive information about a fowl including:

- Basic information (name, breed, generation)
- Status and health information
- Breeding information
- Performance metrics
- Timeline of records with visual indicators
- Notes
- Quick action buttons for common record types

Timeline items show:
- Record type and date
- Description
- Proof count with upload status indicators (pending/uploaded)

The timeline supports pagination for efficient loading of large record sets and uses lightweight projections for better performance on low-end devices.

Quick action buttons allow users to:
- Add vaccination records
- Add weekly update records
- Add other record types

### Add Fowl Record Screen

The Add Fowl Record Screen allows users to add new records to a fowl's timeline:

- Record type selection (vaccination, growth, quarantine, etc.)
- Smart suggestions for vaccination types and previous metrics
- Date picker for record date
- Description field
- Proof documentation management (add/remove proofs)

Smart suggestions include:
- Common vaccination types with radio buttons for quick selection
- Previous vaccination information (when available)
- Previous weekly metrics for copying (when available)

## Reminder System

### Milestone Reminders

The system implements automatic reminders for important milestones:
- 5-week checkup reminder
- 20-week checkup reminder
- Weekly updates after 20 weeks

WorkManager is used to schedule these reminders with appropriate constraints.

### Quarantine Reminders

For fowls in quarantine, the system sends reminders every 12 hours until the quarantine period ends.

WorkManager is used to schedule these reminders with appropriate constraints.

## Proof Upload System

### Deferred Media Uploads

The system implements deferred media uploads with the following workflow:
1. Capture proof locally (camera/gallery)
2. Queue in outbox for upload
3. Upload to storage with WorkManager (with network constraints)
4. Write proof URL to Firestore
5. Mark record as synced

### UploadProofWorker

WorkManager worker that handles uploading proof documents with retry and backoff:
- Handles network failures with exponential backoff
- Updates records with remote URLs after successful upload
- Shows pending-proof indicators in the timeline
- Can be disabled via feature flag for emergency situations

### Thumbnail Caching

The system implements thumbnail caching for proof media to reduce data usage and improve loading times:
- In-memory LRU cache for frequently accessed thumbnails
- Disk cache for persistent storage of thumbnails
- Automatic thumbnail generation with configurable size
- Cache management to prevent excessive memory usage
- Device-class aware cache sizing (low/mid/high-end devices)
- Can be disabled via feature flag for troubleshooting

### Cover Thumbnails

The system implements cover thumbnails for fowl list cards and profile headers to reduce cold-start cost:
- Automatic generation of cover thumbnails from proof images
- Placeholder thumbnail generation with colored circles and initials
- Storage of cover thumbnail URLs in FowlEntity
- Rounded corner bitmap generation for better UI appearance
- Can be disabled via feature flag for troubleshooting

## Export and Sharing

### FowlTimelineExporter

Utility for exporting a fowl's timeline summary for trust-building outside the app:
- Export as plain text format
- Export as HTML format with basic styling
- Share via standard Android sharing intents
- Include fowl information and timeline records
- Include proof document counts (without actual documents for privacy)
- Can be disabled via feature flag for emergency situations

## Feature Flags

The system implements feature flags for remote configuration and gradual rollouts:

- `fowl_records_enabled`: Enable/disable the entire Fowl Records feature
- `upload_proof_worker_enabled`: Enable/disable the UploadProofWorker
- `export_sharing_enabled`: Enable/disable export sharing functionality
- `thumbnail_caching_enabled`: Enable/disable thumbnail caching
- `cover_thumbnails_enabled`: Enable/disable cover thumbnails
- `smart_suggestions_enabled`: Enable/disable smart suggestions in Add Record screen

## Database Migration

The feature includes a database migration from version 2 to 3 that:

- Creates the fowl_records table
- Adds proper indices for performance including (fowlId, recordDate DESC) for pagination
- Maintains foreign key constraints
- Adds coverThumbnailUrl field to fowl table

## Offline Support

The implementation follows the offline-first pattern:

- All operations are performed locally first
- Changes are queued in the outbox for synchronization
- Conflict resolution is handled through versioning
- Operations can be performed without network connectivity
- Proof uploads are deferred when offline
- Pagination reduces memory usage when working with large datasets

## Testing

The feature includes unit tests for:

- FowlRecordEntity creation and validation
- TypeConverters for complex data types
- Repository operations
- Use case logic
- Pagination functionality

## Performance Considerations

- Proper indexing for efficient queries
- Pagination support for large timelines
- Proof count field to avoid deserializing large lists for UI display
- Lightweight projections for list views
- Lazy image loading for proofs with thumbnail-first strategy
- Efficient database queries with proper LIMIT and OFFSET usage
- Reduced memory footprint on low-end devices through lightweight projections
- Thumbnail caching for improved UI performance
- Cover thumbnails for reduced cold-start cost in list views
- Device-class aware cache sizing
- Feature flags for emergency disabling of performance-intensive features

## Accessibility and Rural UX

- Large tap targets for easy interaction on low-end devices
- Clear visual indicators for pending vs. uploaded proofs
- Simple, intuitive interface with minimal cognitive load
- Support for multiple languages (English, Telugu, Tamil, Kannada, Hindi)
- Infinite scrolling with automatic loading of additional records
- Offline indicators and status messages
- Quick action buttons for common tasks
- Export and sharing capabilities for trust-building
- Smart suggestions to reduce data entry
- Feature flags for remote configuration and troubleshooting