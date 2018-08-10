package com.christianbenner.crispinandroid.util;

/**
 * Created by Christian Benner on 16/12/2017.
 */

public class Sound {
    private boolean loaded = false;
    private int sound;

    public Sound(int soundId, boolean loaded)
    {
        this.sound = soundId;
        this.loaded = loaded;
    }

    public int getSound()
    {
        return this.sound;
    }

    public boolean isLoaded()
    {
        return this.loaded;
    }

    public void setLoaded(boolean state)
    {
        this.loaded = state;
    }
}
