package com.cm.busstationsearch;

import java.util.ArrayList;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

/***
 * 根据NearByBusStationActivity传递的公交车站点位置坐标进行定位。
 * 
 * @author Administrator
 * 
 */
public class BusStationMapActivity extends Activity {
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Double mLongitude;
	private Double mLatitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.bus_map);
		mMapView = (MapView) findViewById(R.id.map_view);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		Intent intent = getIntent();
		mLatitude = intent.getDoubleExtra("busStationLatitude", 0.0);
		mLongitude = intent.getDoubleExtra("busStationLongitude", 0.0);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
	}

	public void getBusStationLocation() {
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(u);
		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.marker_small);
		OverlayOptions options = new MarkerOptions().position(latLng).icon(
				bitmapDescriptor);
		mBaiduMap.addOverlay(options);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (null != mMapView) {
			mMapView.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mMapView) {
			mMapView.onDestroy();
		}
	}

	@Override
	protected void onResume() {

		getBusStationLocation();
		super.onResume();
		if (null != mMapView) {
			mMapView.onResume();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
