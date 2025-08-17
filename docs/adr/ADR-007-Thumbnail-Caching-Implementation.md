# ADR-007: Thumbnail Caching Implementation for Fowl Records

## Status

Accepted

## Context

The ROSTRY platform displays proof images in fowl timeline records, which can consume significant bandwidth and memory, especially on low-end devices in rural areas with limited connectivity. As farmers track their fowls over time, the number of proof images can grow significantly, leading to:

1. High data usage when browsing timeline records
2. Slow loading times for proof images
3. Poor user experience on low-end devices
4. Excessive memory consumption
5. Increased battery drain from frequent network requests

We need to implement a thumbnail caching strategy to reduce data usage and improve performance while maintaining the ability to view proof documentation.

## Decision

We will implement a two-level thumbnail caching system with the following approach:

1. **Memory Cache**:
   - Use LruCache for in-memory caching of recently accessed thumbnails
   - Allocate 1/8th of available memory for cache to avoid memory pressure
   - Store Bitmap objects for immediate access

2. **Disk Cache**:
   - Store thumbnails on device storage for persistent caching
   - Use MD5 hash of file path and dimensions as cache keys
   - Store thumbnails in JPEG format with compression to reduce size

3. **Thumbnail Generation**:
   - Generate thumbnails with maximum dimensions of 120x120 pixels
   - Use BitmapFactory with inSampleSize for efficient downsampling
   - Compress thumbnails to JPEG with 80% quality to balance size and quality

4. **Cache Management**:
   - Implement cache eviction policies to prevent excessive storage usage
   - Provide methods to clear cache and remove specific entries
   - Handle cache failures gracefully by falling back to original images

5. **Integration Points**:
   - Create ThumbnailCacheManager as a singleton for centralized cache management
   - Integrate with existing image loading components
   - Use in timeline views where proof images are displayed

## Consequences

### Positive

1. Reduced data usage when browsing timeline records
2. Faster loading times for proof images
3. Improved user experience on low-end devices
4. Reduced memory consumption through proper sizing
5. Decreased battery drain from fewer network requests
6. Better performance in low-connectivity environments

### Negative

1. Increased complexity in image loading pipeline
2. Additional storage usage for cached thumbnails
3. Need for proper cache management to prevent storage bloat
4. Additional code to maintain

### Neutral

1. Follows existing patterns in the codebase
2. Maintains consistency with offline-first approach
3. Provides flexibility to adjust cache sizes and policies

## Implementation Plan

1. Create ThumbnailCacheManager class with memory and disk caching
2. Implement thumbnail generation with efficient downsampling
3. Add cache management methods (clear, remove specific entries)
4. Integrate with existing UI components that display proof images
5. Test caching behavior under various conditions
6. Optimize cache sizes based on testing results
7. Update documentation

## Related Issues

- Performance optimization for low-end devices
- Data usage reduction
- Memory usage optimization
- Offline-first design patterns