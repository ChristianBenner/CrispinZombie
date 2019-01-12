package com.christianbenner.crispinandroid.ui;

import android.content.Context;
import android.opengl.GLES20;

import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.VertexArray;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Geometry;

/**
 * Created by Christian Benner on 22/03/2018.
 */

public class BaseController extends UIBase
{
    private VertexArray baseVertexArray;
    private int baseVertexCount;

    private final float[] vertexData = {
            // XYST Triangles
            0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f,
            1f, 1f, 1f, 0f,
            1f, 1f, 1f, 0f,
            0f, 0f, 0f, 1f,
            1f, 0f, 1f, 1f
    };

    public BaseController(Context context, Geometry.Point position, float radius,
                          int textureResource)
    {
        baseVertexArray = new VertexArray(vertexData);
        baseVertexCount = vertexData.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);

        this.dimensions = new Dimension2D(position.x, position.y,
                radius * 2.0f, radius * 2.0f);
        texture = TextureHelper.loadTexture(context, textureResource, true);
    }

    public BaseController(Context context, float radius, int textureResource) {
        this(context, new Geometry.Point(0.0f, 0.0f, 0.0f), radius, textureResource);
    }

    public float getRadius()
    {
        return this.getWidth() / 2.0f;
    }

    @Override
    public void bindData(ShaderProgram shader) {
        baseVertexArray.setVertexAttribPointer(
                0,
                shader.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        baseVertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                shader.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    @Override
    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, baseVertexCount);
    }
}
