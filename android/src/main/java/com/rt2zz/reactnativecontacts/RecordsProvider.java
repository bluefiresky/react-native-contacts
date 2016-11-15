package com.rt2zz.reactnativecontacts;

import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

public class RecordsProvider {
    private final static String TAG = RecordsProvider.class.getName();
    private final ContentResolver contentResolver;
    private final Context context;

    public RecordsProvider(ContentResolver contentResolver, Context context) {
        this.contentResolver = contentResolver;
        this.context = context;
    }

    /*
    *   获取手机通话记录
    *   1. 没有权限，则返回 null，cursor == null
    *   2. 拥有权限返回 WritableArray 列表，没有数据返回空列表
    * */
    public WritableArray getRecords() {
        WritableArray everyoneElse;
        {
            Cursor cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " desc");

            try {
                everyoneElse = loadRecordsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return everyoneElse;
    }

    @NonNull
    private WritableArray loadRecordsFrom(Cursor cursor) {
        if (cursor == null)
            return null;
        WritableArray records = Arguments.createArray();
        if(cursor.moveToFirst()) {
            do {
                Record record = new Record();
                record.name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                record.number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                record.type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                record.time = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                record.duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                record._new = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW));

                records.pushMap(record.toMap());
                Log.e(TAG, record.toString());
            } while (cursor.moveToNext());
        }

        return records;
    }

    private static class Record {
      private String name;       //  联系人
      private String number;   //  号码
      private int type;              //  呼叫类型
      private long time;           //  呼叫时间
      private long duration;    //   通话时间,单位:s
      private int _new;            //   0表示已看1表示未看

      public WritableMap toMap() {
          WritableMap record = Arguments.createMap();
          record.putString("name", convertName(name));
          record.putString("number", number);
          record.putString("type", convertType(type));
          record.putString("time", convertTime(time));
          record.putString("duration", String.valueOf(duration));

          return record;
      }

      private String convertType(int type){
          String result;
          switch (type) {
              case CallLog.Calls.INCOMING_TYPE:
                  result = "呼入";
                  break;
              case CallLog.Calls.OUTGOING_TYPE:
                  result = "呼出";
                  break;
              case CallLog.Calls.MISSED_TYPE:
                  result = "未接";
                  break;
              default:
                  result = "挂断";//应该是挂断.根据我手机类型判断出的
                  break;
          }
          return result;
      }

      private String convertTime(long time){
          SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
          Date date = new Date(time);
          return sfd.format(date);
      }

      private String convertName(String name){
          return TextUtils.isEmpty(name)? "未知" : name;
      }

      @Override
      public String toString() {
          return "RecordEntity ["+ convertName(name) +", " + number+", " + convertType(type) +", " + convertTime(time)+", " + duration+"s, " + _new + "]";
      }
    }
}
