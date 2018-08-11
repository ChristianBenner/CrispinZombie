package com.christianbenner.crispinandroid.ui;

import android.content.Context;

import com.christianbenner.crispinandroid.data.TextMesh;
import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.ShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static com.christianbenner.crispinandroid.Constants.BYTES_PER_FLOAT;

public class GLText2 extends UIBase {
    // Data Constants
    private static final int POSITION_STRIDE = (POSITION_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static final int TEXTURE_STRIDE = (TEXTURE_COORDINATES_COMPONENT_COUNT) *
            BYTES_PER_FLOAT;

    // Rendering stuff
    private VertexArray vertexArray;
    private VertexArray textureArray;
    private int vertexCount;

    // Formatting Data
    private float maxLineSize;
    private int numberOfLines;
    private boolean centerText = false;

    private String text;
    private GLFont font;
    private float fontSize;
    private float maxLineVertexWidth;
    private float vertexWidth;
    private float vertexHeight;

    public float getFontSize()
    {
        return fontSize;
    }

    public void setNumberOfLines(int number)
    {
        this.numberOfLines = number;
    }

    public boolean isCentered()
    {
        return this.centerText;
    }

    public float getMaxLineSize()
    {
        return this.maxLineSize;
    }

    public String getTextString()
    {
        return this.text;
    }

    private float orthoHeight;
    public GLText2(Context context, String text, float fontSize, GLFont font,
                  float maxLineLength, float orthoWidth, float orthoHeight, boolean centered) {
        this.fontSize = fontSize;
        this.font = font;
        this.maxLineVertexWidth = maxLineLength;
        this.maxLineSize = maxLineVertexWidth / orthoWidth;
        this.centerText = centered;
        this.texture = font.getTextureAtlas();
        this.orthoHeight = orthoHeight;
        setText(text);
        //setPosition(0.0f, 0.0f);
    }
    @Override
    public void bindData(ShaderProgram shader) {
        vertexArray.setVertexAttribPointer(
                0,
                shader.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                POSITION_STRIDE);

        textureArray.setVertexAttribPointer(
                0,
                shader.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                TEXTURE_STRIDE);
    }

    @Override
    public void draw() {
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }

   /* public void setPosition(float x, float y)
    {
        this.dimensions.x = x;
        this.dimensions.y = y + getHeight();
    }*/

    public Geometry.Point getPosition()
    {
        Geometry.Point pos = super.getPosition();
        return new Geometry.Point(pos.x, pos.y + vertexHeight * orthoHeight, pos.z);
    }

    public float getHeight()
    {
        return vertexHeight * orthoHeight;
    }

    public void setText(String text) {
        this.text = text;
        TextMesh data = this.font.loadText(this);
        this.vertexArray = new VertexArray(data.getVertexPositions());
        this.textureArray = new VertexArray(data.getTextureCoords());
        this.vertexCount = data.getVertexCount();

        this.vertexWidth = data.getVertexWidth();
        this.vertexHeight = data.getVertexHeight();
    }
}
