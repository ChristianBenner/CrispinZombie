package com.christianbenner.crispinandroid.programs;

import android.content.Context;

import com.christianbenner.crispinandroid.util.ShaderProgram;
import com.example.crispinandroid.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Christian Benner on 18/01/2018.
 */

public class PerFragLightingTextureShader extends ShaderProgram {
    // Uniform locations
    private final int uMVPMatrixLocation;
    private final int uMVMatrixLocation;
    private final int uLightLocation;
    private final int uColourLocation;
    private final int uAmbientDistance;
    private final int uMinAmbient;
    private final int uMaxAmbient;
    private final int uAmbientLightColour;
    private final int uLightsLocation;
    private final int uLightCountLocation;

    private final String mvpMatrix = "u_MVPMatrix";
    private final String mvMatrix = "u_MVMatrix";
    private final String lightLocation = "u_LightPos";
    private final String normal = "a_Normal";

    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;
    private final int aNormal;

    public PerFragLightingTextureShader(Context context) {
        super(context, R.raw.lighting_texture_perfrag_vertex,
                R.raw.lighting_texture_perfrag_frag);

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
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, ShaderConstants.A_TEXTURE_COORDINATES);
        aNormal = glGetAttribLocation(program, normal);
    }

    public void setMatrixUniform(float[] mvMatrix, float[] mvpMatrix)
    {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
    }

    public void setUniforms(float[] mvMatrix, float[] mvpMatrix, int textureId,
                            float r, float g, float b) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        glUniform4f(uColourLocation, r, g, b, 1.0f);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnitLocation, 0);
    }

    @Override
    public int getPositionAttributeLocation() { return aPositionLocation; }
    @Override
    public int getTextureCoordinatesAttributeLocation(){
        return aTextureCoordinatesLocation;
    }
    @Override
    public int getNormalAttributeLocation() { return aNormal; }
}
