package com.dp.cecile.biomusic;

import android.util.Log;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Created by cecilerobertm on 2016-11-12.
 */

public class MusicMaker {

    private ArrayList<Float> BVP_data = new ArrayList<Float>();
    private ArrayList<String> BVP_data_string = new ArrayList<String>();
    private ArrayList<Float> SC_data = new ArrayList<Float>();
    private ArrayList<String> SC_data_string = new ArrayList<String>();
    private ArrayList<Float> TEMP_data = new ArrayList<Float>();
    private ArrayList<String> TEMP_data_string = new ArrayList<String>();
    private ArrayList<Float> HR_data = new ArrayList<Float>();
    private int oldNote1 = 60, oldNote2 = 64, oldNote3 = 67; //middle C

    // TODO : remove unused variables
    private int offset;
    private int temperatureOffset;
    private int duration;
    private int middleCOffset;
    private double tempSlope;
    private double tempIntercept;
    private int tempFirstPoint;

    private float tempSSThreshhold;
    private boolean playLive;
    //        sb,
    private boolean playing;
    private boolean keepPlaying;
     //       playingThread,
     //       scaleType;

    private MainActivity mActivity;

    public MusicMaker(MainActivity activity) {

        mActivity = activity;
        offset = 0;
        temperatureOffset = 0;
        duration = MusicConstants.DEFAULT_DURATION;
//                ,_scale_type(scale_type)
        middleCOffset = MusicConstants.INITIALIZATION_NUMBER;
        tempSlope = MusicConstants.INITIALIZATION_NUMBER;
        tempIntercept = MusicConstants.INITIALIZATION_NUMBER;
        tempFirstPoint = MusicConstants.INITIALIZATION_NUMBER;
        tempSSThreshhold = 0.002F;
//                ,_play_live(play_live)
//                ,_sb(NULL)
        playing = false;
        keepPlaying = false;
//                ,_playing_thread(NULL)

    }

    public ArrayList<Float> getBVP_data() {
        return BVP_data;
    }
    public void addBVP_data(float bvp) {
        this.BVP_data.add(bvp);
    }
    public ArrayList<String> getBVP_data_string() {
        return BVP_data_string;
    }
    public void addBVP_data_string(String bvp) {this.BVP_data_string.add(bvp);}

    public ArrayList<Float> getSC_data() {
        return SC_data;
    }
    public void addSC_data(float sc) {
        this.SC_data.add(sc);
    }
    public ArrayList<String> getSC_data_string() {
        return SC_data_string;
    }
    public void addSC_data_string(String bvp) {this.SC_data_string.add(bvp);}

    public ArrayList<Float> getTEMP_data() {
        return TEMP_data;
    }
    public void addTEMP_data(float temp) {
        this.TEMP_data.add(temp);
    }
    public ArrayList<String> getTEMP_data_string() {
        return TEMP_data_string;
    }
    public void addTEMP_data_string(String bvp) {this.TEMP_data_string.add(bvp);}

    public ArrayList<Float> getHR_data() {
        return HR_data;
    }
    public void addHR_data(float hr) {
        this.HR_data.add(hr);
    }

    public void removeFirst(String type) {
        switch (type) {
            case "bvp":
                this.BVP_data.remove(0);
                break;
            case "temp":
                this.TEMP_data.remove(0);
                break;
            case "hr":
                this.HR_data.remove(0);
                break;
            case "sc":
                this.SC_data.remove(0);
                break;
            default:
                break;
        }
    }


    //initialize music maker in middle C
    public void initMusic() {
        mActivity.sendMidi(0x91, oldNote1, 60);
        mActivity.sendMidi(0x91, oldNote2, 60);
        mActivity.sendMidi(0x91, oldNote3, 60);
    }

    //play beat according to HR signal
    public void playBeat() {
        if (HR_data.size() > 10) {
            float hr = HR_data.get(0);
            long drum_freq = 0;
            long drum_duration = 50;
            if (hr != 0) {
                drum_freq = 4000000 / ((long) hr * (long) hr);
            }
            mActivity.sendMidi(0x90, 45, 127);
            try {
                sleep(drum_duration);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mActivity.sendMidi(0x80, 45, 0);
            try {
                sleep(drum_freq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //play melody according to EDA signal
    public void playMelody() {
        if ( SC_data.size() > 200 ) {
            float eda_old = SC_data.get(0);
            float eda_new = SC_data.get(100);
            float m = 0.001f;
            if ((eda_new - eda_old) / 100 > m) {
                mActivity.sendMidi(0x81, oldNote1, 0);
                mActivity.sendMidi(0x81, oldNote2, 0);
                mActivity.sendMidi(0x81, oldNote3, 0);
                mActivity.sendMidi(0x91, oldNote1 + 2, 60);
                mActivity.sendMidi(0x91, oldNote2 + 2, 60);
                mActivity.sendMidi(0x91, oldNote3 + 2, 60);
                oldNote1 = oldNote1 + 2;
                oldNote2 = oldNote2 + 2;
                oldNote3 = oldNote3 + 2;
            } else if ((eda_new - eda_old) / 100 < -m) {
                mActivity.sendMidi(0x81, oldNote1, 0);
                mActivity.sendMidi(0x81, oldNote2, 0);
                mActivity.sendMidi(0x81, oldNote3, 0);
                mActivity.sendMidi(0x91, oldNote1 - 2, 60);
                mActivity.sendMidi(0x91, oldNote2 - 2, 60);
                mActivity.sendMidi(0x91, oldNote3 - 2, 60);
                oldNote1 = oldNote1 - 2;
                oldNote2 = oldNote2 - 2;
                oldNote3 = oldNote3 - 2;
            } else {
                mActivity.sendMidi(0x91, oldNote1, 60);
                mActivity.sendMidi(0x91, oldNote2, 60);
                mActivity.sendMidi(0x91, oldNote3, 60);
            }
        }

    }

    //play harmony according to temp signal
    public void checkTemperature(int index) {
        if(tempFirstPoint == MusicConstants.INITIALIZATION_NUMBER)
        {
            // Calculate from the beginning
            if(index > 5)
            {
                setSlopeAndIntercept(0);
            }
            return; // don't change the offset for this one.
        }

        double sum_squares = 0;
        for(int i = tempFirstPoint; i <= index; ++i)
        {
            double diff = (this.TEMP_data.get(i) - (tempSlope * i + tempIntercept));
            sum_squares += diff * diff;
        }
        sum_squares /= (index - tempFirstPoint + 1);

        if(sum_squares > tempSSThreshhold)
        {
            double old_slope = tempSlope;
            // Calculate the new slope, using the last 5 data-points.
            setSlopeAndIntercept(index - 4);
            Log.d("Temperature", "Adjusted slope! New slope: " + tempSlope);

            stopTempChords(index);

            if(old_slope < tempSlope)
                --temperatureOffset; // down a semitone
            else
                ++temperatureOffset; // up a semitone

            startTempChords(index);
        }
    }

    private void setSlopeAndIntercept(int startingIndex)
    {
        double Sx = 0;
        double Sy = 0;
        double Sxy = 0;
        double Sx2 = 0;

        // Populate the above sums
        for(int i = 0; i < 5; ++i)
        {
            float this_temp = this.TEMP_data.get(startingIndex + i);
            Sx += startingIndex + i;
            Sy += this_temp;
            Sxy += this_temp * (startingIndex + i);
            Sx2 += (startingIndex + i)*(startingIndex + i);
        }

        // Now reset our variables
        tempSlope = (5*Sxy - Sx*Sy)/(5*Sx2 - Sx*Sx);
        tempIntercept = (Sy - tempSlope*Sx)/5;
        // Finally, update when we were last here.
        tempFirstPoint = startingIndex;
    }

    private void startTempChords(int index)
    {
        // channel, note, index, duration
//        PlayNote(TempChord, 60 + _temperature_offset, index, 0);
//        PlayNote(TempChord, 64 + _temperature_offset, index, 0);
//        PlayNote(TempChord, 67 + _temperature_offset, index, 0);
    }

    /**
     * Stop the temperature-related base chord; this will get called with key changes,
     * and when teh MM is shutting down.
     */
    private void stopTempChords(int index)
    {
//        StopNote(TempChord, 60 + _temperature_offset, index);
//        StopNote(TempChord, 64 + _temperature_offset, index);
//        StopNote(TempChord, 67 + _temperature_offset, index);
    }

}
