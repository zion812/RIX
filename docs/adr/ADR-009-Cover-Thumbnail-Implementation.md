# ADR-009: Cover Thumbnail Implementation for Fowl Records

## Status

Accepted

## Context

The ROSTRY platform displays fowl information in list views and profile headers. When browsing multiple fowls, especially on low-end devices with limited processing power and memory, loading full-size images for each fowl can result in:

1. Slow rendering of list views
2. High memory consumption
3. Poor scrolling performance
4. Increased data usage
5. Delayed user interactions due to image loading

We need to implement a cover thumbnail system to reduce cold-start cost and improve UI performance while maintaining visual appeal for rural users.

## Decision

We will implement a cover thumbnail system with the following approach:

1. **Cover Thumbnail Field**:
   - Add `coverThumbnailUrl` field to FowlEntity for storing thumbnail URL
   - Use this field in list views and profile headers for quick rendering

2. **Thumbnail Generation**:
   - Create CoverThumbnailGenerator utility class
   - Implement automatic generation from proof images when available
   - Generate placeholder thumbnails with colored circles and initials when no proof images exist
   - Create tiny thumbnails (e.g., 40x40 pixels) to minimize memory usage

3. **Placeholder Thumbnails**:
   - Generate vibrant colors based on fowl attributes (e.g., color, name)
   - Display initials from fowl name for better identification
   - Use rounded corners for better visual appearance
   - Implement efficient bitmap generation and caching

4. **Storage and Retrieval**:
   - Store cover thumbnail URLs in FowlEntity for quick access
   - Cache generated thumbnails in memory for repeated use
   - Implement cleanup mechanisms to prevent excessive memory usage

5. **Integration Points**:
   - Use cover thumbnails in fowl list cards
   - Use cover thumbnails in profile headers
   - Fall back to placeholder thumbnails when needed
   - Update cover thumbnails when new proof images are added

## Consequences

### Positive

1. Improved rendering performance for list views
2. Reduced memory consumption
3. Better scrolling performance
4. Lower data usage
5. Faster user interactions
6. Consistent visual appearance
7. Better user experience on low-end devices

### Negative

1. Increased complexity in image handling
2. Additional storage for thumbnail URLs
3. Need for proper cache management
4. Additional code to maintain

### Neutral

1. Follows existing patterns in the codebase
2. Maintains consistency with offline-first approach
3. Provides flexibility for future enhancements

## Implementation Plan

1. Add coverThumbnailUrl field to FowlEntity
2. Create CoverThumbnailGenerator utility class
3. Implement automatic thumbnail generation from proof images
4. Implement placeholder thumbnail generation with colored circles and initials
5. Add rounded corner bitmap generation
6. Integrate with UI components for list views and profile headers
7. Test thumbnail generation and display
8. Optimize performance and memory usage
9. Update documentation

## Related Issues

- Performance optimization for low-end devices
- Memory usage optimization
- UI rendering performance
- Offline-first design patterns