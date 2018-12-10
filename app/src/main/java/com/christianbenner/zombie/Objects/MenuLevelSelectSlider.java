package com.christianbenner.zombie.Objects;

import android.graphics.PointF;

import com.christianbenner.crispinandroid.ui.Interactive;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;

import java.util.ArrayList;

public class MenuLevelSelectSlider implements Interactive {
    private ArrayList<TouchListener> touchListeners = new ArrayList<>();
    public void addTouchListener(TouchListener listener) { touchListeners.add(listener); }
    public void removeTouchListener(TouchListener listener) { touchListeners.remove(listener); }
    private boolean touchFocus;

    @Override
    public void click(PointF position)
    {
        touchFocus = true;

        // Send touch event
        final TouchEvent CLICK_EVENT = new TouchEvent(this, TouchEvent.Event.CLICK, position);
        for(final TouchListener touchListener : touchListeners)
        {
            touchListener.touchEvent(CLICK_EVENT);
        }
    }

    @Override
    public void release(PointF position)
    {
        if(touchFocus)
        {
            // Send release event
            final TouchEvent RELEASE_EVENT = new TouchEvent(this, TouchEvent.Event.RELEASE, position);
            for(final TouchListener touchListener : touchListeners)
            {
                touchListener.touchEvent(RELEASE_EVENT);
            }

            touchFocus = false;
        }
    }

    @Override
    public void drag(PointF position)
    {
        // Send the drag touch event
        final TouchEvent DOWN_EVENT = new TouchEvent(this, TouchEvent.Event.DOWN, position);
        for(final TouchListener touchListener : touchListeners)
        {
            touchListener.touchEvent(DOWN_EVENT);
        }
    }

    public boolean hasTouchFocus()
    {
        return touchFocus;
    }
}
