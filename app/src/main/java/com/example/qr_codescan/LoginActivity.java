package com.example.qr_codescan;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private EditText ip1,ip2,ip3,ip4;
    private Button access;
    private PreferenceService preferenceService;
    private Map<String, String> map;
    private static MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        preferenceService = new PreferenceService(getApplicationContext());
        //取出存储的IP地址
        map = new HashMap<>();
        map = preferenceService.getPreferences();
        ip1.setText(map.get("ip1"));
        ip2.setText(map.get("ip2"));
        ip3.setText(map.get("ip3"));
        ip4.setText(map.get("ip4"));

        access.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    application = MyApplication.getInstance();
                    application.setIp1(ip1.getText().toString());
                    application.setIp2(ip2.getText().toString());
                    application.setIp3(ip3.getText().toString());
                    application.setIp4(ip4.getText().toString());
                    preferenceService.save(ip1.getText().toString(),ip2.getText().toString(),ip3.getText().toString(),ip4.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra("ip1",ip1.getText().toString());
                intent.putExtra("ip2",ip2.getText().toString());
                intent.putExtra("ip3",ip3.getText().toString());
                intent.putExtra("ip4",ip4.getText().toString());
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });

    }

    private void init() {
        ip1 = (EditText) findViewById(R.id.ip1);
        ip2 = (EditText) findViewById(R.id.ip2);
        ip3 = (EditText) findViewById(R.id.ip3);
        ip4 = (EditText) findViewById(R.id.ip4);
        access = (Button) findViewById(R.id.access);
    }
}
