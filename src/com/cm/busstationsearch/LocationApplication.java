package com.cm.busstationsearch;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

import com.baidu.location.service.LocationService;
import com.baidu.location.service.WriteLog;
import com.baidu.mapapi.SDKInitializer;

public class LocationApplication extends Application {
	public LocationService locationService;
	public Vibrator mVibrator;

	@Override
	public void onCreate() {
		super.onCreate();
		/***
		 * ��ʼ����λsdk��������Application�д���
		 */
		locationService = new LocationService(getApplicationContext());
		mVibrator = (Vibrator) getApplicationContext().getSystemService(
				Service.VIBRATOR_SERVICE);
		WriteLog.getInstance().init(); // ��ʼ����־
		SDKInitializer.initialize(getApplicationContext());

	}
}
