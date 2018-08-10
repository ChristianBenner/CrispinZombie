package com.christianbenner.crispinandroid.ui;

import android.graphics.PointF;

public interface Interactive {
    void click(PointF position);
    void release(PointF position);
    void drag(PointF position);
}
