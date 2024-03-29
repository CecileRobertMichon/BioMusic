package com.dp.cecile.biomusic;

import android.util.Log;

import java.util.ArrayList;

import static java.lang.Math.abs;
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
    private ArrayList<NoteOff> noteOffList = new ArrayList<>();

    private MidiGenerator midiGenerator;

    // TODO : remove unused variables
    private int offset;
    private int temperatureOffset;
    private int duration;
    private int middleCOffset;
    private double tempSlope;
    private double tempIntercept;
    private int tempFirstPoint;
    private float tempSSThreshhold;
    private boolean noteExtended;
    private boolean keepPlaying;

    private MainActivity mActivity;

    public MusicMaker(MainActivity activity) {

        this.midiGenerator = new MidiGenerator(activity);

        this.mActivity = activity;
        this.offset = 0;
        this.temperatureOffset = 0;
        this.duration = MusicConstants.DEFAULT_DURATION;
        this.middleCOffset = MusicConstants.INITIALIZATION_NUMBER;
        this.tempSlope = MusicConstants.INITIALIZATION_NUMBER;
        this.tempIntercept = MusicConstants.INITIALIZATION_NUMBER;
        this.tempFirstPoint = MusicConstants.INITIALIZATION_NUMBER;
        this.tempSSThreshhold = 0.01F;
        this.noteExtended = false;
        this.keepPlaying = true;

    }

    // BVP
    public ArrayList<Float> getBVP_data() {
        return BVP_data;
    }

    public void addBVP_data(float bvp) {
        this.BVP_data.add(bvp);
    }

    public ArrayList<String> getBVP_data_string() {
        return BVP_data_string;
    }

    public void addBVP_data_string(String bvp) {
        this.BVP_data_string.add(bvp);
    }

    public float getBVP(int index) {
        if (index < BVP_data.size()) return BVP_data.get(index);

        // Execution reaches this point if we do not *yet* have enough data for the request. So we wait.
        while (index >= BVP_data.size()) {
            // wait for data
        }
        return BVP_data.get(index);
    }

    public float getSC(int index) {
        if (index < SC_data.size()) return SC_data.get(index);

        // Execution reaches this point if we do not *yet* have enough data for the request. So we wait.
        while (index >= SC_data.size()) {
            // wait for data
            try {
                sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Log.d("MusicMaker", "waiting for SC... index is " + index);
        }
        return SC_data.get(index);
    }

    public float getTemp(int index) {
        if (index < TEMP_data.size()) return SC_data.get(index);

        // Execution reaches this point if we do not *yet* have enough data for the request. So we wait.
        while (index >= TEMP_data.size()) {
            // wait for data
        }
        return TEMP_data.get(index);
    }

    // Skin Conductance / EDA
    public ArrayList<Float> getSC_data() {
        return SC_data;
    }

    public void addSC_data(float sc) {
        this.SC_data.add(sc);
    }

    public ArrayList<String> getSC_data_string() {
        return SC_data_string;
    }

    public void addSC_data_string(String bvp) {
        this.SC_data_string.add(bvp);
    }

    // Temperature
    public ArrayList<Float> getTEMP_data() {
        return TEMP_data;
    }

    public void addTEMP_data(float temp) {
        this.TEMP_data.add(temp);
    }

    public ArrayList<String> getTEMP_data_string() {
        return TEMP_data_string;
    }

    public void addTEMP_data_string(String bvp) {
        this.TEMP_data_string.add(bvp);
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

    public void resetSignals() {
        this.BVP_data.clear();
        this.BVP_data_string.clear();
        this.SC_data.clear();
        this.SC_data_string.clear();
        this.TEMP_data.clear();
        this.TEMP_data_string.clear();
    }


    public void shutDown() {
        keepPlaying = false;
        midiGenerator.stopAllNotes();
        //resetSignals();  // removed to make drive export work
    }

    public void resume() {
        keepPlaying = true;
    }


    public void parseData() {
        int next_eda_note = 0;
        int beat = 0;

        int next_pending_note_off = Integer.MAX_VALUE;

        // We now have data in our PhysioData collection.
        int num_samples = this.getBVP_data().size();
        int beginning_num = num_samples;
        num_samples = beginning_num + 300 * 60 * 30; //30 minutes hard-coded

        next_eda_note = beginning_num;
        int i;
        // Go through our samples, parsing the data we need as we go.
        for (i = beginning_num; (i < num_samples) && keepPlaying; ++i) {
            Log.d("MusicMaker", "parsing signal " + i + ", size of SC is " + SC_data.size());
            // Make sure that we're ready to generate a new note before we terminate notes that end at this point.
            this.getSC(i);

            if (i == beginning_num) {
                startTempChords(i);
            }

            // EDA Notes, drum beats, base key, tempo
            if (i == next_eda_note) {
                // Tempo (via RecalculateDuration), key (only changes on each bar)
                if (beat % 4 == 0) {
                    beat = 0;
                    recalculateDuration(i);
                    next_pending_note_off = playNote(0, 45, i, duration); // Drums!

                    // Change pitch, if appropriate
                    checkTemperature(i);
                }

                // Based on our current offset, get the note that corresponds to this EDA
                int note = getNoteFromEDA(getSC(i));

                // note_extended indicates that the same note was played twice in a row -- information
                // that can be used to make things sound more legato
                this.noteExtended = false;

                next_pending_note_off = removeNoteOffEvents(i, this.noteExtended, note, duration); // will set note_extended
                if (!this.noteExtended) {
                    next_pending_note_off = playNote(1, note, i, duration);
                }

                // If we didn't simply extend the note, then play the doubling notes
                if (!this.noteExtended && note < MusicConstants.MIDDLE_C + MusicConstants.LOW_DOUBLING_NOTE) {
                    next_pending_note_off = playNote(1, note + 12, i, duration);
                }
                if (!this.noteExtended && note > MusicConstants.MIDDLE_C + MusicConstants.HIGH_DOUBLING_NOTE) {
                    next_pending_note_off = playNote(1, note - 12, i, duration);
                }
                ++beat;
                next_eda_note = i + duration;
            }
            // Ending notes
            else if (i >= next_pending_note_off) {
                next_pending_note_off = removeNoteOffEvents(i, false, 0, 0);
            }
        }

        stopTempChords(i);
        removeNoteOffEvents(Integer.MAX_VALUE, false, 0, 0);
    }

    /**
     * RecalculateDuration: Called every beat, this looks at BVP to see whether the tempo needs to be changed.
     */
    public void recalculateDuration(int index) {
        if (index < 1200) //1024) // don't recalculate during first four seconds
        {
            return;
        }

        int bvp_smooth_window = 25;
        int first_peak = 0, last_peak = 0, num_peaks = 0;
        float last_min = getBVP(index - 1000);
        float last_max = getBVP(index - 1000) + 0.001F; // avoid division by zero!
        boolean descending = false;
        boolean false_peak = false;
        for (int i = 1; i < 1000; ++i) // go through the data, comparing each "smoothed" sample with the previous one
        {
            float new_bvp = getBVP(index - 1000 + i);
            float old_bvp = getBVP(index - 1000 + i - bvp_smooth_window);
            if (new_bvp < old_bvp) {
                if (!descending) // a potential peak
                {
                    descending = true;
                    if (false_peak)
                        false_peak = false; // the hiccough has corrected itself
                    else if (abs((new_bvp - last_min) / (last_max - last_min)) > 0.8) // arbitrary threshold
                    {
                        last_max = new_bvp;
                    } else {
                        false_peak = true;
                    }
                }
            } else if (new_bvp > old_bvp) // ignore plateaux
            {
                if (descending) // huzzah! A trough!
                {
                    descending = false;
                    if (false_peak)
                        false_peak = false; // correction from previous hiccough
                    else if (abs((last_max - new_bvp) / (last_max - last_min)) > 0.8) // same arbitrary threshold
                    {
                        last_min = new_bvp;
                        ++num_peaks;
                        last_peak = i - 1;
                        if (first_peak == 0) // not set yet
                        {
                            first_peak = last_peak;
                        }
                    } else {
                        false_peak = true;
                    }
                }
            }
        }

        if (num_peaks > 1) // make sure that we have 2 or more peaks
        {
            // for 60bpm (256 samples/beat), we want duration ~ 128
            // for 90bpm (170 sampels/beat), we want duration ~ 64
            // for 120bpm (128 samples/beat), we want duration ~32
            // for now, attune to the first one: just take samples/beat, divide by 2.
            this.duration = (last_peak - first_peak) / (num_peaks - 1); // round to int; subtract 1 for fencepost.
            this.duration /= 4; // might revise scaling.
        }
    }

    /**
     * GetNoteFromEDA: Given the EDA value, get the note for the scale for which we are configured.
     */
    public int getNoteFromEDA(float eda) {
        int my_note = getMajorScaleNote(eda);

        my_note += offset;
        my_note += temperatureOffset;

        // Now see if we need to "transpose":
        if (my_note > MusicConstants.MIDDLE_C + MusicConstants.HIGH_CUTOFF_NOTE) {
            my_note += MusicConstants.HIGH_NOTE_JUMP; // HNJ is a negative number
            offset += MusicConstants.HIGH_NOTE_JUMP;
        } else if (my_note < MusicConstants.MIDDLE_C + MusicConstants.LOW_CUTOFF_NOTE) {
            my_note += MusicConstants.LOW_NOTE_JUMP; // LNJ is positive
            offset += MusicConstants.LOW_NOTE_JUMP;
        }

        return my_note;
    }

    /**
     * Major scale: 7 notes per scale: C, D, E, F, G, A, B
     */
    public int getMajorScaleNote(float eda) {
        int msn = (int) (eda * MusicConstants.NOTES_PER_USIEMEN * 7 / 12); // "7/12" because we are scaling so that we have 7 notes per uSiemen

        // Apply (or set) the offset
        if (middleCOffset == MusicConstants.INITIALIZATION_NUMBER)
            middleCOffset = -msn; // First note will be 0, which corresponds to middle C

        msn += middleCOffset; // we now have a "note number," for lack of a better term, centred around middle C at 0.

        int output_note = 60 + 12 * (msn / 7); // octave adjustment
        switch (msn % 7) {
            case -6: // super-tonic, down a scale
                output_note -= 10;
                break;
            case -5: // median, down a scale
                output_note -= 8;
                break;
            case -4: // sub-dominant, down a scale
                output_note -= 7;
                break;
            case -3: // dominant, down a scale
                output_note -= 5;
                break;
            case -2: // sub-median
                output_note -= 3;
                break;
            case -1: // leading note
                output_note -= 1;
                break;
            case 0:
                break;
            case 1: // super-tonic
                output_note += 2;
                break;
            case 2: // median
                output_note += 4;
                break;
            case 3: // sub-dominant
                output_note += 5;
            case 4: // dominant
                output_note += 7;
                break;
            case 5: // sub-median
                output_note += 9;
                break;
            case 6: // leading note
                output_note += 11;
                break;
        }

        return output_note;
    }


    //play harmony according to temp signal
    public void checkTemperature(int index) {
        if (tempFirstPoint == MusicConstants.INITIALIZATION_NUMBER) {
            // Calculate from the beginning
            if (index > 5) {
                setSlopeAndIntercept(0);
            }
            return; // don't change the offset for this one.
        }

        double sum_squares = 0;
        for (int i = tempFirstPoint; i <= index; ++i) {
            double diff = (getTemp(i) - (tempSlope * i + tempIntercept));
            sum_squares += diff * diff;
        }
        sum_squares /= (index - tempFirstPoint + 1);

        if (sum_squares > tempSSThreshhold) {
            double old_slope = tempSlope;
            // Calculate the new slope, using the last 5 data-points.
            setSlopeAndIntercept(index - 4);
            Log.d("Temperature", "Adjusted slope! New slope: " + tempSlope);

            stopTempChords(index);

            if (old_slope < tempSlope)
                temperatureOffset -= 7; // down 5 tones -- Circle of Fifths
            else
                temperatureOffset += 7; // up 5 tones -- Circle of Fifths

            startTempChords(index);
        }
    }

    private void setSlopeAndIntercept(int startingIndex) {
        double Sx = 0;
        double Sy = 0;
        double Sxy = 0;
        double Sx2 = 0;

        // Populate the above sums
        for (int i = 0; i < 5; ++i) {
            float this_temp = getTemp(startingIndex + i);
            Sx += startingIndex + i;
            Sy += this_temp;
            Sxy += this_temp * (startingIndex + i);
            Sx2 += (startingIndex + i) * (startingIndex + i);
        }

        // Now reset our variables
        tempSlope = (5 * Sxy - Sx * Sy) / (5 * Sx2 - Sx * Sx);
        tempIntercept = (Sy - tempSlope * Sx) / 5;
        // Finally, update when we were last here.
        tempFirstPoint = startingIndex;
    }

    private void startTempChords(int index) {
        // channel, note, index, duration
        playNote(2, 60 + temperatureOffset, index, 0);
        playNote(2, 64 + temperatureOffset, index, 0);
        playNote(2, 67 + temperatureOffset, index, 0);
    }

    /**
     * Stop the temperature-related base chord; this will get called with key changes,
     * and when teh MM is shutting down.
     */
    private void stopTempChords(int index) {
        stopNote(2, 60 + temperatureOffset, index);
        stopNote(2, 64 + temperatureOffset, index);
        stopNote(2, 67 + temperatureOffset, index);
    }

    public int playNote(int channel, int note, int index, int duration) {

        // midiGenerator.addNote(channel, note, index, duration);

        midiGenerator.noteOn(channel, note, 0x7F);
        if (duration != 0) {
            int when = index + duration;
            NoteOff n = new NoteOff(channel, note, when);
            return addNoteOffEvent(n);
        } else return index; // A cheat so that we don't accidentally miss anything...
    }

    public void stopNote(int channel, int note, int index) {
        int my_channel = channel;
        // midiGenerator.addStop(channel, note, index);
        midiGenerator.noteOff(my_channel, note);
    }

    public int addNoteOffEvent(NoteOff noteOff) {
        // _pending_notes_off is a sorted list of events, to which we are trying to insert my_event
        boolean inserted = false;
        int iter = 0;
        for (NoteOff n : this.noteOffList) {
            if (noteOff.when < n.when) // insert here!
            {
                this.noteOffList.add(iter, noteOff);
                inserted = true;
            }
            iter++;
        }
        if (!inserted) {
            this.noteOffList.add(noteOff);
        }

        return this.noteOffList.get(0).when;
    }

    /**
     * RemoveNoteOffEvents: Go through the list of active notes (or pending notes-off, if you want to look at it that way) and remove
     * the relevant notes. If a note is repeated, leave it on for a more legato feel.
     */
    public int removeNoteOffEvents(int current_index, boolean kept_note, int next_note, int duration) {
        if (kept_note) {
            this.noteExtended = false;
        }

        int i = 0;
        for (i = 0; i < this.noteOffList.size(); i++) {
            NoteOff n = this.noteOffList.get(i);
            if (n.when > current_index) {
                break;
            }
            // Sound the note-off event
            int this_channel = n.channel;

            if ((this_channel == 1)
                    && kept_note != false
                    && n.note % 12 == next_note % 12) {
                // Add a new one later
                NoteOff newOffEvent = n;
                newOffEvent.when += duration;
                addNoteOffEvent(newOffEvent); // will not change anything that we have scanned until this point
                this.noteExtended = true;
            } else {
                midiGenerator.noteOff(this_channel, n.note);
            }
        }

        // Now remove them
        for (int k = 0; k < i; k++) {
            this.noteOffList.remove(0);

        }

        if (this.noteOffList.isEmpty())
            return Integer.MAX_VALUE;
        else
            return this.noteOffList.get(0).when;
    }
}
