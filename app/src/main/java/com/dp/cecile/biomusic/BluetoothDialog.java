package com.dp.cecile.biomusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;

import com.thoughttechnology.api.listener.BTEnableListener;
import com.thoughttechnology.api.service.DeviceService;

import java.util.List;


/**
 * Managing list of Bluetooth devices paired. 
 * Enabling Bluetooth when it's off. 
 *
 */
public class BluetoothDialog {

	private Activity mActivity;
	
	private ProgressDialog mProgressDialog;
	private DeviceService mDeviceService;

	private String mDeviceName;
	
	/**
	 * Constructor
	 * @param activity Current application activity
	 */
	public BluetoothDialog(Activity activity, DeviceService deviceService) {
		mActivity = activity;

		//init Tps Service
		mDeviceService = deviceService;

	}
	

	/**
	 * Check if the Bluetooth is off and asking to turn it on.
	 */
	public void showEnableBTDialog() {
		
		new AlertDialog.Builder(mActivity)
		.setTitle("Bluetooth")
		.setMessage("Bluetooth is off, turn it on?")
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				mProgressDialog = ProgressDialog.show(mActivity, "Bluetooth", "Turning on Bluetooth...", true);
				
				
				mDeviceService.enableBT(new BTEnableListener(){
					@Override
					public void onBTEnabled() {
						// dismiss dialog
		            	if (mProgressDialog != null) {
		            		mProgressDialog.dismiss();
		            		mProgressDialog = null;
		            	}
		            	// connect Tps device
		            	showDeviceList();
					}});
	        }
		})
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();
	}

	/**
	 * Presenting by dialog all paired devices
	 */
	public void showDeviceList() {

		// dialog to pick paired devices
		final List<String> deviceList = mDeviceService.getDeviceNameList();
		
		showListDialog("Please select a device:", deviceList, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				
				// update device
				mDeviceName = (deviceList.get(which));

                // connect to device
                ((MainActivity)mActivity).connectDevice(mDeviceName);
			}
		});
	}

	
	/**
	 * Showing all TPS paired devices
	 * @param title dialog title
	 * @param deviceList list of devices paired
	 * @param onClickListener onClick button
	 */
	private void showListDialog(String title, List<String> deviceList, DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(mActivity);
		
		dlgBuilder.setIcon(android.R.drawable.ic_dialog_info).setTitle(title);
		
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_expandable_list_item_1);
		arrayAdapter.addAll(deviceList);

		dlgBuilder.setNegativeButton(mActivity.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dlgBuilder.setAdapter(arrayAdapter, onClickListener);
			
		dlgBuilder.show();
	}
}
