package com.christianbenner.crispinandroid.render.shaders;

import android.content.Context;

import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.example.crispinandroid.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Christian Benner on 18/01/2018.
 */

public class PerFragLightingShader extends ShaderProgram {
    // Uniform locations
    private int uMVPMatrixLocation;
    private int uMVMatrixLocation;
    private int uLightLocation;
    private int uColourLocation;
    private int uAmbientDistance;
    private int uMinAmbient;
    private int uMaxAmbient;
    private int uAmbientLightColour;
    private int uLightsLocation;
    private int uLightCountLocation;

    private final String mvpMatrix = "u_MVPMatrix";
    private final String mvMatrix = "u_MVMatrix";
    private final String lightLocation = "u_LightPos";
    private final String normal = "a_Normal";

    // Attribute locations
    private int aPositionLocation;
    private int aNormal;

    public PerFragLightingShader(Context context) {
        super(context, R.raw.lighting_perfrag_vertex,
                R.raw.lighting_perfrag_frag);
    }

    public void getShaderVars()
    {
        // Retrieve uniform locations
        uMVPMatrixLocation = glGetUniformLocation(program, mvpMatrix);
        uMVMatrixLocation = glGetUniformLocation(program, mvMatrix);
        uLightLocation = glGetUniformLocation(program, lightLocation);
        uColourLocation = glGetUniformLocation(program, ShaderConstants.U_COLOUR);

        uAmbientDistance = glGetUniformLocation(program, ShaderConstants.U_AMBIENT_DISTANCE);
        uMinAmbient = glGetUniformLocation(program, ShaderConstants.U_MIN_AMBIENT);
        uMaxAmbient = glGetUniformLocation(program, ShaderConstants.U_MAX_AMBIENT);
        uAmbientLightColour = glGetUniformLocation(program, ShaderConstants.U_AMBIENT_LIGHT_COLOUR);

        uLightsLocation = glGetUniformLocation(program, ShaderConstants.U_LIGHTS);
        uLightCountLocation = glGetUniformLocation(program, ShaderConstants.U_LIGHT_COUNT);

        // Retrieve attribute locations
        aPositionLocation = glGetAttribLocation(program, ShaderConstants.A_POSITION);
        aNormal = glGetAttribLocation(program, normal);
    }

    public void setMatrixUniform(float[] mvMatrix, float[] mvpMatrix)
    {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
    }

    public void setUniforms(float[] mvMatrix, float[] mvpMatrix,
                            float lightX, float lightY, float lightZ,
                            float r, float g, float b,
                            float lr, float lg, float lb) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        glUniform3f(uLightLocation, lightX, lightY, lightZ);

        glUniform4f(uColourLocation, r, g, b, 1.0f);

        glUniform1f(uAmbientDistance, ambientDistance);
        glUniform1f(uMinAmbient, attenuation);
        glUniform1f(uMaxAmbient, maxAmbient);
        glUniform3f(uAmbientLightColour, lr, lg, lb);
    }

    public void setUniforms(float[] mvMatrix, float[] mvpMatrix,
                            float r, float g, float b) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        glUniform4f(uColourLocation, r, g, b, 1.0f);
    }

    @Override
    public int getPositionAttributeLocation() { return aPositionLocation; }
    @Override
    public int getTextureCoordinatesAttributeLocation(){
        return -1;
    }
    @Override
    public int getNormalAttributeLocation() { return aNormal; }
}
