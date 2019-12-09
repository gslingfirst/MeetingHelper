package com.example.meetinghelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserActivity extends AppCompatActivity {
    private int user_id;
    private String admin;
    private List<MeetingItem> meetingList = new ArrayList<>();
    private MeetingItemAdapter adapter;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;

    public static void actionStart(Context context, int user_id, String admin) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("admin", admin);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent intent = getIntent();
        user_id = intent.getIntExtra("user_id", 0);
        admin = intent.getStringExtra("admin");
        Log.d("zjj", user_id + "-"+ admin);

        recyclerView = findViewById(R.id.meetings);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        FloatingActionButton fab = findViewById(R.id.add_meeting);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserActivity.this, CreateMeetingActivity.class);
Log.d("zjj", user_id + "-"+ admin);
                intent.putExtra("user_id", user_id);
                intent.putExtra("admin", admin);
                startActivityForResult(intent, 1);
            }
        });

        getMeetings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    // 创建了一个新会议
                    Log.d("zjj", "get a new meeting");
                    MeetingItem meetingItem = (MeetingItem) data.getSerializableExtra("new_meeting");
                    meetingList.add(0, meetingItem);
                    adapter.notifyItemInserted(0);
                }
                break;
                default:
        }
    }

    void getMeetings() {
        showProgressDialog();
        String url = getString(R.string.host) + "/meeting/" + user_id;
        HttpUtil.okHttpGet(url, new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(UserActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("zjj", resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String meetingsStr = jsonObject.getJSONArray("meetings").toString();
                    meetingList = new Gson().fromJson(meetingsStr,
                            new TypeToken<List<MeetingItem>>(){}.getType());
                    Log.d("zjj", "" + meetingList.size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            adapter = new MeetingItemAdapter(meetingList);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
                    Log.d("zjj", "false");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.share:
                // 获取系统剪贴板
                Log.d("zjj", "admin is: " + admin);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
                ClipData clipData = ClipData.newPlainText(null, admin);

                // 把数据集设置（复制）到剪贴板
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(this, "配置已复制到剪切板", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
