package com.christianbenner.crispinandroid.ui;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.christianbenner.crispinandroid.render.util.VertexArray;
import com.christianbenner.crispinandroid.util.Dimension2D;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Created by Christian Benner on 13/04/2018.
 */

public class Image extends UIBase {
    private VertexArray vertexArray;
    private int vertexCount;

    public Image(Dimension2D dimensions, Colour colour, Texture texture)
    {
        this.dimensions = dimensions;
        this.colour = colour;
        this.texture = texture;
    }

    public Image(Dimension2D dimensions)
    {
        this(dimensions, new Colour(1.0f, 1.0f, 1.0f), null);
    }

    public Image(Dimension2D dimensions, Colour colour)
    {
        this(dimensions, colour, null);
    }

    public Image(Dimension2D dimensions, Texture texture)
    {
        this(dimensions, new Colour(1.0f, 1.0f, 1.0f), texture);
    }

    private float[] vertexData = {
            // XYST Triangles
            0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f,
            1f, 1f, 1f, 0f,
            1f, 1f, 1f, 0f,
            0f, 0f, 0f, 1f,
            1f, 0f, 1f, 1f
    };

    public void bindData(ShaderProgram shader)
    {
        vertexArray = new VertexArray(vertexData);
        vertexCount = vertexData.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);

        vertexArray.setVertexAttribPointer(
                0,
                shader.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                shader.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw()
    {
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }
}
