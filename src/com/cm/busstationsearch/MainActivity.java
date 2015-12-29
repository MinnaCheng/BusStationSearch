package com.cm.busstationsearch;

import com.baidu.mapapi.SDKInitializer;
import com.cm.busstationsearch.utils.Constants;

import android.os.Bundle;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	public static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		SDKInitializer.initialize(getApplicationContext());
		addTab();
	}

	@Override
	protected void onDestroy() {
		Intent intent = new Intent(Constants.ACTION_RECEIVED_MESSAGE);
		sendBroadcast(intent);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	private void addTab() {
		TabHost tabHost = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.activity_main,
				tabHost.getTabContentView(), true);
		tabHost.addTab(tabHost.newTabSpec(Constants.TAB_TAG_BUS_STATION_CHECK)
				.setIndicator(getString(R.string.tab_title_bus_station))
				.setContent(new Intent(this, BusStationCheckActivity.class)));
		tabHost.addTab(tabHost.newTabSpec(Constants.TAB_TAG_NEARBY_BUS_STATION)
				.setIndicator(getString(R.string.tab_titile_near_by_station))
				.setContent(new Intent(this, NearByBusStationActivity.class)));
		tabHost.addTab(tabHost.newTabSpec(Constants.TAB_TAG_BUS_MAP)
				.setIndicator(getString(R.string.tab_title_map))
				.setContent(new Intent(this, BusMapActivity.class)));

	}
}
