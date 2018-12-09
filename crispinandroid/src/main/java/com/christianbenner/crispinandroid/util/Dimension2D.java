package com.christianbenner.crispinandroid.util;

public class Dimension2D {
    public float x;
    public float y;
    public float w;
    public float h;

    public Dimension2D(float x, float y, float w, float h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Dimension2D(Geometry.Point2D xy, float w, float h)
    {
        this.x = xy.x;
        this.y = xy.y;
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

    @Override
    public String toString()
    {
        return "x: " + x + ", y: " + y + ", w: " + w + ", h: " + h;
    }
};
