package com.rio.rostry.core.common.compression;

/**
 * Image compression result
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B?\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\b\u001a\u00020\u0006\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\f\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\nH\u00c6\u0003J\t\u0010\u001f\u001a\u00020\fH\u00c6\u0003JQ\u0010 \u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00c6\u0001J\u0013\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010\u0001H\u0096\u0002J\b\u0010$\u001a\u00020%H\u0016J\t\u0010&\u001a\u00020\'H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u000fR\u0011\u0010\b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0011\u00a8\u0006("}, d2 = {"Lcom/rio/rostry/core/common/compression/ImageCompressionResult;", "", "compressedData", "", "thumbnailData", "originalSize", "", "compressedSize", "thumbnailSize", "compressionRatio", "", "targetQuality", "Lcom/rio/rostry/core/common/compression/ImageQuality;", "([B[BJJJDLcom/rio/rostry/core/common/compression/ImageQuality;)V", "getCompressedData", "()[B", "getCompressedSize", "()J", "getCompressionRatio", "()D", "getOriginalSize", "getTargetQuality", "()Lcom/rio/rostry/core/common/compression/ImageQuality;", "getThumbnailData", "getThumbnailSize", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "", "toString", "", "common_debug"})
public final class ImageCompressionResult {
    @org.jetbrains.annotations.NotNull()
    private final byte[] compressedData = null;
    @org.jetbrains.annotations.Nullable()
    private final byte[] thumbnailData = null;
    private final long originalSize = 0L;
    private final long compressedSize = 0L;
    private final long thumbnailSize = 0L;
    private final double compressionRatio = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.core.common.compression.ImageQuality targetQuality = null;
    
    public ImageCompressionResult(@org.jetbrains.annotations.NotNull()
    byte[] compressedData, @org.jetbrains.annotations.Nullable()
    byte[] thumbnailData, long originalSize, long compressedSize, long thumbnailSize, double compressionRatio, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.compression.ImageQuality targetQuality) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] getCompressedData() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final byte[] getThumbnailData() {
        return null;
    }
    
    public final long getOriginalSize() {
        return 0L;
    }
    
    public final long getCompressedSize() {
        return 0L;
    }
    
    public final long getThumbnailSize() {
        return 0L;
    }
    
    public final double getCompressionRatio() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.common.compression.ImageQuality getTargetQuality() {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final byte[] component2() {
        return null;
    }
    
    public final long component3() {
        return 0L;
    }
    
    public final long component4() {
        return 0L;
    }
    
    public final long component5() {
        return 0L;
    }
    
    public final double component6() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.common.compression.ImageQuality component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.common.compression.ImageCompressionResult copy(@org.jetbrains.annotations.NotNull()
    byte[] compressedData, @org.jetbrains.annotations.Nullable()
    byte[] thumbnailData, long originalSize, long compressedSize, long thumbnailSize, double compressionRatio, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.compression.ImageQuality targetQuality) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}