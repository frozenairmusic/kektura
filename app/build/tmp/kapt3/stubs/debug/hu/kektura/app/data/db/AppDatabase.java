package hu.kektura.app.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \t2\u00020\u0001:\u0001\tB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\n"}, d2 = {"Lhu/kektura/app/data/db/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "gpxSegmentDao", "Lhu/kektura/app/data/db/GpxSegmentDao;", "stampPointDao", "Lhu/kektura/app/data/db/StampPointDao;", "userStampDao", "Lhu/kektura/app/data/db/UserStampDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {hu.kektura.app.data.model.GpxSegment.class, hu.kektura.app.data.model.StampPoint.class, hu.kektura.app.data.model.UserStamp.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile hu.kektura.app.data.db.AppDatabase INSTANCE;
    
    /**
     * Adds the trailType column (defaults to 'OKT') and seeds new trail segments.
     */
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIGRATION_1_2 = null;
    @org.jetbrains.annotations.NotNull()
    public static final hu.kektura.app.data.db.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract hu.kektura.app.data.db.GpxSegmentDao gpxSegmentDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract hu.kektura.app.data.db.StampPointDao stampPointDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract hu.kektura.app.data.db.UserStampDao userStampDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\tR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lhu/kektura/app/data/db/AppDatabase$Companion;", "", "()V", "INSTANCE", "Lhu/kektura/app/data/db/AppDatabase;", "MIGRATION_1_2", "Landroidx/room/migration/Migration;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final hu.kektura.app.data.db.AppDatabase getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}