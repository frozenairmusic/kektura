package hu.kektura.app.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u0003H\'J\u0014\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\n0\u0003H\'J\u0016\u0010\r\u001a\u00020\u00062\u0006\u0010\u000e\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u000f\u00a8\u0006\u0010"}, d2 = {"Lhu/kektura/app/data/db/UserStampDao;", "", "countLive", "Landroidx/lifecycle/LiveData;", "", "deleteByPointId", "", "pointId", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllLive", "", "Lhu/kektura/app/data/model/UserStamp;", "getCollectedIdSetLive", "insert", "stamp", "(Lhu/kektura/app/data/model/UserStamp;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface UserStampDao {
    
    @androidx.room.Query(value = "SELECT * FROM user_stamps ORDER BY collectedAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.UserStamp>> getAllLive();
    
    @androidx.room.Query(value = "SELECT stampPointId FROM user_stamps")
    @org.jetbrains.annotations.NotNull()
    public abstract androidx.lifecycle.LiveData<java.util.List<java.lang.Integer>> getCollectedIdSetLive();
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM user_stamps")
    @org.jetbrains.annotations.NotNull()
    public abstract androidx.lifecycle.LiveData<java.lang.Integer> countLive();
    
    @androidx.room.Insert(onConflict = 5)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    hu.kektura.app.data.model.UserStamp stamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM user_stamps WHERE stampPointId = :pointId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteByPointId(int pointId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}