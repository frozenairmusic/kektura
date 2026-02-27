package hu.kektura.app;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0010\u001a\u00020\u0011H\u0082@\u00a2\u0006\u0002\u0010\u0012J\b\u0010\u0013\u001a\u00020\u0011H\u0016J\u000e\u0010\u0014\u001a\u00020\u0011H\u0082@\u00a2\u0006\u0002\u0010\u0012R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u001b\u0010\u000b\u001a\u00020\f8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\n\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0015"}, d2 = {"Lhu/kektura/app/KekturaApp;", "Landroid/app/Application;", "()V", "appScope", "Lkotlinx/coroutines/CoroutineScope;", "database", "Lhu/kektura/app/data/db/AppDatabase;", "getDatabase", "()Lhu/kektura/app/data/db/AppDatabase;", "database$delegate", "Lkotlin/Lazy;", "repository", "Lhu/kektura/app/data/repository/TrailRepository;", "getRepository", "()Lhu/kektura/app/data/repository/TrailRepository;", "repository$delegate", "downloadMissingGpxFiles", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "onCreate", "seedSegmentsIfNeeded", "app_debug"})
public final class KekturaApp extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy repository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope appScope = null;
    
    public KekturaApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final hu.kektura.app.data.db.AppDatabase getDatabase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final hu.kektura.app.data.repository.TrailRepository getRepository() {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final java.lang.Object seedSegmentsIfNeeded(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Downloads GPX for any OKT segments that don't yet have data.
     * Runs silently in background; UI observes LiveData and updates when data arrives.
     */
    private final java.lang.Object downloadMissingGpxFiles(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}