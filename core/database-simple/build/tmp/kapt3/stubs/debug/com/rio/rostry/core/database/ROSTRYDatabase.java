package com.rio.rostry.core.database;

/**
 * Simplified Room database for Phase 2
 * Focus on core functionality with minimal complexity
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/core/database/ROSTRYDatabase;", "Landroidx/room/RoomDatabase;", "()V", "fowlDao", "Lcom/rio/rostry/core/database/dao/FowlDao;", "userDao", "Lcom/rio/rostry/core/database/dao/UserDao;", "Companion", "database-simple_debug"})
@androidx.room.Database(entities = {com.rio.rostry.core.database.entities.UserEntity.class, com.rio.rostry.core.database.entities.FowlEntity.class}, version = 1, exportSchema = false)
@androidx.room.TypeConverters(value = {com.rio.rostry.core.database.converters.DateConverter.class})
public abstract class ROSTRYDatabase extends androidx.room.RoomDatabase {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DATABASE_NAME = "rostry_database";
    
    /**
     * Migration from version 1 to 2 (when needed in future)
     */
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIGRATION_1_2 = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.database.ROSTRYDatabase.Companion Companion = null;
    
    public ROSTRYDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.rio.rostry.core.database.dao.UserDao userDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.rio.rostry.core.database.dao.FowlDao fowlDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/rio/rostry/core/database/ROSTRYDatabase$Companion;", "", "()V", "DATABASE_NAME", "", "MIGRATION_1_2", "Landroidx/room/migration/Migration;", "getMIGRATION_1_2", "()Landroidx/room/migration/Migration;", "database-simple_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Migration from version 1 to 2 (when needed in future)
         */
        @org.jetbrains.annotations.NotNull()
        public final androidx.room.migration.Migration getMIGRATION_1_2() {
            return null;
        }
    }
}