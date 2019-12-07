package com.example.meetinghelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn, signupBtn, signupAdminBtn;
    private EditText accountET, passwdET;
    private int user_id;
    private String admin;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        user_id = sp.getInt("user_id", -1);
        if (user_id != -1) {
            // 已登录
            int isadmin = sp.getInt("isadmin", 0);
            if (isadmin != 0) {
                jmpToAdmin();
            } else {
                admin = sp.getString("admin", "");
                if (!admin.isEmpty()) {
                    jmpToUser();
                } else {
                    showDialog(getString(R.string.prompt_admin));
                }
            }
        }

        loginBtn = findViewById(R.id.login);
        signupBtn = findViewById(R.id.signup_user);
        signupAdminBtn = findViewById(R.id.signup_admin);
        accountET = findViewById(R.id.account);
        passwdET = findViewById(R.id.password);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginAction();
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupAction();
            }
        });

        signupAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupAdminAction();
            }
        });
    }

    // 用来输入别人分享的管理员名字
    private void showDialog(final String title) {
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle(title)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.isEmpty()) {
                            showDialog("名字不能为空！");
                        } else {
                            updateUser(input);
                        }
                    }
                })
                .show();
    }

    private void updateUser(final String admin) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("admin", admin);
            HttpUtil.okHttpPostJSON(getString(R.string.host) + "/user/" + user_id + "/update", jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog("更新失败");
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String body = response.body().string();
                    Log.d("resp body", body);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(body);
                                String status = jsonObject.getString("status");
                                if (!status.equals("ok")) {
                                    showDialog(status);
                                } else {
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("admin", admin);
                                    editor.apply();
                                    LoginActivity.this.admin = admin;
                                    jmpToUser();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean checkInput(String account, String passwd) {
        if (account.isEmpty()) {
            Toast.makeText(this, R.string.account_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (passwd.isEmpty()) {
            Toast.makeText(this, R.string.passwd_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void signupAction() {
        final String account = accountET.getText().toString();
        String passwd = passwdET.getText().toString();
        if (!checkInput(account, passwd)) return;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("account", account);
            jsonObject.put("passwd", passwd);
            jsonObject.put("isadmin", 0);
            HttpUtil.okHttpPostJSON(getString(R.string.host) + "/signup", jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String body = response.body().string();
                    Log.d("zjj", body);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(body);
                                String status = jsonObject.getString("status");
                                if (!status.equals("ok")) {
                                    Toast.makeText(LoginActivity.this, status, Toast.LENGTH_SHORT).show();
                                } else {
                                    user_id = jsonObject.getInt("user_id");
                                    storeInfo(user_id, account, 0, "");
                                    showDialog(getString(R.string.prompt_admin));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 跳至管理员页面
    private void jmpToAdmin() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        finish();
    }

    // 跳至用户页面，传入user_id, admin参数
    private void jmpToUser() {
        UserActivity.actionStart(this, user_id, admin);
        finish();
    }

    private void signupAdminAction() {
        final String account = accountET.getText().toString();
        String passwd = passwdET.getText().toString();
        if (!checkInput(account, passwd)) return;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("account", account);
            jsonObject.put("passwd", passwd);
            jsonObject.put("isadmin", 1);
            HttpUtil.okHttpPostJSON(getString(R.string.host) + "/signup", jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String body = response.body().string();
                    Log.d("zjj", body);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(body);
                                String status = jsonObject.getString("status");
                                if (!status.equals("ok")) {
                                    Toast.makeText(LoginActivity.this, status, Toast.LENGTH_SHORT).show();
                                } else {
                                    user_id = jsonObject.getInt("user_id");
                                    storeInfo(user_id, account, 1, account);
                                    jmpToAdmin();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeInfo(int user_id, String account, int isadmin, String admin) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("user_id", user_id);
        editor.putString("account", account);
        editor.putInt("isadmin", isadmin);
        editor.putString("admin", admin);
        editor.apply();
    }

    private void loginAction() {
        String account = accountET.getText().toString();
        String passwd = passwdET.getText().toString();
        if (!checkInput(account, passwd)) return;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("account", account);
            jsonObject.put("passwd", passwd);
            HttpUtil.okHttpPostJSON(getString(R.string.host) + "/login", jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String body = response.body().string();
                    Log.d("zjj", body);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(body);
                                String status = jsonObject.getString("status");
                                if (!status.equals("ok")) {
                                    Toast.makeText(LoginActivity.this, status, Toast.LENGTH_SHORT).show();
                                } else {
                                    JSONObject user = jsonObject.getJSONObject("user");
                                    String account = user.getString("account");
                                    admin = user.getString("admin");
                                    int isadmin = user.getInt("isadmin");
                                    user_id = user.getInt("id");
                                    storeInfo(user_id, account, isadmin, admin);

                                    if (isadmin != 0) {
                                        jmpToAdmin();
                                    } else if (admin.isEmpty()) {
                                        showDialog(getString(R.string.prompt_admin));
                                    } else {
                                        jmpToUser();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
