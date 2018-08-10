package com.christianbenner.crispinandroid.data;

/**
 * Created by chris on 10/01/2018.
 */

public class Colour {
    public float r;
    public float g;
    public float b;
    public float a;

    public Colour(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1.0f;
    }

    public Colour(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
