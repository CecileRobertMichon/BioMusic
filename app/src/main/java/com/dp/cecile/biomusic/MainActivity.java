package com.dp.cecile.biomusic;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, MidiDriver.OnMidiStartListener {

    private Timer mTimer;            //used to update UI
    private TimerTask mTimerTask;    //used to update UI
    private Handler mHandler;

    public float[] mData;            //used to keep/update data from device's channels
    ArrayList<Float> HR_data = new ArrayList<Float>();
    ArrayList<Float> BVP_data = new ArrayList<Float>();
    ArrayList<Float> SC_data = new ArrayList<Float>();
    ArrayList<Float> TEMP_data = new ArrayList<Float>();

    private BluetoothDialog mBluetoothDialog;    //used to show dialogs to chose the device
    private ManagerDevice mManagerDevice;        //used to manager informations from framework
    protected MidiDriver midi;
    protected MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mData = new float[Data.ARRAY_SIZE];
        mHandler = new Handler();

        //manager device
        mManagerDevice = new ManagerDevice(this);

        //create bluetooth dialog service
        mBluetoothDialog = new BluetoothDialog(this, mManagerDevice.getDeviceService());

        // Create midi driver
        midi = new MidiDriver();

        // Set on midi start listener
        if (midi != null)
            midi.setOnMidiStartListener(this);

        //Set on click listener
        View v = findViewById(R.id.emotion_happy);
        if (v != null)
            Log.d("clicked", "on click listener happy");
            v.setOnClickListener(this);

        v = findViewById(R.id.emotion_angry);
        if (v != null)
            v.setOnClickListener(this);

        //Set on touch listener
        v = findViewById(R.id.emotion_neutral);
        if (v != null)
            v.setOnTouchListener(this);

        v = findViewById(R.id.emotion_sad);
        if (v != null)
            v.setOnTouchListener(this);

//        Button playButton = (Button) this.findViewById(R.id.emotion_happy);
//        playButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
//                    // do MIDI stuff
//                }
//            }
//        });
    }


    @Override
    protected void onDestroy() {

        //stop update UI
        stopUpdateUIDataTimer();

        // Stop midi
        if (midi != null)
            midi.stop();

        // Stop player
        if (player != null)
            player.stop();

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

    /**
     * Show Toast message to UI
     *
     * @param message
     */

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
            clearTextView();
            mManagerDevice.getDeviceService().disconnect();
        } else if (id == R.id.connect_device) {
            if (mManagerDevice.getDeviceService().isBTEnabled()) {
                //show list of devices paired
                mBluetoothDialog.showDeviceList();
            } else {
                // enable Bluetooth first and show list of devices paired
                mBluetoothDialog.showEnableBTDialog();
            }
        } else if (id == R.id.start_midi){
            if (midi != null)
                midi.start();
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



    @Override
    public void onClick(View v)
    {
        int id = v.getId();

        switch (id)
        {
            case R.id.emotion_happy:
                Log.d("clicked", "happy click");
                if (player != null) {
                    player.stop();
                }
                break;

            case R.id.emotion_angry:
                Log.d("clicked", "angry click");
                if (player != null)
                {
                    player.stop();
                    player.release();
                }
                player = MediaPlayer.create(this, R.raw.ants);
                player.start();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event){
        int action = event.getAction();
        int id = v.getId();

        switch (action)
        {
            // Down
            case MotionEvent.ACTION_DOWN:
                switch (id)
                {
                    case R.id.emotion_neutral:
                        sendMidi(0x90, 48, 63);
                        sendMidi(0x90, 52, 63);
                        sendMidi(0x90, 55, 63);
                        break;

                    case R.id.emotion_sad:
                        sendMidi(0x90, 55, 63);
                        sendMidi(0x90, 59, 63);
                        sendMidi(0x90, 62, 63);
                        break;

                    default:
                        return false;
                }
                break;

            // Up

            case MotionEvent.ACTION_UP:
                switch (id)
                {
                    case R.id.emotion_neutral:
                        sendMidi(0x80, 48, 0);
                        sendMidi(0x80, 52, 0);
                        sendMidi(0x80, 55, 0);
                        break;

                    case R.id.emotion_sad:
                        sendMidi(0x80, 55, 0);
                        sendMidi(0x80, 59, 0);
                        sendMidi(0x80, 62, 0);
                        break;

                    default:
                        return false;
                }
                break;

            default:
                return false;
        }

        return false;
    }


    /**
     * Update the UI
     */
    private void updateUI() {
        //add data to the array lists
        SC_data.add(mData[Data.TYPE_SC]);
        TEMP_data.add(mData[Data.TYPE_TEMP]);
        HR_data.add(mData[Data.TYPE_HR]);
        BVP_data.add(mData[Data.TYPE_BVP]);

        //remove data once arraylist has more than 50 elements
        //this keeps the size of the window limited to 50 elements
        if (SC_data.size() > 50 ){
            SC_data.remove(0);
            TEMP_data.remove(0);
            HR_data.remove(0);
            BVP_data.remove(0);
        }

        if (SC_data.size() < 25){
            ((TextView) findViewById(R.id.init_label)).setText(String.format("Initializing..."));
        } else {
            ((TextView) findViewById(R.id.init_label)).setText(String.format(""));
        }

        ((TextView) findViewById(R.id.skin_conductance_value)).setText(String.format("%.2f", mData[Data.TYPE_SC]));
        ((TextView) findViewById(R.id.temperature_value)).setText(String.format("%.2f", mData[Data.TYPE_TEMP]));
        ((TextView) findViewById(R.id.heart_rate_value)).setText(String.format("%.2f", mData[Data.TYPE_HR]));
        //((TextView) findViewById(R.id.heart_rate_value)).setText(String.format("%d", BVP_data.size()));

    }

    // play heart rate sound
    private void playHR() {
        float hr = mData[Data.TYPE_HR];
        long interval = 0;
        if (hr != 0) {
            interval = (long) 60000.0 / (long) hr;
        }
        Log.d("Music","interval is : " + interval);
        try {
            sleep(interval);
            Log.d("Music", "Play sound");
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //vibrate phone according to BVP signal
    private void playBVP() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int i = 0;
        int samp_size = 25;
        boolean ascending;

        if (BVP_data.size() > samp_size) {
            if ( BVP_data.get(i) < BVP_data.get(i+1) ) {
                ascending = true;
            }  else {
                ascending = false;
            }

            while (ascending) {
                while (BVP_data.get(i) < BVP_data.get(i+1)){
                    i += 1;
                    if (i == samp_size - 1) {
                        i = i - samp_size;
                    }
                }
                vibrator.vibrate(100);
                ascending = false;
            }

            while (!ascending) {
                while (BVP_data.get(i) > BVP_data.get(i+1)){
                    i += 1;
                    if (i == samp_size - 1) {
                        i = i - samp_size;
                    }
                }
                ascending = true;
            }

        }

    }


    /**
     * Reset all TextView.
     */
    private void clearTextView() {
        ((TextView) findViewById(R.id.skin_conductance_value)).setText("_ _");
        ((TextView) findViewById(R.id.temperature_value)).setText("_ _");
        ((TextView) findViewById(R.id.heart_rate_value)).setText("_ _");
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

    Runnable mMusic = new Runnable() {
        @Override
        public void run() {
            try {
                playBVP();
                //playHR();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mMusic, 300);
            }
        }
    };

    public void startMusic() { mMusic.run(); }

    public void stopMusic() {
        mHandler.removeCallbacks(mMusic);
    }

    // Listener for sending initial midi messages when the Sonivox
    // synthesizer has been started, such as program change.

    @Override
    public void onMidiStart()
    {
        // Program change - harpsicord

        sendMidi(0xc0, 6);

    }

    // Send a midi message

    protected void sendMidi(int m, int p)
    {
        byte msg[] = new byte[2];

        msg[0] = (byte) m;
        msg[1] = (byte) p;

        midi.write(msg);
    }

    // Send a midi message

    protected void sendMidi(int m, int n, int v)
    {
        byte msg[] = new byte[3];

        msg[0] = (byte) m;
        msg[1] = (byte) n;
        msg[2] = (byte) v;

        midi.write(msg);
    }
}
