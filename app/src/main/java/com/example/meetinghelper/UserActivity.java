package com.example.meetinghelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Response;

public class UserActivity extends AppCompatActivity {
    public static final int UPDATE_INFO = 0;
    public static final int CHECK_COMING_MEETING = 1;
    private int user_id;
    private String admin;
    private List<MeetingItem> meetingList = new ArrayList<>();
    private MeetingItemAdapter adapter;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private ImageView index_bottom_bar_scan;
    private MeetingService updateDb = new MeetingService(0);  //定义一个用于更新数据库的子线程
    private MeetingService checkTime = new MeetingService(1); //检查会议即将开始的会议
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private Handler handler_period = new Handler(){
        public void handleMessage(Message msg)
        {  //用于与主程序交互
            switch (msg.what) {
                case UPDATE_INFO:
                    adapter.notifyDataSetChanged();
                    Log.d("abd", "handleMessage: updateDatabase");
                    break;
                case CHECK_COMING_MEETING:
                    getNotification();
                    break;
                default:
                    break;
            }
        }
    };

    class MeetingService implements Runnable{   //自定义线程定时获取网络数据
        private int type;

        MeetingService(int type){this.type = type;}
        @Override
        public void run() {
            Log.d("abd", "run: " + type);
            Message message = new Message();
            if(type == 0) {
                refreshMeetings();
                checkOutOfDate();
                message.what = UPDATE_INFO;
            }else if(type == 1){
                //message.what = CHECK_COMING_MEETING;
                if(checkComingMeeting()) {
                    message.what = CHECK_COMING_MEETING;
                }
            }
            handler_period.sendMessage(message);
        }
    }
    public static void actionStart(Context context, int user_id, String admin) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("admin", admin);
        context.startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting);
        //meetingList = LitePal.findAll(MeetingItem.class);
        //initAdapter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        dbHelper = new MyDatabaseHelper(this,"MeetingDB.db",null,1);
        db = dbHelper.getWritableDatabase();
        //testLitePal();
        index_bottom_bar_scan = findViewById(R.id.create_new_meeting);
        index_bottom_bar_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting_click);
                Intent intent = new Intent(UserActivity.this, CreateMeetingActivity.class);
                Log.d("zjj", user_id + "-"+ admin);
                intent.putExtra("user_id", user_id);
                intent.putExtra("admin", admin);
                startActivityForResult(intent, 1);
            }
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.user_window);
        navigationView = findViewById(R.id.navi_view);
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_history:
                        Intent intent = new Intent(UserActivity.this,MeetingHistory.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_clock:
                        Intent intent_alarm = new Intent(UserActivity.this,MeetingAlarmManagerActiivity.class);
                        startActivity(intent_alarm);
                        break;
                    case R.id.nav_setting:
                        break;
                    case R.id.nav_share:
                        // 获取系统剪贴板
                        Log.d("zjj", "admin is: " + admin);
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
                        ClipData clipData = ClipData.newPlainText(null, admin);

                        // 把数据集设置（复制）到剪贴板
                        clipboard.setPrimaryClip(clipData);
                        Toast.makeText(UserActivity.this, "配置已复制到剪切板", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_logout:
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(UserActivity.this);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.apply();
                        Intent intent_1 = new Intent(UserActivity.this, LoginActivity.class);
                        startActivity(intent_1);
                        finish();
                        break;
                    case R.id.navi_view:
                        break;
                    default:
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        Intent intent = getIntent();
        user_id = intent.getIntExtra("user_id", 0);
        admin = intent.getStringExtra("admin");
        Log.d("zjj", user_id + "-"+ admin);

        recyclerView = findViewById(R.id.meetings);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting_hide);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState){
                    case 0:
                        index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting);
                        break;
                   default:
                        index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting_hide);
                }
            }
        });
        getMeetings();//获取会议列表
        TextView meeting_count = findViewById(R.id.meeting_count);
        meeting_count.setText("已预约会议：" + meetingList.size());
        TextView time_range = findViewById(R.id.time_range);
        Date s_date = new Date(new Date().getTime());
        Date e_date = new Date(new Date().getTime() + 14 * 24 * 60 * 60 * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String time = simpleDateFormat.format(s_date);
        time += " 至 " + simpleDateFormat.format(e_date);
        time_range.setText(time);
        //setPeriodService();
    }

    private boolean checkComingMeeting(){
        MeetingItem meetingItem = LitePal.findFirst(MeetingItem.class);
        long timeNow = new Date().getTime();
        return meetingItem.getSttime() >= timeNow / 1000 && (meetingItem.getSttime() - timeNow / 1000) <= 3600;
    }

    void setPeriodService(){
        ScheduledExecutorService service1 = Executors.newScheduledThreadPool(2); //定时器
        long initialDelay = 0;
        long period_1 = 30;
        service1.scheduleWithFixedDelay(updateDb, initialDelay, period_1, TimeUnit.MINUTES); //增加定时任务，每30分钟更新一次
    }//定时任务，定时获取网络会议进行更新，并且检查过时会议，半小时一次

    void getNotification(){
        Log.d("abd", "getNotification: in");
        MeetingItem meetingItem = LitePal.findFirst(MeetingItem.class);
        Intent intent = new Intent(this,ComingMeetingActivity.class);
        intent.putExtra("comingMeeting",meetingItem);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel("1","meeting",NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(notificationChannel);
        }
        Notification notification = new NotificationCompat.Builder(this,"1")
                .setContentTitle("会议通知")
                .setContentText("您有一个会议将在1小时之后开始哦！点击查看")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        manager.notify(1,notification);
    } //会议通知

    void testLitePal(){
        //Log.d("abd", "testLitePal: in");
        long time = new Date().getTime()/1000;
        db.delete("meeting_item",null,null);
        db.delete("history_meeting",null,null);
        for(int i = 0; i < 10; i++)
        {
            ContentValues values = getContentValues((i+300)+"",time+500000,time+1000000,i,"topic","beijing","nothing",0,0);
            db.insert("meeting_item",null,values);
            Toast.makeText(this,"insert success", Toast.LENGTH_SHORT).show();
        }
        for(int i = 0; i < 10; i++)
        {
            ContentValues values = getHistoryContentValues((i+300)+"",time-500000,time-100000,i,"topic","beijing","nothing",0,0);
            db.insert("history_meeting",null,values);
            Toast.makeText(this,"insert success", Toast.LENGTH_SHORT).show();
        }
    }

    private ContentValues getContentValues(String room_name, long sstime, long edtime, int meetingid, String topic, String room_location, String room_desc, int type, int alarm_clock){
        ContentValues values = new ContentValues();
        values.put("room_name", room_name);
        values.put("sttime", sstime);
        values.put("edtime",edtime);
        values.put("meetingid",meetingid);
        values.put("topic",topic);
        values.put("room_location", room_location);
        values.put("room_desc", room_desc);
        values.put("type",type);
        values.put("alarm_clock",alarm_clock);
        return  values;
    }
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    // 创建了一个新会议
                    Log.d("zjj", "get a new meeting");
                    MeetingItem meetingItem = (MeetingItem) data.getSerializableExtra("new_meeting");
                    meetingList.add(0, meetingItem);
                    int type = data.getIntExtra("position",-1);
                    ContentValues values = getContentValues(meetingItem.getRoom_name(),meetingItem.getSttime(),meetingItem.getEdtime(),meetingItem.getMeetingid(),meetingItem.getTopic(),meetingItem.getRoom_location(),meetingItem.getRoom_desc(),meetingItem.getType(),meetingItem.getAlarm_clock());
                    if(type != -1)
                    {
                        db.update("meeting_item",values,"meetingid = ?", new String[]{""+ meetingItem.getMeetingid()});
                        //meetingItem.update(type);
                    }else {
                        db.insert("meeting_item",null,values);
                        adapter.notifyItemInserted(0);
                        TextView meeting_count = findViewById(R.id.meeting_count);
                        meeting_count.setText("已预约会议：" + meetingList.size());
                        //meetingItem.save();
                    }
                }
                break;
            default:
        }
    }

    void clickMeetingItem() //item点击事件处理
    {
        //adapter = new MeetingItemAdapter(meetingList);
        adapter.setOnItemClickListener(new MeetingItemAdapter.OnItemClickListener() { //会议室预约条目的点击事件
            @Override
            public void onItemClick(View view, int position) {
                //Toast.makeText(UserActivity.this, position + " Click",Toast.LENGTH_SHORT).show();
                MeetingItem meetingItem = meetingList.get(position);
                if(meetingItem.getType() == 0)
                {
                    meetingItem.setType(1);
                    adapter.notifyItemChanged(position);
                    //delete_modify_listener(position);
                }else{
                    meetingItem.setType(0);
                    adapter.notifyItemChanged(position);
                }
            }
            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(UserActivity.this,"Long Click",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkOutOfDate(){
        for(int i = 0; i < meetingList.size(); i++)
        {
            if(meetingList.get(i).getEdtime() < (new Date().getTime()/ 1000))
            {
                db.delete("meeting_item","meetingid = ?", new String[]{"" + meetingList.get(i).getMeetingid()});
                ContentValues values = getHistoryContentValues(meetingList.get(i).getRoom_name(),meetingList.get(i).getSttime(),meetingList.get(i).getEdtime(),meetingList.get(i).getMeetingid(),meetingList.get(i).getTopic(),meetingList.get(i).getRoom_location(),meetingList.get(i).getRoom_desc(),0,0);
                db.insert("history_meeting",null,values);
                meetingList.remove(i);
            }
        }
    } //查找已过时的会议，更新数据库

    private ContentValues getHistoryContentValues(String room_name, long sstime, long edtime, int meetingid, String topic, String room_location, String room_desc, int type, int histype){
        ContentValues values = new ContentValues();
        values.put("room_name", room_name);
        values.put("sttime", sstime);
        values.put("edtime",edtime);
        values.put("meetingid",meetingid);
        values.put("topic",topic);
        values.put("room_location", room_location);
        values.put("room_desc", room_desc);
        values.put("type",type);
        values.put("hisType",histype);
        return  values;
    }

    public void swipeRefresh(){
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { //刷新动画后对调此方法
                //设置可见
                swipeRefreshLayout.setRefreshing(true);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //模拟加载时间，设置不可见
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },3000);
                refreshMeetings();
                checkOutOfDate();
            }
        });
    } //recyclerView下拉刷新

    private void refreshMeetings(){
        String url = getString(R.string.host) + "/meeting/" + user_id;
        HttpUtil.okHttpGet(url, new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserActivity.this, "请检查网络连接是否正常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("zjj", resp);
                try {
                    //List<MeetingItem> mlist = new ArrayList<MeetingItem>();
                    JSONObject jsonObject = new JSONObject(resp);
                    List<JsonMeetingItem> jlist = new ArrayList<JsonMeetingItem>();
                    String meetingsStr = jsonObject.getJSONArray("meetings").toString();
                    //Log.d("zjj", "ok11111");
                    jlist = new Gson().fromJson(meetingsStr,
                            new TypeToken<List<JsonMeetingItem>>(){}.getType());
                    //Log.d("zjj", "ok22222");
                    transToMeetings(jlist);
                    Collections.sort(meetingList, new Comparator<MeetingItem>() {
                        @Override
                        public int compare(MeetingItem meetingItem, MeetingItem t1) {
                            long c = meetingItem.getSttime() - t1.getSttime();
                            if(c > 0)
                            {
                                return 1;
                            }else if(c < 0)
                            {
                                return -1;
                            }
                            return 0;
                        }
                    });
                    //syncMeetingDb();
                    Log.d("zjj", "" + meetingList.size());
                } catch (Exception e) {
                    Log.d("zjj", "false");
                    e.printStackTrace();
                }
            }
        });
    } //下拉刷新处理

    void getMeetings() {
        showProgressDialog("正在加载会议。。。");
        String url = getString(R.string.host) + "/meeting/" + user_id;
        Log.d("zjj", url);
        HttpUtil.okHttpGet(url, new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                loadMeetingList();
                //initAdapter();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView meeting_count = findViewById(R.id.meeting_count);
                        meeting_count.setText("已预约会议：" + meetingList.size());
                        closeProgressDialog();
                        initAdapter();
                        swipeRefresh();
                        Toast.makeText(UserActivity.this, "请检查网络连接是否正常", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("zjj", "ok"+ resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    List<JsonMeetingItem> jlist = new ArrayList<JsonMeetingItem>();
                    String meetingsStr = jsonObject.getJSONArray("meetings").toString();
                    Log.d("zjj", "ok11111");
                    jlist = new Gson().fromJson(meetingsStr,
                            new TypeToken<List<JsonMeetingItem>>(){}.getType());
                    Log.d("zjj", "ok22222");
                    transToMeetings(jlist);
                    Collections.sort(meetingList, new Comparator<MeetingItem>() {
                        @Override
                        public int compare(MeetingItem meetingItem, MeetingItem t1) {
                            long c = meetingItem.getSttime() - t1.getSttime();
                            if(c > 0)
                            {
                                return 1;
                            }else if(c < 0)
                            {
                                return -1;
                            }
                            return 0;
                        }
                    });
                    if(meetingList.size() > 0){
                        syncMeetingDb();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView meeting_count = findViewById(R.id.meeting_count);
                            meeting_count.setText("已预约会议：" + meetingList.size());
                            closeProgressDialog();
                            //Toast.makeText(UserActivity.this, "获取网络数据成功", Toast.LENGTH_SHORT).show();
                            initAdapter();
                            swipeRefresh();
                        }
                    });
                    Log.d("zjj", "" + meetingList.size());
                } catch (Exception e) {
                    Log.d("zjj", "fail1111");
                    loadMeetingList();
                    Log.d("zjj", "fail22222");
                    //initAdapter();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView meeting_count = findViewById(R.id.meeting_count);
                            meeting_count.setText("已预约会议：" + meetingList.size());
                            closeProgressDialog();
                            initAdapter();
                            swipeRefresh();
                            //Toast.makeText(UserActivity.this, "获取网络数据失败exception", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.d("zjj", "false");
                    e.printStackTrace();
                }
            }
        });

    } //获取网络会议数据

    public void transToMeetings(List<JsonMeetingItem> jlist){
        meetingList.clear();
        for(int i = 0; i < jlist.size(); i++){
            meetingList.add(new MeetingItem(jlist.get(i).room_name,jlist.get(i).sttime,jlist.get(i).edtime,jlist.get(i).id,jlist.get(i).topic,jlist.get(i).room_location,jlist.get(i).room_desc));
        }
    }

    public void initAdapter(){
        adapter = new MeetingItemAdapter(meetingList);
        Log.d("zjj", "fail3333");
        recyclerView.setAdapter(adapter);
        Log.d("zjj", "fail44444");
        clickMeetingItem();
    } //初始化recyclerAdapter

    public void syncMeetingDb(){  //同步数据库
        db.delete("meeting_item",null,null);
        for(int i = 0; i < meetingList.size(); i++){
            ContentValues values = getContentValues(meetingList.get(i).getRoom_name(),meetingList.get(i).getSttime(),meetingList.get(i).getEdtime(),meetingList.get(i).getMeetingid(),meetingList.get(i).getTopic(),meetingList.get(i).getRoom_location(),meetingList.get(i).getRoom_desc(),meetingList.get(i).getType(),meetingList.get(i).getAlarm_clock());
            db.insert("meeting_item",null,values);
        }
    }

    public void loadMeetingList(){
        meetingList.clear();
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
    } //从数据库加载会议列表

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void showProgressDialog(String content) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(content);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
