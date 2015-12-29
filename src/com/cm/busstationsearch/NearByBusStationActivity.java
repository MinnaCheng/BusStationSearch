package com.cm.busstationsearch;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.PoiInfo.POITYPE;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.cm.busstationsearch.utils.Constants;
import com.cm.busstationsearch.widget.PullToRefreshListView;
import com.cm.busstationsearch.widget.PullToRefreshListView.IReflashListener;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class NearByBusStationActivity extends Activity implements
		OnGetPoiSearchResultListener, IReflashListener, OnItemClickListener {
	public static final String TAG = "NearByBusStationActivity_test";
	public static final String LCA = "NearByBusStationActivity_LCA";
	public boolean isActivityStoped = false;
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.ACTION_RECEIVED_MESSAGE.equals(intent.getAction())) {
				isActivityStoped = true;
			}

		}
	};
	private PullToRefreshListView mListViewNearByBusStation;
	private ArrayList<String> mBusStationList;
	private PoiSearch mPoiSearch;
	private List<PoiInfo> mpoiInfoList;
	private ArrayAdapter<String> mAdapter;

	private LocationClient mlocationClient;
	private LatLng mcurrentLatLng;
	private boolean isActive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.nearby_bus_station_activity);

		mPoiSearch = PoiSearch.newInstance();
		mBusStationList = new ArrayList<String>();
		mpoiInfoList = new ArrayList<PoiInfo>();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		showList();

	}

	private void showList() {
		if (mAdapter == null) {
			mListViewNearByBusStation = (PullToRefreshListView) findViewById(R.id.listViewNearByBusStation);
			mListViewNearByBusStation.setInterface(this);
			mListViewNearByBusStation.setOnItemClickListener(this);
			mAdapter = new ArrayAdapter<String>(this,
					R.layout.station_list_item, mBusStationList);
			mListViewNearByBusStation.setAdapter(mAdapter);
		} else {
			mAdapter.notifyDataSetChanged();
		}

	}

	@Override
	protected void onDestroy() {
		Log.d(LCA, "onDestroyed");
		if (isActivityStoped) {
			mPoiSearch.destroy();
			mlocationClient.stop();
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.d(LCA, "onPaused");
		isActive = false;
		if (isActivityStoped) {
			mPoiSearch.destroy();
			mlocationClient.stop();
		}
		super.onPause();
	}

	@Override
	protected void onRestart() {
		Log.d(LCA, "onRestarted");
		isActive = true;
		super.onRestart();
		searchStation();
	}

	@Override
	protected void onResume() {
		Log.d(LCA, "onResumed");
		isActive = true;
		super.onResume();
		searchStation();
	}

	@Override
	protected void onStart() {
		Log.d(LCA, "onStarted");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(LCA, "onStopped");
		if (isActivityStoped) {
			mPoiSearch.destroy();
		}
		if (isActivityStoped) {
			mlocationClient.stop();
		}
		super.onStop();
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		Log.d(TAG, "onGetPoiResult");
		if (!isActive) {
			return;
		}
		if (result == null || result.error == ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(NearByBusStationActivity.this,
					getResources().getString(R.string.notFound),
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (result.error == ERRORNO.NO_ERROR) {
			mBusStationList.clear();
			mpoiInfoList.clear();
			mpoiInfoList = result.getAllPoi();
			for (int i = 0; i < mpoiInfoList.size(); i++) {
				Log.e(TAG, mpoiInfoList.get(i).name);
				Log.e(TAG, mpoiInfoList.get(i).address);
				if (mpoiInfoList.get(i).type == POITYPE.BUS_STATION) {
					mBusStationList.add(mpoiInfoList.get(i).name + "\n" + "("
							+ mpoiInfoList.get(i).address + ")");
				}
			}
			mAdapter.notifyDataSetChanged();
			mListViewNearByBusStation.invalidate();
			return;
		}

	}

	private void searchStation() {
		Log.e(TAG, "searchStation");
		getCurrentLocation();
	}

	private void getCurrentLocation() {
		mlocationClient = new LocationClient(getApplicationContext());
		mlocationClient = new LocationClient(getApplicationContext());
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);
		option.setOpenGps(true);
		option.setCoorType("bd0911");// 返回百度经纬度坐标系 ：bd09ll
		option.setProdName("BusSearchDemo");
		option.setScanSpan(30000);// 每分自动钟定位一次
		mlocationClient.setLocOption(option);
		mlocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation location) {
				if (!isActive) {
					return;
				}
				mcurrentLatLng = new LatLng(location.getLatitude(), location
						.getLongitude());
				Log.e(TAG, "Receive:mCurrentLatLng:" + location.getLatitude());
				Log.e(TAG, "mCurrentLatLng:" + location.getLongitude());
				if (mcurrentLatLng == null) {
					Log.e(TAG, "mCurrentLatLng=null");
					mcurrentLatLng = new LatLng(31.33539206, 120.770669);
				}
				// 设置周边查询参数
				PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
				nearbySearchOption.location(mcurrentLatLng);
				nearbySearchOption.radius(1000);
				nearbySearchOption.keyword("公交");
				mPoiSearch.searchNearby(nearbySearchOption);
			}
		});
		mlocationClient.start();
		if (mlocationClient != null && mlocationClient.isStarted()) {
			mlocationClient.requestLocation();
		} else {
			Log.e(TAG, "not start");
		}
	}

	@Override
	public void onReflash() {
		// 延时效果
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				// 获取最新数据
				searchStation();
				// 通知界面显示
				showList();
				// 通知ListView刷新数据完毕
				mListViewNearByBusStation.reflashComplete();
			}
		}, 1000);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long colum) {
		if (mpoiInfoList != null && mpoiInfoList.size() > 0) {
			Log.d(TAG, "position=" + position);
			LatLng busStationLatLng = mpoiInfoList.get(position - 1).location;
			Log.d(TAG, "busStationLatLng=" + busStationLatLng);
			ArrayList<Double> busStationLatLngList = new ArrayList<Double>();
			Double busStationLatitude = busStationLatLng.latitude;
			Double busStationLongitude = busStationLatLng.longitude;
			Intent intent = new Intent();
			intent.putExtra("busStationLatitude", busStationLatitude);
			intent.putExtra("busStationLongitude", busStationLongitude);
			intent.setClass(NearByBusStationActivity.this,
					BusStationMapActivity.class);
			startActivity(intent);
		}
	}
}
