package com.rio.rostry.core.database.dao;

/**
 * Simplified Fowl DAO for Phase 2
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\t0\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u00102\u0006\u0010\b\u001a\u00020\tH\'J\u0016\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u0017\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ$\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0019\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001c\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u0013\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\f0\u00102\u0006\u0010\u0013\u001a\u00020\tH\'J\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00050\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u0015\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\f0\u0010H\'J\u0016\u0010 \u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010!\u001a\u00020\u00032\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00050\fH\u00a7@\u00a2\u0006\u0002\u0010#J\u0016\u0010$\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J(\u0010%\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\t2\u0006\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010)H\u00a7@\u00a2\u0006\u0002\u0010*J\u001e\u0010+\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\t2\u0006\u0010,\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u001a\u00a8\u0006-"}, d2 = {"Lcom/rio/rostry/core/database/dao/FowlDao;", "", "deleteFowl", "", "fowl", "Lcom/rio/rostry/core/database/entities/FowlEntity;", "(Lcom/rio/rostry/core/database/entities/FowlEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteFowlById", "fowlId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllBreeds", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFowlById", "getFowlByIdFlow", "Lkotlinx/coroutines/flow/Flow;", "getFowlCountByOwner", "", "ownerId", "getFowlCountByRegion", "region", "getFowlsByBreed", "breed", "getFowlsByLocation", "district", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFowlsByOwner", "getFowlsByOwnerFlow", "getFowlsForSale", "getFowlsForSaleByRegion", "getFowlsForSaleFlow", "insertFowl", "insertFowls", "fowls", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateFowl", "updateFowlSaleStatus", "isForSale", "", "price", "", "(Ljava/lang/String;ZLjava/lang/Double;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateFowlStatus", "status", "database-simple_debug"})
@androidx.room.Dao()
public abstract interface FowlDao {
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE id = :fowlId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlById(@org.jetbrains.annotations.NotNull()
    java.lang.String fowlId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.rio.rostry.core.database.entities.FowlEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE id = :fowlId")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.rio.rostry.core.database.entities.FowlEntity> getFowlByIdFlow(@org.jetbrains.annotations.NotNull()
    java.lang.String fowlId);
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE ownerId = :ownerId AND status = \'ACTIVE\' ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlsByOwner(@org.jetbrains.annotations.NotNull()
    java.lang.String ownerId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rio.rostry.core.database.entities.FowlEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE ownerId = :ownerId AND status = \'ACTIVE\' ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rio.rostry.core.database.entities.FowlEntity>> getFowlsByOwnerFlow(@org.jetbrains.annotations.NotNull()
    java.lang.String ownerId);
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE breed = :breed AND status = \'ACTIVE\' ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlsByBreed(@org.jetbrains.annotations.NotNull()
    java.lang.String breed, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rio.rostry.core.database.entities.FowlEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE region = :region AND district = :district AND status = \'ACTIVE\' ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlsByLocation(@org.jetbrains.annotations.NotNull()
    java.lang.String region, @org.jetbrains.annotations.NotNull()
    java.lang.String district, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rio.rostry.core.database.entities.FowlEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE isForSale = 1 AND status = \'ACTIVE\' ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlsForSale(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rio.rostry.core.database.entities.FowlEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE isForSale = 1 AND status = \'ACTIVE\' ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rio.rostry.core.database.entities.FowlEntity>> getFowlsForSaleFlow();
    
    @androidx.room.Query(value = "SELECT * FROM fowls WHERE isForSale = 1 AND region = :region AND status = \'ACTIVE\' ORDER BY createdAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlsForSaleByRegion(@org.jetbrains.annotations.NotNull()
    java.lang.String region, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rio.rostry.core.database.entities.FowlEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertFowl(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.database.entities.FowlEntity fowl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertFowls(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rio.rostry.core.database.entities.FowlEntity> fowls, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateFowl(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.database.entities.FowlEntity fowl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFowl(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.database.entities.FowlEntity fowl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM fowls WHERE id = :fowlId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFowlById(@org.jetbrains.annotations.NotNull()
    java.lang.String fowlId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE fowls SET status = :status WHERE id = :fowlId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateFowlStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String fowlId, @org.jetbrains.annotations.NotNull()
    java.lang.String status, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE fowls SET isForSale = :isForSale, price = :price WHERE id = :fowlId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateFowlSaleStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String fowlId, boolean isForSale, @org.jetbrains.annotations.Nullable()
    java.lang.Double price, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId AND status = \'ACTIVE\'")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlCountByOwner(@org.jetbrains.annotations.NotNull()
    java.lang.String ownerId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM fowls WHERE region = :region AND status = \'ACTIVE\'")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFowlCountByRegion(@org.jetbrains.annotations.NotNull()
    java.lang.String region, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT DISTINCT breed FROM fowls WHERE status = \'ACTIVE\' ORDER BY breed")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllBreeds(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion);
}