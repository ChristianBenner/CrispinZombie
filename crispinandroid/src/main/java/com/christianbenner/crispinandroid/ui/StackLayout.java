package com.christianbenner.crispinandroid.ui;

import com.christianbenner.crispinandroid.util.Geometry;

import java.util.Vector;

/**
 * Created by Christian Benner on 25/12/2017.
 */

public class StackLayout {
    Vector<UIBase> uiElements = new Vector<>();

    private float posX = 0.0f;
    private float posY = 0.0f;
    private float horizontalBuildup;
    private float verticalBuildup;
    private float paddingX = 0.0f;
    private float paddingY = 0.0f;

    boolean horizontal;
    public StackLayout(boolean horizontal)
    {
        this.horizontal = horizontal;
        this.horizontalBuildup = posX;
        this.verticalBuildup = posY;
    }

    public StackLayout()
    {
        this.horizontal = false;
        this.horizontalBuildup = posX;
        this.verticalBuildup = posY;
    }

    public void setNDCPadding(float x, float y)
    {
        this.paddingX = x;
        this.paddingY = y;
    }

    public void setPixelPadding(int x, int y, int viewportWidth, int viewportHeight)
    {
        this.paddingX = x / (float)viewportWidth;
        this.paddingY = y / (float)viewportHeight;
    }

    public void setPosition(float x, float y)
    {
        this.posX = x;
        this.posY = y;
        this.horizontalBuildup = posX;
        this.verticalBuildup = posY;
    }

    // Returns X, Y NDC
    public void add(UIBase element)
    {
        element.setPosition(new Geometry.Point(horizontalBuildup, verticalBuildup, 0.0f));

        if(horizontal)
        {
            horizontalBuildup += (element.getWidth() + this.paddingX);
        }
        else
        {
            verticalBuildup += (element.getHeight() + this.paddingY);
        }

        uiElements.add(element);
    }

    public void draw()
    {
        for(UIBase ui : uiElements)
        {
            ui.draw();
        }
    }
}
