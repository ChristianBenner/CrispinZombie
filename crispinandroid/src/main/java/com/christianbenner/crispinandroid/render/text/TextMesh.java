package com.christianbenner.crispinandroid.render.text;

import com.christianbenner.crispinandroid.ui.UIBase;

/**
 * Created by Christian Benner on 27/11/2017.
 */

public class TextMesh {
    private float[] vertexPositions;
    private float[] textureCoordinates;
    private float vertexWidth;
    private float vertexHeight;

    public TextMesh(float[] vertexPositions, float[] textureCoords, float width, float height)
    {
        this.vertexPositions = vertexPositions;
        this.textureCoordinates = textureCoords;
        this.vertexWidth = width;
        this.vertexHeight = height;
    }

    public float[] getVertexPositions() { return vertexPositions; }
    public float[] getTextureCoords() { return textureCoordinates; }
    public int getVertexCount() { return vertexPositions.length / UIBase.POSITION_COMPONENT_COUNT; }
    public float getVertexWidth() { return this.vertexWidth; }
    public float getVertexHeight() { return this.vertexHeight; }
}
