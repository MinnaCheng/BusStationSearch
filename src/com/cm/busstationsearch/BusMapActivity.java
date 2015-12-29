package com.cm.busstationsearch;

import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class BusMapActivity extends Activity {
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationClient mLocationClient;
	private LatLng mCurrentLatLng;
	private BitmapDescriptor mCurrentMarker;
	boolean mIsFirstLoc = true;// 是否首次定位
	private boolean isActive;// 一个标记
	private static final String TAG = "BusMapActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前，需要初始化context信息，传入
		// ApplicationContext ，注意该方法要在setContentView
		// 方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.bus_map);
		mMapView = (MapView) findViewById(R.id.map_view);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 若mMapView视图不为空，则销毁
		if (null != mMapView) {
			mMapView.onDestroy();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive = false;// 活动标记设为false
		if (null != mMapView) {
			mMapView.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;// 活动标记设为true
		// 在获取焦点时获取当前定位
		locatCurrentPosition();
		if (null != mMapView) {
			mMapView.onResume();
		}
	}

	public void locatCurrentPosition() {
		mLocationClient = new LocationClient(getApplicationContext());
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);
		option.setOpenGps(true);
		option.setCoorType("bd0911");// 返回百度经纬度坐标系 ：bd09ll
		option.setProdName("BusSearchDemo");
		option.setScanSpan(5000);// 每五秒钟定位一次
		mLocationClient.setLocOption(option);
		mLocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation location) {
				if (null != location
						&& location.getLocType() != BDLocation.TypeServerError) {
					StringBuffer sb = new StringBuffer(256);
					sb.append("time : ");
					/**
					 * 时间也可以使用systemClock.elapsedRealtime()方法
					 * 获取的是自从开机以来，每次回调的时间； location.getTime()
					 * 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
					 */
					sb.append(location.getTime());
					sb.append("\nerror code : ");
					sb.append(location.getLocType());
					sb.append("\nlatitude : ");
					sb.append(location.getLatitude());
					sb.append("\nlontitude : ");
					sb.append(location.getLongitude());
					sb.append("\nradius : ");
					sb.append(location.getRadius());
					sb.append("\nCountryCode : ");
					sb.append(location.getCountryCode());
					sb.append("\nCountry : ");
					sb.append(location.getCountry());
					sb.append("\ncitycode : ");
					sb.append(location.getCityCode());
					sb.append("\ncity : ");
					sb.append(location.getCity());
					sb.append("\nDistrict : ");
					sb.append(location.getDistrict());
					sb.append("\nStreet : ");
					sb.append(location.getStreet());
					sb.append("\naddr : ");
					sb.append(location.getAddrStr());
					sb.append("\nDescribe: ");
					sb.append(location.getLocationDescribe());
					sb.append("\nDirection(not all devices have value): ");
					sb.append(location.getDirection());
					sb.append("\nPoi: ");
					if (location.getPoiList() != null
							&& !location.getPoiList().isEmpty()) {
						for (int i = 0; i < location.getPoiList().size(); i++) {
							Poi poi = (Poi) location.getPoiList().get(i);
							sb.append(poi.getName() + ";");
						}
					}
					if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
						sb.append("\nspeed : ");
						sb.append(location.getSpeed());// 单位：km/h
						sb.append("\nsatellite : ");
						sb.append(location.getSatelliteNumber());
						sb.append("\nheight : ");
						sb.append(location.getAltitude());// 单位：米
						sb.append("\ndescribe : ");
						sb.append("gps定位成功");
					} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
						// 运营商信息
						sb.append("\noperationers : ");
						sb.append(location.getOperators());
						sb.append("\ndescribe : ");
						sb.append("网络定位成功");
					} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
						sb.append("\ndescribe : ");
						sb.append("离线定位成功，离线定位结果也是有效的");
					} else if (location.getLocType() == BDLocation.TypeServerError) {
						sb.append("\ndescribe : ");
						sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
					} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
						sb.append("\ndescribe : ");
						sb.append("网络不同导致定位失败，请检查网络是否通畅");
					} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
						sb.append("\ndescribe : ");
						sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
					}
					Log.e("locationMessage", sb.toString());
				}

				Log.d("onReceiveLocation", "onReceiveLocation");
				if (!isActive) {
					return;
				}
				// map view 销毁后不再处理新接收的位置
				if (location == null || mMapView == null) {
					return;
				}
				// 设置开发者获取的方向信息，顺时针0-360.
				MyLocationData locData = new MyLocationData.Builder()
						.accuracy(location.getRadius()).direction(100)
						.latitude(location.getLongitude()).build();
				if (null == locData) {
					locData = new MyLocationData.Builder()
							.accuracy(location.getRadius()).direction(100)
							.latitude(location.getLongitude()).build();
				}
				mBaiduMap.setMyLocationData(locData);
				if (mIsFirstLoc) {
					// 首次定位时获取经度和纬度，并根据定位绘制地图。
					Log.d("firstLoc", "location");
					mIsFirstLoc = false;
					LatLng l1 = new LatLng(location.getLatitude(), location
							.getLongitude());
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(l1);
					mBaiduMap.animateMapStatus(u);
					BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
							.fromResource(R.drawable.marker_small);
					OverlayOptions options = new MarkerOptions().position(l1)
							.icon(bitmapDescriptor);
					mBaiduMap.addOverlay(options);
				}

			}
		});
		// 开启定位sdk
		mLocationClient.start();
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.requestLocation();
		} else {
			Log.e("test", "not start");

		}

	}

}
