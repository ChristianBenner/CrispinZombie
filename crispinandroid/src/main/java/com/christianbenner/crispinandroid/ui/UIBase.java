package com.christianbenner.crispinandroid.ui;

import android.opengl.GLES20;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Geometry;

import static com.christianbenner.crispinandroid.Constants.BYTES_PER_FLOAT;

/**
 * Created by Christian Benner on 30/03/2018.
 */

public abstract class UIBase {
    // Constants used in rendering/data uploading to gpu
    public static final int POSITION_COMPONENT_COUNT = 2;
    public static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    public static final int STRIDE = (POSITION_COMPONENT_COUNT +
            TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    public static final int STRIDE_NO_TEXELS = POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT;

    // Common to all UI elements (texture may not be used check if == null)
    protected Texture texture;
    protected Colour colour;
    protected Dimension2D dimensions;

    // Common functionality to all UI elements
    public abstract void bindData(ShaderProgram shader);
    public abstract void draw();

    public UIBase()
    {
        // Set default values
        texture = null;
        dimensions = new Dimension2D(0.0f, 0.0f, 0.0f, 0.f);
        colour = new Colour(1.0f, 1.0f, 1.0f);
    }

    public Texture getTexture()
    {
        return this.texture;
    }

    public void removeTexture()
    {
        if(texture != null)
        {
            // Delete the texture from video memory
            int[] textureID = new int[1];
            textureID[0] = this.texture.getTextureId();
            GLES20.glDeleteTextures(1, textureID, 0);
            this.texture = null;
        }
    }

    public void setTexture(Texture texture)
    {
        this.texture = texture;
    }

    public Colour getColour() {
        return colour;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public void setDimensions(Dimension2D dimensions)
    {
        this.dimensions = dimensions;
    }

    public Dimension2D getDimensions()
    {
        return this.dimensions;
    }

    public void setPosition(Geometry.Point position)
    {
        this.dimensions.x = position.x;
        this.dimensions.y = position.y;
    }

    public Geometry.Point getPosition()
    {
        return new Geometry.Point(this.dimensions.x, this.dimensions.y, 0.0f);
    }

    public float getWidth()
    {
        return this.dimensions.w;
    }

    public float getHeight()
    {
        return this.dimensions.h;
    }

    public void setAlpha(float alpha)
    {
        this.colour.a = alpha;
    }
    public void setRed(float red) { this.colour.r = red; }
    public void setGreen(float green) { this.colour.g = green; }
    public void setBlue(float blue) { this.colour.b = blue; }
}
