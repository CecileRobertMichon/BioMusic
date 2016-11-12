package com.dp.cecile.biomusic;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Created by cecilerobertm on 2016-11-12.
 */

public class MusicMaker {

    private ArrayList<Float> BVP_data = new ArrayList<Float>();
    private ArrayList<Float> SC_data = new ArrayList<Float>();
    private ArrayList<Float> TEMP_data = new ArrayList<Float>();
    private ArrayList<Float> HR_data = new ArrayList<Float>();
    private int oldNote1 = 60, oldNote2 = 64, oldNote3 = 67; //middle C

    private MainActivity mActivity;

    public MusicMaker(MainActivity activity) {

        mActivity = activity;

    }

    public ArrayList<Float> getBVP_data() {
        return BVP_data;
    }

    public void addBVP_data(float bvp) {
        this.BVP_data.add(bvp);
    }

    public ArrayList<Float> getSC_data() {
        return SC_data;
    }

    public void addSC_data(float sc) {
        this.SC_data.add(sc);
    }

    public ArrayList<Float> getTEMP_data() {
        return TEMP_data;
    }

    public void addTEMP_data(float temp) {
        this.TEMP_data.add(temp);
    }

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
    public void playHarmony() {

    }
}
