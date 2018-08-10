package com.christianbenner.crispinandroid.data.objects;

import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.programs.ColourShaderProgram;
import com.christianbenner.crispinandroid.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static com.christianbenner.crispinandroid.Constants.BYTES_PER_FLOAT;

/**
 * Created by Christian Benner on 16/11/2017.
 */

public class Square {
    VertexArray vertexArray;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT +
            TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            // XYST
            // Triangles
            -1f, 1f, 0f, 0f,
            -1f, -1f, 0f, 1f,
            1f, 1f, 1f, 0f,
            1f, 1f, 1f, 0f,
            -1f, -1f, 0f, 1f,
            1f, -1f, 1f, 1f
    };

    public Square() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(TextureShaderProgram textureShaderProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureShaderProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void bindData(ColourShaderProgram colourShaderProgram)
    {
        vertexArray.setVertexAttribPointer(
                0,
                colourShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw(){
        int count = VERTEX_DATA.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);
        glDrawArrays(GL_TRIANGLES, 0, count);
    }
}