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
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Christian Benner on 10/08/2017.
 */

public class TextureShaderProgram extends ShaderProgram {
    // GLSL Program

    // Uniform locations
    private int uMatrixLocation;
    private int uTextureUnitLocation;
    private int uColor;

    // Attribute locations
    private int aPositionLocation;
    private int aTextureCoordinatesLocation;

    public TextureShaderProgram(Context context) {
        super(context, R.raw.texture_vertex,
                R.raw.texture_fragment);
    }

    public void getShaderVars()
    {
        // Retrieve uniform locations
        uMatrixLocation = glGetUniformLocation(program, ShaderConstants.U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, ShaderConstants.U_TEXTURE_UNIT);
        uColor = glGetUniformLocation(program, ShaderConstants.U_COLOUR);

        // Retrieve attribute locations
        aPositionLocation = glGetAttribLocation(program, ShaderConstants.A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, ShaderConstants.A_TEXTURE_COORDINATES);
    }

    public void setUniforms(int textureId, float alpha) {
        setUniforms(DefaultMatrix, textureId, alpha);
    }

    public void setTextureUniform(int textureid)
    {
        // Set alpha for tex
        glUniform4f(uColor, 1.0f, 1.0f, 1.0f, 1.0f);

        // Set the active texture unit to 0
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit
        glBindTexture(GL_TEXTURE_2D, textureid);

        // Tell texture uniform sampler to use by telling it to read from texture unit 0
        glUniform1i(uTextureUnitLocation, 0);
    }

    public void setMatrixUniform(float[] matrix)
    {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public void setColorUniform(float red, float green, float blue, float alpha)
    {
        // Set alpha for tex
        glUniform4f(uColor, red, green, blue, alpha);
    }

    public void setUniforms(int textureId)
    {
        setUniforms(DefaultMatrix, textureId, 1.0f);
    }

    public void setUniforms(float[] matrix, int textureId)
    {
        setUniforms(matrix, textureId, 1.0f);
    }

    public void setUniforms(float[] matrix, int textureId, float alpha) {
        setUniforms(matrix, textureId, 1.0f, 1.0f, 1.0f, alpha);
    }

    public void setUniforms(float[] matrix, int textureId, float red, float green, float blue,
                            float alpha) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // Set alpha for tex
        glUniform4f(uColor, red, green, blue, alpha);

        // Set the active texture unit to 0
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell texture uniform sampler to use by telling it to read from texture unit 0
        glUniform1i(uTextureUnitLocation, 0);
    }

    @Override
    public int getPositionAttributeLocation() { return aPositionLocation; }
    @Override
    public int getTextureCoordinatesAttributeLocation(){
        return aTextureCoordinatesLocation;
    }
    @Override
    public int getNormalAttributeLocation() { return -1; }
}
