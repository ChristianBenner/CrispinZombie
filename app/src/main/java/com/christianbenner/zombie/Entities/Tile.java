package com.christianbenner.zombie.Entities;

import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;

/**
 * Created by Christian Benner on 02/06/2018.
 */

public class Tile
{
    private RendererModel rendererModel;
    private char type;
    public int hCost = 0;
    public int gCost = 0;
    public int fCost = 0;
    public Tile prev;

    public Tile(RendererModel model, char type)
    {
        this.rendererModel = model;
        this.type = type;
    }

    public RendererModel getModel()
    {
        return this.rendererModel;
    }

    public char getType()
    {
        return this.type;
    }

    public Geometry.Point getPosition()
    {
        return this.rendererModel.getPosition();
    }

    public void setCosts(int hCost, int gCost)
    {
        this.hCost = hCost;
        this.gCost = gCost;
        this.fCost = hCost + gCost;
    }

    public int getFCost()
    {
        return this.fCost;
    }
}