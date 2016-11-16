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

public class SMSsProvider {
    private final static String TAG = SMSsProvider.class.getName();
    private final ContentResolver contentResolver;
    private final Context context;

    private static final String SMS_URI_ALL = "content://sms/";
    private static final List<String> SMS_PROJECTION = new ArrayList<String>() {{
        add("_id")
        add("address");
        add("person");
        add("body");
        add("date");
        add("type");
        add("read");
    }};

    public SMSsProvider(ContentResolver contentResolver, Context context) {
        this.contentResolver = contentResolver;
        this.context = context;
    }

    /*
    *   获取短信记录
    *   1. 没有权限，则返回 null，cursor == null
    *   2. 拥有权限返回 WritableArray 列表，没有数据返回空列表
    * */
    public WritableArray getSMSs() {
        WritableArray everyoneElse;
        {
            Cursor cursor = contentResolver.query(
                    Uri.parse(SMS_URI_ALL),
                    SMS_PROJECTION.toArray(new String[SMS_PROJECTION.size()]),
                    null,
                    null,
                    "date desc"
            );

            try {
                everyoneElse = loadSMSsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return everyoneElse;
    }

    @NonNull
    private WritableArray loadSMSsFrom(Cursor cursor) {
        if (cursor == null)
            return null;
        WritableArray SMSs = Arguments.createArray();
        if(cursor.moveToFirst()) {
            do {
                SMS sms = new SMS();
                sms.smsID = cursor.getString(cursor.getColumnIndex("_id"));
                sms.person = cursor.getString(cursor.getColumnIndex("person"));
                sms.address = cursor.getString(cursor.getColumnIndex("address"));
                sms.body = cursor.getString(cursor.getColumnIndex("body"));
                sms.type = cursor.getInt(cursor.getColumnIndex("type"));
                sms.date = cursor.getLong(cursor.getColumnIndex("date"));
                sms.read = cursor.getInt(cursor.getColumnIndex("read"));

                SMSs.pushMap(sms.toMap());
                Log.e(TAG, sms.toString());
            } while (cursor.moveToNext());
        }

        return SMSs;
    }

    private static class SMS {
      private String smsID;
      private String person;         // 发件人
      private String address;       // 发件地址
      private long date;            // 收件日期
      private String body;           //  短信内容
      private int type;                 // 短信类型1是接收到的，2是已发出
      //    private int protocol;           // 协议0SMS_PROTO短信，1MMS_PROTO彩信
      private int read;                 // 是否阅读0未读，1已读

      public WritableMap toMap() {
          WritableMap record = Arguments.createMap();
          record.putString("id", smsID);
          record.putString("person", convertName(person));
          record.putString("address", address);
          record.putInt("type", type);
          record.putString("date", String.valueOf(date));
          record.putString("body", body);
          record.putInt("read", read);

          return record;
      }

      private String convertType(int type){
          String result;
          switch (type) {
              case 1:
                  result = "接收";
                  break;
              case 2:
                  result = "发送";
                  break;
              default:
                  result = "未知";
          }
          return result;
      }

      private String convertRead(int read){
          String result;
          switch (read) {
              case 0:
                  result = "未读";
                  break;
              case 1:
                  result = "已读";
                  break;
              default:
                  result = "未知";
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
          return "SMSEntity ["+ convertName(person) +", " + address+", " + convertType(type) +", " + convertRead(read) + ", "+ convertTime(date)+", "  + body + "]";
      }
    }
}
