package com.dp.cecile.biomusic;

import android.os.Bundle;

import com.thoughttechnology.api.listener.BvpListener;
import com.thoughttechnology.api.listener.DataListener;
import com.thoughttechnology.api.listener.DeviceStateChangeListener;
import com.thoughttechnology.api.listener.HeartRateListener;
import com.thoughttechnology.api.listener.HrvListener;
import com.thoughttechnology.api.listener.SkinConductanceListener;
import com.thoughttechnology.api.listener.StressIndexListener;
import com.thoughttechnology.api.listener.TemperatureListener;
import com.thoughttechnology.api.service.DeviceService;
import com.thoughttechnology.api.utils.Constants.DeviceStatus;
import com.thoughttechnology.api.utils.Constants.DeviceType;


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
			mActivity.startUpdateUIDataTimer();	//start timer to update UI
			mActivity.startMusic();
			mActivity.toastMessage("Connected successfully!");
			break;
			
		case CONNECT_FAILURE:
			mActivity.toastMessage("Failed to connect ");
			break;
		
		case DISCONNECTED:
			mActivity.stopUpdateUIDataTimer();
			mActivity.stopMusic();
			mActivity.toastMessage("Disconnected.");
			break;
		
	/*	case BATTERY_STATE_CHANGE:
			mActivity.showBatteryInfo(bundle.getInt(DeviceStatus.BATTERY_STATE_CHANGE.name()));
			break;
		
		case MOVEMENT_START:
			mActivity.showMovementInfo(true);
			break;
		
		case MOVEMENT_STOP:
			mActivity.showMovementInfo(false);
			break;
		
		case CONTACT_OFF:
			mActivity.showTpsContactInfo(true);
			break;
		
		case CONTACT_NORMAL:
			mActivity.showTpsContactInfo(false);			
			break;
		
		case CONTACT_UNKNOWN:
			mActivity.showTpsContactInfo(false);
			break; */
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
                if (mActivity.getMusicMaker().getHR_data().size() > 20 )
                    mActivity.getMusicMaker().removeFirst("hr");
			}
		});
		
		mDeviceService.addListener(new SkinConductanceListener() {
			@Override
			public void onSkinConductance(float sc) {
				//receive SkinConductance data
				mActivity.mData[Data.TYPE_SC] = sc;
                mActivity.getMusicMaker().addSC_data(sc);
                if (mActivity.getMusicMaker().getSC_data().size() > 100000 )
                    mActivity.getMusicMaker().removeFirst("sc");
			}
		});
		
//		mDeviceService.addListener(new StressIndexListener() {
//			@Override
//			public void onStressIndex(float si) {
//				//receive StressIndex data
//				mActivity.mData[Data.TYPE_SI] = si;
//			}
//		});
		
		mDeviceService.addListener(new TemperatureListener() {
			@Override
			public void onTemperature(float temp) {
				//receive Temperature data
				mActivity.mData[Data.TYPE_TEMP] = temp;
                mActivity.getMusicMaker().addTEMP_data(temp);
                if (mActivity.getMusicMaker().getTEMP_data().size() > 100000 )
                    mActivity.getMusicMaker().removeFirst("temp");
			}
		});
		
		mDeviceService.addListener(new BvpListener() {
			@Override
			public void onBvp(float bvp) {
				//receive Blood Volume Pulse data
				mActivity.mData[Data.TYPE_BVP] = bvp;
                mActivity.getMusicMaker().addBVP_data(bvp);
                if (mActivity.getMusicMaker().getBVP_data().size() > 100000 )
                    mActivity.getMusicMaker().removeFirst("bvp");
			}
		});
		
//		mDeviceService.addListener(new HrvListener() {
//			@Override
//			public void onHrv(float vlf, float lf, float hf) {
//				//receive Very Low Frequency data
//				mActivity.mData[Data.TYPE_VLF] = vlf;
//				//receive Low Frequency data
//				mActivity.mData[Data.TYPE_LF]  = lf;
//				//receive High Frequency data
//				mActivity.mData[Data.TYPE_HF]  = hf;
//			}
//		});
	}

}
