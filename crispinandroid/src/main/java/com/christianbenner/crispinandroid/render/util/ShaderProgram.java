package com.christianbenner.crispinandroid.render.util;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Light;
import com.christianbenner.crispinandroid.render.shaders.ShaderConstants;
import com.christianbenner.crispinandroid.render.text.TextResourceReader;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_INVALID_VALUE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Christian Benner on 10/08/2017.
 */

public abstract class ShaderProgram {
    // For lighting shader programs
    protected float ambientDistance = ShaderConstants.DEFAULT_AMBIENT_DISTANCE;
    protected float attenuation = ShaderConstants.DEFAULT_ATTENUATION;
    protected float maxAmbient = ShaderConstants.DEFAULT_MAX_AMBIENT;
    private ArrayList<Light> lights;

    protected int uLightColourArrayLocation;
    protected int uLightPositionArrayLocation;
    protected int uLightCountLocation;
    protected int uLightAmbientData;
    protected int uTextureUnitLocation;
    protected int uMVMatrixLocation;
    protected int uMVPMatrixLocation;
    protected int uMatrix;
    protected int uColourLocation;

    // Shader program
    protected int program;
    protected int vertexShader;
    protected int fragmentShader;

    private String vertexShaderCode;
    private String fragmentShaderCode;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceID){
        // Read shaders to string
        vertexShaderCode = TextResourceReader
                .readTextFileFromResource(context, vertexShaderResourceId);
        fragmentShaderCode = TextResourceReader
                .readTextFileFromResource(context, fragmentShaderResourceID);
    }

    protected ShaderProgram(String vertexShaderCode, String fragmentShaderCode)
    {
        this.vertexShaderCode = vertexShaderCode;
        this.fragmentShaderCode = fragmentShaderCode;
    }

    public abstract void getShaderVars();

    public void onSurfaceCreated()
    {
        ShaderHelper.buildProgram(vertexShaderCode, fragmentShaderCode, this);

        uLightColourArrayLocation =
                glGetUniformLocation(program, ShaderConstants.U_LIGHT_COLOURS_ARRAY);
        uLightPositionArrayLocation =
                glGetUniformLocation(program, ShaderConstants.U_LIGHT_POSITIONS_ARRAY);
        uLightAmbientData =
                glGetUniformLocation(program, ShaderConstants.U_LIGHT_AMBIENT_DATA_ARRAY);
        uLightCountLocation =
                glGetUniformLocation(program, ShaderConstants.U_LIGHT_COUNT);
        uTextureUnitLocation = glGetUniformLocation(program, ShaderConstants.U_TEXTURE_UNIT);
        uMVMatrixLocation = glGetUniformLocation(program, ShaderConstants.U_MV_MATRIX);
        uMVPMatrixLocation = glGetUniformLocation(program, ShaderConstants.U_MVP_MATRIX);
        uColourLocation = glGetUniformLocation(program, ShaderConstants.U_COLOUR);
        uMatrix = glGetUniformLocation(program, ShaderConstants.U_MATRIX);

        getShaderVars();
    }

    public void setColourUniforms(Colour colour) {
        if(uColourLocation != GL_INVALID_VALUE) {
            glUniform4f(uColourLocation, colour.r, colour.g, colour.b, colour.a);
        }
    }

    public void setMVMatrixUniform(float[] matrix) {
        if(uMVMatrixLocation != GL_INVALID_VALUE) {
            glUniformMatrix4fv(uMVMatrixLocation, 1, false, matrix, 0);
        }
    }

    public void setMVPMatrixUniform(float[] matrix) {
        if(uMVPMatrixLocation != GL_INVALID_VALUE) {
            glUniformMatrix4fv(uMVPMatrixLocation, 1, false, matrix, 0);
        }
    }

    public void setMatrix(float[] matrix) {
        if(uMatrix != GL_INVALID_VALUE) {
            glUniformMatrix4fv(uMatrix, 1, false, matrix, 0);
        }
    }

    public void setAmbientDistance(float ambientDistance)
    {
        this.ambientDistance = ambientDistance;
    }

    public void setLights(ArrayList<Light> lights)
    {
        this.lights = lights;
        colourFloats = new float[lights.size() * 3];
        positionFloats = new float[lights.size() * 3];
        ambientDataFloats = new float[lights.size() * 3];
    }

    public void setMinAmbient(float minAmbient)
    {
        this.attenuation = minAmbient;
    }

    public void setMaxAmbient(float maxAmbient)
    {
        this.maxAmbient = maxAmbient;
    }

    public void setAmbientData(float distance, float max, float min)
    {
        this.ambientDistance = distance;
        this.maxAmbient = max;
        this.attenuation = min;
    }

    public float getAmbientDistance() { return this.ambientDistance; }
    public float getMinAmbient() { return this.attenuation; }
    public float getMaxAmbient() { return this.maxAmbient; }

    protected final float[] DefaultMatrix =
    {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    public void useProgram() {
        // Set the current OpenGL shader to this shader
        glUseProgram(program);
    }

    public void unbindProgram()
    {
        glUseProgram(0);
    }

    public void cleanUp()
    {
        glDeleteShader(program);
    }

    public abstract int getPositionAttributeLocation();
    public abstract int getNormalAttributeLocation();
    public abstract int getTextureCoordinatesAttributeLocation();

    public void setTextureUniforms(int textureId)
    {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnitLocation, 0);
    }

    // Pass light data
    private float[] colourFloats;
    private float[] positionFloats;
    private float[] ambientDataFloats;

    public void setLightUniforms()
    {
        if(lights != null)
        {
            if(lights.size() > colourFloats.length / 3)
            {
                colourFloats = new float[lights.size() * 3];
                positionFloats = new float[lights.size() * 3];
                ambientDataFloats = new float[lights.size() * 3];
            }


            glUniform1i(uLightCountLocation, lights.size());

            int colourIndex = 0;
            int positionIndex = 0;
            int ambientDataIndex = 0;

            for(int i = 0; i < lights.size(); i++)
            {
                // Check Light class to see what values are stored in the light data
                float[] lightData = lights.get(i).getLightData();

                colourFloats[colourIndex++] = lightData[0];
                colourFloats[colourIndex++] = lightData[1];
                colourFloats[colourIndex++] = lightData[2];
                positionFloats[positionIndex++] = lightData[3];
                positionFloats[positionIndex++] = lightData[4];
                positionFloats[positionIndex++] = lightData[5];
                ambientDataFloats[ambientDataIndex++] = lightData[6];
                ambientDataFloats[ambientDataIndex++] = lightData[7];
                ambientDataFloats[ambientDataIndex++] = lightData[8];

                // Pass Colour Data
                glUniform3fv(uLightColourArrayLocation, i + 1, colourFloats, 0);

                // Pass Position Data
                glUniform3fv(uLightPositionArrayLocation, i + 1, positionFloats, 0);

                // Pass Ambient Data
                glUniform3fv(uLightAmbientData, i + 1, ambientDataFloats, 0);
            }
        }
        else
        {
            System.out.println("Lights are null");
        }
    }
}
