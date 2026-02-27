package hu.kektura.app.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u000e\u0018\u0000 (2\u00020\u0001:\u0001(B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0016J\u0018\u0010\u0017\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0018\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0016J \u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0007J\u001a\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00070\u00062\u0006\u0010\u001d\u001a\u00020\u000fJ\u0016\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0016J(\u0010 \u001a\u00020\u00142\u0006\u0010!\u001a\u00020\u000f2\u0006\u0010\"\u001a\u00020\u001b2\b\b\u0002\u0010#\u001a\u00020\u001bH\u0086@\u00a2\u0006\u0002\u0010$J\u001e\u0010%\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u000f2\u0006\u0010&\u001a\u00020\u001bH\u0086@\u00a2\u0006\u0002\u0010\'R\u001d\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\nR\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\nR\u001d\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lhu/kektura/app/data/repository/TrailRepository;", "", "db", "Lhu/kektura/app/data/db/AppDatabase;", "(Lhu/kektura/app/data/db/AppDatabase;)V", "allSegments", "Landroidx/lifecycle/LiveData;", "", "Lhu/kektura/app/data/model/GpxSegment;", "getAllSegments", "()Landroidx/lifecycle/LiveData;", "allStampPoints", "Lhu/kektura/app/data/model/StampPoint;", "getAllStampPoints", "collectedCount", "", "getCollectedCount", "collectedPointIds", "getCollectedPointIds", "collectStamp", "", "stampPointId", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSegment", "id", "getSegmentsByTrailTypesLive", "trailTypes", "", "getStampsBySegmentLive", "segId", "removeStamp", "pointId", "syncWaypointsFromGpxText", "segmentId", "gpxText", "regionName", "(ILjava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateSegmentGpx", "gpxContent", "(ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class TrailRepository {
    @org.jetbrains.annotations.NotNull()
    private final hu.kektura.app.data.db.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.GpxSegment>> allSegments = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.StampPoint>> allStampPoints = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<java.lang.Integer>> collectedPointIds = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.Integer> collectedCount = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> STAMP_PREFIXES = null;
    @org.jetbrains.annotations.NotNull()
    public static final hu.kektura.app.data.repository.TrailRepository.Companion Companion = null;
    
    public TrailRepository(@org.jetbrains.annotations.NotNull()
    hu.kektura.app.data.db.AppDatabase db) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.GpxSegment>> getAllSegments() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.GpxSegment>> getSegmentsByTrailTypesLive(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> trailTypes) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getSegment(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super hu.kektura.app.data.model.GpxSegment> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateSegmentGpx(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String gpxContent, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.StampPoint>> getAllStampPoints() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.StampPoint>> getStampsBySegmentLive(int segId) {
        return null;
    }
    
    /**
     * Parses waypoints from the provided GPX text and upserts stamp points
     * for [segmentId]. IDs are assigned as segmentId * 1000 + (1-based index).
     *
     * Stamp code is extracted from <desc> by finding the last parenthesized code
     * that matches a known trail stamp prefix (OKTPH, DDKPH, AKPH).
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncWaypointsFromGpxText(int segmentId, @org.jetbrains.annotations.NotNull()
    java.lang.String gpxText, @org.jetbrains.annotations.NotNull()
    java.lang.String regionName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<java.lang.Integer>> getCollectedPointIds() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Integer> getCollectedCount() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object collectStamp(int stampPointId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object removeStamp(int pointId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\u0005J\u000e\u0010\b\u001a\u00020\u00052\u0006\u0010\t\u001a\u00020\u0005R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lhu/kektura/app/data/repository/TrailRepository$Companion;", "", "()V", "STAMP_PREFIXES", "", "", "groupKeyFor", "stampCode", "parseStampCode", "desc", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Extracts the last stamp code from a GPX waypoint <desc>.
         * Handles all three trail types:
         * - OKT  → OKTPH_XX
         * - RPDDK → DDKPH_XX
         * - AK   → AKPH_XX
         *
         * e.g. "Liptói-menedékház (EM143INF) (OKTPH_100_2)" → "OKTPH_100_2"
         */
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String parseStampCode(@org.jetbrains.annotations.NotNull()
        java.lang.String desc) {
            return null;
        }
        
        /**
         * Derives the display group key by stripping numeric-only suffixes:
         *  OKTPH_07      → OKTPH_07
         *  OKTPH_07_1    → OKTPH_07   (numeric suffix → same physical location)
         *  OKTPH_07_2    → OKTPH_07
         *  OKTPH_07_B    → OKTPH_07_B (letter → inserted stamp, own group)
         *  DDKPH_03_1    → DDKPH_03
         *  AKPH_05       → AKPH_05
         */
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String groupKeyFor(@org.jetbrains.annotations.NotNull()
        java.lang.String stampCode) {
            return null;
        }
    }
}