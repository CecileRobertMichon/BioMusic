package com.dp.cecile.biomusic;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer mTimer;            //used to update UI
    private TimerTask mTimerTask;    //used to update UI

    public float[] mData;            //used to keep/update data from device's channels

    private BluetoothDialog mBluetoothDialog;    //used to show dialogs to chose the device
    private ManagerDevice mManagerDevice;        //used to manager informations from framework
    private FileManager mFileManager;
    private MusicMaker mMusicMaker;
    private MidiGenerator mMidiGenerator;

    // get start time when connection is made and stop time when connection is stopped
    // this is for time stamping the text file
    public String startTime;
    public String stopTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mData = new float[Data.ARRAY_SIZE];

        //manager device
        mManagerDevice = new ManagerDevice(this);

        //create bluetooth dialog service
        mBluetoothDialog = new BluetoothDialog(this, mManagerDevice.getDeviceService());

        // create file manager
        mFileManager = new FileManager(this);

        // create music maker
        mMusicMaker = new MusicMaker(this);

        // create midi generator
        mMidiGenerator = new MidiGenerator(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Start midi

        if (mMidiGenerator.getMidi() != null)
            mMidiGenerator.startMidi();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Stop midi

        if (mMidiGenerator.getMidi() != null)
            mMidiGenerator.stopMidi();
    }

    @Override
    protected void onDestroy() {

        //stop update UI
        stopUpdateUIDataTimer();

        // Stop midi
        if (mMidiGenerator.getMidi() != null)
            mMidiGenerator.stopMidi();

        // stop music
        stopMusic();

        //destroy manager device framework
        if (mManagerDevice.getDeviceService() != null)
            mManagerDevice.getDeviceService().onDestroy();

        super.onDestroy();
    }

    /**
     * Return it self.
     *
     * @return MainActivity instance
     */
    private Activity getSelf() {
        return this;
    }


    public MusicMaker getMusicMaker() {
        return mMusicMaker;
    }

    public void toastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getSelf(), message, Toast.LENGTH_SHORT).show();
            }
        });
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
        if (id == R.id.disconnect_device) {
            mManagerDevice.getDeviceService().disconnect();
        } else if (id == R.id.connect_device) {
            if (mManagerDevice.getDeviceService().isBTEnabled()) {
                //show list of devices paired
                mBluetoothDialog.showDeviceList();
            } else {
                // enable Bluetooth first and show list of devices paired
                mBluetoothDialog.showEnableBTDialog();
            }
        } else if (id == R.id.save_signals) {
            mFileManager.showConnectToDrive();
        } else if (id == R.id.reset_signals) {
            mMusicMaker.resetSignals();
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectDevice(String device) {
        if (device == null || device.isEmpty()) {
            toastMessage("Please select device first!");
            return;
        }
        clearTextView();
        mManagerDevice.connectDevice(device);
    }

    /**
     * Update the UI
     */
    private void updateUI() {

        if (mMusicMaker.getBVP_data().size() < 5000){
            ((TextView) findViewById(R.id.init_label)).setText(String.format("Initializing..."));
        } else {
            ((TextView) findViewById(R.id.init_label)).setText(String.format(""));
            ((TextView) findViewById(R.id.skin_conductance_value)).setText(String.format("%.2f", mData[Data.TYPE_SC]));
            ((TextView) findViewById(R.id.temperature_value)).setText(String.format("%.2f", mData[Data.TYPE_TEMP]));
            ((TextView) findViewById(R.id.heart_rate_value)).setText(String.format("%.2f", mData[Data.TYPE_HR]));
        }
    }

    /**
     * Reset all TextView.
     */
    private void clearTextView() {
        ((TextView) findViewById(R.id.skin_conductance_value)).setText("_ _");
        ((TextView) findViewById(R.id.temperature_value)).setText("_ _");
        ((TextView) findViewById(R.id.heart_rate_value)).setText("_ _");
        ((TextView) findViewById(R.id.init_label)).setText(String.format(""));

    }

    ///// TIMER TO UPDATE UI FROM DEVICE'S VALUES /////

    /**
     * Start timer to update UI using data received from device
     */
    public void startUpdateUIDataTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                getSelf().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 0, 300);
    }

    /**
     * Stop time to update the UI
     */
    public void stopUpdateUIDataTimer() {
        if (null != mTimerTask) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (null != mTimer) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    Thread mMusic = new Thread() {
        @Override
        public void run() {
            try {
                    while (mMusicMaker.getBVP_data().size() < 5000) {
                        // wait
                    }
                    if (!isInterrupted()) {
                        mMusicMaker.parseData();
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void startMusic() {
        mMusic.start();
    }

    public void stopMusic() {
        mMusic.interrupt(); //does this do anything?
        mMusicMaker.shutDown();
        clearTextView();
    }

}
