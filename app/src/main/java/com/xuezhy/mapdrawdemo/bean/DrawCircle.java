package com.xuezhy.mapdrawdemo.bean;

import com.amap.api.maps.model.Circle;

/**
 * 类功能描述:
 * 作者:        zhongyangxue
 * 创建时间:     2019/11/10 下午6:11
 * 邮箱         1366411749@qq.com
 * 版本:        1.0
 */
public class DrawCircle {
    private Circle circle;
    private Boolean isBigCircle;

    public DrawCircle(Circle circle, Boolean isBigCircle) {
        this.circle = circle;
        this.isBigCircle = isBigCircle;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public Boolean getBigCircle() {
        return isBigCircle;
    }

    public void setBigCircle(Boolean bigCircle) {
        isBigCircle = bigCircle;
    }
}
