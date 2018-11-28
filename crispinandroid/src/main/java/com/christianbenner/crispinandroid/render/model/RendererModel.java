package com.christianbenner.crispinandroid.render.model;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.util.Geometry;
import com.example.crispinandroid.R;

import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Christian Benner on 13/02/2018.
 */

public class RendererModel extends Model {
    protected Geometry.Point position;
    protected Colour colour;
    protected float scaleX;
    protected float scaleY;
    protected float scaleZ;
    protected float angle;
    protected float angleX;
    protected float angleY;
    protected float angleZ;
    protected Texture texture;
    private Context context;
    private boolean vboModel;

    public float[] getFirstFloats()
    {
        float[] firstFloats = {
                vertexArray.floatBuffer.get(0),
                vertexArray.floatBuffer.get(1),
                vertexArray.floatBuffer.get(2),
                vertexArray.floatBuffer.get(3)
        };

        return firstFloats;
    }



    private float[] modelMatrix = new float[16];

    private int[] vbo;

    public RendererModel(Context context, int objectResourceId, Texture texture, Model.AllowedData allowedData)
    {
        super(context, objectResourceId, allowedData);
        initVars(context, texture);
    }

    public RendererModel(Context context, int objectResourceId, Model.AllowedData allowedData)
    {
        super(context, objectResourceId, allowedData);
        initVars(context, null);
    }

    public RendererModel(Context context, int objectResourceId, Texture texture)
    {
        super(context, objectResourceId);
        initVars(context, texture);
    }

    public RendererModel(Context context, int objectResourceId)
    {
        super(context, objectResourceId);
        initVars(context, null);
    }

    private void initVars(Context context, Texture texture)
    {
        this.context = context;
        this.texture = texture;

        // Create a virtual buffer object so that model data is stored in the GPU
        vbo = new int[1];

        // Ask OpenGL ES to generate buffer object for the data
        GLES20.glGenBuffers(1, vbo, 0);

        // Bind the buffer so that GL commands will be associated to this buffer object
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

        //System.out.println("SIZE: " + vertexArray.floatBuffer.capacity());

        // Transfer the data from client memory to the buffer
        vertexArray.floatBuffer.position(0);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
                vertexArray.floatBuffer.capacity() * BYTES_PER_FLOAT,
                vertexArray.floatBuffer, GLES20.GL_STATIC_DRAW);

        // Unbind the buffer
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        this.position = new Geometry.Point(0.0f, 0.0f, 0.0f);
        this.colour = new Colour(1.0f, 1.0f, 1.0f, 1.0f);
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.scaleZ = 1.0f;
        this.angle = 0.0f;
        this.angleX = 1.0f;
        this.angleY = 1.0f;
        this.angleZ = 1.0f;
        newIdentity();
    }

    public int getVBO()
    {
        return vbo[0];
    }

    public void setTexture(Texture texture)
    {
        this.texture = texture;
    }

    public float[] getModelMatrix()
    {
        return this.modelMatrix;
    }

    public void newIdentity()
    {
        setIdentityM(modelMatrix, 0);
    }

    public void rotate(float angle, float x, float y, float z)
    {
        this.angle = angle;
        this.angleX = x;
        this.angleY = y;
        this.angleZ = z;
        rotateM(modelMatrix, 0, angle, x, y, z);
    }

    public void rotate(float[] rotationMatrix)
    {
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0);
    }

    public void rotate(float x, float y, float z)
    {
        float[] rotationEuler = new float[16];
        Matrix.setRotateEulerM(rotationEuler, 0, x, y, z);
        Matrix.translateM(rotationEuler, 0, -0.05f, -0.35f, 0f);
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationEuler, 0);
    }

    public void translate(Geometry.Vector vector)
    {
        translateM(modelMatrix, 0, vector.x, vector.y, vector.z);
    }

    public void setPosition(Geometry.Point position)
    {
        this.position = position;
        translateM(modelMatrix, 0, position.x, position.y, position.z);
    }

    public void rotateAroundPos(Geometry.Point pos, float angle, float x, float y, float z)
    {
        Matrix.translateM(modelMatrix, 0, pos.x, pos.y, pos.z);
        Matrix.rotateM(modelMatrix, 0, angle, x, y, z);
        Matrix.translateM(modelMatrix, 0, -pos.x, -pos.y, -pos.z);
    }

    public Geometry.Point getPosition() { return this.position; }

    public void setColour(Colour colour)
    {
        this.colour = colour;
    }
    public Colour getColour() { return this.colour; }

    public void setScale(float scale)
    {
        setScale(scale, scale, scale);
    }

    public void setScale(float x, float y, float z)
    {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        Matrix.scaleM(modelMatrix,0, x, y, z);
    }

    // Returns array of 3 floats x,y,z
    public float[] getScale()
    {
        float[] scales = { scaleX, scaleY, scaleZ };
        return scales;
    }

    public float getScaleX()
    {
        return scaleX;
    }

    public float getScaleY()
    {
        return scaleY;
    }

    public float getScaleZ()
    {
        return scaleZ;
    }

    public Texture getTexture()
    {
        return this.texture;
    }

    public float getAngle() { return this.angle; }
    public float getAngleX() { return this.angleX; }
    public float getAngleY() { return this.angleY; }
    public float getAngleZ() { return this.angleZ; }

    public int getTextureId() {
        if(texture == null)
        {
            return R.drawable.unknown_texture;
        }
        else
        {
            return this.texture.getTextureId();
        }
    }

    public void bindData(ShaderProgram shader)
    {
        if(shader.getPositionAttributeLocation() != -1 && verticesLoaded)
        {
            vertexArray.setVertexAttribPointer(vertexStartPosition, shader.getPositionAttributeLocation(),
                    FLOATS_PER_VERTEX, stride);
        }

        if(shader.getTextureCoordinatesAttributeLocation() != -1 && texelsLoaded)
        {
            vertexArray.setVertexAttribPointer(texelStartPosition, shader.getTextureCoordinatesAttributeLocation(),
                    FLOATS_PER_TEXEL, stride);
        }

        if(shader.getNormalAttributeLocation() != -1 && normalsLoaded)
        {
            vertexArray.setVertexAttribPointer(normalStartPosition, shader.getNormalAttributeLocation(),
                    FLOATS_PER_NORMAL, stride);
        }
    }

    public void deleteBuffers()
    {
        GLES20.glDeleteBuffers(1, vbo, 0);
    }
}
