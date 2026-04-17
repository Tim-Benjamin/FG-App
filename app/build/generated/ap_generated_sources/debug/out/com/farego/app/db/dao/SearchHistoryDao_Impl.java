package com.farego.app.db.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.farego.app.db.entity.SearchHistory;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SearchHistoryDao_Impl implements SearchHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SearchHistory> __insertionAdapterOfSearchHistory;

  private final SharedSQLiteStatement __preparedStmtOfClearHistoryForUser;

  public SearchHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSearchHistory = new EntityInsertionAdapter<SearchHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `search_history` (`id`,`user_id`,`origin_label`,`destination_label`,`distance_km`,`estimated_fare`,`transport_type`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SearchHistory entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.userId);
        if (entity.originLabel == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.originLabel);
        }
        if (entity.destinationLabel == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.destinationLabel);
        }
        statement.bindDouble(5, entity.distanceKm);
        statement.bindDouble(6, entity.estimatedFare);
        if (entity.transportType == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.transportType);
        }
        statement.bindLong(8, entity.timestamp);
      }
    };
    this.__preparedStmtOfClearHistoryForUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM search_history WHERE user_id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertHistory(final SearchHistory history) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSearchHistory.insert(history);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearHistoryForUser(final int userId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearHistoryForUser.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, userId);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearHistoryForUser.release(_stmt);
    }
  }

  @Override
  public LiveData<List<SearchHistory>> getHistoryForUser(final int userId) {
    final String _sql = "SELECT * FROM search_history WHERE user_id = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"search_history"}, false, new Callable<List<SearchHistory>>() {
      @Override
      @Nullable
      public List<SearchHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfOriginLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "origin_label");
          final int _cursorIndexOfDestinationLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "destination_label");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distance_km");
          final int _cursorIndexOfEstimatedFare = CursorUtil.getColumnIndexOrThrow(_cursor, "estimated_fare");
          final int _cursorIndexOfTransportType = CursorUtil.getColumnIndexOrThrow(_cursor, "transport_type");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<SearchHistory> _result = new ArrayList<SearchHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SearchHistory _item;
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpOriginLabel;
            if (_cursor.isNull(_cursorIndexOfOriginLabel)) {
              _tmpOriginLabel = null;
            } else {
              _tmpOriginLabel = _cursor.getString(_cursorIndexOfOriginLabel);
            }
            final String _tmpDestinationLabel;
            if (_cursor.isNull(_cursorIndexOfDestinationLabel)) {
              _tmpDestinationLabel = null;
            } else {
              _tmpDestinationLabel = _cursor.getString(_cursorIndexOfDestinationLabel);
            }
            final double _tmpDistanceKm;
            _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            final double _tmpEstimatedFare;
            _tmpEstimatedFare = _cursor.getDouble(_cursorIndexOfEstimatedFare);
            final String _tmpTransportType;
            if (_cursor.isNull(_cursorIndexOfTransportType)) {
              _tmpTransportType = null;
            } else {
              _tmpTransportType = _cursor.getString(_cursorIndexOfTransportType);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new SearchHistory(_tmpUserId,_tmpOriginLabel,_tmpDestinationLabel,_tmpDistanceKm,_tmpEstimatedFare,_tmpTransportType,_tmpTimestamp);
            _item.id = _cursor.getInt(_cursorIndexOfId);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
