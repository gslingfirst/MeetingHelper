package com.example.meetinghelper;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity {
    public static final int UPDATE_INFO = 0;
    private int user_id;
    private String admin;
    private List<Room> roomList = new ArrayList<>();
    private RoomAdapter adapter;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private ImageView index_bottom_bar_scan;
    private RoomService updateDb = new RoomService(0);  //定义一个用于更新数据库的子线程
    private ListPopupWindow mlistPop;
    private List<String> popList = new ArrayList<String>();
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    TextView meeting_count;

    private Handler handler_period = new Handler(){
        public void handleMessage(Message msg)
        {  //用于与主程序交互
            switch (msg.what) {
                case UPDATE_INFO:
                    adapter.notifyDataSetChanged();
                    //Log.d("abd", "handleMessage: updateDatabase");
                    break;
                default:
                    break;
            }
        }
    };
    class RoomService implements Runnable{   //自定义线程定时获取网络数据
        private int type;

        RoomService(int type){this.type = type;}
        @Override
        public void run() {
            //Log.d("abd", "run: " + type);
            Message message = new Message();
            if(type == 0) {
                refreshRooms();
                message.what = UPDATE_INFO;
            }else if(type == 1){
            }
            handler_period.sendMessage(message);
        }
    }
    public static void adminActionStart(Context context, int user_id, String admin) {
        Intent intent = new Intent(context, AdminActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("admin", admin);
        Log.d("zjj", "adminActionStart: inin");
        context.startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //testLitePal();
        setContentView(R.layout.activity_admin);
        dbHelper = new MyDatabaseHelper(this,"MeetingDB.db",null,1);
        db = dbHelper.getWritableDatabase();
        index_bottom_bar_scan = findViewById(R.id.create_new_room);
        index_bottom_bar_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting_click);
                Intent intent = new Intent(AdminActivity.this, CreateRoomActivity.class);
                //Log.d("zjj", user_id + "-"+ admin);
                intent.putExtra("user_id", user_id);
                intent.putExtra("admin", admin);
                startActivityForResult(intent, 1);
            }
        });
        toolbar = findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.admin_window);
        navigationView = findViewById(R.id.admin_navi_view);
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.admin_nav_setting:
                        break;
                    case R.id.admin_nav_share:
                        // 获取系统剪贴板
                        Log.d("zjj", "admin is: " + admin);
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
                        ClipData clipData = ClipData.newPlainText(null, admin);

                        // 把数据集设置（复制）到剪贴板
                        clipboard.setPrimaryClip(clipData);
                        Toast.makeText(AdminActivity.this, "配置已复制到剪切板", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.admin_nav_logout:
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AdminActivity.this);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.apply();
                        Intent intent_1 = new Intent(AdminActivity.this, LoginActivity.class);
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

        recyclerView = findViewById(R.id.rooms);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator()); //设置添加和删除动画
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                index_bottom_bar_scan.setImageResource(R.mipmap.create_new_meeting_hide);
            }
        });
        meeting_count = findViewById(R.id.room_count);
        meeting_count.setText("已创建会议室：" + roomList.size());
        getRooms();//获取会议列表
        Log.d("zjj", "" + roomList.size());
        //setPeriodService();
    }

    void setPeriodService(){
        ScheduledExecutorService service = Executors.newScheduledThreadPool(2); //定时器
        long initialDelay = 0;
        long period_1 = 30;
        service.scheduleWithFixedDelay(updateDb, initialDelay, period_1, TimeUnit.SECONDS); //增加定时任务，每30分钟更新一次
    }//定时任务，定时获取网络会议进行更新，半小时一次
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
                    // 创建了一个新会议室
                    Log.d("abd", "get a new Room");
                    Room roomItem = (Room) data.getSerializableExtra("new_room");
                    ContentValues values = getContentValues(roomItem.getRoomid(),roomItem.getName(),roomItem.getLocation(),roomItem.getDesc(),roomItem.getType());
                    int type = data.getIntExtra("position",-1);
                    if(type != -1)
                    {
                        roomList.set(type,roomItem);
                        db.update("room",values,"roomid = ?",new String[]{""+roomItem.getRoom_id()});
                        adapter.notifyItemChanged(type);
                    }else {
                        roomList.add(0, roomItem);
                        db.insert("room",null,values);
                        adapter.notifyItemInserted(0);
                        TextView meeting_count = findViewById(R.id.room_count);
                        meeting_count.setText("已创建会议室：" + roomList.size());
                    }
                }
                break;
            default:
        }
    }
    void clickRoomsItem() //item点击事件处理
    {
        //adapter = new MeetingItemAdapter(meetingList);
        adapter.setOnItemClickListener(new RoomAdapter.OnItemClickListener() { //会议室预约条目的点击事件
            @Override
            public void onItemClick(View view, int position) {
                //Toast.makeText(UserActivity.this, position + " Click",Toast.LENGTH_SHORT).show();
                Room roomItem = roomList.get(position);
                if(roomItem.getType() == 0)
                {
                    //Log.d("zjj", "get_type0:"+roomItem.getType());
                    roomItem.setType(1);
                    adapter.notifyItemChanged(position);
                }else{
                    //Log.d("zjj", "get_type1:"+roomItem.getType());
                    roomItem.setType(0);
                    adapter.notifyItemChanged(position);
                }
            }
            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(AdminActivity.this,"Long Click",Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void swipeRefresh(){
        swipeRefreshLayout = findViewById(R.id.admin_swipe_layout);
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
                refreshRooms();
            }
        });
    } //recyclerView下拉刷新

    private void refreshRooms(){
        String url = getString(R.string.host) + "/room/" + admin;
        HttpUtil.okHttpGet(url, new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdminActivity.this, "请检查网络连接是否正常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("abd", resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    List<JsonRoom> jlist = new ArrayList<>();
                    String roomsStr = jsonObject.getJSONArray("rooms").toString();
                    jlist = new Gson().fromJson(roomsStr,
                            new TypeToken<List<JsonRoom>>(){}.getType());
                    transToRoom(jlist);
                    Collections.sort(roomList, new Comparator<Room>() {
                        @Override
                        public int compare(Room room, Room r1) {
                            long c = room.getRoom_id() - r1.getRoom_id();
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
                    syncRoomDb();
                    Log.d("zjj", "" + roomList.size());
                } catch (Exception e) {
                    Log.d("zjj", "false");
                    e.printStackTrace();
                }
            }
        });
    } //下拉刷新处理

    void getRooms() {
        showProgressDialog();
        String url = getString(R.string.host) + "/room/" + admin;
        HttpUtil.okHttpGet(url, new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                loadRoomList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initAdapter();
                        swipeRefresh();
                        closeProgressDialog();
                        Toast.makeText(AdminActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String resp = response.body().string();
                Log.d("zjj", resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    List<JsonRoom> jlist = new ArrayList<JsonRoom>();
                    String roomsStr = jsonObject.getJSONArray("rooms").toString();
                    jlist = new Gson().fromJson(roomsStr,
                            new TypeToken<List<JsonRoom>>(){}.getType());
                    Log.d("zjj", jlist.size()+"sizeofjlist");
                    transToRoom(jlist);
                    Log.d("zjj", "aftertrans");
                    Collections.sort(roomList, new Comparator<Room>() {
                        @Override
                        public int compare(Room room, Room t1) {
                            long c = room.getRoom_id() - t1.getRoom_id();
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
                    syncRoomDb();
                    Log.d("zjj", roomList.size()+"sizeofroom");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initAdapter();
                            swipeRefresh();
                            closeProgressDialog();
                            //Toast.makeText(AdminActivity.this, "获取网络数据成功", Toast.LENGTH_SHORT).show();
                            meeting_count.setText("已创建会议室：" + roomList.size());
                        }
                    });
                    Log.d("zjj", "" + roomList.size());
                } catch (Exception e) {
                    loadRoomList();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initAdapter();
                            swipeRefresh();
                            closeProgressDialog();
                            //Toast.makeText(AdminActivity.this, "解析异常", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.d("zjj", "false");
                    e.printStackTrace();
                }
            }
        });

    } //获取网络会议数据

    public void transToRoom(List<JsonRoom> jlist){
        roomList.clear();
        for(int i = 0; i < jlist.size(); i++){
            roomList.add(new Room(jlist.get(i).room_id,jlist.get(i).name,jlist.get(i).location,jlist.get(i).desc, 0));
        }
    }

    public void initAdapter(){
        adapter = new RoomAdapter(roomList);
        recyclerView.setAdapter(adapter);
        clickRoomsItem();
    } //初始化recyclerAdapter

    private ContentValues getContentValues(int roomid, String name, String location, String desc, int type){
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("roomid", roomid);
        values.put("location", location);
        values.put("description", desc);
        values.put("type",type);
        return  values;
    }

    public void syncRoomDb(){  //同步数据库
        db.delete("room",null,null);
        for(int i = 0; i < roomList.size(); i++){
            ContentValues values = getContentValues(roomList.get(i).getRoomid(),roomList.get(i).getName(),roomList.get(i).getLocation(),roomList.get(i).getDesc(),roomList.get(i).getType());
            db.insert("room",null,values);
        }
    }

    public void loadRoomList(){
        roomList.clear();
        Cursor cursor = db.query("room",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int roomid = cursor.getInt(cursor.getColumnIndex("roomid"));
                String location = cursor.getString(cursor.getColumnIndex("location"));
                String desc = cursor.getString(cursor.getColumnIndex("description"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                Room room = new Room(roomid,name,location,desc,type);
                roomList.add(room);
            }while(cursor.moveToNext());
        }
        cursor.close();
    } //从数据库加载会议列表

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载会议...");
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
