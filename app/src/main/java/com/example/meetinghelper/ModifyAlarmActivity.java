package com.example.meetinghelper;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.calendarprovidermanager.CalendarAlarmDateType;
import com.example.calendarprovidermanager.CalendarProviderManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ModifyAlarmActivity extends AppCompatActivity {

    Button btn_date, btn_start_time, btn_save;
    TextView date, start_time, alarm_room, alarm_topic, alarm_meeting_time;
    EditText last_time;
    private Calendar stTimeCalendar, ltTimeCalendar;
    int last_time_input;
    long start_time_input;
    int position;
    MeetingAlarmItem meetingAlarmItem;
    private CalendarProviderManager.Builder mBuilder;
    long event_id;
    SQLiteDatabase db;
    MyDatabaseHelper dbHelper;
    private Toolbar toolbar;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            //startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("zjj", "modify onCreate:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_alarm);
        dbHelper = new MyDatabaseHelper(this,"MeetingDB.db",null,1);
        db = dbHelper.getWritableDatabase();
        toolbar = findViewById(R.id.modify_alarm_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mBuilder = new CalendarProviderManager.Builder(this);
        btn_date = findViewById(R.id.alarm_date);
        btn_start_time = findViewById(R.id.alarm_sttime);
        btn_save = findViewById(R.id.save_alarm);
        date = findViewById(R.id.desc_6);
        start_time = findViewById(R.id.desc_4);
        last_time = findViewById(R.id.desc_5);
        alarm_room = findViewById(R.id.alarm_room);
        alarm_topic = findViewById(R.id.alarm_topic);
        alarm_meeting_time = findViewById(R.id.alarm_meeting_time);
        last_time_input = 0;
        start_time_input = 0;

        Intent receive_intent = getIntent();
        meetingAlarmItem = (MeetingAlarmItem) receive_intent.getSerializableExtra("alarmInfo");
        position = receive_intent.getIntExtra("position", -1);
        alarm_room.setText(meetingAlarmItem.getRoom_name());
        alarm_topic.setText(meetingAlarmItem.getMeeting_topic());
        alarm_meeting_time.setText(translateTime(meetingAlarmItem.getSttime(),meetingAlarmItem.getEdtime()));
        if(meetingAlarmItem.getEnable_clock() == 1)
        {
            start_time.setText(meetingAlarmItem.getStart_time()+"");
            last_time.setText(meetingAlarmItem.getLast_time()+"");
        }
        stTimeCalendar = Calendar.getInstance();
        ltTimeCalendar = Calendar.getInstance();
        stTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
        stTimeCalendar.set(Calendar.MINUTE, 0);
        ltTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
        ltTimeCalendar.set(Calendar.MINUTE, 0);
        final int year = stTimeCalendar.get(Calendar.YEAR),
                month = stTimeCalendar.get(Calendar.MONTH),
                dayOfMonth = stTimeCalendar.get(Calendar.DAY_OF_MONTH);
        date.setText(convertYMD(year, month, dayOfMonth));
        start_time.setText(convertHM(0, 0));
        last_time.setText(convertHM(0, 0));
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAlarm();
            }
        });
        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(ModifyAlarmActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("zjj", "date picked");
                                if (stTimeCalendar.get(Calendar.YEAR) != year
                                        || stTimeCalendar.get(Calendar.MONTH) != month
                                        || stTimeCalendar.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
                                    stTimeCalendar.set(year, month, dayOfMonth);
                                    ltTimeCalendar.set(year, month, dayOfMonth);
                                    date.setText(convertYMD(year, month, dayOfMonth));
                                    //添加检查时间
                                    //
                                }
                            }
                        }, year, month, dayOfMonth);
                DatePicker datePicker = dialog.getDatePicker();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                datePicker.setMinDate(calendar.getTimeInMillis());
                calendar.add(Calendar.DAY_OF_MONTH, 14);
                datePicker.setMaxDate(calendar.getTimeInMillis());
                dialog.show();
            }
        });

        //
        btn_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(ModifyAlarmActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                stTimeCalendar.set(Calendar.HOUR_OF_DAY, i);
                                stTimeCalendar.set(Calendar.MINUTE, i1);
                                start_time.setText(convertHM(i, i1));
                            }
                        }, 0, 0, true);
                dialog.show();
            }
        });
    }

    String translateTime(long t1, long t2){
        String time;
        Date st = new Date(t1*1000), ed = new Date(t2*1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        time = simpleDateFormat.format(st);
        time += "-" + simpleDateFormat.format(ed).substring(11,16);
        return time;
    }
    private String convertYMD(int year, int month, int dayOfMonth) {
        return year + "-" + (month + 1) + "-" + dayOfMonth;
    }

    private String convertHM(int hour, int minute) {
        String h = "" + hour, m = "" + minute;
        if (hour < 10) h = "0" + h;
        if (minute < 10) m += "0";
        return h + ":" + m;
    }
    private void addAlarm(){
        last_time_input = Integer.parseInt(last_time.getText().toString());
        start_time_input = stTimeCalendar.getTimeInMillis()/1000;
        if(checkInput()){
            if(meetingAlarmItem.getEnable_clock() == 0){
                ModifyAlarmActivityPermissionsDispatcher.addWithPermissionCheck(ModifyAlarmActivity.this);
            }else{
                ModifyAlarmActivityPermissionsDispatcher.updateWithPermissionCheck(ModifyAlarmActivity.this, meetingAlarmItem.getEventId());
            }
        }

    }

    private boolean checkInput(){
        if(last_time_input == 0){
            Toast.makeText(ModifyAlarmActivity.this,"请输入闹钟持续时间",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (start_time_input <= new Date().getTime() / 1000) {
            Toast.makeText(this, "开始时间必须晚于现在", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @NeedsPermission({Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR})
    public void add() {
        long eventId = mBuilder
                .setCalendarName(null)
                .setEvent("会议提醒", meetingAlarmItem.getRoom_name() + " " + meetingAlarmItem.getMeeting_topic() + " " + translateTime(meetingAlarmItem.getSttime(),meetingAlarmItem.getEdtime()))
                .setHasAlarm(true)
                .setAlarmStartTime(start_time_input*1000)
                .setAlarmDurationTime(last_time_input)
                .setAlarmLeadTime(2, CalendarAlarmDateType.MINUTE)
                .build()
                .addCalendarEvent();
        event_id = eventId;
        if (eventId > -1){
            Toast.makeText(ModifyAlarmActivity.this, "添加成功",Toast.LENGTH_SHORT).show();
            ContentValues values = new ContentValues();
            values.put("eventId",eventId);
            values.put("enable_clock", 1);
            values.put("start_time",start_time_input);
            values.put("last_time",last_time_input);
            db.update("meeting_alarm_item",values,"room_name = ? and sttime = ? and topic = ?", new String[]{meetingAlarmItem.getRoom_name(),meetingAlarmItem.getSttime()+"",meetingAlarmItem.getMeeting_topic()});
            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
            finish();
        }
        else {
            Toast.makeText(ModifyAlarmActivity.this, "添加失败",Toast.LENGTH_SHORT).show();
        }
    }
    @NeedsPermission({Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR})
    public void update(long eventId) {

        boolean isUpdate = mBuilder
                .setHasAlarm(true)
                .setAlarmStartTime(start_time_input)
                .setAlarmDurationTime(last_time_input)
                .setAlarmLeadTime(2,CalendarAlarmDateType.MINUTE)
                .build()
                .updateCalendarEvent(eventId);

        if (isUpdate){
            Toast.makeText(ModifyAlarmActivity.this, "更新成功",Toast.LENGTH_SHORT).show();
            ContentValues values = new ContentValues();
            values.put("eventId",eventId);
            values.put("enable_clock", 1);
            values.put("start_time",start_time_input);
            values.put("last_time",last_time_input);
            db.update("meeting_alarm_item",values,"room_name = ? and sttime = ? and topic = ?", new String[]{meetingAlarmItem.getRoom_name(),meetingAlarmItem.getSttime()+"",meetingAlarmItem.getMeeting_topic()});
            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
            finish();
        } else {
            Toast.makeText(ModifyAlarmActivity.this, "更新失败",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ModifyAlarmActivityPermissionsDispatcher.onRequestPermissionsResult(this,requestCode,grantResults);
    }
}
