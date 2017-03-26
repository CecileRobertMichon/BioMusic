package com.dp.cecile.biomusic;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Timer mTimer;            //used to update UI
    private TimerTask mTimerTask;    //used to update UI

    public float[] mData;            //used to keep/update data from device's channels

    private BluetoothDialog mBluetoothDialog;    //used to show dialogs to chose the device
    private ManagerDevice mManagerDevice;        //used to manager informations from framework
    private FileManager mFileManager;
    private MusicMaker mMusicMaker;
    private MidiGenerator mMidiGenerator;

    private Switch switchMusic;
    private Button startButton;
    private Button stopButton;
    private Boolean musicOn;
    private TextView init;

    // get start time when connection is made and stop time when connection is stopped
    // this is for time stamping the text file
    public String startTime;
    public String stopTime;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mFragmentTitles;

    SQLiteHelper sqLiteHelper;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getPreferences(Context.MODE_PRIVATE);
        if (prefs.getBoolean("firstLaunch", true)) {
            prefs.edit().putBoolean("firstLaunch", false).commit();
            startActivity(new Intent(getApplicationContext(), HelpActivity.class));
        }

        mTitle = mDrawerTitle = getTitle();
        mFragmentTitles = new String[2];
        mFragmentTitles[0] = "Home";
        mFragmentTitles[1] = "History";
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item, mFragmentTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        // new ActionBarDrawerToggle(this, dl, R.string.drawer_open, R.string.drawer_close)
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

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

        switchMusic = (Switch) findViewById(R.id.switch1);
        musicOn = true;
        init = (TextView) findViewById(R.id.init_label);

        if (switchMusic != null) {
            switchMusic.setOnCheckedChangeListener(this);
        }

        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        this.sqLiteHelper = SQLiteHelper.getInstance(MainActivity.this);

    }

    public void showSaveSessionDialog() {
        SessionDialog dialog = new SessionDialog();
        dialog.show(getSupportFragmentManager(), "session dialog");

    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        if (position == 1) {
            Intent i = new Intent(this, HistoryActivity.class);
            startActivity(i);
        }
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mFragmentTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Start midi

        if (mMidiGenerator.getMidi() != null)
            mMidiGenerator.startMidi();
    }

    @Override
    protected void onPause() {
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

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Toast.makeText(this, "Biomusic turned " + (isChecked ? "on" : "off"),
                Toast.LENGTH_SHORT).show();
        if (isChecked) {
            musicOn = true;
        } else {
            musicOn = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_signals) {
            mFileManager.showConnectToDrive();
        } else if (id == R.id.help) {
            startActivity(new Intent(getApplicationContext(), HelpActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void connectDevice(String device) {
        if (device == null || device.isEmpty()) {
            toastMessage("Please select device first!");
            return;
        }
        clearTextView();
        mMusicMaker.resetSignals();
        mManagerDevice.connectDevice(device);
    }

    /**
     * Update the UI
     */
    private void updateUI() {

        if (mMusicMaker.getBVP_data().size() < 5000) {
            init.setText("Initializing...");
        } else {
            init.setText("");
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

    public void switchButtons() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Button start = (Button) findViewById(R.id.start_button);
                Button stop = (Button) findViewById(R.id.stop_button);
                if (start.getVisibility() == View.VISIBLE)
                {
                    start.setVisibility(View.GONE);
                    stop.setVisibility(View.VISIBLE);
                } else {
                    start.setVisibility(View.VISIBLE);
                    stop.setVisibility(View.GONE);
                }
            }
        });
    }

    public void startMusic() {

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

        if(musicOn) {
            mMusicMaker.resume();
            mMusic.start();
        }
    }

    public void stopMusic() {
        mMusicMaker.shutDown();
        clearTextView();
        this.mData = new float[Data.ARRAY_SIZE];
    }

    @Override
    public void onClick(View v) {
        // do something when the button is clicked
        // Yes we will handle click here but which button clicked??? We don't know

        // So we will make
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.start_button:
                if (mManagerDevice.getDeviceService().isBTEnabled()) {
                    //show list of devices paired
                    mBluetoothDialog.showDeviceList();
                } else {
                    // enable Bluetooth first and show list of devices paired
                    mBluetoothDialog.showEnableBTDialog();
                }
                break;
            case R.id.stop_button:
                mManagerDevice.getDeviceService().disconnect();
                break;
            default:
                break;
        }
    }

}
