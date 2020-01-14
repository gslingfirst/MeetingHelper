package com.example.meetinghelper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.calendarprovidermanager.CalendarProviderManager;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MeetingAlarmManagerActiivity extends AppCompatActivity {

    private List<MeetingAlarmItem> aList = new ArrayList<>();
    private MeetingAlarmItemAdapter aAdapter;
    private ProgressDialog progressDialog;
    private RecyclerView aRecyclerView;
    private SwipeRefreshLayout aSwipeRefreshLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Toolbar toolbar;
    private CalendarProviderManager.Builder mBuilder;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onRestart() {
        Log.d("zjj", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("zjj", "onCreate:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_alarm_manager_actiivity);
        toolbar = findViewById(R.id.alarm_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        aRecyclerView = findViewById(R.id.meetings_alarm);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        aRecyclerView.setLayoutManager(layoutManager);
        mBuilder = new CalendarProviderManager.Builder(this);
        dbHelper = new MyDatabaseHelper(this,"MeetingDB.db",null,1);
        db = dbHelper.getWritableDatabase();

        aList = new ArrayList<MeetingAlarmItem>();
        initAlarmMeeting();
        aAdapter = new MeetingAlarmItemAdapter(aList);
        aRecyclerView.setAdapter(aAdapter);
        clickAlarmItem();
        swipeRefresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            //Intent intent = new Intent(MeetingHistory.this, UserActivity.class);
            //startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    void clickAlarmItem() //item点击事件处理
    {
        //adapter = new MeetingItemAdapter(meetingList);
        aAdapter.setOnItemClickListener(new MeetingAlarmItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MeetingAlarmItem meetingAlarmItem = aList.get(position);
                Intent intent = new Intent(MeetingAlarmManagerActiivity.this, ModifyAlarmActivity.class);
                intent.putExtra("alarmInfo", meetingAlarmItem);
                intent.putExtra("position", position);
                startActivityForResult(intent,1);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                MeetingAlarmItem meetingAlarmItem = aList.get(position);
                if(meetingAlarmItem.getEnable_clock() == 0){
                    Toast.makeText(MeetingAlarmManagerActiivity.this, "此会议还未设置提醒，无法取消！",Toast.LENGTH_SHORT).show();
                }else{
                    final MeetingAlarmItem meetingAlarmItem1 = new MeetingAlarmItem(meetingAlarmItem);
                    final int pos = position;
                    AlertDialog alertDialog = new AlertDialog.Builder(view.getContext())
                            .setTitle("删除会议提醒")
                            .setMessage("是否删除会议提醒")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MeetingAlarmManagerActiivityPermissionsDispatcher.deleteWithPermissionCheck(MeetingAlarmManagerActiivity.this,
                                            meetingAlarmItem1.getEventId(), pos);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            }).create();
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    getMeetings();
                    aAdapter.notifyItemRangeChanged(0,aList.size());
                }
                break;
            default:
        }
    }

    void initAlarmMeeting(){
        aList.clear();
        List<MeetingItem> meetingList = new ArrayList<>();
        Cursor cursor = db.query("meeting_item",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String room_name = cursor.getString(cursor.getColumnIndex("room_name"));
                long sttime = cursor.getLong(cursor.getColumnIndex("sttime"));
                long edtime = cursor.getLong(cursor.getColumnIndex("edtime"));
                int meetingid = cursor.getInt(cursor.getColumnIndex("meetingid"));
                String topic = cursor.getString(cursor.getColumnIndex("topic"));
                String room_location = cursor.getString(cursor.getColumnIndex("room_location"));
                String room_desc = cursor.getString(cursor.getColumnIndex("room_desc"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                int alarm_clock = cursor.getInt(cursor.getColumnIndex("alarm_clock"));
                MeetingItem meetingItem = new MeetingItem(room_name,sttime,edtime,meetingid,topic,room_location,room_desc);
                meetingItem.setType(type);
                meetingItem.setAlarm_clock(alarm_clock);
                meetingList.add(meetingItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
        List<MeetingAlarmItem> alarmItemList = new ArrayList<>();
        Cursor cursor_1 = db.query("meeting_alarm_item",null,null,null,null,null,null);
        if(cursor_1.moveToFirst()){
            do{
                long eventId = cursor_1.getLong(cursor_1.getColumnIndex("eventId"));
                String room_name = cursor_1.getString(cursor_1.getColumnIndex("room_name"));
                long sttime = cursor_1.getLong(cursor_1.getColumnIndex("sttime"));
                long edtime = cursor_1.getLong(cursor_1.getColumnIndex("edtime"));
                String topic = cursor_1.getString(cursor_1.getColumnIndex("topic"));
                int enable_clock = cursor_1.getInt(cursor_1.getColumnIndex("enable_clock"));
                long start_time = cursor_1.getLong(cursor_1.getColumnIndex("start_time"));
                int last_time = cursor_1.getInt(cursor_1.getColumnIndex("last_time"));
                MeetingAlarmItem meetingAlarmItem = new MeetingAlarmItem(eventId,room_name,sttime,edtime,topic,start_time,last_time);
                meetingAlarmItem.setEnable_clock(enable_clock);
                alarmItemList.add(meetingAlarmItem);
            }while(cursor_1.moveToNext());
        }
        //aList.addAll(alarmItemList);
        cursor_1.close();
        Log.d("zjj", meetingList.size() + " " + aList.size());
        int[] record = new int[meetingList.size()];
        for(int i = 0; i < meetingList.size(); i++){
            record[i] = 0;
        }
        int[] record_alarm = new int[alarmItemList.size()];
        for(int i = 0; i < alarmItemList.size(); i++){
            record_alarm[i] = 0;
        }
        int flag = 0;
        for(int i = 0; i < alarmItemList.size(); i++){
            flag = 0;
            for(int j = 0; j < meetingList.size(); j++){
                if(meetingList.get(j).equalTo(alarmItemList.get(i))){
                    record[j] = 1;
                    flag = 1;
                    break;
                }
            }
            if(flag == 0){
                record_alarm[i] = 1;
            }
        }
        for(int i = 0; i < alarmItemList.size(); i++){
            if(record_alarm[i] == 0){
                aList.add(alarmItemList.get(i));
            }
        }
        for(int i = 0; i < meetingList.size(); i++){
            if( record[i] == 0){
                MeetingAlarmItem meetingAlarmItem = new MeetingAlarmItem(-1,meetingList.get(i).getRoom_name(),meetingList.get(i).getSttime(),meetingList.get(i).getEdtime(),meetingList.get(i).getTopic(),0,0);
                aList.add(meetingAlarmItem);
            }
        }
        db.delete("meeting_alarm_item",null,null);
        for(int i = 0; i< aList.size();i++){
            ContentValues values = getContentValues(aList.get(i).getEventId(),aList.get(i).getRoom_name(),aList.get(i).getSttime(),aList.get(i).getEdtime(),aList.get(i).getMeeting_topic(),aList.get(i).getEnable_clock(),aList.get(i).getStart_time(),aList.get(i).getLast_time());
            db.insert("meeting_alarm_item",null, values);
        }
    }

    private ContentValues getContentValues(long eventId, String room_name, long sstime, long edtime, String topic,int enable_clock, long start_time, int last_time){
        ContentValues values = new ContentValues();
        values.put("eventId", eventId);
        values.put("room_name", room_name);
        values.put("sttime", sstime);
        values.put("edtime",edtime);
        values.put("topic",topic);
        values.put("enable_clock",enable_clock);
        values.put("start_time",start_time);
        values.put("last_time",last_time);
        return  values;
    }
    private void getMeetings(){
        aList.clear();
        Cursor cursor = db.query("meeting_alarm_item",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                long eventId = cursor.getLong(cursor.getColumnIndex("eventId"));
                String room_name = cursor.getString(cursor.getColumnIndex("room_name"));
                long sttime = cursor.getLong(cursor.getColumnIndex("sttime"));
                long edtime = cursor.getLong(cursor.getColumnIndex("edtime"));
                String topic = cursor.getString(cursor.getColumnIndex("topic"));
                int enable_clock = cursor.getInt(cursor.getColumnIndex("enable_clock"));
                long start_time = cursor.getLong(cursor.getColumnIndex("start_time"));
                int last_time = cursor.getInt(cursor.getColumnIndex("last_time"));
                MeetingAlarmItem meetingAlarmItem = new MeetingAlarmItem(eventId,room_name,sttime,edtime,topic,start_time,last_time);
                meetingAlarmItem.setEnable_clock(enable_clock);
                aList.add(meetingAlarmItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
    }
    private void swipeRefresh(){
        aSwipeRefreshLayout = findViewById(R.id.alarm_swipe_layout);
        aSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { //刷新动画后对调此方法
                //设置可见
                aSwipeRefreshLayout.setRefreshing(true);
                //getMeetings();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //模拟加载时间，设置不可见
                        aSwipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });
    }

    @NeedsPermission({Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR})
    public void delete(long eventId, int position) {

        boolean isDelete = mBuilder.build().deleteCalendarEvent(eventId);
        if (isDelete){
            Toast.makeText(MeetingAlarmManagerActiivity.this, "会议提醒取消成功",Toast.LENGTH_SHORT).show();
            aList.get(position).setEnable_clock(0);
            aAdapter.notifyItemChanged(position);
            ContentValues values = new ContentValues();
            values.put("enable_clock", 0);
            db.update("meeting_alarm_item",values,"room_name = ? and sttime = ? and topic = ?", new String[]{aList.get(position).getRoom_name(),aList.get(position).getSttime()+"",aList.get(position).getMeeting_topic()});
            //aList.get(position).update(position);
        }
        else
        {
            Toast.makeText(MeetingAlarmManagerActiivity.this, "会议提醒取消失败",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MeetingAlarmManagerActiivityPermissionsDispatcher.onRequestPermissionsResult(this,requestCode,grantResults);
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
