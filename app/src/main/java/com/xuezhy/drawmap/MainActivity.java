package com.xuezhy.drawmap;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TileOverlayOptions;
import com.orhanobut.hawk.Hawk;
import com.xuezhy.drawmap.bean.LocationEvent;
import com.xuezhy.drawmap.bean.DrawLatLng;
import com.xuezhy.drawmap.bean.LatlngBean;
import com.xuezhy.drawmap.utils.CommonUtil;
import com.xuezhy.drawmap.utils.DensityUtil;
import com.xuezhy.drawmap.utils.DrawMapUtil;
import com.xuezhy.drawmap.utils.GoogleMapUtil;
import com.xuezhy.drawmap.utils.GpsManager;
import com.xuezhy.drawmap.utils.StatusBarUtil;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends BaseActivity implements AMap.OnMapClickListener, AMap.OnMarkerClickListener, View.OnClickListener {
    public double latitude = 39.90613850442552;  //默认北京
    public double longitude = 116.40717000000001; //默认北京
    private TileOverlayOptions options;
    private Boolean hasDrawFinish = false; //是否绘制完毕
    private List<DrawLatLng> points = new ArrayList<>();
    private ArrayList<Marker> circleList = new ArrayList<>();
    private int CIRCLE_DEFAULT;
    private Polyline polyline;
    private Polygon polygon;
    private int movePosition = -1;
    private int signMarkPosition = -1;
    private Marker signMark;
    private Boolean canClickMap = false;
    private MapView mMapView = null;
    private Boolean isDrawPress = false;
    private Map<Integer, Integer> distanceMap = new HashMap<>();
    private Boolean pressCircle_isBig = false;
    private Boolean moveSign_center = false;
    private Boolean moveSign_right = false;
    private Boolean isFirstPoint = false;
    private Boolean isLastPoint = false;
    private int down_x = 0;
    private int down_y = 0;
    private int move_x = 0;
    private int move_y = 0;

    private AMap aMap;
    private ImageView ivCancel;
    private ImageView ivDelete;
    private ImageView ivPre;
    private ImageView ivNext;

    @Override
    public int onBindLayout() {
        return R.layout.act_main;
    }


    @Override
    public void initView(Bundle savedInstanceState) {
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //tools
        ivCancel = findViewById(R.id.iv_cancel);
        ivDelete = findViewById(R.id.iv_delete);
        ivPre = findViewById(R.id.iv_pre);
        ivNext = findViewById(R.id.iv_next);
        ivNext.setOnClickListener(this);
        ivPre.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivCancel.setOnClickListener(this);


        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);//Logo位置(地图右下角)
        aMap.getUiSettings().setRotateGesturesEnabled(false);//关闭旋转手势
        aMap.getUiSettings().setTiltGesturesEnabled(false);//关闭倾斜手势
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        //隐藏高德地图默认的放大缩小控件
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        MyLocationStyle style = new MyLocationStyle();
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.map_positioning);
        style.myLocationIcon(bitmapDescriptor);
        style.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        style.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
//        style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        style.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        aMap.setMyLocationStyle(style);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
        options = GoogleMapUtil.getGooleMapTileOverlayOptions(); //有的地区没有图层切片，可以调用谷歌接口获取切片数据（任意地方都有数据）
        aMap.addTileOverlay(options);
        aMap.setLoadOfflineData(true);
        aMap.setOnMarkerClickListener(this);

        if (Hawk.get("latitude") != null && Hawk.get("longitude") != null) {
            //说明没有定位过，定位到
            latitude = Hawk.get("latitude");
            longitude = Hawk.get("longitude");
        }
        LatLng latLng = new LatLng(latitude, longitude);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

    }

    @Override
    public void initData() {
        canClickMap = true;
        CIRCLE_DEFAULT = DensityUtil.dip2px(this, 20.0f);
        try {
            GpsManager.getInstance().init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //通过 检查定位又没有打开
        if (GpsManager.getInstance().checkGps()) {
            //开始定位
            GpsManager.getInstance().startLocation();
        }
    }

    @Override
    protected Boolean isRegisterEventBus() {
        //如果返回true 必须重写@Subscribe(threadMode = ThreadMode.MAIN) 如果返回false则不需要做任何事
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LocationEvent event) {
        if (event.getCode() == EventCode.LOCATION_CODE && event.getData() instanceof LatlngBean) {
            LatlngBean latLng = (LatlngBean) event.getData();
            try {
                if (latLng == null) {
                    return;
                }
                if (Hawk.get("latitude") == null || Hawk.get("longitude") == null) {
                    //第一次
                    latitude = latLng.getLatitude();
                    longitude = latLng.getLongitude();
                } else {
                    //判断上次经纬度和这次的经纬度相差多少米 超过界限就移动视图
                    double distance = (int) AMapUtils.calculateLineDistance(
                            new LatLng(Hawk.get("latitude", 0), Hawk.get("longitude", 0)),
                            new LatLng(latLng.getLatitude(), latLng.getLongitude()));
                    Log.e("xuezhy", "distance=" + distance);
                    if (distance < 1000) {
                        return;
                    }
                }

                LatLng newLatlng = new LatLng(latitude, longitude);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatlng, 18));
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatlng, 18));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_cancel) {
            if (hasDrawFinish) {
                //已完成
                circleList.get(circleList.size() - 1).remove();
                circleList.remove(circleList.size() - 1);
                points.remove(points.size() - 1);
                hasDrawFinish = false;
            } else {
                //还没完成
                if (circleList.size() > 0) {
                    //删除最后一个
                    circleList.get(circleList.size() - 1).remove();
                    circleList.remove(circleList.size() - 1);
                    points.remove(points.size() - 1);

                    if (circleList.size() > 1) {
                        //删除倒数第二个
                        circleList.get(circleList.size() - 1).remove();
                        circleList.remove(circleList.size() - 1);
                        points.remove(points.size() - 1);
                    }
                }

            }
            if (signMark != null) {
                signMark.remove();
                signMarkPosition = -1;
            }
            drawLine();
        } else if (id == R.id.iv_delete) {
            for (int i = 0; i < circleList.size(); i++) {
                circleList.get(i).remove();
            }
            circleList.clear();
            points.clear();
            if (polygon != null) {
                polygon.remove();
            }
            if (polyline != null) {
                polyline.remove();
            }

            if (signMark != null) {
                signMark.remove();
                movePosition = -1;
                signMarkPosition = -1;
            }

            hasDrawFinish = false;
        } else if (id == R.id.iv_pre) {
            if (signMarkPosition == -1) {
                return;
            }
            if (signMarkPosition == 0) {
                DrawLatLng drawLatLng = points.get(points.size() - 1);
                signMarkPosition = points.size() - 1;
                drawSignMark(drawLatLng.getLatLng());
            } else {
                DrawLatLng drawLatLng = points.get(signMarkPosition - 1);
                signMarkPosition = signMarkPosition - 1;
                drawSignMark(drawLatLng.getLatLng());
            }
        } else if (id == R.id.iv_next) {
            if (signMarkPosition == -1) {
                return;
            }

            if (signMarkPosition == points.size() - 1) {
                DrawLatLng drawLatLng = points.get(0);
                signMarkPosition = 0;
                drawSignMark(drawLatLng.getLatLng());
            } else {
                DrawLatLng drawLatLng = points.get(signMarkPosition + 1);
                signMarkPosition = signMarkPosition + 1;
                drawSignMark(drawLatLng.getLatLng());
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onMapClick(LatLng latLng) {
        if (aMap == null || isDrawPress || !canClickMap) {
            return;
        }

        //如果没有
        if (!hasDrawFinish) {
            if (points.size() == 0) {
                //画个大圆圈
                DrawLatLng myLatlng = new DrawLatLng(latLng, true);
                circleList.add(drawBigCircle(latLng, 0));
                points.add(myLatlng);
//                upDateLandInfo();
                return;
            }
            //判断是否点击页面的点
            for (int i = 0; i < points.size(); i++) {
                double distance = DrawMapUtil.getScreenDistance(aMap, points.get(i).getLatLng(), latLng);
//                double distance =  (int) AMapUtils.calculateLineDistance(points.get(i).getLatLng(), latLng);
                if (distance < CIRCLE_DEFAULT) {
                    if (points.size() > 1) {
                        DrawLatLng drawLatLng = points.get(i);
                        //判断该点是不是小点

                        if (!drawLatLng.getBigCircle()) {

                            drawOneBigAndTwoSmallCirclr(i, drawLatLng.getLatLng());
                            return;
                        }
                    }

                    return;
                }

            }

            //先画小圈圈
            LatLng centerLatLng = getCenterLatlng(points.get(points.size() - 1).getLatLng(), latLng);
            circleList.add(drawSmallCircle(centerLatLng, circleList.size()));

            //再画大圈圈

            circleList.add(drawBigCircle(latLng, circleList.size()));
            points.add(new DrawLatLng(centerLatLng, false));
            points.add(new DrawLatLng(latLng, true));

            drawLine();
            return;

        }

        //判断是否点击页面的点
        for (int i = 0; i < points.size(); i++) {
            double distance = DrawMapUtil.getScreenDistance(aMap, points.get(i).getLatLng(), latLng);
//            double distance =  (int) AMapUtils.calculateLineDistance(points.get(i).getLatLng(), latLng);
            if (distance < CIRCLE_DEFAULT) {
                DrawLatLng drawLatLng = points.get(i);
                //选中了该点
                //判断是大圆圈还是小圆圈
                if (!points.get(i).getBigCircle()) {
                    drawOneBigAndTwoSmallCirclr(i, drawLatLng.getLatLng());
                    return;
                }

                /******选中标签****************/
                signMarkPosition = i;
                drawSignMark(points.get(i).getLatLng());

                return;
            }


        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!canClickMap) {
            return false;
        }
        if (points.size() <= 2) {
            return false;
        }
        if (!hasDrawFinish && marker.getPosition().latitude == points.get(0).getLatLng().latitude
                && marker.getPosition().longitude == points.get(0).getLatLng().longitude) {
            //画小圈圈
            LatLng centerLatLng_last = getCenterLatlng(points.get(points.size() - 1).getLatLng(), points.get(0).getLatLng());
            circleList.add(drawSmallCircle(centerLatLng_last, circleList.size()));
            points.add(new DrawLatLng(centerLatLng_last, false));
            drawPolygon();
            hasDrawFinish = true;
        }
        String title = marker.getTitle();
        if (!TextUtils.isEmpty(title)) {
            int position = Integer.valueOf(title);
            signMarkPosition = position;
            drawSignMark(points.get(position).getLatLng());
        }

        return true;
    }


    /**
     * 监听手势滑动
     *
     * @param ev
     * @return
     */

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {

        if (points.size() <= 2 || !canClickMap) {
            return super.dispatchTouchEvent(ev);
        }
        try {
            if (MotionEvent.ACTION_DOWN == ev.getAction()) {
                down_x = (int) ev.getX();
                down_y = (int) ev.getY();
                move_x = (int) ev.getX();
                move_y = (int) ev.getY();
            } else if (MotionEvent.ACTION_MOVE == ev.getAction()) {
                if (isDrawPress) {
                    handlePressEvent(ev);
                } else {
                    move_x = (int) ev.getX();
                    move_y = (int) ev.getY();
                    double distance = Math.sqrt(Math.pow(down_x - move_x, 2)
                            + Math.pow(down_y - move_y, 2));
                    if (distance > DensityUtil.dip2px(this, 10.0f)) {
                        isDrawPress = true;
                        handleDownTouchEvent(ev);
                    }
                }

            } else if (MotionEvent.ACTION_UP == ev.getAction()) {
                handleMoveTouchEvent();
            }
        } catch (Exception e) {

        }

        return super.dispatchTouchEvent(ev);

    }


    /**************************************************************绘制**************************************************************/

    /**
     * 画大圆圈
     */
    private Marker drawBigCircle(LatLng latLng, int position) {
        return aMap.addMarker(new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .title(String.valueOf(position))
                .icon(BitmapDescriptorFactory.fromView(getLayoutInflater().inflate(R.layout.map_draw_marker_big_white, null))));
    }

    /**
     * 画小圆圈
     */
    private Marker drawSmallCircle(LatLng latLng, int position) {
        return aMap.addMarker(new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .title(String.valueOf(position))
                .icon(BitmapDescriptorFactory.fromView(getLayoutInflater().inflate(R.layout.map_draw_marker_small_white, null))));
    }

    /**
     * 获取两个经纬度的中点经纬度
     *
     * @param latLng1
     * @param latLng2
     * @return
     */
    private LatLng getCenterLatlng(LatLng latLng1, LatLng latLng2) {
        Point point1 = aMap.getProjection().toScreenLocation(latLng1);
        Point point2 = aMap.getProjection().toScreenLocation(latLng2);
        Point centerPoint = new Point(CommonUtil.avg(point1.x, point2.x),
                CommonUtil.avg(point1.y, point2.y));
        return aMap.getProjection().fromScreenLocation(centerPoint);
    }

    /**
     * 绘制线，把点连起来
     */
    protected void drawLine() {

        if (polyline != null) {
            polyline.remove();
        }

        if (polygon != null) {
            polygon.remove();
        }

        if (aMap != null && points.size() > 0) {
            polyline = aMap.addPolyline(new PolylineOptions().
                    addAll(DrawMapUtil.drawLatlngToLatlng(points)).width(2).color(getResources().getColor(R.color.green)).zIndex(999));
        }
    }

    private void drawOneBigAndTwoSmallCirclr(int i, LatLng latLng) {
        //再判断该点是不是最后一个点
        if (i == points.size() - 1) {
            /******左边圆圈****************/
            LatLng centerLatLng_left = getCenterLatlng(points.get(i - 1).getLatLng(), points.get(i).getLatLng());
            circleList.add(i, drawSmallCircle(centerLatLng_left, i));
            points.add(i, new DrawLatLng(centerLatLng_left, false));

            /******大圆圈****************/
            circleList.get(i + 1).remove();
            circleList.remove(i + 1);
            points.remove(i + 1);
            circleList.add(i + 1, drawBigCircle(latLng, i + 1));
            points.add(i + 1, new DrawLatLng(latLng, true));

            /******选中标签****************/
            signMarkPosition = movePosition;
            drawSignMark(latLng);

            /******右边圆圈****************/
            LatLng centerLatLng_right = getCenterLatlng(points.get(i + 1).getLatLng(), points.get(0).getLatLng());
            circleList.add(drawSmallCircle(centerLatLng_right, circleList.size()));
            points.add(new DrawLatLng(centerLatLng_right, false));
            return;
        }

        /******左边圆圈****************/
        LatLng centerLatLng_left = getCenterLatlng(points.get(i - 1).getLatLng(), points.get(i).getLatLng());
        circleList.add(i, drawSmallCircle(centerLatLng_left, i));
        points.add(i, new DrawLatLng(centerLatLng_left, false));

        /******大圆圈****************/
        circleList.get(i + 1).remove();
        circleList.remove(i + 1);
        points.remove(i + 1);
        circleList.add(i + 1, drawBigCircle(latLng, i + 1));
        points.add(i + 1, new DrawLatLng(latLng, true));

        /******选中标签****************/
        signMarkPosition = movePosition;
        drawSignMark(latLng);

        /******右边圆圈****************/
        LatLng centerLatLng_right = getCenterLatlng(points.get(i + 1).getLatLng(), points.get(i + 2).getLatLng());
        circleList.add(i + 2, drawSmallCircle(centerLatLng_right, i + 2));
        points.add(i + 2, new DrawLatLng(centerLatLng_right, false));
    }


    /**
     * @param latLng
     */
    private void drawSignMark(LatLng latLng) {
        LatLng centerLatlng = CommonUtil.getCenterOfDrawPoint(points);
        View view = getLayoutInflater().inflate(R.layout.map_draw_marker, null);
        /******选中标签****************/
        if (signMark != null) {
            signMark.remove();
        }

        signMark = aMap.addMarker(new MarkerOptions()
                .position(latLng).icon(BitmapDescriptorFactory.fromView(view)));
        //判断方向
        if (latLng.latitude > centerLatlng.latitude && latLng.longitude < centerLatlng.longitude) {
            //第一象限
            signMark.setRotateAngle(45);

        } else if (latLng.latitude < centerLatlng.latitude && latLng.longitude < centerLatlng.longitude) {
            //第二象限
            signMark.setRotateAngle(135);
        } else if (latLng.latitude < centerLatlng.latitude && latLng.longitude > centerLatlng.longitude) {
            //第三象限
            signMark.setRotateAngle(225);
        } else if (latLng.latitude > centerLatlng.latitude && latLng.longitude > centerLatlng.longitude) {
            //第四象限
            signMark.setRotateAngle(-45);
        }

    }

    /**
     * 绘制多边形
     */
    private void drawPolygon() {

        if (polyline != null) {
            polyline.remove();
        }

        if (polygon != null) {
            polygon.remove();
        }

        // 声明 多边形参数对象
        PolygonOptions polygonOptions = new PolygonOptions();
        // 添加 多边形的每个顶点（顺序添加）
        for (int i = 0; i < points.size(); i++) {
            polygonOptions.add(points.get(i).getLatLng());
        }

        Boolean hasOverlap = DrawMapUtil.checkDraw(points, aMap);
        if (hasOverlap) {
            //说明重合了
            polygonOptions.strokeWidth(2) // 多边形的边框
                    .strokeColor(getResources().getColor(R.color.white)) // 边框颜色
                    .fillColor(getResources().getColor(R.color.half_red));   // 多边形的填充色
        } else {
            polygonOptions.strokeWidth(2) // 多边形的边框
                    .strokeColor(getResources().getColor(R.color.white)) // 边框颜色
                    .fillColor(getResources().getColor(R.color.black60));   // 多边形的填充色

        }

        polygon = aMap.addPolygon(polygonOptions.zIndex(100));

//        if (!hasOverlap) {
//            upDateLandInfo();
//        }

    }


    /**************************************************************处理滑动事件**************************************************************/

    /**
     * 滑动事件
     *
     * @param ev
     */
    private void handlePressEvent(MotionEvent ev) {
        if (movePosition == -1) {
            return;
        }
        if (hasDrawFinish) {
            handleFinishPress(ev);
        } else {
            handleCommonPress(ev);

        }

    }

    private void handleDownTouchEvent(MotionEvent ev) {
        distanceMap.clear();
        for (int i = 0; i < points.size(); i++) {
            Point point = aMap.getProjection().toScreenLocation(points.get(i).getLatLng());
            int distance = (int) Math.sqrt(Math.pow(ev.getX() - point.x, 2)
                    + Math.pow(ev.getY() - point.y, 2));
            distanceMap.put(i, distance);
        }

        int[] arr = CommonUtil.getKeyOfMinValue(distanceMap);
        if (arr != null && arr[1] < CIRCLE_DEFAULT) {
            isDrawPress = true;
            aMap.getUiSettings().setAllGesturesEnabled(false);
            //说明选中该点
            if (signMark != null) {
                signMark.remove();
            }

            movePosition = arr[0];
            pressCircle_isBig = points.get(movePosition).getBigCircle();
        }

    }

    /**
     * 滑动结束
     */
    private void handleMoveTouchEvent() {
        isDrawPress = false;
        pressCircle_isBig = false;
        movePosition = -1;
        moveSign_center = false;
        moveSign_right = false;
        isFirstPoint = false;
        isLastPoint = false;
        aMap.getUiSettings().setAllGesturesEnabled(true);
        aMap.getUiSettings().setRotateGesturesEnabled(false);//关闭旋转手势
        aMap.getUiSettings().setTiltGesturesEnabled(false);//关闭倾斜手势
    }

    /**
     * 没有闭合
     *
     * @param ev
     */
    private void handleCommonPress(MotionEvent ev) {
        LatLng latLng = aMap.getProjection().fromScreenLocation(new Point((int) ev.getX(), (int) ev.getY()));

        if (pressCircle_isBig) {

            if (movePosition == 0) {

                /******大圆圈****************/
                circleList.get(0).remove();
                circleList.remove(0);
                points.remove(0);
                circleList.add(0, drawBigCircle(latLng, 0));
                points.add(0, new DrawLatLng(latLng, true));

                /*********画右边的小圆圈***********/
                LatLng latLng_right = getCenterLatlng(points.get(2).getLatLng(), latLng);
                circleList.get(1).remove();
                circleList.remove(1);
                points.remove(1);
                circleList.add(1, drawSmallCircle(latLng_right, 1));

                points.add(1, new DrawLatLng(latLng_right, false));

                /******选中标签****************/
                signMarkPosition = movePosition;
                drawSignMark(latLng);
                drawLine();
                return;
            }

            if (movePosition == points.size() - 1) {
                /*********画左边的小圆圈***********/
                LatLng latLng_left = getCenterLatlng(points.get(movePosition - 2).getLatLng(), latLng);
                circleList.get(movePosition - 1).remove();
                circleList.remove(movePosition - 1);
                points.remove(movePosition - 1);
                circleList.add(movePosition - 1, drawSmallCircle(latLng_left, movePosition - 1));

                points.add(movePosition - 1, new DrawLatLng(latLng_left, false));


                /******大圆圈****************/
                circleList.get(movePosition).remove();
                circleList.remove(movePosition);
                points.remove(movePosition);
                circleList.add(movePosition, drawBigCircle(latLng, movePosition));
                points.add(movePosition, new DrawLatLng(latLng, true));

                /******选中标签****************/
                signMarkPosition = movePosition;
                drawSignMark(latLng);
                drawLine();
                return;
            }

            //移动的是大圆圈
            //重新计算两边小圆圈的位置

            /*********画左边的小圆圈***********/
            LatLng latLng_left = getCenterLatlng(points.get(movePosition - 2).getLatLng(), latLng);
            circleList.get(movePosition - 1).remove();
            circleList.remove(movePosition - 1);
            points.remove(movePosition - 1);
            circleList.add(movePosition - 1, drawSmallCircle(latLng_left, movePosition - 1));

            points.add(movePosition - 1, new DrawLatLng(latLng_left, false));


            /******大圆圈****************/
            circleList.get(movePosition).remove();
            circleList.remove(movePosition);
            points.remove(movePosition);
            circleList.add(movePosition, drawBigCircle(latLng, movePosition));
            points.add(movePosition, new DrawLatLng(latLng, true));

            /*********画右边的小圆圈***********/
            LatLng latLng_right = getCenterLatlng(points.get(movePosition + 2).getLatLng(), latLng);
            circleList.get(movePosition + 1).remove();
            circleList.remove(movePosition + 1);
            points.remove(movePosition + 1);
            circleList.add(movePosition + 1, drawSmallCircle(latLng_right, movePosition + 1));

            points.add(movePosition + 1, new DrawLatLng(latLng_right, false));

            /******选中标签****************/
            signMarkPosition = movePosition;
            drawSignMark(latLng);
            drawLine();
        } else {
            if (movePosition == points.size() - 1 || isLastPoint) {
                handleSmallCircleIsLastPoint(ev, latLng);
                isLastPoint = true;
                return;
            }

            //判断是否是位置1
            if (movePosition == 1 || isFirstPoint) {
                handleSmallCircleIsFirstPoint(ev, latLng);
                isFirstPoint = true;
                return;
            }
            handleSmallCircleNotLastPoint(ev, latLng);
        }
    }

    /**
     * 已经完成了
     *
     * @param ev
     */
    private void handleFinishPress(MotionEvent ev) {
        LatLng latLng = aMap.getProjection().fromScreenLocation(new Point((int) ev.getX(), (int) ev.getY()));
        if (pressCircle_isBig) {
            // 完成，需要判断是否是第一个点
            if (movePosition == 0 || isFirstPoint) {
                isFirstPoint = true;
                handleBigCircleIsFirtPoint(ev, latLng);
                return;
            }
            //判断是否是最后一个大圆圈
            if (movePosition == points.size() - 2 || isLastPoint) {
                isLastPoint = true;
                handleBigCircleIsLastPoint(ev, latLng);
                return;
            }

            handleBigCircleNotFirtPoint(ev, latLng);


        } else {
            //移动的是小圆圈,小圆圈需要判断是否是最后一个

            if (movePosition == points.size() - 1 || isLastPoint) {
                isLastPoint = true;
                handleSmallCircleIsLastPoint(ev, latLng);
                return;
            }

            //判断是否是位置1
            if (movePosition == 1 || isFirstPoint) {
                isFirstPoint = true;
                handleSmallCircleIsFirstPoint(ev, latLng);
                return;
            }

            handleSmallCircleNotLastPoint(ev, latLng);
        }

    }

    private void handleSmallCircleIsLastPoint(MotionEvent ev, LatLng latLng) {
        /*********画左边的小圆圈***********/
        LatLng latLng_left = getCenterLatlng(points.get(movePosition - 1).getLatLng(), latLng);

        //两边是小圆圈，清除上次的
        circleList.get(movePosition).remove();
        circleList.remove(movePosition);
        points.remove(movePosition);
        circleList.add(movePosition, drawSmallCircle(latLng_left, movePosition));
        points.add(movePosition, new DrawLatLng(latLng_left, false));


        /******大圆圈****************/

        if (moveSign_center) {
            circleList.get(movePosition + 1).remove();
            circleList.remove(movePosition + 1);
            points.remove(movePosition + 1);
            circleList.add(movePosition + 1, drawBigCircle(latLng, movePosition + 1));
            points.add(movePosition + 1, new DrawLatLng(latLng, true));
        } else {
            circleList.add(drawBigCircle(latLng, circleList.size()));
            points.add(new DrawLatLng(latLng, true));
            moveSign_center = true;
        }

        /******选中标签****************/
        signMarkPosition = movePosition;
        drawSignMark(latLng);

        /*********画右边的小圆圈***********/
        LatLng latLng_right = getCenterLatlng(points.get(0).getLatLng(), latLng);

        if (moveSign_right) {
            circleList.get(movePosition + 2).remove();
            circleList.remove(movePosition + 2);
            points.remove(movePosition + 2);
            circleList.add(movePosition + 2, drawSmallCircle(latLng_right, movePosition + 2));
            points.add(movePosition + 2, new DrawLatLng(latLng_right, false));
        } else {
            circleList.add(drawSmallCircle(latLng_right, circleList.size()));
            points.add(new DrawLatLng(latLng_right, false));
            moveSign_right = true;
        }

        if (hasDrawFinish) {
            drawPolygon();
        } else {
            drawLine();
        }

    }

    private void handleBigCircleNotFirtPoint(MotionEvent ev, LatLng latLng) {
        //移动的是大圆圈
        //重新计算两边小圆圈的位置

        /*********画左边的小圆圈***********/
        LatLng latLng_left = getCenterLatlng(points.get(movePosition - 2).getLatLng(), latLng);
        circleList.get(movePosition - 1).remove();
        circleList.remove(movePosition - 1);
        points.remove(movePosition - 1);
        circleList.add(movePosition - 1, drawSmallCircle(latLng_left, movePosition - 1));

        points.add(movePosition - 1, new DrawLatLng(latLng_left, false));


        /******大圆圈****************/

        circleList.get(movePosition).remove();
        circleList.remove(movePosition);
        points.remove(movePosition);
        circleList.add(movePosition, drawBigCircle(latLng, movePosition));
        points.add(movePosition, new DrawLatLng(latLng, true));

        /******选中标签****************/
        signMarkPosition = movePosition;
        drawSignMark(latLng);

        /*********画右边的小圆圈***********/
        LatLng latLng_right = getCenterLatlng(points.get(movePosition + 2).getLatLng(), latLng);

        circleList.get(movePosition + 1).remove();
        circleList.remove(movePosition + 1);
        points.remove(movePosition + 1);
        circleList.add(movePosition + 1, drawSmallCircle(latLng_right, movePosition + 1));

        points.add(movePosition + 1, new DrawLatLng(latLng_right, false));

        drawPolygon();
    }

    private void handleBigCircleIsFirtPoint(MotionEvent ev, LatLng latLng) {

        /*********画左边的小圆圈***********/
        LatLng latLng_left = getCenterLatlng(points.get(points.size() - 2).getLatLng(), latLng);

        circleList.get(circleList.size() - 1).remove();
        circleList.remove(circleList.size() - 1);
        circleList.add(drawSmallCircle(latLng_left, circleList.size()));
        points.remove(points.size() - 1);
        points.add(new DrawLatLng(latLng_left, false));


        /******大圆圈****************/

        circleList.get(0).remove();
        circleList.remove(0);
        points.remove(0);
        circleList.add(0, drawBigCircle(latLng, 0));
        points.add(0, new DrawLatLng(latLng, true));

        /******选中标签****************/
        signMarkPosition = movePosition;
        drawSignMark(latLng);

        /*********画右边的小圆圈***********/
        LatLng latLng_right = getCenterLatlng(points.get(2).getLatLng(), latLng);

        //清除上次的
        circleList.get(1).remove();
        circleList.remove(1);
        points.remove(1);

        circleList.add(1, drawSmallCircle(latLng_right, 1));
        points.add(1, new DrawLatLng(latLng_right, false));
        drawPolygon();
    }


    private void handleSmallCircleIsFirstPoint(MotionEvent ev, LatLng latLng) {
        /*********画左边的小圆圈***********/

        Point point_left = aMap.getProjection().toScreenLocation(points.get(0).getLatLng());
        Point centerPoint_left = new Point(CommonUtil.avg(point_left.x, (int) ev.getX()),
                CommonUtil.avg(point_left.y, (int) ev.getY()));

        LatLng latLng_left = aMap.getProjection().fromScreenLocation(centerPoint_left);

        //两边是小圆圈，清除上次的
        circleList.get(movePosition).remove();
        circleList.remove(movePosition);
        points.remove(movePosition);

        circleList.add(movePosition, drawSmallCircle(latLng_left, movePosition));
        points.add(movePosition, new DrawLatLng(latLng_left, false));


        /******大圆圈****************/

        if (moveSign_center) {
            circleList.get(movePosition + 1).remove();
            circleList.remove(movePosition + 1);
            points.remove(movePosition + 1);
            circleList.add(movePosition + 1, drawBigCircle(latLng, movePosition + 1));
            points.add(movePosition + 1, new DrawLatLng(latLng, true));
        } else {
            circleList.add(movePosition + 1, drawBigCircle(latLng, movePosition + 1));
            points.add(movePosition + 1, new DrawLatLng(latLng, true));
            moveSign_center = true;
        }


        /******选中标签****************/
        signMarkPosition = movePosition;
        drawSignMark(latLng);

        /*********画右边的小圆圈***********/

        LatLng newLatLng;
        if (moveSign_right) {
            newLatLng = points.get(movePosition + 3).getLatLng();
        } else {
            newLatLng = points.get(movePosition + 2).getLatLng();
        }

        Point point_right = aMap.getProjection().toScreenLocation(newLatLng);
        Point centerPoint_right = new Point(CommonUtil.avg(point_right.x, (int) ev.getX()),
                CommonUtil.avg(point_right.y, (int) ev.getY()));

        LatLng latLng_right = aMap.getProjection().fromScreenLocation(centerPoint_right);

        if (moveSign_right) {
            //两边是小圆圈，清除上次的
            circleList.get(movePosition + 2).remove();
            circleList.remove(movePosition + 2);
            points.remove(movePosition + 2);
            circleList.add(movePosition + 2, drawSmallCircle(latLng_right, movePosition + 2));
            points.add(movePosition + 2, new DrawLatLng(latLng_right, false));

        } else {
            circleList.add(movePosition + 2, drawSmallCircle(latLng_right, movePosition + 2));
            points.add(movePosition + 2, new DrawLatLng(latLng_right, false));
            moveSign_right = true;
        }
        if (hasDrawFinish) {
            drawPolygon();
        } else {
            drawLine();
        }
    }

    private void handleSmallCircleNotLastPoint(MotionEvent ev, LatLng latLng) {
        /*********画左边的小圆圈***********/

        Point point_left = aMap.getProjection().toScreenLocation(points.get(movePosition - 1).getLatLng());
        Point centerPoint_left = new Point(CommonUtil.avg(point_left.x, (int) ev.getX()),
                CommonUtil.avg(point_left.y, (int) ev.getY()));

        LatLng latLng_left = aMap.getProjection().fromScreenLocation(centerPoint_left);

        //两边是小圆圈，清除上次的
        circleList.get(movePosition).remove();
        circleList.remove(movePosition);
        points.remove(movePosition);
        circleList.add(movePosition, drawSmallCircle(latLng_left, movePosition));
        points.add(movePosition, new DrawLatLng(latLng_left, false));


        /******大圆圈****************/

        if (moveSign_center) {
            circleList.get(movePosition + 1).remove();
            circleList.remove(movePosition + 1);
            points.remove(movePosition + 1);
            circleList.add(movePosition + 1, drawBigCircle(latLng, movePosition + 1));
            points.add(movePosition + 1, new DrawLatLng(latLng, true));

        } else {
            circleList.add(movePosition + 1, drawBigCircle(latLng, movePosition + 1));
            points.add(movePosition + 1, new DrawLatLng(latLng, true));
            moveSign_center = true;
        }


        /******选中标签****************/
        signMarkPosition = movePosition;
        drawSignMark(latLng);

        /*********画右边的小圆圈***********/
        LatLng newLatlng;
        if (moveSign_right) {
            newLatlng = points.get(movePosition + 3).getLatLng();
        } else {
            newLatlng = points.get(movePosition + 2).getLatLng();
        }

        Point point_right = aMap.getProjection().toScreenLocation(newLatlng);
        Point centerPoint_right = new Point(CommonUtil.avg(point_right.x, (int) ev.getX()),
                CommonUtil.avg(point_right.y, (int) ev.getY()));

        LatLng latLng_right = aMap.getProjection().fromScreenLocation(centerPoint_right);

        if (moveSign_right) {
            //两边是小圆圈，清除上次的
            circleList.get(movePosition + 2).remove();
            circleList.remove(movePosition + 2);
            points.remove(movePosition + 2);
            circleList.add(movePosition + 2, drawSmallCircle(latLng_right, movePosition + 2));
            points.add(movePosition + 2, new DrawLatLng(latLng_right, false));
        } else {
            circleList.add(movePosition + 2, drawSmallCircle(latLng_right, movePosition + 2));
            points.add(movePosition + 2, new DrawLatLng(latLng_right, false));
            moveSign_right = true;
        }

        if (hasDrawFinish) {
            drawPolygon();
        } else {
            drawLine();
        }
    }

    private void handleBigCircleIsLastPoint(MotionEvent ev, LatLng latLng) {
        //移动的是大圆圈
        //重新计算两边小圆圈的位置

        /*********画左边的小圆圈***********/

        Point point_left = aMap.getProjection().toScreenLocation(points.get(movePosition - 2).getLatLng());
        Point centerPoint_left = new Point(CommonUtil.avg(point_left.x, (int) ev.getX()),
                CommonUtil.avg(point_left.y, (int) ev.getY()));

        LatLng latLng_left = aMap.getProjection().fromScreenLocation(centerPoint_left);

        circleList.get(movePosition - 1).remove();
        circleList.remove(movePosition - 1);
        points.remove(movePosition - 1);
        circleList.add(movePosition - 1, drawSmallCircle(latLng_left, movePosition - 1));

        points.add(movePosition - 1, new DrawLatLng(latLng_left, false));


        /******大圆圈****************/

        circleList.get(movePosition).remove();
        circleList.remove(movePosition);
        points.remove(movePosition);
        circleList.add(movePosition, drawBigCircle(latLng, movePosition));
        points.add(movePosition, new DrawLatLng(latLng, true));

        /******选中标签****************/
        signMarkPosition = movePosition;
        drawSignMark(latLng);

        /*********画右边的小圆圈***********/

        Point point_right = aMap.getProjection().toScreenLocation(points.get(0).getLatLng());
        Point centerPoint_right = new Point(CommonUtil.avg(point_right.x, (int) ev.getX()),
                CommonUtil.avg(point_right.y, (int) ev.getY()));

        LatLng latLng_right = aMap.getProjection().fromScreenLocation(centerPoint_right);

        circleList.get(movePosition + 1).remove();
        circleList.remove(movePosition + 1);
        points.remove(movePosition + 1);
        circleList.add(movePosition + 1, drawSmallCircle(latLng_right, movePosition + 1));

        points.add(movePosition + 1, new DrawLatLng(latLng_right, false));

        drawPolygon();
    }

}
