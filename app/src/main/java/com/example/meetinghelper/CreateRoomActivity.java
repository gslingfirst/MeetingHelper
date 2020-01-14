package com.example.meetinghelper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateRoomActivity extends AppCompatActivity {

    private EditText et_room_name, et_room_location, et_room_desc;
    private Button btn_save;
    private int user_id;
    private String admin;
    private ProgressDialog progressDialog;
    int type = -1; //判断是修改操作，还是创建操作
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Room room;

    @Override
    public void onBackPressed() {
        //Intent intent = new Intent();
        //setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        et_room_name = findViewById(R.id.room_name_input);
        et_room_location = findViewById(R.id.room_location_input);
        et_room_desc = findViewById(R.id.room_desc_input);
        btn_save = findViewById(R.id.save_new_room_info);

        Intent intent = getIntent();
        user_id = intent.getIntExtra("user_id", 0);
        admin = intent.getStringExtra("admin");
        type = intent.getIntExtra("type",-1);
        room = (Room)intent.getSerializableExtra("roomInfo");
        if(type != -1){
            et_room_name.setText(room.getName());
            et_room_desc.setText(room.getDesc());
            et_room_location.setText(room.getLocation());
        }
        dbHelper = new MyDatabaseHelper(this,"MeetingDB.db",null,1);
        db = dbHelper.getWritableDatabase();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInfoEnough()){
                    if(CheckInput()){
                        Log.d("zjj", "check over");
                        if(type == -1){
                            createRoom();
                        }else{
                            updateRoom();
                        }
                    }
                }
            }
        });
    }
    private boolean CheckInput(){
        List<Room> roomList  = new ArrayList<>();
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
        for(int i = 0; i < roomList.size(); i++){
            if(et_room_name.getText().toString() == roomList.get(i).getName())
            {
                Toast.makeText(this, "会议室名称已存在", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
    private boolean isInfoEnough(){
        if(et_room_name.getText().toString().isEmpty())
        {
            Toast.makeText(this, "请输入会议室名称", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(et_room_location.getText().toString().isEmpty())
        {
            Toast.makeText(this, "请输入会议室地址", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(et_room_desc.getText().toString().isEmpty())
        {
            Toast.makeText(this, "请输入会议室描述", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void createRoom(){
        showProgressDialog("保存中...");
        String url = getString(R.string.host) + "/room/create";
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", et_room_name.getText().toString());
            jsonObject.put("location", et_room_location.getText().toString());
            jsonObject.put("desc", et_room_desc.getText().toString());
            jsonObject.put("user_id", user_id);
            HttpUtil.okHttpPostJSON(url, jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(CreateRoomActivity.this, "创建会议室失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    Log.d("zjj", "createRoom" + resp);
                    try {
                        final JSONObject jsonObject1 = new JSONObject(resp);
                        String status = jsonObject1.getString("status");
                        if (status.equals("ok")) {
                            final int room_id = jsonObject1.getInt("room_id");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    // 新建的会议返回到UserActivity meetings最前面
                                    Intent intent = new Intent();
                                    Room roomItem = new Room(room_id,et_room_name.getText().toString(),et_room_location.getText().toString(),et_room_desc.getText().toString(),0);
                                    intent.putExtra("new_room", roomItem);
                                    if(type != -1)
                                    {
                                        intent.putExtra("position", type);
                                    }
                                    setResult(RESULT_OK, intent);
                                    //finish();
                                }
                            });

                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                Toast.makeText(CreateRoomActivity.this, "创建会议室异常", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d("zjj", "createRoom exception" );
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeProgressDialog();
                    Toast.makeText(CreateRoomActivity.this, "创建会议室异常", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }
    private void updateRoom(){
        showProgressDialog("保存中...");
        String url = getString(R.string.host) + "/" + room.getRoom_id() + "/update";
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", et_room_name.getText().toString());
            jsonObject.put("location", et_room_location.getText().toString());
            jsonObject.put("desc", et_room_desc.getText().toString());
            HttpUtil.okHttpPostJSON(url, jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(CreateRoomActivity.this, "更新会议室失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    Log.d("zjj", "createRoom" + resp);
                    try {
                        final JSONObject jsonObject1 = new JSONObject(resp);
                        String status = jsonObject1.getString("status");
                        if (status.equals("ok")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    // 新建的会议返回到UserActivity meetings最前面
                                    Intent intent = new Intent();
                                    room.setName(et_room_name.getText().toString());
                                    room.setLocation(et_room_location.getText().toString());
                                    room.setDesc(et_room_desc.getText().toString());
                                    room.setType(0);
                                    intent.putExtra("new_room", room);
                                    if(type != -1)
                                    {
                                        intent.putExtra("position", type);
                                    }
                                    setResult(RESULT_OK, intent);
                                    //finish();
                                }
                            });

                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                Toast.makeText(CreateRoomActivity.this, "更新会议室异常", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d("zjj", "createRoom exception" );
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showProgressDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

