package com.example.meetinghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateMeetingActivity extends AppCompatActivity {
    private Button datePicker, stTimePicker, edTimePicker, btnSave;
    private TextView tvDate, tvStTime, tvEdTime, tvIntervals;
    private Spinner roomSpinner;
    private ProgressDialog progressDialog;
    private EditText etTopic;
    private ArrayAdapter<String> adapter;
    private List<String> roomNames = new ArrayList<>();
    private int user_id;
    private String admin;
    private List<Room> roomList = new ArrayList<>();
    private int room_idx = -1; // 会议室列表选中项的下标
    private Calendar stTimeCalendar, edTimeCalendar;
    private List<Interval> intervals = new ArrayList<>();

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        datePicker = findViewById(R.id.btn_date_picker);
        tvDate = findViewById(R.id.tv_date);
        btnSave = findViewById(R.id.save_meeting);
        stTimePicker = findViewById(R.id.btn_sttime_picker);
        edTimePicker = findViewById(R.id.btn_edtime_picker);
        tvStTime = findViewById(R.id.sttime);
        tvEdTime = findViewById(R.id.edtime);
        roomSpinner = findViewById(R.id.room_spinner);
        etTopic = findViewById(R.id.topic_input);
        tvIntervals = findViewById(R.id.intervals);

        Intent intent = getIntent();
        user_id = intent.getIntExtra("user_id", 0);
        admin = intent.getStringExtra("admin");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roomNames);
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("zjj", "selected: " + i);
                room_idx = i;
                query_time();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        stTimeCalendar = Calendar.getInstance();
        edTimeCalendar = Calendar.getInstance();
        stTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
        stTimeCalendar.set(Calendar.MINUTE, 0);
        edTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
        edTimeCalendar.set(Calendar.MINUTE, 0);
        final int year = stTimeCalendar.get(Calendar.YEAR),
                month = stTimeCalendar.get(Calendar.MONTH),
                dayOfMonth = stTimeCalendar.get(Calendar.DAY_OF_MONTH);
        tvDate.setText(convertYMD(year, month, dayOfMonth));
        tvStTime.setText(convertHM(0, 0));
        tvEdTime.setText(convertHM(0, 0));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMeeting();
            }
        });
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(CreateMeetingActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Log.d("zjj", "date picked");
                                if (stTimeCalendar.get(Calendar.YEAR) != year
                                        || stTimeCalendar.get(Calendar.MONTH) != month
                                        || stTimeCalendar.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
                                    stTimeCalendar.set(year, month, dayOfMonth);
                                    edTimeCalendar.set(year, month, dayOfMonth);
                                    tvDate.setText(convertYMD(year, month, dayOfMonth));
                                    query_time();
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
        stTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(CreateMeetingActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                stTimeCalendar.set(Calendar.HOUR_OF_DAY, i);
                                stTimeCalendar.set(Calendar.MINUTE, i1);
                                tvStTime.setText(convertHM(i, i1));
                            }
                        }, 0, 0, true);
                dialog.show();
            }
        });

        edTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(CreateMeetingActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                edTimeCalendar.set(Calendar.HOUR_OF_DAY, i);
                                edTimeCalendar.set(Calendar.MINUTE, i1);
                                tvEdTime.setText(convertHM(i, i1));
                            }
                        }, 0, 0, true);
                dialog.show();
            }
        });

        getRooms();
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

    // TODO: 补充输入检查
    private boolean checkInput() {
        if (etTopic.getText().toString().isEmpty()) {
            Toast.makeText(this, "请输入会议主题", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createMeeting() {
        if (room_idx == -1) {
            Toast.makeText(this, "无可用的会议室", Toast.LENGTH_SHORT).show();
            return;
        }
        final long sttime = stTimeCalendar.getTimeInMillis() / 1000;
        if (sttime <= new Date().getTime() / 1000) {
            Toast.makeText(this, "开始时间必须晚于现在", Toast.LENGTH_SHORT).show();
            return;
        }
        final long edtime = edTimeCalendar.getTimeInMillis() / 1000;
        if (sttime >= edtime) {
            Toast.makeText(this, "结束时间必须大于开始时间", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Interval interval : intervals) {
            if (interval.getSttime() >= edtime || interval.getEdtime() <= sttime) {
                // 不冲突
            } else {
                Toast.makeText(this, "和某时间段冲突", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final String topic = etTopic.getText().toString();
        if (topic.isEmpty()) {
            Toast.makeText(this, "请输入会议主题", Toast.LENGTH_SHORT).show();
            return;
        }
        final Room room = roomList.get(room_idx);
        int room_id = room.getRoom_id();
        showProgressDialog("保存中...");
        String url = getString(R.string.host) + "/meeting/create";
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("topic", topic);
            jsonObject.put("user_id", user_id);
            jsonObject.put("room_id", room_id);
            jsonObject.put("sttime", sttime);
            jsonObject.put("edtime", edtime);
            HttpUtil.okHttpPostJSON(url, jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(CreateMeetingActivity.this, "创建会议失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    Log.d("zjj", resp);
                    try {
                        final JSONObject jsonObject1 = new JSONObject(resp);
                        String status = jsonObject1.getString("status");
                        if (status.equals("ok")) {
                            final int meeting_id = jsonObject1.getInt("meeting_id");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    // 新建的会议返回到UserActivity meetings最前面
                                    Intent intent = new Intent();
                                    MeetingItem meetingItem = new MeetingItem(room.getName(), sttime, edtime,
                                            meeting_id, topic, room.getLocation(), room.getDesc());
                                    intent.putExtra("new_meeting", meetingItem);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void query_time() {
        showProgressDialog("获取已占用的时间段...");
        String url = getString(R.string.host) + "/query_time";
        JSONObject jsonObject = new JSONObject();
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(stTimeCalendar.get(Calendar.YEAR), stTimeCalendar.get(Calendar.MONTH),
                    stTimeCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            jsonObject.put("date", calendar.getTimeInMillis() / 1000);
            jsonObject.put("room_id", roomList.get(room_idx).getRoom_id());
            HttpUtil.okHttpPostJSON(url, jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(CreateMeetingActivity.this, "获取时间段失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    Log.d("zjj", resp);
                    try {
                        JSONObject jsonObject1 = new JSONObject(resp);
                        String status = jsonObject1.getString("status");
                        if (status.equals("ok")) {
                            String itvStr = jsonObject1.getJSONArray("intervals").toString();
                            intervals = new Gson().fromJson(itvStr, new TypeToken<List<Interval>>() {
                            }.getType());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    if (intervals.isEmpty()) {
                                        tvIntervals.setText("可任意选择任意时间段");
                                    } else {
                                        // TODO: 改成StringBuilder
                                        String info = "不要和下面时间段冲突\n";
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        for (Interval interval : intervals) {
                                            Date st = new Date(interval.getSttime() * 1000), ed = new Date(interval.getEdtime() * 1000);
                                            info += simpleDateFormat.format(st).substring(11, 16);
                                            info += "-" + simpleDateFormat.format(ed).substring(11, 16) + " ";
                                        }
                                        tvIntervals.setText(info);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getRooms() {
        String url = getString(R.string.host) + "/room/" + admin;
        showProgressDialog("获取会议室信息中...");
        HttpUtil.okHttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(CreateMeetingActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("zjj", resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    final String status = jsonObject.getString("status");
                    if (status.equals("ok")) {
                        String roomsStr = jsonObject.getJSONArray("rooms").toString();
                        roomList = new Gson().fromJson(roomsStr, new TypeToken<List<Room>>() {
                        }.getType());
                        roomNames.clear();
                        for (Room room : roomList) {
                            roomNames.add(room.getName());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
//                                adapter.notifyDataSetChanged();
                                if (roomList.isEmpty()) {
                                    Toast.makeText(CreateMeetingActivity.this, "无可用的会议室", Toast.LENGTH_SHORT).show();
                                } else {
                                    roomSpinner.setAdapter(adapter);
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                Toast.makeText(CreateMeetingActivity.this, status, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {

                }

            }
        });
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
