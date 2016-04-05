package com.example.qr_codescan;

import android.app.Application;

/**
 * Created by huqiang on 2016/1/24 16:44.
 */
public class MyApplication extends Application {
    private String ip1;
    private String ip2;
    private String ip3;
    private String ip4;
    private static MyApplication instance;
    public static MyApplication getInstance(){
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public String getIp1() {
        return ip1;
    }

    public void setIp1(String ip1) {
        this.ip1 = ip1;
    }

    public String getIp2() {
        return ip2;
    }

    public void setIp2(String ip2) {
        this.ip2 = ip2;
    }

    public String getIp3() {
        return ip3;
    }

    public void setIp3(String ip3) {
        this.ip3 = ip3;
    }

    public String getIp4() {
        return ip4;
    }

    public void setIp4(String ip4) {
        this.ip4 = ip4;
    }
}
