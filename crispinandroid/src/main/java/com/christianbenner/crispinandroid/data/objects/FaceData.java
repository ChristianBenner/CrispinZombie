package com.christianbenner.crispinandroid.data.objects;

/**
 * Created by Christian Benner on 25/01/2018.
 */

public class FaceData{
    public FaceData(int v, int t, int n)
    {
        vertex = v;
        texel = t;
        normal = n;
    }

    public FaceData(int v, int n, boolean isTexel)
    {
        vertex = v;
        if(isTexel) { texel = n; normal = -1; }
        else { texel = -1; normal = n; }
    }

    public FaceData(int v)
    {
        vertex = v;
        texel = -1;
        normal = -1;
    }

    public int vertex;
    public int normal;
    public int texel;
}
