package com.christianbenner.crispinandroid.ui;

import android.graphics.PointF;

import java.util.EventObject;

/**
 * Created by Christian Benner on 03/02/2018.
 */

public class TouchEvent extends EventObject {
    public enum Event
    {
        CLICK,
        DOWN,
        RELEASE
    }

    private Event event;
    private PointF position;

    public TouchEvent(Object source, Event event, PointF position)
    {
        super(source);
        this.event = event;
        this.position = position;
    }

    public PointF getPosition()
    {
        return this.position;
    }

    public Event getEvent()
    {
        return this.event;
    }
}
