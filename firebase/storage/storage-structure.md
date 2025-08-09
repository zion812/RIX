# RIO Firebase Storage Structure

## Storage Bucket Organization

```
rio-storage-bucket/
├── users/
│   └── {userId}/
│       ├── profile/
│       │   ├── avatar/
│       │   │   ├── original.jpg
│       │   │   ├── compressed.jpg
│       │   │   └── thumbnail.jpg
│       │   ├── farm/
│       │   │   ├── infrastructure/
│       │   │   └── facilities/
│       │   └── credentials/
│       │       ├── certificates/
│       │       └── documents/
│       └── fowls/
│           └── {fowlId}/
│               ├── profile/
│               │   ├── primary/
│               │   │   ├── original.jpg
│               │   │   ├── high.jpg (720p)
│               │   │   ├── medium.jpg (480p)
│               │   │   ├── low.jpg (240p)
│               │   │   └── thumbnail.jpg (150x150)
│               │   └── gallery/
│               │       ├── {imageId}/
│               │       │   ├── original.jpg
│               │       │   ├── compressed.jpg
│               │       │   └── thumbnail.jpg
│               ├── health/
│               │   ├── records/
│               │   ├── certificates/
│               │   └── medical_photos/
│               ├── breeding/
│               │   ├── lineage_photos/
│               │   ├── performance_videos/
│               │   └── documentation/
│               ├── transfers/
│               │   └── {transferId}/
│               │       ├── verification/
│               │       │   ├── before_transfer/
│               │       │   │   ├── condition_photos/
│               │       │   │   └── ownership_proof/
│               │       │   ├── during_transfer/
│               │       │   │   ├── handover_photos/
│               │       │   │   └── transport_videos/
│               │       │   └── after_transfer/
│               │       │       ├── delivery_confirmation/
│               │       │       └── condition_verification/
│               │       └── legal/
│               │           ├── contracts/
│               │           ├── certificates/
│               │           └── signatures/
│               └── marketplace/
│                   └── {listingId}/
│                       ├── photos/
│                       │   ├── {photoId}/
│                       │   │   ├── original.jpg
│                       │   │   ├── high.jpg
│                       │   │   ├── medium.jpg
│                       │   │   ├── low.jpg
│                       │   │   └── thumbnail.jpg
│                       └── videos/
│                           └── {videoId}/
│                               ├── original.mp4
│                               ├── 720p.mp4
│                               ├── 480p.mp4
│                               ├── 240p.mp4
│                               └── thumbnail.jpg
├── temp/
│   └── uploads/
│       └── {sessionId}/
│           ├── chunks/
│           └── metadata/
└── system/
    ├── backups/
    ├── analytics/
    └── maintenance/
```

## Storage Quotas by User Tier

### General Users
- Total Storage: 500MB
- Images: 100 files max
- Videos: 10 files max (5MB each)
- Transfer Documentation: 50MB

### Farmer Users
- Total Storage: 2GB
- Images: 500 files max
- Videos: 50 files max (20MB each)
- Transfer Documentation: 200MB
- Health Records: 100MB

### Enthusiast Users
- Total Storage: 5GB
- Images: 1000 files max
- Videos: 100 files max (50MB each)
- Transfer Documentation: 500MB
- Health Records: 200MB
- Breeding Documentation: 300MB

## File Naming Conventions

### Images
- Format: `{timestamp}_{purpose}_{quality}.{ext}`
- Example: `1640995200_profile_high.jpg`

### Videos
- Format: `{timestamp}_{purpose}_{quality}_{duration}.{ext}`
- Example: `1640995200_breeding_720p_120s.mp4`

### Documents
- Format: `{timestamp}_{type}_{version}.{ext}`
- Example: `1640995200_certificate_v1.pdf`

## Compression Standards

### Images
- **Original**: Uncompressed (for legal documents)
- **High**: 90% quality, max 1920x1080
- **Medium**: 70% quality, max 1280x720
- **Low**: 50% quality, max 854x480
- **Thumbnail**: 60% quality, 150x150 square

### Videos
- **Original**: Source quality (for critical documentation)
- **720p**: H.264, 2Mbps bitrate
- **480p**: H.264, 1Mbps bitrate
- **240p**: H.264, 500kbps bitrate

## Access Patterns

### Critical (Transfer Documentation)
- Immediate availability required
- Multiple quality versions
- Legal retention requirements
- Backup to multiple regions

### High Priority (Fowl Profiles, Marketplace)
- Fast loading for user experience
- Progressive loading support
- CDN optimization
- Thumbnail preloading

### Medium Priority (User Profiles)
- Standard loading
- Basic compression
- Regional caching

### Low Priority (Historical Records)
- Lazy loading acceptable
- Aggressive compression
- Coldline storage after 90 days

## Regional Optimization

### Primary Region: asia-south1 (Mumbai)
- Lowest latency for Indian users
- Primary storage for active data

### Secondary Region: asia-southeast1 (Singapore)
- Backup and disaster recovery
- CDN edge location

### Tertiary Region: us-central1
- Long-term archival
- Cost optimization for old data
