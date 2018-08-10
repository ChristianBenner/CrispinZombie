package com.christianbenner.crispinandroid.ui;

import android.content.Context;

import com.christianbenner.crispinandroid.data.TextMesh;
import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.programs.FontShaderProgram;
import com.christianbenner.crispinandroid.util.ShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static com.christianbenner.crispinandroid.Constants.BYTES_PER_FLOAT;

/**
 * Created by Christian Benner on 16/12/2017.
 */

public class GLText extends UIBase {
    // Data Constants
    private static final int POSITION_STRIDE = (POSITION_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static final int TEXTURE_STRIDE = (TEXTURE_COORDINATES_COMPONENT_COUNT) *
            BYTES_PER_FLOAT;

    private String text;
    private GLFont font;
    private float fontSize;

    // Formatting Data
    private float maxLineSize;
    private int numberOfLines;
    private boolean centerText = false;

    // Rendering stuff
    private VertexArray vertexArray;
    private VertexArray textureArray;
    private int vertexCount;
    private FontShaderProgram fontShaderProgram;

    private float[] identityM = new float[16];
    private float[] finalM = new float[16];
    private final float[] defaultMatrix =
    {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private float orthoWidth;
    private float orthoHeight;
    private float vertexWidth;
    private float vertexHeight;
    private float maxLineVertexWidth;

    public GLText(Context context, String text, float fontSize, GLFont font,
                  float maxLineLength, float orthoWidth, float orthoHeight, boolean centered) {
        this.fontShaderProgram = new FontShaderProgram(context);
        this.fontSize = fontSize;
        this.font = font;
        this.maxLineVertexWidth = maxLineLength;
        this.maxLineSize = maxLineVertexWidth / orthoWidth;
        this.centerText = centered;
        this.orthoWidth = orthoWidth;
        this.orthoHeight = orthoHeight;
        this.texture = font.getTextureAtlas();
        setText(text);
        setPosition(0.0f, 0.0f);
    }

    public GLText(Context context, String text, float fontSize, GLFont font,
                  float orthoWidth, float orthoHeight, float maxLineLength) {
        this(context, text, fontSize, font, maxLineLength, orthoWidth, orthoHeight, false);
    }

    public void setOrthoDimensions(float width, float height)
    {
        this.orthoWidth = width;
        this.orthoHeight = height;

        dimensions.w = (vertexWidth * this.orthoWidth);
        dimensions.h = (vertexHeight * this.orthoHeight);
        this.maxLineSize = maxLineVertexWidth / orthoWidth;
    }

    public void setText(String text) {
        this.text = text;
        TextMesh data = this.font.loadText(this);
        this.vertexArray = new VertexArray(data.getVertexPositions());
        this.textureArray = new VertexArray(data.getTextureCoords());
        this.vertexCount = data.getVertexCount();

        this.vertexWidth = data.getVertexWidth();
        this.vertexHeight = data.getVertexHeight();

        dimensions.w = (data.getVertexWidth() * this.orthoWidth);
        dimensions.h = (data.getVertexHeight() * this.orthoHeight);
    }

    // If you wish to seperately call the bind
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

    // If you wish to seperately set the uniforms
    public void setUniforms()
    {
        fontShaderProgram.setUniforms(defaultMatrix, font.getTextureAtlas().getTextureId(),
                colour.r, colour.g, colour.b, colour.a);
    }

    public void setPosition(float x, float y)
    {
        this.dimensions.x = x;
        this.dimensions.y = y + getHeight();
    }

    // If you wish to draw the text seperately
    public void draw()
    {
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }

    public GLFont getFont()
    {
        return font;
    }

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

    public int getNumberOfLines()
    {
        return numberOfLines;
    }
}
