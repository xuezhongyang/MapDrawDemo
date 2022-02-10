package com.xuezhy.drawmap.utils;

import android.graphics.Color;
import android.graphics.Point;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.xuezhy.drawmap.bean.DrawCircle;
import com.xuezhy.drawmap.bean.DrawLatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述:
 * 作者:        zhongyangxue
 * 创建时间:     2019/11/10 下午2:42
 * 邮箱         1366411749@qq.com
 * 版本:        1.0
 */
public class DrawMapUtil {

    public static float getScreenDistance(AMap aMap, LatLng latLng1, LatLng latLng2) {
        Point point1 = aMap.getProjection().toScreenLocation(latLng1);
        Point point2 = aMap.getProjection().toScreenLocation(latLng2);
        float fLen = (float) Math.sqrt(Math.pow(point1.x - point2.x, 2)
                + Math.pow(point1.y - point2.y, 2));
        return fLen;
    }

    public static void drawBigCircle(AMap aMap, DrawLatLng latLng) {
        Circle circle = aMap.addCircle(new CircleOptions()
                .center(latLng.getLatLng())
                .radius(5)
                .fillColor(Color.argb(255, 255, 255, 255))
                .strokeColor(Color.argb(255, 255, 255, 255))
                .strokeWidth(0).zIndex(999));
    }

    public static void drawSmallCircle(AMap aMap, LatLng latLng1, LatLng latLng2) {
        Point point1 = aMap.getProjection().toScreenLocation(latLng1);
        Point point2 = aMap.getProjection().toScreenLocation(latLng2);
        Point centerPoint = new Point(avg(point1.x, point2.x),
               avg(point1.y, point2.y));

        LatLng centerLatLng = aMap.getProjection().fromScreenLocation(centerPoint);
        Circle circle = aMap.addCircle(new CircleOptions()
                .center(centerLatLng)
                .radius(3)
                .fillColor(Color.argb(255, 255, 255, 255))
                .strokeColor(Color.argb(255, 255, 255, 255))
                .strokeWidth(0).zIndex(999));
    }

    public static void drawBigCircle2(AMap aMap, ArrayList<DrawCircle> circleList, DrawLatLng latLng, int position) {
        Circle circle = aMap.addCircle(new CircleOptions()
                .center(latLng.getLatLng())
                .radius(5)
                .fillColor(Color.argb(255, 255, 255, 255))
                .strokeColor(Color.argb(255, 255, 255, 255))
                .strokeWidth(0).zIndex(999));
        circleList.add(position, new DrawCircle(circle, true));
    }


    public static void drawSmallCircle2(AMap aMap, ArrayList<DrawCircle> circleList, DrawLatLng drawLatLng, ArrayList<DrawLatLng> points, int position) {
        Circle circle = aMap.addCircle(new CircleOptions()
                .center(drawLatLng.getLatLng())
                .radius(3)
                .fillColor(Color.argb(255, 255, 255, 255))
                .strokeColor(Color.argb(255, 255, 255, 255))
                .strokeWidth(0).zIndex(999));
        circleList.add(position, new DrawCircle(circle, false));
        points.add(position, new DrawLatLng(drawLatLng.getLatLng(), false));
    }

    public static void drawLine(AMap aMap, LatLng latLng1, LatLng latLng2, List<Polyline> polylineList) {
        Polyline polyline = aMap.addPolyline(new PolylineOptions().
                add(latLng1, latLng2).width(5).color(Color.argb(255, 255, 255, 255)).zIndex(999));
        polylineList.add(polyline);
    }

    public static ArrayList<LatLng> drawLatlngToLatlng(List<DrawLatLng> drawLatLngs) {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (int i = 0; i < drawLatLngs.size(); i++) {
            latLngs.add(drawLatLngs.get(i).getLatLng());
        }
        return latLngs;
    }



    public static Boolean checkDraw(List<DrawLatLng> latLngs, AMap aMap) {
        Boolean flag = false;
        try {

            for (int i = 0; i < latLngs.size() - 2; i++) {
                Point point1, point2, point3, point4;

                point1 = aMap.getProjection().toScreenLocation(latLngs.get(i).getLatLng());
                point2 = aMap.getProjection().toScreenLocation(latLngs.get(i + 1).getLatLng());

                for (int j = i + 2; j < latLngs.size(); j++) {

                    if (i == 0 && j == latLngs.size() - 1) {
                        break;

                    } else if (j == latLngs.size() - 1) {
                        point3 = aMap.getProjection().toScreenLocation(latLngs.get(j).getLatLng());
                        point4 = aMap.getProjection().toScreenLocation(latLngs.get(0).getLatLng());
                        flag = intersection(point1, point2, point3, point4);

                    } else {
                        point3 = aMap.getProjection().toScreenLocation(latLngs.get(j).getLatLng());
                        point4 = aMap.getProjection().toScreenLocation(latLngs.get(j + 1).getLatLng());
                        flag = intersection(point1, point2, point3, point4);
                    }

                    if (flag) {
                        return flag;
                    }
                }
            }

        } catch (Exception e) {
        }


        return flag;
    }


    public static boolean intersection(Point point1, Point point2, Point point3, Point point4) {
        double l1x1 = point1.x;
        double l1y1 = point1.y;
        double l1x2 = point2.x;
        double l1y2 = point2.y;
        double l2x1 = point3.x;
        double l2y1 = point3.y;
        double l2x2 = point4.x;
        double l2y2 = point4.y;


        // 快速排斥实验 首先判断两条线段在 x 以及 y 坐标的投影是否有重合。 有一个为真，则代表两线段必不可交。
        if (Math.max(l1x1, l1x2) < Math.min(l2x1, l2x2)
                || Math.max(l1y1, l1y2) < Math.min(l2y1, l2y2)
                || Math.max(l2x1, l2x2) < Math.min(l1x1, l1x2)
                || Math.max(l2y1, l2y2) < Math.min(l1y1, l1y2)) {
            return false;
        }
        // 跨立实验  如果相交则矢量叉积异号或为零，大于零则不相交
        if ((((l1x1 - l2x1) * (l2y2 - l2y1) - (l1y1 - l2y1) * (l2x2 - l2x1))
                * ((l1x2 - l2x1) * (l2y2 - l2y1) - (l1y2 - l2y1) * (l2x2 - l2x1))) > 0
                || (((l2x1 - l1x1) * (l1y2 - l1y1) - (l2y1 - l1y1) * (l1x2 - l1x1))
                * ((l2x2 - l1x1) * (l1y2 - l1y1) - (l2y2 - l1y1) * (l1x2 - l1x1))) > 0) {
            return false;
        }
        return true;
    }

    /**
     * 计算平均值
     *
     * @param a
     * @param b
     * @return
     */
    public static int avg(int a, int b) {

        return ((a & b) + ((a ^ b) >> 1));

    }
}
