package com.baidu.location.service;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import android.content.Context;

/**
 * 
 * @author baidu
 *
 */
public class LocationService {
	private LocationClient client = null;
	private LocationClientOption mOption,DIYoption;
	private Object  objLock = new Object();

	/***
	 * 
	 * @param locationContext
	 */
	public LocationService(Context locationContext){
		synchronized (objLock) {
			if(client == null){
				client = new LocationClient(locationContext);
				client.setLocOption(getDefaultLocationClientOption());
			}
		}
	}
	
	/***
	 * 
	 * @param listener
	 * @return
	 */
	
	public boolean registerListener(BDLocationListener listener){
		boolean isSuccess = false;
		if(listener != null){
			client.registerLocationListener(listener);
			isSuccess = true;
		}
		return  isSuccess;
	}
	
	public void unregisterListener(BDLocationListener listener){
		if(listener != null){
			client.unRegisterLocationListener(listener);
		}
	}
	
	/***
	 * 
	 * @param option
	 * @return isSuccessSetOption
	 */
	public boolean setLocationOption(LocationClientOption option){
		boolean isSuccess = false;
		if(option != null){
			if(client.isStarted())
				client.stop();
			DIYoption = option;
			client.setLocOption(option);
		}
		return isSuccess;
	}
	
	public LocationClientOption getOption(){
		return DIYoption;
	}
	/***
	 * 
	 * @return DefaultLocationClientOption
	 */
	public LocationClientOption getDefaultLocationClientOption(){
		if(mOption == null){
			mOption = new LocationClientOption();
			mOption.setLocationMode(LocationMode.Hight_Accuracy);//鍙�锛岄粯璁ら珮绮惧害锛岃缃畾浣嶆ā寮忥紝楂樼簿搴︼紝浣庡姛鑰楋紝浠呰澶�			mOption.setCoorType("bd09ll");//鍙�锛岄粯璁cj02锛岃缃繑鍥炵殑瀹氫綅缁撴灉鍧愭爣绯伙紝濡傛灉閰嶅悎鐧惧害鍦板浘浣跨敤锛屽缓璁缃负bd09ll;
			mOption.setScanSpan(3000);//鍙�锛岄粯璁�锛屽嵆浠呭畾浣嶄竴娆★紝璁剧疆鍙戣捣瀹氫綅璇锋眰鐨勯棿闅旈渶瑕佸ぇ浜庣瓑浜�000ms鎵嶆槸鏈夋晥鐨�		    mOption.setIsNeedAddress(true);//鍙�锛岃缃槸鍚﹂渶瑕佸湴鍧�俊鎭紝榛樿涓嶉渶瑕�		    mOption.setIsNeedLocationDescribe(true);//鍙�锛岃缃槸鍚﹂渶瑕佸湴鍧�弿杩�		    mOption.setNeedDeviceDirect(false);//鍙�锛岃缃槸鍚﹂渶瑕佽澶囨柟鍚戠粨鏋�		    mOption.setLocationNotify(false);//鍙�锛岄粯璁alse锛岃缃槸鍚﹀綋gps鏈夋晥鏃舵寜鐓�S1娆￠鐜囪緭鍑篏PS缁撴灉
		    mOption.setIgnoreKillProcess(true);//鍙�锛岄粯璁rue锛屽畾浣峉DK鍐呴儴鏄竴涓猄ERVICE锛屽苟鏀惧埌浜嗙嫭绔嬭繘绋嬶紝璁剧疆鏄惁鍦╯top鐨勬椂鍊欐潃姝昏繖涓繘绋嬶紝榛樿涓嶆潃姝�  
		    mOption.setIsNeedLocationDescribe(true);//鍙�锛岄粯璁alse锛岃缃槸鍚﹂渶瑕佷綅缃涔夊寲缁撴灉锛屽彲浠ュ湪BDLocation.getLocationDescribe閲屽緱鍒帮紝缁撴灉绫讳技浜庘�鍦ㄥ寳浜ぉ瀹夐棬闄勮繎鈥�		    mOption.setIsNeedLocationPoiList(true);//鍙�锛岄粯璁alse锛岃缃槸鍚﹂渶瑕丳OI缁撴灉锛屽彲浠ュ湪BDLocation.getPoiList閲屽緱鍒�		    mOption.SetIgnoreCacheException(false);//鍙�锛岄粯璁alse锛岃缃槸鍚︽敹闆咰RASH淇℃伅锛岄粯璁ゆ敹闆�		}
		
		}return mOption;
		
	}
	
	public void start(){
		synchronized (objLock) {
			if(client != null && !client.isStarted()){
				client.start();
			}
		}
	}
	public void stop(){
		synchronized (objLock) {
			if(client != null && client.isStarted()){
				client.stop();
			}
		}
	}
	
}
