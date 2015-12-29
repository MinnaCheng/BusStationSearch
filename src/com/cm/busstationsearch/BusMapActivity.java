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
	boolean mIsFirstLoc = true;// �Ƿ��״ζ�λ
	private boolean isActive;// һ�����
	private static final String TAG = "BusMapActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ʹ��SDK�����֮ǰ����Ҫ��ʼ��context��Ϣ������
		// ApplicationContext ��ע��÷���Ҫ��setContentView
		// ����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.bus_map);
		mMapView = (MapView) findViewById(R.id.map_view);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// ��mMapView��ͼ��Ϊ�գ�������
		if (null != mMapView) {
			mMapView.onDestroy();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive = false;// ������Ϊfalse
		if (null != mMapView) {
			mMapView.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;// ������Ϊtrue
		// �ڻ�ȡ����ʱ��ȡ��ǰ��λ
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
		option.setCoorType("bd0911");// ���ذٶȾ�γ������ϵ ��bd09ll
		option.setProdName("BusSearchDemo");
		option.setScanSpan(5000);// ÿ�����Ӷ�λһ��
		mLocationClient.setLocOption(option);
		mLocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation location) {
				if (null != location
						&& location.getLocType() != BDLocation.TypeServerError) {
					StringBuffer sb = new StringBuffer(256);
					sb.append("time : ");
					/**
					 * ʱ��Ҳ����ʹ��systemClock.elapsedRealtime()����
					 * ��ȡ�����Դӿ���������ÿ�λص���ʱ�䣻 location.getTime()
					 * ��ָ����˳����ν����ʱ�䣬���λ�ò������仯����ʱ�䲻��
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
					if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS��λ���
						sb.append("\nspeed : ");
						sb.append(location.getSpeed());// ��λ��km/h
						sb.append("\nsatellite : ");
						sb.append(location.getSatelliteNumber());
						sb.append("\nheight : ");
						sb.append(location.getAltitude());// ��λ����
						sb.append("\ndescribe : ");
						sb.append("gps��λ�ɹ�");
					} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// ���綨λ���
						// ��Ӫ����Ϣ
						sb.append("\noperationers : ");
						sb.append(location.getOperators());
						sb.append("\ndescribe : ");
						sb.append("���綨λ�ɹ�");
					} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// ���߶�λ���
						sb.append("\ndescribe : ");
						sb.append("���߶�λ�ɹ������߶�λ���Ҳ����Ч��");
					} else if (location.getLocType() == BDLocation.TypeServerError) {
						sb.append("\ndescribe : ");
						sb.append("��������綨λʧ�ܣ����Է���IMEI�źʹ��嶨λʱ�䵽loc-bugs@baidu.com��������׷��ԭ��");
					} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
						sb.append("\ndescribe : ");
						sb.append("���粻ͬ���¶�λʧ�ܣ����������Ƿ�ͨ��");
					} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
						sb.append("\ndescribe : ");
						sb.append("�޷���ȡ��Ч��λ���ݵ��¶�λʧ�ܣ�һ���������ֻ���ԭ�򣬴��ڷ���ģʽ��һ���������ֽ�����������������ֻ�");
					}
					Log.e("locationMessage", sb.toString());
				}

				Log.d("onReceiveLocation", "onReceiveLocation");
				if (!isActive) {
					return;
				}
				// map view ���ٺ��ٴ����½��յ�λ��
				if (location == null || mMapView == null) {
					return;
				}
				// ���ÿ����߻�ȡ�ķ�����Ϣ��˳ʱ��0-360.
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
					// �״ζ�λʱ��ȡ���Ⱥ�γ�ȣ������ݶ�λ���Ƶ�ͼ��
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
		// ������λsdk
		mLocationClient.start();
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.requestLocation();
		} else {
			Log.e("test", "not start");

		}

	}

}
