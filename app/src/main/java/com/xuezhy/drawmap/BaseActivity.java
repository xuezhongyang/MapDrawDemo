package com.xuezhy.drawmap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import org.greenrobot.eventbus.EventBus;

public abstract class BaseActivity extends AppCompatActivity {
    //是否注册eventBus
    public Boolean isRegisterEventBus = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (onBindLayout() != -1) {
            setContentView(onBindLayout());
        }
        isRegisterEventBus = (isRegisterEventBus() == null ? false : isRegisterEventBus());
        if (isRegisterEventBus) {
            EventBus.getDefault().register(this);
        }
        initView(savedInstanceState);
        initData();
    }


    public abstract int onBindLayout();

    public abstract void initView(Bundle savedInstanceState);

    public abstract void initData();

    protected abstract Boolean isRegisterEventBus();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegisterEventBus) {
            EventBus.getDefault().unregister(this);
        }
    }
}
