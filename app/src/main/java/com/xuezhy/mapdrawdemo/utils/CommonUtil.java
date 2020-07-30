package com.xuezhy.mapdrawdemo.utils;

import com.amap.api.maps.model.LatLng;
import com.xuezhy.mapdrawdemo.bean.DrawLatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CommonUtil {
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

    /**
     * 获取不规则多边形重心点
     *
     * @param mPoints
     * @return
     */
    public static LatLng getCenterOfDrawPoint(List<DrawLatLng> mPoints) {
        double area = 0.0;//多边形面积
        double Gx = 0.0, Gy = 0.0;// 重心的x、y
        for (int i = 1; i <= mPoints.size(); i++) {
            double iLat = mPoints.get(i % mPoints.size()).getLatLng().latitude;
            double iLng = mPoints.get(i % mPoints.size()).getLatLng().longitude;
            double nextLat = mPoints.get(i - 1).getLatLng().latitude;
            double nextLng = mPoints.get(i - 1).getLatLng().longitude;
            double temp = (iLat * nextLng - iLng * nextLat) / 2.0;
            area += temp;
            Gx += temp * (iLat + nextLat) / 3.0;
            Gy += temp * (iLng + nextLng) / 3.0;
        }
        Gx = Gx / area;
        Gy = Gy / area;
        return new LatLng(Gx, Gy);
    }



    public static int[] getKeyOfMinValue(Map<Integer, Integer> map) {
        int[] arr = new int[2];//设置一个 长度为2的数组 用作记录 规定第一个元素存储角标 第二个元素存储值


        List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                //升序排列
                return (o1.getValue() - o2.getValue());

                //降序排列

                //return (o1.getValue() - o2.getValue());

            }
        });

        arr[0] = list.get(0).getKey();
        arr[1] = list.get(0).getValue();
        return arr;
    }
}
