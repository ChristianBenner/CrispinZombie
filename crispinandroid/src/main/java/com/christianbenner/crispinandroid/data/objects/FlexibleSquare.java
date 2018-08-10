package com.christianbenner.crispinandroid.data.objects;

import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.programs.ColourShaderProgram;
import com.christianbenner.crispinandroid.programs.TextureShaderProgram;
import com.christianbenner.crispinandroid.util.Logger;
import com.christianbenner.crispinandroid.util.ShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static com.christianbenner.crispinandroid.Constants.BYTES_PER_FLOAT;

/**
 * Created by Christian Benner on 13/12/2017.
 */

public class FlexibleSquare {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT +
            TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private VertexArray vertexArray;
    private int vertexCount;

    private float[] vertexData = {
            // XYST
            // Triangles
            -1f, 1f, 0f, 0f,
            -1f, -1f, 0f, 1f,
            1f, 1f, 1f, 0f,
            1f, 1f, 1f, 0f,
            -1f, -1f, 0f, 1f,
            1f, -1f, 1f, 1f
    };


    public FlexibleSquare(float x, float y, float endX, float endY,
                          float s, float t, float endS, float endT) {
        setDimensions(x, y, endX, endY);
        setTextureCoordinates(s, t, endS, endT);
    }

    public float getVertexData(int vertex)
    {
        if(vertex < vertexData.length && vertex >= 0)
        {
            return vertexData[vertex];
        }
        else
        {
            Logger.errorf("Vertex %d in FlexibleSquare object doesn't exist.", vertex);
            return 0.0f;
        }
    }

    public void setDimensions(float x, float y, float endX, float endY)
    {
        vertexData[0] = x;      vertexData[1] = y;
        vertexData[4] = x;      vertexData[5] = endY;
        vertexData[8] = endX;   vertexData[9] = y;
        vertexData[12] = endX;  vertexData[13] = y;
        vertexData[16] = x;     vertexData[17] = endY;
        vertexData[20] = endX;  vertexData[21] = endY;
    }

    public void setTextureCoordinates(float s, float t, float endS, float endT)
    {
        vertexData[2] = s;      vertexData[3] = t;
        vertexData[6] = s;      vertexData[7] = endT;
        vertexData[10] = endS;  vertexData[11] = t;
        vertexData[14] = endS;  vertexData[15] = t;
        vertexData[18] = s;     vertexData[19] = endT;
        vertexData[22] = endS;  vertexData[23] = endT;
    }

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

    public void bindData(TextureShaderProgram textureShaderProgram) {
        vertexArray = new VertexArray(vertexData);
        vertexCount = vertexData.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);

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

    public void bindData(ColourShaderProgram textureShaderProgram) {
        vertexArray = new VertexArray(vertexData);
        vertexCount = vertexData.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);

        vertexArray.setVertexAttribPointer(
                0,
                textureShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
    }

    public void bindMeshData(ColourShaderProgram textureShaderProgram) {
        vertexArray = new VertexArray(vertexData);
        vertexCount = vertexData.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);

        vertexArray.setVertexAttribPointer(
                0,
                textureShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw(){
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }
}
