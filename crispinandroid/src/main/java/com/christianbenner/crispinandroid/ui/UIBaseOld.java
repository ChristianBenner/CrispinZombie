package com.christianbenner.crispinandroid.ui;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.util.ShaderProgram;

/**
 * Created by Christian Benner on 25/12/2017.
 */

@Deprecated
public abstract class UIBaseOld {
    // Text constants
    public final char TOP_LEFT = 'a';
    public final char TOP_RIGHT = 'b';
    public final char BOTTOM_LEFT = 'c';
    public final char BOTTOM_RIGHT = 'd';
    public final char MIDDLE = 'e';

    public final char RED = 'a';
    public final char GREEN = 'b';
    public final char BLUE = 'c';
    public final char WHITE = 'd';
    public final char BLACK = 'e';

    // RGBA
    private float[] colour = new float[4];

    // XY (Normalised Device Co-ordinates)
    private float[] position = new float[2];

    private float width;
    private float height;

    public UIBaseOld()
    {
        setColour(0.0f, 0.0f, 0.0f, 1.0f);
        position[0] = 0.0f;
        position[1] = 0.0f;
        width = 0.0f;
        height = 0.0f;
    }

    public abstract void bindData(ShaderProgram shader);
    public abstract void draw();
    public abstract void render();

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    public void setWidth(float width)
    {
        this.width = width;
    }

    public void setHeight(float height)
    {
        this.height = height;
    }

    public void setPosition(float normalisedX, float normalisedY)
    {
        position[0] = normalisedX;
        position[1] = normalisedY;
    }

    public void setPosition(float[] pos)
    {
        this.position[0] = pos[0];
        this.position[1] = pos[1];
    }

    public void setPosition(int xPixels, int yPixels, int viewportWidth, int viewportHeight)
    {
        position[0] = (((float)xPixels / (float)viewportWidth) * 2.0f) - 1.0f;
        position[1] = (((float)yPixels / (float)viewportHeight) * 2.0f) - 1.0f;
    }

    // Returns array of two floats (X, Y)
    public float[] getPosition()
    {
        return this.position;
    }

    public float getNormalisedX()
    {
        return position[0];
    }

    public float getNormalisedY()
    {
        return position[1];
    }

    public int getPositionPixelsX(int viewportWidth)
    {
        return (int)((this.position[0] + 1.0f) / 2.0f) * viewportWidth;
    }

    public int getPositionPixelsY(int viewportHeight)
    {
        return (int)((this.position[1] + 1.0f) / 2.0f) * viewportHeight;
    }

    public void setPosition(char position_code)
    {
        switch (position_code)
        {
            case TOP_LEFT:
                setPosition(-1.0f, -1.0f);
                break;
            case TOP_RIGHT:
                setPosition(1.0f, -1.0f);
                break;
            case BOTTOM_LEFT:
                setPosition(-1.0f, 1.0f);
                break;
            case BOTTOM_RIGHT:
                setPosition(1.0f, 1.0f);
                break;
            case MIDDLE:
                setPosition(0.0f, 0.0f);
                break;
        }
    }

    public void setAlpha(float alpha)
    {
        colour[3] = alpha;
    }

    public void setAlpha(int alpha)
    {
        colour[3] = alpha / 255.0f;
    }

    public void setColour(char colour_code)
    {
        switch (colour_code)
        {
            case RED:
                setColour(1.0f, 0.0f, 0.0f);
                break;
            case GREEN:
                setColour(0.0f, 1.0f, 0.0f);
                break;
            case BLUE:
                setColour(0.0f, 0.0f, 1.0f);
                break;
            case WHITE:
                setColour(1.0f, 1.0f, 1.0f);
                break;
            default:
            case BLACK:
                setColour(0.0f, 0.0f, 0.0f);
                break;
        }
    }

    public void setColour(float r, float g, float b, float a)
    {
        colour[0] = r;
        colour[1] = g;
        colour[2] = b;
        colour[3] = a;
    }

    public void setColour(float r, float g, float b)
    {
        colour[0] = r;
        colour[1] = g;
        colour[2] = b;
    }

    public void setColour(int r, int g, int b, int a)
    {
        colour[0] = r / (float)255.0f;
        colour[1] = g / (float)255.0f;
        colour[2] = b / (float)255.0f;
        colour[3] = a / (float)255.0f;
    }

    public void setColour(int r, int g, int b)
    {
        colour[0] = r / (float)255.0f;
        colour[1] = g / (float)255.0f;
        colour[2] = b / (float)255.0f;
    }

    // Returns an array of 4 floats (R, G, B, A)
    public Colour getColour()
    {
        return new Colour(colour[0], colour[1], colour[2], colour[3]);
    }

    public float getRedValue() { return colour[0]; }
    public float getGreenValue() { return colour[1]; }
    public float getBlueValue() { return colour[2]; }
    public float getAlphaValue() { return colour[3]; }
}
