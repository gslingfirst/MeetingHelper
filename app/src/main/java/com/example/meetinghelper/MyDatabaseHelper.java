package com.example.meetinghelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_MEETINGITEM = "create table meeting_item("
            + "id integer primary key autoincrement,"
            + "room_name text,"
            + "sttime integer,"
            + "edtime integer,"
            + "meetingid integer,"
            + "topic text,"
            + "room_location text,"
            + "room_desc text,"
            + "type integer,"
            + "alarm_clock integer)";
    public static final String CREATE_ROOM = "create table room("
            + "id integer primary key autoincrement,"
            + "roomid integer,"
            + "name text,"
            + "location text,"
            + "description text,"
            + "type integer)";
    public static final String CREATE_HISTORYMEETING = "create table history_meeting("
            + "id integer primary key autoincrement,"
            + "room_name text,"
            + "sttime integer,"
            + "edtime integer,"
            + "meetingid integer,"
            + "topic text,"
            + "room_location text,"
            + "room_desc text,"
            + "type integer,"
            + "hisType integer)";
    public static final String CREATE_MEETINGALARMITEM="create table meeting_alarm_item("
            + "id integer primary key autoincrement,"
            + "eventId integer,"
            + "room_name text,"
            + "sttime integer,"
            + "edtime integer,"
            + "topic text,"
            + "enable_clock integer,"
            + "start_time integer,"
            + "last_time integer)";
    private Context context;
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_ROOM);
        sqLiteDatabase.execSQL(CREATE_HISTORYMEETING);
        sqLiteDatabase.execSQL(CREATE_MEETINGITEM);
        sqLiteDatabase.execSQL(CREATE_MEETINGALARMITEM);
        Toast.makeText(context, "create success",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
