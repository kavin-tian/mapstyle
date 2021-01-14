package com.kavin.location;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.Timer;
import java.util.TimerTask;

/**自定义地图样式：https://lbs.amap.com/api/android-sdk/guide/create-map/custom
 *
 * 定位精度圈标扩散效果：https://lbs.amap.com/dev/demo/location-circle#Android
 */
public class MainActivity extends CheckPermissionsActivity {
    MapView mMapView = null;
    private AMap aMap;
    private Marker locMarker;
    private Circle ac;
    private Circle c;

    private long start;
    private final Interpolator interpolator1 = new LinearInterpolator();
    private TimerTask mTimerTask;
    private Timer mTimer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        aMap = mMapView.getMap();


        customMapStyle();

        initMapView();

    }

    /**
     * 自定义地图样式
     */
    private void customMapStyle() {
        String diskCacheDir = FileUtils.getDiskCacheDir(getApplicationContext());
        //该方法在AMap类中提供
        aMap.setCustomMapStyle(new CustomMapStyleOptions()
                .setEnable(true)
                .setStyleDataPath(diskCacheDir + "/style.data")
                .setStyleExtraPath(diskCacheDir + "/style_extra.data")
        );
    }

    /**
     * 初始地图
     */
    private void initMapView() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。
        //方法自5.1.0版本后支持
        myLocationStyle.showMyLocation(false);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.getUiSettings().setZoomControlsEnabled(false); // 不显示缩放按钮
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                if (location != null) {
                    LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 17));
                    addLocationMarker(location);
                } else {
                    String errText = "定位失败!";
                    Log.e("AmapErr", errText);
                }

            }
        });

    }

    /**
     * 添加精度圈，水波纹扩散效果
     */
    private void addLocationMarker(Location location) {

        LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
        float accuracy = location.getAccuracy(); //精确度
        if (locMarker == null) {
            locMarker = addMarker(mylocation);
            ac = aMap.addCircle(new CircleOptions().center(mylocation)
                    .fillColor(Color.argb(100, 255, 218, 185)).radius(accuracy)
                    .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(0));
            c = aMap.addCircle(new CircleOptions().center(mylocation)
                    .fillColor(Color.argb(70, 255, 218, 185))
                    .radius(accuracy).strokeColor(Color.argb(255, 255, 228, 185))
                    .strokeWidth(0));
        } else {
            locMarker.setPosition(mylocation);
            ac.setCenter(mylocation);
            ac.setRadius(accuracy);
            c.setCenter(mylocation);
            c.setRadius(accuracy);
        }
        Scalecircle(c);
    }


    /**
     * 添加标注
     */
    private Marker addMarker(LatLng point) {
        Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_locked);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);
        Marker marker = aMap.addMarker(new MarkerOptions().position(point).icon(des)
                .anchor(0.5f, 0.5f));
        return marker;
    }

    /**
     * 循环水波纹扩散效果，任务
     */
    public void Scalecircle(final Circle circle) {
        start = SystemClock.uptimeMillis();
        if (mTimerTask == null) {
            mTimerTask = new circleTask(circle, 1000);
            mTimer.schedule(mTimerTask, 0, 30);
        }
    }


    private class circleTask extends TimerTask {
        private double r;
        private Circle circle;
        private long duration = 1000;

        public circleTask(Circle circle, long rate) {
            this.circle = circle;
            this.r = circle.getRadius();
            if (rate > 0) {
                this.duration = rate;
            }
        }

        @Override
        public void run() {
            try {
                long elapsed = SystemClock.uptimeMillis() - start;
                float input = (float) elapsed / duration;
//                外圈循环缩放
//                float t = interpolator.getInterpolation((float)(input-0.25));//return (float)(Math.sin(2 * mCycles * Math.PI * input))
//                double r1 = (t + 2) * r;
//                外圈放大后消失
                float t = interpolator1.getInterpolation(input);
                double r1 = (t + 1) * r;
                circle.setRadius(r1);
                if (input > 2) {
                    start = SystemClock.uptimeMillis();
                }
            } catch (Throwable e) {
                e.printStackTrace();
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
}