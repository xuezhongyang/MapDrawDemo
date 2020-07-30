package com.xuezhy.mapdrawdemo.bean;


import com.amap.api.maps.model.LatLng;

/**
 * 类功能描述:
 * 作者:        zhongyangxue
 * 创建时间:     2019/10/5 下午1:33
 * 邮箱         1366411749@qq.com
 * 版本:        1.0
 */
public class DrawLatLng {
    private LatLng latLng;  //经纬度
    private Boolean isBigCircle; //是否是原始采集点

    public DrawLatLng(LatLng latLng, Boolean isBigCircle) {
        this.latLng = latLng;
        this.isBigCircle = isBigCircle;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Boolean getBigCircle() {
        return isBigCircle;
    }

    public void setBigCircle(Boolean bigCircle) {
        isBigCircle = bigCircle;
    }

}
