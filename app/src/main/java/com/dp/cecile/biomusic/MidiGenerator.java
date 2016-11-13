package com.dp.cecile.biomusic;

import org.billthefarmer.mididriver.MidiDriver;

/**
 * Created by cecilerobertm on 2016-11-13.
 */

public class MidiGenerator implements MidiDriver.OnMidiStartListener {

    private MidiDriver midi;
    private MainActivity mActivity;
    char instruments[] = {0x75, 0x65, 0x52};
    char volumes[] = {0x40, 0x7F, 0x20};

    public MidiGenerator(MainActivity activity) {

        this.mActivity = activity;
        // Create midi driver
        midi = new MidiDriver();

        // Set on midi start listener
        if (midi != null)
            midi.setOnMidiStartListener(this);
    }


    public MidiDriver getMidi() {
        return midi;
    }

    public void startMidi() {
        this.midi.start();
    }

    public void stopMidi() {
        this.midi.stop();
    }

    // Listener for sending initial midi messages when the Sonivox
    // synthesizer has been started, such as program change.

    @Override
    public void onMidiStart()
    {
        // Channel 0 - BVP - Taiko drums
        sendMidi(0xc0, this.instruments[0]);
        sendMidi(0xb0, 7, this.volumes[0]);

        //Channel 1 - EDA -  Goblins
        sendMidi(0xc1, this.instruments[1]);
        sendMidi(0xb0, 7, this.volumes[1]);

        //Channel 2 - Temp - Choir "oohs"
        sendMidi(0xc2, this.instruments[2]);
        sendMidi(0xb0, 7, this.volumes[2]);

        //sustain pedal: ON
//        sendMidi(0xb0, 64, 64);
//        sendMidi(0xb1, 64, 64);
//        sendMidi(0xb2, 64, 64);

    }

    public void noteOff(int ch, int kk)
    {
        sendMidi(0x90 + ch, kk, 0x0);
    }

    void noteOn(int ch, int kk, int v)
    {
        sendMidi(0x90 + ch, kk, v);
    }

    void pitchBend(int ch, int l, int m)
    {
        sendMidi(0xE0 + ch, l, m);
    }

    void program_change(int ch, int cc0nr , int pnr)
    {
        sendMidi(ch, 0, cc0nr);
        sendMidi(0xC0 + ch, pnr, 0);
    }

    void reset_controllers()
    {
        for (int i = 0; i < 16; i++)
            sendMidi(0xB0 + i, 0x79, 0);
    }

    // Send a midi message

    public void sendMidi(int m, int p)
    {
        byte msg[] = new byte[2];

        msg[0] = (byte) m;
        msg[1] = (byte) p;

        midi.write(msg);
    }


    // Send a midi message

    public void sendMidi(int m, int n, int v)
    {
        byte msg[] = new byte[3];

        msg[0] = (byte) m;
        msg[1] = (byte) n;
        msg[2] = (byte) v;

        midi.write(msg);
    }

}