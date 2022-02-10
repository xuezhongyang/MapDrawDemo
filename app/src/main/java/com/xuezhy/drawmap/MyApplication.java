package com.xuezhy.drawmap;

import android.app.Application;
import com.orhanobut.hawk.Hawk;

public class MyApplication extends Application {
    private static MyApplication mApplication;

    public static MyApplication getInstance() {
        return mApplication;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        Hawk.init(getApplicationContext()).build();
    }

}
