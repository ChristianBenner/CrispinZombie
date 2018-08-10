package com.christianbenner.crispinandroid.data;

/**
 * Created by Christian Benner on 16/12/2017.
 */

public class GLCharacter {
    private int id;
    private double xTextureCoord;
    private double yTextureCoord;
    private double xMaxTextureCoord;
    private double yMaxTextureCoord;
    private double xOffset;
    private double yOffset;
    private double sizeX;
    private double sizeY;
    private double xAdvance;

    public GLCharacter(int id, double xTex, double yTex,
                    double xTexSize, double yTexSize,
                    double xOff, double yOff,
                    double quadWidth, double quadHeight,
                    double xAdvance)
    {
        this.id = id;
        this.xTextureCoord = xTex;
        this.yTextureCoord = yTex;
        this.xOffset = xOff;
        this.yOffset = yOff;
        this.sizeX = quadWidth;
        this.sizeY = quadHeight;
        this.xMaxTextureCoord = xTexSize + xTex;
        this.yMaxTextureCoord = yTexSize + yTex;
        this.xAdvance = xAdvance;
    }

    public void printValues()
    {
        System.out.println("Char[" + (char)id + ", " + id + "], " +
                "texCoord[" + xTextureCoord + ", " + yTextureCoord + "], " +
                "maxTexCoord[" + xMaxTextureCoord + ", " + yMaxTextureCoord + "], " +
                "offset[" + xOffset + ", " + yOffset + "], " +
                "size[" + sizeX + ", " + sizeY + "], " +
                "xAdvance[" + xAdvance + "]");
    }

    public int getID()
    {
        return id;
    }

    public double getxTextureCoord()
    {
        return xTextureCoord;
    }

    public double getyTextureCoord()
    {
        return yTextureCoord;
    }

    public double getxMaxTextureCoord()
    {
        return xMaxTextureCoord;
    }

    public double getyMaxTextureCoord()
    {
        return yMaxTextureCoord;
    }

    public double getxOffset()
    {
        return xOffset;
    }

    public double getyOffset()
    {
        return yOffset;
    }

    public double getSizeX()
    {
        return sizeX;
    }

    public double getSizeY()
    {
        return sizeY;
    }

    public double getxAdvance()
    {
        return xAdvance;
    }
}
