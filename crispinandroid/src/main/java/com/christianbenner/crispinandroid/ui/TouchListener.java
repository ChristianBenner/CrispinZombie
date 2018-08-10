package com.christianbenner.crispinandroid.ui;

/**
 * Created by Christian Benner on 03/02/2018.
 */

import java.util.EventListener;

public interface TouchListener extends EventListener {
    void touchEvent(TouchEvent e);
}
