package hu.kektura.app.ui.stamps;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u001c\u0010\u0005\u001a\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0007\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00070\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00160\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006\u0019"}, d2 = {"Lhu/kektura/app/ui/stamps/StampsViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "currentSegmentsSource", "Landroidx/lifecycle/LiveData;", "", "Lhu/kektura/app/data/model/GpxSegment;", "repo", "Lhu/kektura/app/data/repository/TrailRepository;", "segmentRows", "Landroidx/lifecycle/MediatorLiveData;", "Lhu/kektura/app/ui/stamps/SegmentRow;", "getSegmentRows", "()Landroidx/lifecycle/MediatorLiveData;", "selectedTrailTypes", "Landroidx/lifecycle/MutableLiveData;", "", "getSelectedTrailTypes", "()Landroidx/lifecycle/MutableLiveData;", "totalCollected", "", "getTotalCollected", "()Landroidx/lifecycle/LiveData;", "app_debug"})
public final class StampsViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final hu.kektura.app.data.repository.TrailRepository repo = null;
    
    /**
     * Selected trail types (as TrailType.name strings), e.g. ["OKT", "ALFOLDI"]
     */
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<java.lang.String>> selectedTrailTypes = null;
    @org.jetbrains.annotations.Nullable()
    private androidx.lifecycle.LiveData<java.util.List<hu.kektura.app.data.model.GpxSegment>> currentSegmentsSource;
    
    /**
     * All segments for the currently selected trails, enriched with live stamp/collected counts.
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
     * Selected trail types (as TrailType.name strings), e.g. ["OKT", "ALFOLDI"]
     */
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.util.List<java.lang.String>> getSelectedTrailTypes() {
        return null;
    }
    
    /**
     * All segments for the currently selected trails, enriched with live stamp/collected counts.
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