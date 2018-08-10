package com.christianbenner.crispinandroid.programs;

import android.content.Context;

import com.christianbenner.crispinandroid.util.ShaderProgram;
import com.example.crispinandroid.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static com.christianbenner.crispinandroid.programs.ShaderConstants.A_POSITION;
import static com.christianbenner.crispinandroid.programs.ShaderConstants.U_COLOUR;
import static com.christianbenner.crispinandroid.programs.ShaderConstants.U_MATRIX;

/**
 * Created by Christian Benner on 13/12/2017.
 */

public class ColourShaderProgram extends ShaderProgram {
    int uMatrixLocation;
    int uColor;
    int aPositionLocation;

    public ColourShaderProgram(Context context) {
        super(context, R.raw.colour_vertex, R.raw.colour_fragment);

        // Retrieve uniform locations
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uColor = glGetUniformLocation(program, U_COLOUR);

        // Retrieve attribute locations
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
    }

    public void setUniforms(float[] matrix, float r, float g, float b, float a) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // Set alpha for tex
        glUniform4f(uColor, r, g, b, a);
    }

    public void setUniforms(float r, float g, float b, float a)
    {
        setUniforms(DefaultMatrix, r, g, b, a);
    }

    @Override
    public int getPositionAttributeLocation() { return aPositionLocation; }
    @Override
    public int getTextureCoordinatesAttributeLocation(){
        return -1;
    }
    @Override
    public int getNormalAttributeLocation() { return -1; }
}
