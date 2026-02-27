package hu.kektura.app.ui.stamps;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u0012"}, d2 = {"Lhu/kektura/app/ui/stamps/StampsViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "repo", "Lhu/kektura/app/data/repository/TrailRepository;", "segmentRows", "Landroidx/lifecycle/MediatorLiveData;", "", "Lhu/kektura/app/ui/stamps/SegmentRow;", "getSegmentRows", "()Landroidx/lifecycle/MediatorLiveData;", "totalCollected", "Landroidx/lifecycle/LiveData;", "", "getTotalCollected", "()Landroidx/lifecycle/LiveData;", "app_debug"})
public final class StampsViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final hu.kektura.app.data.repository.TrailRepository repo = null;
    
    /**
     * All 27 OKT segments enriched with live stamp/collected counts.
     * Deduplicates by OKTPH group key exactly as in the original app.
     */
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MediatorLiveData<java.util.List<hu.kektura.app.ui.stamps.SegmentRow>> segmentRows = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.Integer> totalCollected = null;
    
    public StampsViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    /**
     * All 27 OKT segments enriched with live stamp/collected counts.
     * Deduplicates by OKTPH group key exactly as in the original app.
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MediatorLiveData<java.util.List<hu.kektura.app.ui.stamps.SegmentRow>> getSegmentRows() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Integer> getTotalCollected() {
        return null;
    }
}