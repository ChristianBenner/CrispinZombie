package com.christianbenner.crispinandroid.util;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.programs.ShaderConstants;

/**
 * Created by Christian Benner on 03/02/2018.
 */

public class Light {
    // colour, position, max, min, distance (CHANGE TO INTENSITY)
    private Colour colour;
    private Geometry.Point position;
    private float maxAmbience;
    private float attenuation;
    private float ambienceIntensity;

    // 9 pieces of data for lights
    //0,1,2 = colour
    //3,4,5 = position
    //6 = max ambience
    //7 = min ambience
    //8 = ambience intensity
    private float[] lightData = new float[9];

    public Light()
    {
        setColour(new Colour(1.0f, 1.0f, 1.0f));
        setPosition(new Geometry.Point(0.0f, 0.0f, 0.0f));
        setMaxAmbience(ShaderConstants.DEFAULT_MAX_AMBIENT);
        setAttenuation(ShaderConstants.DEFAULT_ATTENUATION);
        setAmbienceIntensity(ShaderConstants.DEFAULT_AMBIENT_DISTANCE);
    }

    public float[] getLightData()
    {
        return lightData;
    }

    public void setColour(Colour colour)
    {
        this.colour = colour;
        lightData[0] = colour.r;
        lightData[1] = colour.g;
        lightData[2] = colour.b;
    }

    public Colour getColour()
    {
        return this.colour;
    }

    public void setPosition(Geometry.Point position)
    {
        this.position = position;
        lightData[3] = position.x;
        lightData[4] = position.y;
        lightData[5] = position.z;
    }

    public Geometry.Point getPosition()
    {
        return this.position;
    }

    public void setMaxAmbience(float val)
    {
        this.maxAmbience = val;
        lightData[6] = val;
    }

    public float getMaxAmbience()
    {
        return this.maxAmbience;
    }

    public void setAttenuation(float val)
    {
        this.attenuation = val;
        lightData[7] = val;
    }

    public float getAttenuation()
    {
        return this.attenuation;
    }

    public void setAmbienceIntensity(float val)
    {
        this.ambienceIntensity = val;
        lightData[8] = val;
    }

    public float getAmbienceIntensity()
    {
        return this.ambienceIntensity;
    }
}
