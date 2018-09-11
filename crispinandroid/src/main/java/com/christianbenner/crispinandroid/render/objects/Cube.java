package com.christianbenner.crispinandroid.render.objects;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.data.Dimensions;
import com.christianbenner.crispinandroid.render.data.Vertex;
import com.christianbenner.crispinandroid.render.util.VertexArray;
import com.christianbenner.crispinandroid.render.shaders.ChangingColourShaderProgram;
import com.christianbenner.crispinandroid.render.shaders.PerVertexLightingTextureShader;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.util.Geometry;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Christian Benner on 13/01/2018.
 */

public class Cube {
    // Not by the center anymore
    private final Dimensions dimensions;

    private final int FACES = 6;
    private final int FLOATS_PER_VERTEX = 3;
    private final int VERTEX_PER_FACE = 6;
    private final int TEX_FLOATS_PER_VERTEX = 2;
    private final int FLOATS_IN_DATA = FLOATS_PER_VERTEX * VERTEX_PER_FACE * FACES;

    private Geometry.Point position = new Geometry.Point(0.0f, 0.0f, 0.0f);
    private Colour colour = new Colour(1.0f, 0.0f, 0.0f);

    private float[] vertexData = new float[FLOATS_IN_DATA];
    private VertexArray vertexArray;
    private float[] normalData = new float[FLOATS_IN_DATA];
    private VertexArray normalArray;
    private float[] textureData = new float[TEX_FLOATS_PER_VERTEX * VERTEX_PER_FACE * FACES];
    private VertexArray textureArray;
    private int textureIndex = 0;
    private int normalDataIndex = 0;
    private int dataIndex = 0;

    private float[] modelMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];

    // NO BUILT IN OBJECTS, USE MODEL READER
    public Cube(Dimensions dimensions)
    {
        this.dimensions = dimensions;
        generateData();
        vertexArray = new VertexArray(vertexData);
        normalArray = new VertexArray(normalData);
        textureArray = new VertexArray(textureData);
    }

    public Cube()
    {
        this.dimensions = new Dimensions(1.0f, 1.0f, -1.0f);
        generateData();
        vertexArray = new VertexArray(vertexData);
        normalArray = new VertexArray(normalData);
        textureArray = new VertexArray(textureData);
    }

    public void bindData(ChangingColourShaderProgram shader)
    {
        vertexArray.setVertexAttribPointer(0, shader.getPositionAttributeLocation(),
                FLOATS_PER_VERTEX, 0);
    }

    public void draw()
    {
        glDrawArrays(GL_TRIANGLES, 0, dataIndex);
    }

    public void render(Camera camera, ChangingColourShaderProgram shader, float colourTime)
    {
        // Position the cube
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, position.x, position.y, position.z);
        multiplyMM(modelViewProjectionMatrix, 0, camera.getViewProjectionMatrix(),
                0, modelMatrix, 0);

        // Draw the cube
        shader.useProgram();
        vertexArray.setVertexAttribPointer(0, shader.getPositionAttributeLocation(),
                FLOATS_PER_VERTEX, 0);
        shader.setUniforms(modelViewProjectionMatrix, colourTime, colour);
        glDrawArrays(GL_TRIANGLES, 0, FLOATS_IN_DATA / FLOATS_PER_VERTEX);
    }

    public void render(Camera camera, PerVertexLightingTextureShader shader, int textureId, float translateX, float translateY, float translateZ)
    {
        // Position the cube
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, position.x, position.y, position.z);
        multiplyMM(modelViewProjectionMatrix, 0, camera.getViewProjectionMatrix(),
                0, modelMatrix, 0);

        // Draw the cube
        shader.useProgram();
        vertexArray.setVertexAttribPointer(0, shader.getPositionAttributeLocation(),
                FLOATS_PER_VERTEX, 0);
        normalArray.setVertexAttribPointer(0, shader.getNormalAttributeLocation(),
                FLOATS_PER_VERTEX, 0);
        textureArray.setVertexAttribPointer(0, shader.getTextureCoordinatesAttributeLocation(),
                TEX_FLOATS_PER_VERTEX, 0);
        shader.setUniforms(modelMatrix, modelViewProjectionMatrix, textureId, translateX, translateY, translateZ, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, FLOATS_IN_DATA / FLOATS_PER_VERTEX);

        // Position the cube
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, position.x + 2.0f, position.y, position.z);
        multiplyMM(modelViewProjectionMatrix, 0, camera.getViewProjectionMatrix(),
                0, modelMatrix, 0);
        shader.setUniforms(modelMatrix, modelViewProjectionMatrix, textureId, translateX, translateY, translateZ, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, FLOATS_IN_DATA / FLOATS_PER_VERTEX);

        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, position.x + 1.0f, position.y + 1.0f, position.z);
        multiplyMM(modelViewProjectionMatrix, 0, camera.getViewProjectionMatrix(),
                0, modelMatrix, 0);
        shader.setUniforms(modelMatrix, modelViewProjectionMatrix, textureId, translateX, translateY, translateZ, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, FLOATS_IN_DATA / FLOATS_PER_VERTEX);
    }

    public void setPosition(Geometry.Point position)
    {
        this.position = position;
    }

    public Geometry.Point getPosition()
    {
        return position;
    }

    public void setColour(Colour colour)
    {
        this.colour = colour;
    }

    public Colour getColour()
    {
        return colour;
    }

    private void generateData()
    {
        // Define the 8 vertexs
        Vertex ftl = new Vertex(0.0f, dimensions.height, 0.0f);
        Vertex fbl = new Vertex(0.0f, 0.0f, 0.0f);
        Vertex ftr = new Vertex(dimensions.width, dimensions.height, 0.0f);
        Vertex fbr = new Vertex(dimensions.width, 0.0f, 0.0f);
        Vertex btl = new Vertex(0.0f, dimensions.height, dimensions.depth);
        Vertex bbl = new Vertex(0.0f, 0.0f, dimensions.depth);
        Vertex btr = new Vertex(dimensions.width, dimensions.height, dimensions.depth);
        Vertex bbr = new Vertex(dimensions.width, 0.0f, dimensions.depth);

        // Cube
        addFace(ftl, fbl, ftr, fbr); // Front

        // add normal data
        for(int i = 0; i < 6; i++)
        {
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = 1.0f;
        }

        for(int i = 0; i < FACES; i++)
        {
            textureData[textureIndex++] = 0.0f;
            textureData[textureIndex++] = 0.0f;
            textureData[textureIndex++] = 0.0f;
            textureData[textureIndex++] = 1.0f;
            textureData[textureIndex++] = 1.0f;
            textureData[textureIndex++] = 0.0f;
            textureData[textureIndex++] = 0.0f;
            textureData[textureIndex++] = 1.0f;
            textureData[textureIndex++] = 1.0f;
            textureData[textureIndex++] = 1.0f;
            textureData[textureIndex++] = 1.0f;
            textureData[textureIndex++] = 0.0f;
        }

        addFace(ftr, fbr, btr, bbr); // Right

        // add normal data
        for(int i = 0; i < 6; i++)
        {
            normalData[normalDataIndex++] = 1.0f;
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = 0.0f;
        }

        addFace(btl, bbl, btr, bbr); // Back

        // add normal data
        for(int i = 0; i < 6; i++)
        {
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = -1.0f;
        }

        addFace(btl, bbl, ftl, fbl); // Left
        // add normal data
        for(int i = 0; i < 6; i++)
        {
            normalData[normalDataIndex++] = -1.0f;
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = 0.0f;
        }

        addFace(btl, ftl, btr, ftr); // Top

        // add normal data
        for(int i = 0; i < 6; i++)
        {
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = 1.0f;
            normalData[normalDataIndex++] = 0.0f;
        }

        addFace(bbl, fbl, bbr, fbr); // Bottom

        // add normal data
        for(int i = 0; i < 6; i++)
        {
            normalData[normalDataIndex++] = 0.0f;
            normalData[normalDataIndex++] = -1.0f;
            normalData[normalDataIndex++] = 0.0f;
        }
    }

    private void addVertex(Vertex vertex)
    {
        vertexData[dataIndex++] = vertex.x;
        vertexData[dataIndex++] = vertex.y;
        vertexData[dataIndex++] = vertex.z;
    }

    private void addFace(Vertex tl, Vertex bl, Vertex tr, Vertex br)
    {
        addVertex(tl);
        addVertex(bl);
        addVertex(tr);
        addVertex(tr);
        addVertex(bl);
        addVertex(br);
    }
}
