package com.dp.cecile.biomusic;

import android.os.Bundle;

import com.thoughttechnology.api.listener.BvpListener;
import com.thoughttechnology.api.listener.DataListener;
import com.thoughttechnology.api.listener.DeviceStateChangeListener;
import com.thoughttechnology.api.listener.HeartRateListener;
import com.thoughttechnology.api.listener.SkinConductanceListener;
import com.thoughttechnology.api.listener.TemperatureListener;
import com.thoughttechnology.api.service.DeviceService;
import com.thoughttechnology.api.utils.Constants.DeviceStatus;
import com.thoughttechnology.api.utils.Constants.DeviceType;

import java.text.DateFormat;
import java.util.Date;


/**
 * This class is a bridge between UI and Framework. 
 * The communication to Framework is established using DeviceService class. 
 * 
 * Listeners are registered to Framework allowing this class receives data as BVP, 
 * Temperature, StressIndex, HeartRate, SkinConductance, Blood Volume Pulse,      
 * Very Low Frequency, Low Frequency and High Frequency.
 * 
 * The device state is reached by using the listener DeviceStateChangeListener.
 * 
 */
public class ManagerDevice implements DataListener, DeviceStateChangeListener {

	private DeviceService mDeviceService;
	private MainActivity mActivity;
	private float currentTemp;
    private float currentEda;
	private SmoothData bvpBuffer = new SmoothData(MusicConstants.WINDOW_SIZE);
	private SmoothData tempBuffer = new SmoothData(MusicConstants.WINDOW_SIZE);
	private SmoothData edaBuffer = new SmoothData(MusicConstants.WINDOW_SIZE);

	public ManagerDevice(MainActivity activity) {

		//init Tps Service
		mDeviceService = new DeviceService(activity, DeviceType.TTL_TPS);
		mActivity = activity;
		
	}
	
	/**
	 * Get DeviceService 
	 * @return
	 */
	public DeviceService getDeviceService() {
		return mDeviceService;
	}
	
	
	/**
	 * Make connection to the device. 
	 * @param name Device chosen
	 */
	public void connectDevice(String name) {
		//do connection
		mDeviceService.connect(mDeviceService.getMacAddressByName(name), this);
	}

	
	
	/**
	 * Implementation method for DeviceStateChangeListener.
	 * Here we have all current events fired by the device. 
	 * 
	 * @param newState Represents the current state fired.  
	 * @param bundle Load information.
	 */
	@Override
	public void onDeviceStateChange(DeviceStatus newState, Bundle bundle) {
		
		switch (newState) {
		
		case CONNECT_SUCCESS:
			registerTpsDataListener();			//register listeners
			mActivity.startTime = DateFormat.getDateTimeInstance().format(new Date());
			mActivity.startUpdateUIDataTimer();	//start timer to update UI
			mActivity.startMusic();
			mActivity.toastMessage("Connected successfully!");
			break;
			
		case CONNECT_FAILURE:
			mActivity.toastMessage("Failed to connect ");
			break;
		
		case DISCONNECTED:
			mActivity.stopTime = DateFormat.getDateTimeInstance().format(new Date());
			mActivity.stopUpdateUIDataTimer();
			mActivity.stopMusic();
			mActivity.toastMessage("Disconnected.");
			break;
		default:
			break;
		}
	}

	
	/**
	 * Register listeners to receive data from device.
	 * Here we have all current data fired by the device.
	 */
	private void registerTpsDataListener() {

		 mDeviceService.addListener(new HeartRateListener() {
			@Override
			public void onHeartRate(float hr) {
				//receive HeartRate data
				mActivity.mData[Data.TYPE_HR] = hr;
                mActivity.getMusicMaker().addHR_data(hr);
			}
		});
		
		mDeviceService.addListener(new SkinConductanceListener() {
			@Override
			public void onSkinConductance(float sc) {
				//receive SkinConductance data
                edaBuffer.add(sc);
                float average = edaBuffer.getAverage();
				mActivity.mData[Data.TYPE_SC] = average;
                currentEda = average;
			}
		});
		
		mDeviceService.addListener(new TemperatureListener() {
			@Override
			public void onTemperature(float temp) {
				//receive Temperature data
                tempBuffer.add(temp);
                float average = tempBuffer.getAverage();
				mActivity.mData[Data.TYPE_TEMP] = average;
                currentTemp = average;
			}
		});
		
		mDeviceService.addListener(new BvpListener() {
			@Override
			public void onBvp(float bvp) {
				//receive Blood Volume Pulse data
                bvpBuffer.add(bvp);
                float average = bvpBuffer.getAverage();
				mActivity.mData[Data.TYPE_BVP] = average;
                mActivity.getMusicMaker().addBVP_data(average);
				mActivity.getMusicMaker().addBVP_data_string(String.format("%.2f", average));
                // add temp
                mActivity.getMusicMaker().addTEMP_data(currentTemp);
                mActivity.getMusicMaker().addTEMP_data_string(String.format("%.2f",currentTemp));
                // add eda
                mActivity.getMusicMaker().addSC_data(currentEda);
				mActivity.getMusicMaker().addSC_data_string(String.format("%.2f",currentEda));
			}
		});
	}

}
