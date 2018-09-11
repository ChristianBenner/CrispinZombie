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

// This class is built for GLSL program files 'font_vertex_shader.glsl' and
// 'font_fragment_shader.glsl'

public class FontShaderProgram extends ShaderProgram {
    // Uniform locations
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;
    private final int uColor;

    // Uniform Variables
    private final String U_COLOR = "u_Color";
    private final String U_MATRIX = "u_Matrix";
    private final String U_TEXTURE_UNIT = "u_TextureUnit";
    private final String A_POSITION = "a_Position";
    private final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    private float[] modelMatrix = new float[16];

    public FontShaderProgram(Context context) {
        super(context, R.raw.font_vertex,
                R.raw.font_fragment);

        // Retrieve uniform locations
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        uColor = glGetUniformLocation(program, U_COLOR);

        // Retrieve attribute locations
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix, int textureId, float r, float g, float b, float alpha) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // Set alpha for tex
        glUniform4f(uColor, r, g, b, alpha);

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
