package com.christianbenner.crispinandroid.ui;

/**
 * Created by Christian Benner on 28/03/2018.
 */

public class UIDimension {
    public float x;
    public float y;
    public float w;
    public float h;

    public UIDimension(float x, float y, float w, float h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setPosition(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void setDimensions(float x, float y, float w, float h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void setDimensions(float width, float height)
    {
        this.w = width;
        this.h = height;
    }
}
