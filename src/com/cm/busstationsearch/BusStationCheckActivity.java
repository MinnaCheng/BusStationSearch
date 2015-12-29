package com.cm.busstationsearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.core.PoiInfo.POITYPE;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.cm.busstationsearch.R.string;
import com.cm.busstationsearch.utils.Constants;

public class BusStationCheckActivity extends Activity implements
		OnItemClickListener, OnClickListener, OnGetPoiSearchResultListener,
		OnGetBusLineSearchResultListener {
	private static final String TAG = "BusStationCheckActivity_test";
	private boolean isActivityStoped = false;
	private BroadcastReceiver mbBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.ACTION_RECEIVED_MESSAGE.equals(intent.getAction())) {
				isActivityStoped = true;
			}

		}
	};
	private ListView mstationlistView;
	// private EditText mEditTextCity;
	private AutoCompleteTextView mAutoCompleteTextViewCity;
	private EditText mEditTextBusNumber;
	private ImageButton mImageButtonSearch;
	private List<String> mStatonList;
	private ArrayAdapter mAdapter;
	private List<String> mDetaiStationList;
	private Dialog mBuslineDialog;

	private PoiSearch mPoiSearch;
	private BusLineSearch mBusLineSearch;
	private List<PoiInfo> mPoiInfoList;
	private int mLoadIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.buscheck_activity);
		mPoiInfoList = new ArrayList<PoiInfo>();
		mStatonList = new ArrayList<String>();
		mDetaiStationList = new ArrayList<String>();
		initView();
		// 输入城市自动提示补全。
		ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this,
				R.array.cities, android.R.layout.simple_spinner_item);
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_item);
		mAutoCompleteTextViewCity.setAdapter(arrayAdapter);
		// 站点检索
		// 第一步，创建POI检索实例
		mPoiSearch = PoiSearch.newInstance();
		mBusLineSearch = BusLineSearch.newInstance();
		// 第二步，设置检索监听器
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mBusLineSearch.setOnGetBusLineSearchResultListener(this);
	}

	public void initView() {
		mstationlistView = (ListView) findViewById(R.id.listView1);
		mstationlistView.setOnItemClickListener(this);
		mAutoCompleteTextViewCity = (AutoCompleteTextView) findViewById(R.id.autoTextCity);
		mEditTextBusNumber = (EditText) findViewById(R.id.editTextBusNumber);
		mImageButtonSearch = (ImageButton) findViewById(R.id.imageButton1);
		mImageButtonSearch.setOnClickListener(this);
	}

	/**
	 * 接收mainActivity广播时销毁。
	 * */
	@Override
	protected void onDestroy() {
		if (isActivityStoped) {
			mPoiSearch.destroy();
			mBusLineSearch.destroy();
			Log.d(TAG, "onDestroied");
		}
		super.onDestroy();

	}

	@Override
	protected void onResume() {
		// Do Nothing
		Log.d(TAG, "onResumed");
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStarted");
		super.onStart();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPaused");
		closeKeyBoard();
		super.onPause();
	}

	@Override
	protected void onStop() {

		Log.e(TAG, "onStop");
		closeKeyBoard();
		if (mBuslineDialog != null && mBuslineDialog.isShowing()) {
			mBuslineDialog.dismiss();
		}
		super.onStop();
	}

	/**
	 * 获取城市和公交车号 非空执行搜索方法 城市输入框默认值为北京 车号为空提示toast
	 * */
	public void handleSearchStation() {
		String inputBusNumber = mEditTextBusNumber.getText().toString();
		String inputCity = mAutoCompleteTextViewCity.getText().toString();
		if (inputCity.isEmpty()) {
			inputCity = "北京";
			mAutoCompleteTextViewCity.setText("北京");
		}
		if (inputBusNumber != null && !inputBusNumber.isEmpty()) {
			closeKeyBoard();
			searchStation();
		} else {
			showErrorToast();
		}
	}

	private void showErrorToast() {
		Toast.makeText(
				this,
				getResources()
						.getString(R.string.error_message_bus_number_null),
				Toast.LENGTH_SHORT).show();

	}

	/**
	 * 关闭软键盘
	 * */
	private void closeKeyBoard() {
		if (mEditTextBusNumber != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(
					mEditTextBusNumber.getWindowToken(), 0);
		}
	}

	/**
	 * 在listView中添加ArrayAdapter,调用查询方法。
	 * */
	private void searchStation() {

		mAdapter = new ArrayAdapter<String>(this, R.layout.station_list_item,
				mStatonList);
		mstationlistView.setAdapter(mAdapter);
		poiCitySearch();

	}

	/**
	 * 根据城市查询公交车站
	 * */
	private void poiCitySearch() {
		// 设置检索参数
		PoiCitySearchOption poiCitySearchOption = new PoiCitySearchOption();
		poiCitySearchOption
				.city(mAutoCompleteTextViewCity.getText().toString());
		poiCitySearchOption.keyword(mEditTextBusNumber.getText().toString());

		mPoiSearch.searchInCity(poiCitySearchOption);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long colum) {
		Log.e(TAG, "position=" + position);
//当公交起始站信息列表不为空，查询所点击的线路
		if (mPoiInfoList != null && mPoiInfoList.size() > 0
				&& mPoiInfoList.size() > position) {
			PoiInfo poiInfo = mPoiInfoList.get(position);
			BusLineSearchOption busLineSearchOption = new BusLineSearchOption();
			busLineSearchOption.city(mAutoCompleteTextViewCity.getText()
					.toString());
			busLineSearchOption.uid(poiInfo.uid);
			mBusLineSearch.searchBusLine(busLineSearchOption);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.imageButton1:
			handleSearchStation();
			break;

		default:
			break;
		}
	}

	@Override
	public void onGetBusLineResult(BusLineResult busLineResult) {
		Log.e(TAG, "BussLine:");

		List<BusStation> busStations = busLineResult.getStations();
		Log.e(TAG, "busStations:" + busStations);
		if (busStations != null) {
			mDetaiStationList.clear();
			for (int i = 0; i < busStations.size(); i++) {
				Log.e(TAG, "" + busStations.get(i).getTitle());
				mDetaiStationList.add(busStations.get(i).getTitle());
			}
			Log.d(TAG, "mDetaiStationList:" + mDetaiStationList.size());
			showBusLineDialog();
		} else {
			Toast.makeText(this, getResources().getString(R.string.notFound),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
		if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(BusStationCheckActivity.this,
					getResources().getString(R.string.notFound),
					Toast.LENGTH_SHORT).show();
		} else {
			Log.e(TAG, "Detail:");
			Log.e(TAG, poiDetailResult.getName());
			Log.e(TAG, poiDetailResult.toString());
			Log.e(TAG, poiDetailResult.getAddress());
			Log.e(TAG, poiDetailResult.getLocation() + "");

		}

	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		Log.d(TAG, "onGetPoiResult");
		if (result == null || result.error == ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(this, getResources().getString(R.string.notFound),
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (result.error == ERRORNO.NO_ERROR) {
			mStatonList.clear();
			mPoiInfoList.clear();
			mPoiInfoList = result.getAllPoi();
			for (int i = 0; i < mPoiInfoList.size(); i++) {
				Log.e(TAG, mPoiInfoList.get(i).name);
				Log.e(TAG, mPoiInfoList.get(i).address);
				if (mPoiInfoList.get(i).type == POITYPE.BUS_LINE
						|| mPoiInfoList.get(i).type == POITYPE.SUBWAY_LINE) {
					mStatonList.add(mPoiInfoList.get(i).name);
				}
			}
			mAdapter.notifyDataSetChanged();
			mstationlistView.invalidate();
			return;
		}

	}

	/**
	 * 显示具体线路dialog
	 * */
	public void showBusLineDialog() {
		Log.d(TAG, "showBusLineDialog");
		if (mBuslineDialog != null) {
			mBuslineDialog = null;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = LayoutInflater.from(this).inflate(
				R.layout.dialog_bus_station_details, null);
		ListView busStationDetailsListView = (ListView) view
				.findViewById(R.id.listViewBusStationDetails);
		ArrayAdapter<String> busStationDetailsAdapter = new ArrayAdapter<String>(
				this, R.layout.station_list_item, mDetaiStationList);

		busStationDetailsListView.setAdapter(busStationDetailsAdapter);
		builder.setView(view);
		mBuslineDialog = builder.create();
		mBuslineDialog.show();
	}

	@Override
	public void onBackPressed() {
		if (mBuslineDialog != null && mBuslineDialog.isShowing()) {
			mBuslineDialog.dismiss();
		}
		super.onBackPressed();
	}

}
