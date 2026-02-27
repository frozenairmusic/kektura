package hu.kektura.app.ui.stampdetail;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0002\u0003\u0004B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0002\u0005\u0006\u00a8\u0006\u0007"}, d2 = {"Lhu/kektura/app/ui/stampdetail/StampListItem;", "", "()V", "GroupHeader", "StampEntry", "Lhu/kektura/app/ui/stampdetail/StampListItem$GroupHeader;", "Lhu/kektura/app/ui/stampdetail/StampListItem$StampEntry;", "app_debug"})
public abstract class StampListItem {
    
    private StampListItem() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lhu/kektura/app/ui/stampdetail/StampListItem$GroupHeader;", "Lhu/kektura/app/ui/stampdetail/StampListItem;", "group", "Lhu/kektura/app/ui/stampdetail/StampGroupUi;", "(Lhu/kektura/app/ui/stampdetail/StampGroupUi;)V", "getGroup", "()Lhu/kektura/app/ui/stampdetail/StampGroupUi;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class GroupHeader extends hu.kektura.app.ui.stampdetail.StampListItem {
        @org.jetbrains.annotations.NotNull()
        private final hu.kektura.app.ui.stampdetail.StampGroupUi group = null;
        
        public GroupHeader(@org.jetbrains.annotations.NotNull()
        hu.kektura.app.ui.stampdetail.StampGroupUi group) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final hu.kektura.app.ui.stampdetail.StampGroupUi getGroup() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final hu.kektura.app.ui.stampdetail.StampGroupUi component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final hu.kektura.app.ui.stampdetail.StampListItem.GroupHeader copy(@org.jetbrains.annotations.NotNull()
        hu.kektura.app.ui.stampdetail.StampGroupUi group) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\f\u001a\u00020\u0005H\u00c6\u0003J\u001d\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u00d6\u0003J\t\u0010\u0012\u001a\u00020\u0013H\u00d6\u0001J\t\u0010\u0014\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0015"}, d2 = {"Lhu/kektura/app/ui/stampdetail/StampListItem$StampEntry;", "Lhu/kektura/app/ui/stampdetail/StampListItem;", "item", "Lhu/kektura/app/ui/stampdetail/StampPointUi;", "groupKey", "", "(Lhu/kektura/app/ui/stampdetail/StampPointUi;Ljava/lang/String;)V", "getGroupKey", "()Ljava/lang/String;", "getItem", "()Lhu/kektura/app/ui/stampdetail/StampPointUi;", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class StampEntry extends hu.kektura.app.ui.stampdetail.StampListItem {
        @org.jetbrains.annotations.NotNull()
        private final hu.kektura.app.ui.stampdetail.StampPointUi item = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String groupKey = null;
        
        public StampEntry(@org.jetbrains.annotations.NotNull()
        hu.kektura.app.ui.stampdetail.StampPointUi item, @org.jetbrains.annotations.NotNull()
        java.lang.String groupKey) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final hu.kektura.app.ui.stampdetail.StampPointUi getItem() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getGroupKey() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final hu.kektura.app.ui.stampdetail.StampPointUi component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final hu.kektura.app.ui.stampdetail.StampListItem.StampEntry copy(@org.jetbrains.annotations.NotNull()
        hu.kektura.app.ui.stampdetail.StampPointUi item, @org.jetbrains.annotations.NotNull()
        java.lang.String groupKey) {
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
}