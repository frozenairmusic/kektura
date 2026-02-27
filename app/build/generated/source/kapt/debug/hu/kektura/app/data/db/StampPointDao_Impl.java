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
import androidx.sqlite.db.SupportSQLiteStatement;
import hu.kektura.app.data.model.StampPoint;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class StampPointDao_Impl implements StampPointDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StampPoint> __insertionAdapterOfStampPoint;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBySegmentId;

  public StampPointDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStampPoint = new EntityInsertionAdapter<StampPoint>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stamp_points` (`id`,`name`,`latitude`,`longitude`,`region`,`stampCode`,`notes`,`segmentId`,`elevation`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StampPoint entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        if (entity.getRegion() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRegion());
        }
        if (entity.getStampCode() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStampCode());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getNotes());
        }
        statement.bindLong(8, entity.getSegmentId());
        statement.bindDouble(9, entity.getElevation());
      }
    };
    this.__preparedStmtOfDeleteBySegmentId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM stamp_points WHERE segmentId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<StampPoint> points,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStampPoint.insert(points);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBySegmentId(final int segId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBySegmentId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, segId);
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
          __preparedStmtOfDeleteBySegmentId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<StampPoint>> getAllLive() {
    final String _sql = "SELECT * FROM stamp_points ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"stamp_points"}, false, new Callable<List<StampPoint>>() {
      @Override
      @Nullable
      public List<StampPoint> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRegion = CursorUtil.getColumnIndexOrThrow(_cursor, "region");
          final int _cursorIndexOfStampCode = CursorUtil.getColumnIndexOrThrow(_cursor, "stampCode");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfSegmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "segmentId");
          final int _cursorIndexOfElevation = CursorUtil.getColumnIndexOrThrow(_cursor, "elevation");
          final List<StampPoint> _result = new ArrayList<StampPoint>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StampPoint _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpRegion;
            if (_cursor.isNull(_cursorIndexOfRegion)) {
              _tmpRegion = null;
            } else {
              _tmpRegion = _cursor.getString(_cursorIndexOfRegion);
            }
            final String _tmpStampCode;
            if (_cursor.isNull(_cursorIndexOfStampCode)) {
              _tmpStampCode = null;
            } else {
              _tmpStampCode = _cursor.getString(_cursorIndexOfStampCode);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final int _tmpSegmentId;
            _tmpSegmentId = _cursor.getInt(_cursorIndexOfSegmentId);
            final double _tmpElevation;
            _tmpElevation = _cursor.getDouble(_cursorIndexOfElevation);
            _item = new StampPoint(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRegion,_tmpStampCode,_tmpNotes,_tmpSegmentId,_tmpElevation);
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
  public LiveData<List<StampPoint>> getBySegmentIdLive(final int segId) {
    final String _sql = "SELECT * FROM stamp_points WHERE segmentId = ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, segId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"stamp_points"}, false, new Callable<List<StampPoint>>() {
      @Override
      @Nullable
      public List<StampPoint> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRegion = CursorUtil.getColumnIndexOrThrow(_cursor, "region");
          final int _cursorIndexOfStampCode = CursorUtil.getColumnIndexOrThrow(_cursor, "stampCode");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfSegmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "segmentId");
          final int _cursorIndexOfElevation = CursorUtil.getColumnIndexOrThrow(_cursor, "elevation");
          final List<StampPoint> _result = new ArrayList<StampPoint>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StampPoint _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpRegion;
            if (_cursor.isNull(_cursorIndexOfRegion)) {
              _tmpRegion = null;
            } else {
              _tmpRegion = _cursor.getString(_cursorIndexOfRegion);
            }
            final String _tmpStampCode;
            if (_cursor.isNull(_cursorIndexOfStampCode)) {
              _tmpStampCode = null;
            } else {
              _tmpStampCode = _cursor.getString(_cursorIndexOfStampCode);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final int _tmpSegmentId;
            _tmpSegmentId = _cursor.getInt(_cursorIndexOfSegmentId);
            final double _tmpElevation;
            _tmpElevation = _cursor.getDouble(_cursorIndexOfElevation);
            _item = new StampPoint(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRegion,_tmpStampCode,_tmpNotes,_tmpSegmentId,_tmpElevation);
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
  public Object getAll(final Continuation<? super List<StampPoint>> $completion) {
    final String _sql = "SELECT * FROM stamp_points";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StampPoint>>() {
      @Override
      @NonNull
      public List<StampPoint> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRegion = CursorUtil.getColumnIndexOrThrow(_cursor, "region");
          final int _cursorIndexOfStampCode = CursorUtil.getColumnIndexOrThrow(_cursor, "stampCode");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfSegmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "segmentId");
          final int _cursorIndexOfElevation = CursorUtil.getColumnIndexOrThrow(_cursor, "elevation");
          final List<StampPoint> _result = new ArrayList<StampPoint>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StampPoint _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpRegion;
            if (_cursor.isNull(_cursorIndexOfRegion)) {
              _tmpRegion = null;
            } else {
              _tmpRegion = _cursor.getString(_cursorIndexOfRegion);
            }
            final String _tmpStampCode;
            if (_cursor.isNull(_cursorIndexOfStampCode)) {
              _tmpStampCode = null;
            } else {
              _tmpStampCode = _cursor.getString(_cursorIndexOfStampCode);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final int _tmpSegmentId;
            _tmpSegmentId = _cursor.getInt(_cursorIndexOfSegmentId);
            final double _tmpElevation;
            _tmpElevation = _cursor.getDouble(_cursorIndexOfElevation);
            _item = new StampPoint(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRegion,_tmpStampCode,_tmpNotes,_tmpSegmentId,_tmpElevation);
            _result.add(_item);
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
