package com.christianbenner.crispinandroid.render.shaders;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.example.crispinandroid.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static com.christianbenner.crispinandroid.render.shaders.ShaderConstants.A_POSITION;
import static com.christianbenner.crispinandroid.render.shaders.ShaderConstants.U_COLOUR;
import static com.christianbenner.crispinandroid.render.shaders.ShaderConstants.U_MATRIX;
import static com.christianbenner.crispinandroid.render.shaders.ShaderConstants.U_TIME;

/**
 * Created by chris on 10/01/2018.
 */

public class ChangingColourShaderProgram extends ShaderProgram
{
    private final int uMatrixLocation;
    private final int uColourLocation;
    private final int aPositionLocation;
    private final int uTimeLocation;

    public ChangingColourShaderProgram(Context context) {
        super(context, R.raw.changing_colour_vertex, R.raw.changing_colour_fragment);

        // Retrieve uniform locations
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTimeLocation = glGetUniformLocation(program, U_TIME);
        uColourLocation = glGetUniformLocation(program, U_COLOUR);

        // Retrieve attribute locations
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
    }

    public void setUniforms(float[] matrix, float time, Colour colour) {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // Set colour uniform
        glUniform4f(uColourLocation, colour.r, colour.g, colour.b, colour.a);

        // Set time uniform
        glUniform1f(uTimeLocation, time);
    }

    public void setUniforms(float time, Colour colour)
    {
        setUniforms(DefaultMatrix, time, colour);
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
