package com.dp.cecile.biomusic;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer mTimer;			//used to update UI
    private TimerTask mTimerTask;	//used to update UI

    public float[] mData;			//used to keep/update data from device's channels
    public String deviceName;		//use to have the device name chosen

    private BluetoothDialog mBluetoothDialog;	//used to show dialogs to chose the device
    private ManagerDevice mManagerDevice;		//used to manager informations from framework

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // mData = new float[Data.ARRAY_SIZE];

        //manager device
        mManagerDevice = new ManagerDevice(this);

        //create bluetooth dialog service
        mBluetoothDialog = new BluetoothDialog(this, mManagerDevice.getDeviceService());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.connect_device) {
            if (mManagerDevice.getDeviceService().isBTEnabled()) {
                //show list of devices paired
                mBluetoothDialog.showDeviceList();
            } else {
                // enable Bluetooth first and show list of devices paired
                mBluetoothDialog.showEnableBTDialog();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
