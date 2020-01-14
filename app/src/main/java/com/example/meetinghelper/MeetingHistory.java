package com.example.meetinghelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MeetingHistory extends AppCompatActivity {

    private List<MeetingItem> hList = new ArrayList<>();
    private MeetingItemAdapter hAdapter;
    private ProgressDialog progressDialog;
    private RecyclerView hRecyclerView;
    private SwipeRefreshLayout hSwipeRefreshLayout;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Toolbar toolbar;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_history);
        dbHelper = new MyDatabaseHelper(this,"MeetingDB.db",null,1);
        db = dbHelper.getWritableDatabase();
        toolbar = findViewById(R.id.history_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        hRecyclerView = findViewById(R.id.meetings_his);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        hRecyclerView.setLayoutManager(layoutManager);
        hRecyclerView.setItemAnimator(new DefaultItemAnimator()); //设置添加和删除动画
        getMeetings();
        hAdapter = new MeetingItemAdapter(hList);
        hRecyclerView.setAdapter(hAdapter);
        clickMeetingItem();
        swipeRefresh();
    }

    void clickMeetingItem() //item点击事件处理
    {
        //adapter = new MeetingItemAdapter(meetingList);
        hAdapter.setOnItemClickListener(new MeetingItemAdapter.OnItemClickListener() { //会议室预约条目的点击事件
            @Override
            public void onItemClick(View view, int position) {
            }
            @Override
            public void onItemLongClick(View view,final int position) {
                AlertDialog alertDialog = new AlertDialog.Builder(view.getContext())
                        .setTitle("删除历史会议")
                        .setMessage("是否删除历史会议")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteItem(position);
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
        });
    }
    private void deleteItem(int position){
        MeetingItem meetingItem = hList.get(position);
        db.delete("history_meeting","meetingid = ?", new String[]{meetingItem.getMeetingid()+""});
        hList.remove(position);
        hAdapter.notifyItemRemoved(position);
    }

    private void getMeetings(){
        hList.clear();
        Cursor cursor = db.query("history_meeting",null,null,null,null,null,null);
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
                int hisType = cursor.getInt(cursor.getColumnIndex("hisType"));
                MeetingItem meetingItem = new MeetingItem(room_name,sttime,edtime,meetingid,topic,room_location,room_desc);
                meetingItem.setType(type);
                HistoryMeetingItem historyMeetingItem = new HistoryMeetingItem(meetingItem, hisType);
                hList.add(meetingItem);
            }while(cursor.moveToNext());
        }
        cursor.close();
    }
    private void swipeRefresh(){
        hSwipeRefreshLayout = findViewById(R.id.his_swipe_layout);
        hSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { //刷新动画后对调此方法
                //设置可见
                hSwipeRefreshLayout.setRefreshing(true);
                getMeetings();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //模拟加载时间，设置不可见
                        hSwipeRefreshLayout.setRefreshing(false);
                    }
                },1000);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history_delete_all:
                AlertDialog alertDialog = new AlertDialog.Builder(MeetingHistory.this)
                        .setTitle("清空历史会议")
                        .setMessage("是否清空历史会议")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteAll();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).create();
                alertDialog.show();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
    private void deleteAll(){
        db.delete("history_meeting",null,null);
        int len = hList.size();
        hList.clear();
        hAdapter.notifyItemRangeRemoved(0,len);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
