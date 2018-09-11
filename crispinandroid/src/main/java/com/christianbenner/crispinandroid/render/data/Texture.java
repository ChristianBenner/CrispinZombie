package com.christianbenner.crispinandroid.render.data;

/**
 * Created by Christian Benner on 17/12/2017.
 */

public class Texture
{
    private int textureId;
    private int width;
    private int height;
    private float aspectRatio;
    private boolean lowQuality;

    public Texture(int textureId, int width, int height, boolean lowQuality)
    {
        this.textureId = textureId;
        this.width = width;
        this.height = height;
        this.aspectRatio = (float)width / (float)height;
        this.lowQuality = lowQuality;
    }

    public boolean isLowQuality()
    {
        return lowQuality;
    }

    public int getTextureId()
    {
        return textureId;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public float getAspectRatio()
    {
        return aspectRatio;
    }

    public void setTextureId(int textureId)
    {
        this.textureId = textureId;
    }

    public void setDimensions(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.aspectRatio = (float)width / (float)height;
    }
}
