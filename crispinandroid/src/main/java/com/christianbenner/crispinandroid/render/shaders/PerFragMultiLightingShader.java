package com.christianbenner.crispinandroid.render.shaders;

import android.content.Context;

import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.example.crispinandroid.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Christian Benner on 18/01/2018.
 */

public class PerFragMultiLightingShader extends ShaderProgram {

    // Attribute locations
    private int aPositionLocation;
    private int aNormal;
    private int aTextureCoordinatesLocation;

    public PerFragMultiLightingShader(Context context) {
        super(context, R.raw.multi_lighting_perfrag_vertex,
                R.raw.multi_lighting_perfrag_frag);

        // Retrieve uniform locations



    }

    public void getShaderVars()
    {
        // Retrieve attribute locations
        aPositionLocation = glGetAttribLocation(program, ShaderConstants.A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, ShaderConstants.A_TEXTURE_COORDINATES);
        aNormal = glGetAttribLocation(program, ShaderConstants.A_NORMAL);
    }

    public void setMatrixUniform(float[] mvMatrix, float[] mvpMatrix)
    {
        // Pass in matrix to shader program
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
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
        return aTextureCoordinatesLocation;
    }
    @Override
    public int getNormalAttributeLocation() { return aNormal; }
}
