package com.dp.cecile.biomusic;

/**
 * Created by cecilerobertm on 2017-01-22.
 */

public class SmoothData {
    private int size;
    private float total = 0f;
    private int index = 0;
    private float samples[];

    public SmoothData(int size) {
        this.size = size;
        samples = new float[size];
        for (int i = 0; i < size; i++) samples[i] = 0f;
    }

    public void add(float x) {
        total -= samples[index];
        samples[index] = x;
        total += x;
        if (++index == size) index = 0; // cheaper than modulus
    }

    public float getAverage() {
        return total / size;
    }

    public void clear()  {
        for (int i = 0; i < size; i++) samples[i] = 0f;
        total = 0f;
    }
}