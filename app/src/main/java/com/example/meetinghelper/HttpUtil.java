package com.example.meetinghelper;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    public static void okHttpGet(String url, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(url).build();
        client.newCall(req).enqueue(callback);
    }

    public static void okHttpPostJSON(String url, String jsonStr, okhttp3.Callback callback) {
        Log.d("zjj", "post json: " + url + ": " + jsonStr);
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
//        String jsonStr = "{\"username\":\"lisi\",\"nickname\":\"李四\"}";//json数据.
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}

//HttpUtil.sendOkHttpGet(dailyUrl, new okhttp3.Callback() {
//@Override
//public void onResponse(Call call, Response response) throws IOException {
//final String resp = response.body().string();
//final DailyResultBean dailyRes = HttpUtil.handleDailyResp(resp);
//        runOnUiThread(new Runnable() {
//@Override
//public void run() {
//        if(dailyRes != null && dailyRes.getStatus().equals("ok")) {
//        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
//        editor.putString("dailyWeather", today + weatherId + " " + resp);
//        editor.apply();
//        showDaily(dailyRes);
//        } else {
//        Toast.makeText(MainActivity.this, "获取当天天气失败", Toast.LENGTH_SHORT).show();
//        }
//        }
//        });
//        }
//
//@Override
//public void onFailure(Call call, IOException e) {
//        e.printStackTrace();
//        Toast.makeText(MainActivity.this, "获取当天天气失败", Toast.LENGTH_SHORT).show();
//        }
//        });
//        }