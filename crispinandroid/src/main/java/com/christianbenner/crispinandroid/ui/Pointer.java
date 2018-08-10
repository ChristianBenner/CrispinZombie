package com.christianbenner.crispinandroid.ui;

import android.graphics.PointF;

public class Pointer {
    private int pointerId;
    private boolean inUse;
    private PointF position;
    private Interactive element;

    public Pointer(int pointerId, PointF position)
    {
        this.pointerId = pointerId;
        this.position = position;
        this.element = null;
    }

    public void setControlOver(Interactive element)
    {
        this.element = element;
        this.element.click(position);
    }

    public void releaseControl()
    {
        // The pointer may have never had a UI element
        if(this.element != null)
        {
            this.element.release(position);
        }
    }

    public void handleDrag()
    {
        if(this.element != null)
        {
            this.element.drag(position);
        }
    }

    public PointF getPosition()
    {
        return position;
    }

    public void setPosition(PointF position)
    {
        this.position = position;
    }

    public boolean isInUse()
    {
        return inUse;
    }

    public int getPointerId()
    {
        return pointerId;
    }

    public void setInUse(boolean state)
    {
        this.inUse = state;
    }

    public void setPointerId(int pointerId)
    {
        this.pointerId = pointerId;
    }
}
