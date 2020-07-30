package com.xuezhy.mapdrawdemo.utils;

import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.TileProvider;
import com.amap.api.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 类功能描述: 高德地图切换到有的地方没有卫星图片，此处使用谷歌地图的卫星切片替换
 * 作者:        zhongyangxue
 * 创建时间:     2019/10/16 上午12:40
 * 邮箱         1366411749@qq.com
 * 版本:        1.0
 */
public class GoogleMapUtil {
//        final static String url = "http://mt2.google.cn/vt/lyrs=y@167000000&hl=zh-CN&gl=cn&x=%d&y=%d&z=%d&s=Galil";
//    final static String url = "http://mt0.google.cn/vt/lyrs=y@198&hl=zh-CN&gl=cn&src=app&x=%d&y=%d&z=%d&s=";
    final static String url = "http://mt3.google.cn/maps/vt?lyrs=y@194&hl=zh-CN&gl=cn&x=%d&y=%d&z=%d";


    public static TileOverlayOptions getGooleMapTileOverlayOptions() {

        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            public URL getTileUrl(int x, int y, int zoom) {
                try {
                    return new URL(String.format(url, x, y, zoom));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        return new TileOverlayOptions()
                .tileProvider(tileProvider)
                .diskCacheEnabled(true)
                .diskCacheSize(50000)
                .diskCacheDir("/storage/emulated/0/amap/OMCcache")
                .memoryCacheEnabled(false)
                .memCacheSize(10000)
                .zIndex(-9999);
    }

}
