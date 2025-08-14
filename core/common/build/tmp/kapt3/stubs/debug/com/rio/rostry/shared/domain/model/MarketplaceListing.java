package com.rio.rostry.shared.domain.model;

/**
 * Domain model for MarketplaceListing
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\bR\b\u0086\b\u0018\u00002\u00020\u0001B\u0085\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u0003\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0003\u0012\u0006\u0010\u0010\u001a\u00020\u0003\u0012\u0006\u0010\u0011\u001a\u00020\u0012\u0012\u0006\u0010\u0013\u001a\u00020\u0003\u0012\u0006\u0010\u0014\u001a\u00020\u0003\u0012\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00030\u0016\u0012\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00030\u0016\u0012\u0006\u0010\u0018\u001a\u00020\u0019\u0012\u0006\u0010\u001a\u001a\u00020\u0019\u0012\u0006\u0010\u001b\u001a\u00020\u0019\u0012\u0006\u0010\u001c\u001a\u00020\u0019\u0012\b\u0010\u001d\u001a\u0004\u0018\u00010\u001e\u0012\u0006\u0010\u001f\u001a\u00020 \u0012\u0006\u0010!\u001a\u00020\u001e\u0012\u0006\u0010\"\u001a\u00020\u001e\u0012\u0006\u0010#\u001a\u00020\u001e\u0012\u0006\u0010$\u001a\u00020\u0003\u0012\u0006\u0010%\u001a\u00020\u0003\u0012\b\u0010&\u001a\u0004\u0018\u00010\'\u0012\u0006\u0010(\u001a\u00020\'\u0012\u0006\u0010)\u001a\u00020\'\u00a2\u0006\u0002\u0010*J\t\u0010U\u001a\u00020\u0003H\u00c6\u0003J\t\u0010V\u001a\u00020\u0003H\u00c6\u0003J\t\u0010W\u001a\u00020\u0003H\u00c6\u0003J\t\u0010X\u001a\u00020\u0012H\u00c6\u0003J\t\u0010Y\u001a\u00020\u0003H\u00c6\u0003J\t\u0010Z\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010[\u001a\b\u0012\u0004\u0012\u00020\u00030\u0016H\u00c6\u0003J\u000f\u0010\\\u001a\b\u0012\u0004\u0012\u00020\u00030\u0016H\u00c6\u0003J\t\u0010]\u001a\u00020\u0019H\u00c6\u0003J\t\u0010^\u001a\u00020\u0019H\u00c6\u0003J\t\u0010_\u001a\u00020\u0019H\u00c6\u0003J\t\u0010`\u001a\u00020\u0003H\u00c6\u0003J\t\u0010a\u001a\u00020\u0019H\u00c6\u0003J\u0010\u0010b\u001a\u0004\u0018\u00010\u001eH\u00c6\u0003\u00a2\u0006\u0002\u00107J\t\u0010c\u001a\u00020 H\u00c6\u0003J\t\u0010d\u001a\u00020\u001eH\u00c6\u0003J\t\u0010e\u001a\u00020\u001eH\u00c6\u0003J\t\u0010f\u001a\u00020\u001eH\u00c6\u0003J\t\u0010g\u001a\u00020\u0003H\u00c6\u0003J\t\u0010h\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010i\u001a\u0004\u0018\u00010\'H\u00c6\u0003J\t\u0010j\u001a\u00020\'H\u00c6\u0003J\t\u0010k\u001a\u00020\u0003H\u00c6\u0003J\t\u0010l\u001a\u00020\'H\u00c6\u0003J\t\u0010m\u001a\u00020\u0003H\u00c6\u0003J\t\u0010n\u001a\u00020\u0003H\u00c6\u0003J\t\u0010o\u001a\u00020\tH\u00c6\u0003J\t\u0010p\u001a\u00020\u0003H\u00c6\u0003J\t\u0010q\u001a\u00020\fH\u00c6\u0003J\t\u0010r\u001a\u00020\u000eH\u00c6\u0003J\u00ca\u0002\u0010s\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00032\b\b\u0002\u0010\u0014\u001a\u00020\u00032\u000e\b\u0002\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00030\u00162\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00030\u00162\b\b\u0002\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u001a\u001a\u00020\u00192\b\b\u0002\u0010\u001b\u001a\u00020\u00192\b\b\u0002\u0010\u001c\u001a\u00020\u00192\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\b\b\u0002\u0010\u001f\u001a\u00020 2\b\b\u0002\u0010!\u001a\u00020\u001e2\b\b\u0002\u0010\"\u001a\u00020\u001e2\b\b\u0002\u0010#\u001a\u00020\u001e2\b\b\u0002\u0010$\u001a\u00020\u00032\b\b\u0002\u0010%\u001a\u00020\u00032\n\b\u0002\u0010&\u001a\u0004\u0018\u00010\'2\b\b\u0002\u0010(\u001a\u00020\'2\b\b\u0002\u0010)\u001a\u00020\'H\u00c6\u0001\u00a2\u0006\u0002\u0010tJ\u0013\u0010u\u001a\u00020\u00192\b\u0010v\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010w\u001a\u00020\u001eH\u00d6\u0001J\t\u0010x\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0013\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010,R\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010,R\u0011\u0010\u001f\u001a\u00020 \u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0011\u0010(\u001a\u00020\'\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010,R\u0011\u0010\u001c\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0015\u0010\u001d\u001a\u0004\u0018\u00010\u001e\u00a2\u0006\n\n\u0002\u00108\u001a\u0004\b6\u00107R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010,R\u0011\u0010%\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010,R\u0013\u0010&\u001a\u0004\u0018\u00010\'\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u00102R\u0011\u0010\"\u001a\u00020\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010=R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00030\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010?R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010,R\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010BR\u0011\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u00105R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010,R\u0011\u0010#\u001a\u00020\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010=R\u0011\u0010\u001a\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u00105R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010HR\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u0010,R\u0011\u0010\u001b\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u00105R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00030\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010?R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\bL\u0010MR\u0011\u0010$\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bN\u0010,R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u0010,R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\bP\u0010QR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bR\u0010,R\u0011\u0010)\u001a\u00020\'\u00a2\u0006\b\n\u0000\u001a\u0004\bS\u00102R\u0011\u0010!\u001a\u00020\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\bT\u0010=\u00a8\u0006y"}, d2 = {"Lcom/rio/rostry/shared/domain/model/MarketplaceListing;", "", "id", "", "sellerId", "fowlId", "title", "description", "price", "", "currency", "listingType", "Lcom/rio/rostry/shared/domain/model/ListingType;", "status", "Lcom/rio/rostry/shared/domain/model/ListingStatus;", "category", "breed", "gender", "Lcom/rio/rostry/shared/domain/model/Gender;", "age", "location", "photos", "", "features", "healthCertified", "", "lineageVerified", "negotiable", "deliveryAvailable", "deliveryRadius", "", "contactPreference", "Lcom/rio/rostry/shared/domain/model/ContactPreference;", "viewCount", "favoriteCount", "inquiryCount", "region", "district", "expiresAt", "Ljava/util/Date;", "createdAt", "updatedAt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;Lcom/rio/rostry/shared/domain/model/ListingType;Lcom/rio/rostry/shared/domain/model/ListingStatus;Ljava/lang/String;Ljava/lang/String;Lcom/rio/rostry/shared/domain/model/Gender;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;ZZZZLjava/lang/Integer;Lcom/rio/rostry/shared/domain/model/ContactPreference;IIILjava/lang/String;Ljava/lang/String;Ljava/util/Date;Ljava/util/Date;Ljava/util/Date;)V", "getAge", "()Ljava/lang/String;", "getBreed", "getCategory", "getContactPreference", "()Lcom/rio/rostry/shared/domain/model/ContactPreference;", "getCreatedAt", "()Ljava/util/Date;", "getCurrency", "getDeliveryAvailable", "()Z", "getDeliveryRadius", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getDescription", "getDistrict", "getExpiresAt", "getFavoriteCount", "()I", "getFeatures", "()Ljava/util/List;", "getFowlId", "getGender", "()Lcom/rio/rostry/shared/domain/model/Gender;", "getHealthCertified", "getId", "getInquiryCount", "getLineageVerified", "getListingType", "()Lcom/rio/rostry/shared/domain/model/ListingType;", "getLocation", "getNegotiable", "getPhotos", "getPrice", "()D", "getRegion", "getSellerId", "getStatus", "()Lcom/rio/rostry/shared/domain/model/ListingStatus;", "getTitle", "getUpdatedAt", "getViewCount", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;Lcom/rio/rostry/shared/domain/model/ListingType;Lcom/rio/rostry/shared/domain/model/ListingStatus;Ljava/lang/String;Ljava/lang/String;Lcom/rio/rostry/shared/domain/model/Gender;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;ZZZZLjava/lang/Integer;Lcom/rio/rostry/shared/domain/model/ContactPreference;IIILjava/lang/String;Ljava/lang/String;Ljava/util/Date;Ljava/util/Date;Ljava/util/Date;)Lcom/rio/rostry/shared/domain/model/MarketplaceListing;", "equals", "other", "hashCode", "toString", "common_debug"})
public final class MarketplaceListing {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String sellerId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fowlId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String description = null;
    private final double price = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String currency = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.shared.domain.model.ListingType listingType = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.shared.domain.model.ListingStatus status = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String category = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String breed = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.shared.domain.model.Gender gender = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String age = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String location = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> photos = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> features = null;
    private final boolean healthCertified = false;
    private final boolean lineageVerified = false;
    private final boolean negotiable = false;
    private final boolean deliveryAvailable = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer deliveryRadius = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.shared.domain.model.ContactPreference contactPreference = null;
    private final int viewCount = 0;
    private final int favoriteCount = 0;
    private final int inquiryCount = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String region = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String district = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.Date expiresAt = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Date createdAt = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Date updatedAt = null;
    
    public MarketplaceListing(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String sellerId, @org.jetbrains.annotations.NotNull()
    java.lang.String fowlId, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, double price, @org.jetbrains.annotations.NotNull()
    java.lang.String currency, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.ListingType listingType, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.ListingStatus status, @org.jetbrains.annotations.NotNull()
    java.lang.String category, @org.jetbrains.annotations.NotNull()
    java.lang.String breed, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.Gender gender, @org.jetbrains.annotations.NotNull()
    java.lang.String age, @org.jetbrains.annotations.NotNull()
    java.lang.String location, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> photos, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> features, boolean healthCertified, boolean lineageVerified, boolean negotiable, boolean deliveryAvailable, @org.jetbrains.annotations.Nullable()
    java.lang.Integer deliveryRadius, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.ContactPreference contactPreference, int viewCount, int favoriteCount, int inquiryCount, @org.jetbrains.annotations.NotNull()
    java.lang.String region, @org.jetbrains.annotations.NotNull()
    java.lang.String district, @org.jetbrains.annotations.Nullable()
    java.util.Date expiresAt, @org.jetbrains.annotations.NotNull()
    java.util.Date createdAt, @org.jetbrains.annotations.NotNull()
    java.util.Date updatedAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSellerId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFowlId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDescription() {
        return null;
    }
    
    public final double getPrice() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrency() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.ListingType getListingType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.ListingStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCategory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBreed() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.Gender getGender() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAge() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getPhotos() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getFeatures() {
        return null;
    }
    
    public final boolean getHealthCertified() {
        return false;
    }
    
    public final boolean getLineageVerified() {
        return false;
    }
    
    public final boolean getNegotiable() {
        return false;
    }
    
    public final boolean getDeliveryAvailable() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getDeliveryRadius() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.ContactPreference getContactPreference() {
        return null;
    }
    
    public final int getViewCount() {
        return 0;
    }
    
    public final int getFavoriteCount() {
        return 0;
    }
    
    public final int getInquiryCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRegion() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDistrict() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date getExpiresAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Date getCreatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Date getUpdatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.Gender component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component16() {
        return null;
    }
    
    public final boolean component17() {
        return false;
    }
    
    public final boolean component18() {
        return false;
    }
    
    public final boolean component19() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final boolean component20() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component21() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.ContactPreference component22() {
        return null;
    }
    
    public final int component23() {
        return 0;
    }
    
    public final int component24() {
        return 0;
    }
    
    public final int component25() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component26() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component27() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date component28() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Date component29() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Date component30() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    public final double component6() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.ListingType component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.ListingStatus component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.model.MarketplaceListing copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String sellerId, @org.jetbrains.annotations.NotNull()
    java.lang.String fowlId, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, double price, @org.jetbrains.annotations.NotNull()
    java.lang.String currency, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.ListingType listingType, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.ListingStatus status, @org.jetbrains.annotations.NotNull()
    java.lang.String category, @org.jetbrains.annotations.NotNull()
    java.lang.String breed, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.Gender gender, @org.jetbrains.annotations.NotNull()
    java.lang.String age, @org.jetbrains.annotations.NotNull()
    java.lang.String location, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> photos, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> features, boolean healthCertified, boolean lineageVerified, boolean negotiable, boolean deliveryAvailable, @org.jetbrains.annotations.Nullable()
    java.lang.Integer deliveryRadius, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.model.ContactPreference contactPreference, int viewCount, int favoriteCount, int inquiryCount, @org.jetbrains.annotations.NotNull()
    java.lang.String region, @org.jetbrains.annotations.NotNull()
    java.lang.String district, @org.jetbrains.annotations.Nullable()
    java.util.Date expiresAt, @org.jetbrains.annotations.NotNull()
    java.util.Date createdAt, @org.jetbrains.annotations.NotNull()
    java.util.Date updatedAt) {
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
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}