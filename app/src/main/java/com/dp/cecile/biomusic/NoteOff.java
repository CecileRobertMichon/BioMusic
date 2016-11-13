package com.dp.cecile.biomusic;

/**
 * Created by cecilerobertm on 2016-11-13.
 */

public class NoteOff {
    int channel;
    int note;
    int when;

    public NoteOff(int c, int n, int w) {
        this.channel = c;
        this.note = n;
        this.when = w;
    }
}
