package hu.kektura.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile GpxSegmentDao _gpxSegmentDao;

  private volatile StampPointDao _stampPointDao;

  private volatile UserStampDao _userStampDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `gpx_segments` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `region` TEXT NOT NULL, `distanceKm` REAL NOT NULL, `gpxContent` TEXT, `hasData` INTEGER NOT NULL, `visible` INTEGER NOT NULL, `lastUpdated` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stamp_points` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `region` TEXT NOT NULL, `stampCode` TEXT NOT NULL, `notes` TEXT NOT NULL, `segmentId` INTEGER NOT NULL, `elevation` REAL NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_stamps` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `stampPointId` INTEGER NOT NULL, `collectedAt` INTEGER NOT NULL, `note` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bbbb2278d2547dd873403b853de0ac03')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `gpx_segments`");
        db.execSQL("DROP TABLE IF EXISTS `stamp_points`");
        db.execSQL("DROP TABLE IF EXISTS `user_stamps`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsGpxSegments = new HashMap<String, TableInfo.Column>(8);
        _columnsGpxSegments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGpxSegments.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGpxSegments.put("region", new TableInfo.Column("region", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGpxSegments.put("distanceKm", new TableInfo.Column("distanceKm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGpxSegments.put("gpxContent", new TableInfo.Column("gpxContent", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGpxSegments.put("hasData", new TableInfo.Column("hasData", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGpxSegments.put("visible", new TableInfo.Column("visible", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGpxSegments.put("lastUpdated", new TableInfo.Column("lastUpdated", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGpxSegments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGpxSegments = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGpxSegments = new TableInfo("gpx_segments", _columnsGpxSegments, _foreignKeysGpxSegments, _indicesGpxSegments);
        final TableInfo _existingGpxSegments = TableInfo.read(db, "gpx_segments");
        if (!_infoGpxSegments.equals(_existingGpxSegments)) {
          return new RoomOpenHelper.ValidationResult(false, "gpx_segments(hu.kektura.app.data.model.GpxSegment).\n"
                  + " Expected:\n" + _infoGpxSegments + "\n"
                  + " Found:\n" + _existingGpxSegments);
        }
        final HashMap<String, TableInfo.Column> _columnsStampPoints = new HashMap<String, TableInfo.Column>(9);
        _columnsStampPoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("region", new TableInfo.Column("region", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("stampCode", new TableInfo.Column("stampCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("segmentId", new TableInfo.Column("segmentId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStampPoints.put("elevation", new TableInfo.Column("elevation", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStampPoints = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStampPoints = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStampPoints = new TableInfo("stamp_points", _columnsStampPoints, _foreignKeysStampPoints, _indicesStampPoints);
        final TableInfo _existingStampPoints = TableInfo.read(db, "stamp_points");
        if (!_infoStampPoints.equals(_existingStampPoints)) {
          return new RoomOpenHelper.ValidationResult(false, "stamp_points(hu.kektura.app.data.model.StampPoint).\n"
                  + " Expected:\n" + _infoStampPoints + "\n"
                  + " Found:\n" + _existingStampPoints);
        }
        final HashMap<String, TableInfo.Column> _columnsUserStamps = new HashMap<String, TableInfo.Column>(4);
        _columnsUserStamps.put("uid", new TableInfo.Column("uid", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserStamps.put("stampPointId", new TableInfo.Column("stampPointId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserStamps.put("collectedAt", new TableInfo.Column("collectedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserStamps.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserStamps = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserStamps = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserStamps = new TableInfo("user_stamps", _columnsUserStamps, _foreignKeysUserStamps, _indicesUserStamps);
        final TableInfo _existingUserStamps = TableInfo.read(db, "user_stamps");
        if (!_infoUserStamps.equals(_existingUserStamps)) {
          return new RoomOpenHelper.ValidationResult(false, "user_stamps(hu.kektura.app.data.model.UserStamp).\n"
                  + " Expected:\n" + _infoUserStamps + "\n"
                  + " Found:\n" + _existingUserStamps);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "bbbb2278d2547dd873403b853de0ac03", "410874232ce5c83365eaa6b72ce12146");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "gpx_segments","stamp_points","user_stamps");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `gpx_segments`");
      _db.execSQL("DELETE FROM `stamp_points`");
      _db.execSQL("DELETE FROM `user_stamps`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(GpxSegmentDao.class, GpxSegmentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StampPointDao.class, StampPointDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserStampDao.class, UserStampDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public GpxSegmentDao gpxSegmentDao() {
    if (_gpxSegmentDao != null) {
      return _gpxSegmentDao;
    } else {
      synchronized(this) {
        if(_gpxSegmentDao == null) {
          _gpxSegmentDao = new GpxSegmentDao_Impl(this);
        }
        return _gpxSegmentDao;
      }
    }
  }

  @Override
  public StampPointDao stampPointDao() {
    if (_stampPointDao != null) {
      return _stampPointDao;
    } else {
      synchronized(this) {
        if(_stampPointDao == null) {
          _stampPointDao = new StampPointDao_Impl(this);
        }
        return _stampPointDao;
      }
    }
  }

  @Override
  public UserStampDao userStampDao() {
    if (_userStampDao != null) {
      return _userStampDao;
    } else {
      synchronized(this) {
        if(_userStampDao == null) {
          _userStampDao = new UserStampDao_Impl(this);
        }
        return _userStampDao;
      }
    }
  }
}
