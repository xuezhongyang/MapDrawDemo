package com.xuezhy.mapdrawdemo.utils;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.xuezhy.mapdrawdemo.EventCode;
import com.xuezhy.mapdrawdemo.bean.LatlngBean;
import com.xuezhy.mapdrawdemo.bean.LocationEvent;
import org.greenrobot.eventbus.EventBus;

import static android.content.Context.LOCATION_SERVICE;

/**
 * 类功能描述:
 * 作者:        zhongyangxue
 * 创建时间:     2020-03-30 14:18
 * 邮箱         1366411749@qq.com
 * 版本:        1.0
 */
public class GpsManager {
    private AMapLocationClient locationClient = null;
    private LocationManager mLocationManager;
    private static final GpsManager checkGpsManager = new GpsManager();

    private GpsManager() {
    }


    public static GpsManager getInstance() {
        return checkGpsManager;
    }

    public void init(Activity context) {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        }
        if (locationClient == null) {
            //初始化client
            locationClient = new AMapLocationClient(context);
            //设置定位参数
            locationClient.setLocationOption(getDefaultOption());
            // 设置定位监听
            locationClient.setLocationListener(locationListener);
            // 启动定位
//            locationClient.startLocation();
        }
    }

    public void startLocation() {
        if (locationClient != null) {
            //先暂停
            locationClient.stopLocation();
            // 启动定位
            locationClient.startLocation();
        }
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30 * 000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(50 * 1000);//可选，设置定位间隔。
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setMockEnable(false); ////设置是否允许模拟位置,默认为false，不允许模拟位置
        return mOption;
    }

    /**
     * 判断是否打开定位
     *
     * @return
     */
    public Boolean checkGps() {
        //判断gps是否打开
        return mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 跳转到设置页面
     *
     * @param context
     * @param GPS_REQUEST_CODE
     */
    public void goToSetting(Activity context, int GPS_REQUEST_CODE) {
        PopWindowUtil.buildNoTitleEnsureDialog(context, "请在设置中打开定位", "取消", "去设置", new PopWindowUtil.EnsureListener() {
            @Override
            public void sure(Object obj) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivityForResult(intent, GPS_REQUEST_CODE);
            }

            @Override
            public void cancel() {

            }
        });

    }


    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null == loc) {
                return;
            }
            try {
                if (loc.getLatitude() != 0 &&loc.getLongitude() != 0) {
                    EventBus.getDefault().post(new LocationEvent(EventCode.LOCATION_CODE,new LatlngBean(loc.getLatitude(),loc.getLongitude())));
                }

            } catch (Exception e) {

            }

        }
    };


}
