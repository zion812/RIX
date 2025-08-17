# Cache Management Runbook

This document provides operational guidance for cache management in the ROSTRY platform, particularly for thumbnails and other cached media.

## Cache Reset Procedures

### Thumbnail Cache Reset

To reset the thumbnail cache when users report issues with thumbnail display or when troubleshooting performance problems:

1. Navigate to Settings > Advanced > Cache Management
2. Tap "Clear Thumbnail Cache"
3. Confirm the action when prompted

This will:
- Clear all in-memory cached thumbnails
- Delete all disk-cached thumbnails
- Force regeneration of thumbnails on next access

### Manual Cache Reset (Support Team)

For support team members assisting users with cache-related issues:

1. Access the device (physical or remote)
2. Open the ROSTRY app
3. Navigate to Settings > Advanced > Cache Management
4. Tap "Clear All Caches"
5. Restart the application

This will:
- Clear thumbnail cache (memory and disk)
- Clear any other cached data
- Reset cache statistics

## Rebuild Thumbnails

To rebuild thumbnails for all fowls when there are display issues or after a major update:

1. Navigate to Settings > Advanced > Cache Management
2. Tap "Rebuild Thumbnails"
3. Confirm the action when prompted

This process will:
- Clear existing thumbnail cache
- Queue regeneration of thumbnails for all fowls with proof images
- Show progress indicator during regeneration
- Complete automatically in the background

## Cache Monitoring

### Memory Cache Monitoring

The application monitors memory cache usage and will automatically evict entries when:
- Memory pressure is detected
- Cache reaches 90% of allocated space
- Application is backgrounded for extended periods

### Disk Cache Monitoring

The application monitors disk cache usage and will automatically clean up when:
- Cache reaches 95% of allocated space
- Device storage is low
- Application is updated

### Cache Statistics

Users and support team can view cache statistics in Settings > Advanced > Cache Management:
- Current memory usage
- Current disk usage
- Cache hit/miss ratio
- Total items cached
- Last cleanup time

## Device Class Awareness

The cache management system automatically adjusts based on device class:

### High-End Devices (3GB+ RAM, Android 8.0+)
- Memory cache: 1/4 of available memory
- Disk cache: 50MB

### Mid-End Devices (1.5GB-3GB RAM, Android 6.0-7.1)
- Memory cache: 1/6 of available memory
- Disk cache: 30MB

### Low-End Devices (≤1.5GB RAM, Android <6.0 or isLowRamDevice)
- Memory cache: 1/8 of available memory
- Disk cache: 15MB

## Troubleshooting

### Common Issues and Solutions

#### Thumbnails Not Displaying
1. Clear thumbnail cache and restart app
2. Check network connectivity if thumbnails are fetched from remote sources
3. Verify storage permissions

#### App Performance Degradation
1. Check cache statistics for unusual usage patterns
2. Clear all caches and monitor performance
3. Report persistent issues to development team

#### Storage Space Issues
1. Check disk cache usage in settings
2. Clear cache if usage is excessive
3. Consider disabling thumbnail caching for very low-storage devices

### Logs and Monitoring

For support team members investigating cache-related issues:

1. Enable verbose logging in developer settings
2. Reproduce the issue while monitoring logs
3. Look for cache-related messages:
   - "Cache hit" / "Cache miss"
   - "Memory cache evicted"
   - "Disk cache cleaned"
4. Include cache statistics in bug reports

## Best Practices

### For Users
- Regular cache clearing can help with performance issues
- Rebuild thumbnails if images appear corrupted or outdated
- Be aware that clearing cache may temporarily increase data usage as thumbnails are regenerated

### For Support Team
- Always check cache statistics before troubleshooting
- Guide users through cache reset procedures when appropriate
- Document cache-related issues for product improvement

### For Developers
- Monitor cache performance metrics
- Test cache behavior on different device classes
- Implement proper error handling for cache operations
- Update this document when cache mechanisms change