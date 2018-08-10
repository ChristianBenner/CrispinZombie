package com.christianbenner.crispinandroid.ui;

import java.util.EventObject;

/**
 * Created by Christian Benner on 03/02/2018.
 */

public class TouchEvent extends EventObject {
    public TouchEvent(Object source) {
        super(source);
    }

    public enum Event
    {
        CLICK,
        DOWN,
        RELEASE
    }

    public Event event;

    public void setEvent(Event e)
    {
        this.event = e;
    }

    public Event getEvent()
    {
        return this.event;
    }
}
