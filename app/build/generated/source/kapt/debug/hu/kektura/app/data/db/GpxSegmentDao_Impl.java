package hu.kektura.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import hu.kektura.app.data.model.GpxSegment;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GpxSegmentDao_Impl implements GpxSegmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GpxSegment> __insertionAdapterOfGpxSegment;

  private final SharedSQLiteStatement __preparedStmtOfUpdateGpxContent;

  private final SharedSQLiteStatement __preparedStmtOfClearGpxContent;

  public GpxSegmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGpxSegment = new EntityInsertionAdapter<GpxSegment>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `gpx_segments` (`id`,`trailType`,`name`,`region`,`distanceKm`,`gpxContent`,`hasData`,`visible`,`lastUpdated`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GpxSegment entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTrailType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTrailType());
        }
        if (entity.getName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getName());
        }
        if (entity.getRegion() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getRegion());
        }
        statement.bindDouble(5, entity.getDistanceKm());
        if (entity.getGpxContent() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getGpxContent());
        }
        final int _tmp = entity.getHasData() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getVisible() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getLastUpdated() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getLastUpdated());
        }
      }
    };
    this.__preparedStmtOfUpdateGpxContent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE gpx_segments SET gpxContent = ?, hasData = 1, lastUpdated = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearGpxContent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE gpx_segments SET gpxContent = NULL, hasData = 0, lastUpdated = NULL WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<GpxSegment> segments,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGpxSegment.insert(segments);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateGpxContent(final int id, final String gpx, final String ts,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateGpxContent.acquire();
        int _argIndex = 1;
        if (gpx == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, gpx);
        }
        _argIndex = 2;
        if (ts == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, ts);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateGpxContent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearGpxContent(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearGpxContent.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearGpxContent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<GpxSegment>> getAllLive() {
    final String _sql = "SELECT * FROM gpx_segments ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"gpx_segments"}, false, new Callable<List<GpxSegment>>() {
      @Override
      @Nullable
      public List<GpxSegment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTrailType = CursorUtil.getColumnIndexOrThrow(_cursor, "trailType");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRegion = CursorUtil.getColumnIndexOrThrow(_cursor, "region");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfGpxContent = CursorUtil.getColumnIndexOrThrow(_cursor, "gpxContent");
          final int _cursorIndexOfHasData = CursorUtil.getColumnIndexOrThrow(_cursor, "hasData");
          final int _cursorIndexOfVisible = CursorUtil.getColumnIndexOrThrow(_cursor, "visible");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final List<GpxSegment> _result = new ArrayList<GpxSegment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GpxSegment _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTrailType;
            if (_cursor.isNull(_cursorIndexOfTrailType)) {
              _tmpTrailType = null;
            } else {
              _tmpTrailType = _cursor.getString(_cursorIndexOfTrailType);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpRegion;
            if (_cursor.isNull(_cursorIndexOfRegion)) {
              _tmpRegion = null;
            } else {
              _tmpRegion = _cursor.getString(_cursorIndexOfRegion);
            }
            final float _tmpDistanceKm;
            _tmpDistanceKm = _cursor.getFloat(_cursorIndexOfDistanceKm);
            final String _tmpGpxContent;
            if (_cursor.isNull(_cursorIndexOfGpxContent)) {
              _tmpGpxContent = null;
            } else {
              _tmpGpxContent = _cursor.getString(_cursorIndexOfGpxContent);
            }
            final boolean _tmpHasData;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasData);
            _tmpHasData = _tmp != 0;
            final boolean _tmpVisible;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfVisible);
            _tmpVisible = _tmp_1 != 0;
            final String _tmpLastUpdated;
            if (_cursor.isNull(_cursorIndexOfLastUpdated)) {
              _tmpLastUpdated = null;
            } else {
              _tmpLastUpdated = _cursor.getString(_cursorIndexOfLastUpdated);
            }
            _item = new GpxSegment(_tmpId,_tmpTrailType,_tmpName,_tmpRegion,_tmpDistanceKm,_tmpGpxContent,_tmpHasData,_tmpVisible,_tmpLastUpdated);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<GpxSegment>> getByTrailTypesLive(final List<String> trailTypes) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM gpx_segments WHERE trailType IN (");
    final int _inputSize = trailTypes.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") ORDER BY id ASC");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : trailTypes) {
      if (_item == null) {
        _statement.bindNull(_argIndex);
      } else {
        _statement.bindString(_argIndex, _item);
      }
      _argIndex++;
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"gpx_segments"}, false, new Callable<List<GpxSegment>>() {
      @Override
      @Nullable
      public List<GpxSegment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTrailType = CursorUtil.getColumnIndexOrThrow(_cursor, "trailType");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRegion = CursorUtil.getColumnIndexOrThrow(_cursor, "region");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfGpxContent = CursorUtil.getColumnIndexOrThrow(_cursor, "gpxContent");
          final int _cursorIndexOfHasData = CursorUtil.getColumnIndexOrThrow(_cursor, "hasData");
          final int _cursorIndexOfVisible = CursorUtil.getColumnIndexOrThrow(_cursor, "visible");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final List<GpxSegment> _result = new ArrayList<GpxSegment>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GpxSegment _item_1;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTrailType;
            if (_cursor.isNull(_cursorIndexOfTrailType)) {
              _tmpTrailType = null;
            } else {
              _tmpTrailType = _cursor.getString(_cursorIndexOfTrailType);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpRegion;
            if (_cursor.isNull(_cursorIndexOfRegion)) {
              _tmpRegion = null;
            } else {
              _tmpRegion = _cursor.getString(_cursorIndexOfRegion);
            }
            final float _tmpDistanceKm;
            _tmpDistanceKm = _cursor.getFloat(_cursorIndexOfDistanceKm);
            final String _tmpGpxContent;
            if (_cursor.isNull(_cursorIndexOfGpxContent)) {
              _tmpGpxContent = null;
            } else {
              _tmpGpxContent = _cursor.getString(_cursorIndexOfGpxContent);
            }
            final boolean _tmpHasData;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasData);
            _tmpHasData = _tmp != 0;
            final boolean _tmpVisible;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfVisible);
            _tmpVisible = _tmp_1 != 0;
            final String _tmpLastUpdated;
            if (_cursor.isNull(_cursorIndexOfLastUpdated)) {
              _tmpLastUpdated = null;
            } else {
              _tmpLastUpdated = _cursor.getString(_cursorIndexOfLastUpdated);
            }
            _item_1 = new GpxSegment(_tmpId,_tmpTrailType,_tmpName,_tmpRegion,_tmpDistanceKm,_tmpGpxContent,_tmpHasData,_tmpVisible,_tmpLastUpdated);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final int id, final Continuation<? super GpxSegment> $completion) {
    final String _sql = "SELECT * FROM gpx_segments WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GpxSegment>() {
      @Override
      @Nullable
      public GpxSegment call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTrailType = CursorUtil.getColumnIndexOrThrow(_cursor, "trailType");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRegion = CursorUtil.getColumnIndexOrThrow(_cursor, "region");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfGpxContent = CursorUtil.getColumnIndexOrThrow(_cursor, "gpxContent");
          final int _cursorIndexOfHasData = CursorUtil.getColumnIndexOrThrow(_cursor, "hasData");
          final int _cursorIndexOfVisible = CursorUtil.getColumnIndexOrThrow(_cursor, "visible");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final GpxSegment _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTrailType;
            if (_cursor.isNull(_cursorIndexOfTrailType)) {
              _tmpTrailType = null;
            } else {
              _tmpTrailType = _cursor.getString(_cursorIndexOfTrailType);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpRegion;
            if (_cursor.isNull(_cursorIndexOfRegion)) {
              _tmpRegion = null;
            } else {
              _tmpRegion = _cursor.getString(_cursorIndexOfRegion);
            }
            final float _tmpDistanceKm;
            _tmpDistanceKm = _cursor.getFloat(_cursorIndexOfDistanceKm);
            final String _tmpGpxContent;
            if (_cursor.isNull(_cursorIndexOfGpxContent)) {
              _tmpGpxContent = null;
            } else {
              _tmpGpxContent = _cursor.getString(_cursorIndexOfGpxContent);
            }
            final boolean _tmpHasData;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasData);
            _tmpHasData = _tmp != 0;
            final boolean _tmpVisible;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfVisible);
            _tmpVisible = _tmp_1 != 0;
            final String _tmpLastUpdated;
            if (_cursor.isNull(_cursorIndexOfLastUpdated)) {
              _tmpLastUpdated = null;
            } else {
              _tmpLastUpdated = _cursor.getString(_cursorIndexOfLastUpdated);
            }
            _result = new GpxSegment(_tmpId,_tmpTrailType,_tmpName,_tmpRegion,_tmpDistanceKm,_tmpGpxContent,_tmpHasData,_tmpVisible,_tmpLastUpdated);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
