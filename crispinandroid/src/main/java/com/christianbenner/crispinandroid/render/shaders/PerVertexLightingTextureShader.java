package com.christianbenner.crispinandroid.render.shaders;

import android.content.Context;

import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.example.crispinandroid.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Christian Benner on 18/01/2018.
 */

public class PerVertexLightingTextureShader extends ShaderProgram {
    // Uniform locations
    private final int uMVPMatrixLocation;
    private final int uMVMatrixLocation;
    private final int uTextureUnitLocation;
    private final int uLightLocation;
    private final int uColourLocation;
    private final int uAmbientDistance;
    private final int uMinAmbient;
    private final int uMaxAmbient;
    private final int uAmbientLightColour;

    private final String mvpMatrix = "u_MVPMatrix";
    private final String mvMatrix = "u_MVMatrix";
    private final String lightLocation = "u_LightPos";
    private final String normal = "a_Normal";

    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;
    private final int aNormal;

    public PerVertexLightingTextureShader(Context context) {
        super(context, R.raw.lighting_texture_pervertex_vertex,
                R.raw.lighting_texture_pervertex_frag);

        // Retrieve uniform locations
        uMVPMatrixLocation = glGetUniformLocation(program, mvpMatrix);
        uMVMatrixLocation = glGetUniformLocation(program, mvMatrix);
        uTextureUnitLocation = glGetUniformLocation(program, ShaderConstants.U_TEXTURE_UNIT);
        uLightLocation = glGetUniformLocation(program, lightLocation);
        uColourLocation = glGetUniformLocation(program, ShaderConstants.U_COLOUR);
        uAmbientDistance = glGetUniformLocation(program, ShaderConstants.U_AMBIENT_DISTANCE);
        uMinAmbient = glGetUniformLocation(program, ShaderConstants.U_MIN_AMBIENT);
        uMaxAmbient = glGetUniformLocation(program, ShaderConstants.U_MAX_AMBIENT);
        uAmbientLightColour = glGetUniformLocation(program, ShaderConstants.U_AMBIENT_LIGHT_COLOUR);

        // Retrieve attribute locations
        aPositionLocation = glGetAttribLocation(program, ShaderConstants.A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, ShaderConstants.A_TEXTURE_COORDINATES);
        aNormal = glGetAttribLocation(program, normal);
    }

    public void setTextureUniform(int textureid)
    {
        // Set the active texture unit to 0
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit
        glBindTexture(GL_TEXTURE_2D, textureid);

        // Tell texture uniform sampler to use by telling it to read from texture unit 0
        glUniform1i(uTextureUnitLocation, 0);
    }

    public void setMatrixUniform(float[] mvMatrix, float[] mvpMatrix)
    {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
    }

    public void setUniforms(float[] mvMatrix, float[] mvpMatrix, int textureId,
                            float lightX, float lightY, float lightZ,
                            float r, float g, float b,
                            float lr, float lg, float lb) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        glUniform3f(uLightLocation, lightX, lightY, lightZ);

        glUniform4f(uColourLocation, r, g, b, 1.0f);

        // Set the active texture unit to 0
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell texture uniform sampler to use by telling it to read from texture unit 0
        glUniform1i(uTextureUnitLocation, 0);

        glUniform1f(uAmbientDistance, ambientDistance);
        glUniform1f(uMinAmbient, attenuation);
        glUniform1f(uMaxAmbient, maxAmbient);
        glUniform3f(uAmbientLightColour, lr, lg, lb);
    }

    public void setUniforms(float[] mvMatrix, float[] mvpMatrix, int textureId,
                            float r, float g, float b) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        glUniform4f(uColourLocation, r, g, b, 1.0f);

        // Set the active texture unit to 0
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell texture uniform sampler to use by telling it to read from texture unit 0
        glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation(){
        return aTextureCoordinatesLocation;
    }

    public int getNormalAttributeLocation() { return aNormal; }
}
