package com.christianbenner.crispinandroid.render.objects;

import com.christianbenner.crispinandroid.render.util.VertexArray;
import com.christianbenner.crispinandroid.render.shaders.ColourShaderProgram;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;
import static com.christianbenner.crispinandroid.Constants.BYTES_PER_FLOAT;

/**
 * Created by Christian Benner on 08/01/2018.
 */

public class Sphere {
    VertexArray vertexArray;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int STRIDE = POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private static float[] VERTEX_DATA = new float[200];

    public Sphere() {
        // Generate the sphere
        // Generate circl of dots
        for(int i = 0; i < 100; i++)
        {
            double x = i * (Math.PI / 100);
            double y = Math.cos(x);
         //   double z = 1.0f;

            VERTEX_DATA[(POSITION_COMPONENT_COUNT * i)] = (float)x;
            VERTEX_DATA[(POSITION_COMPONENT_COUNT * i) + 1] = (float)y;
         //   VERTEX_DATA[(POSITION_COMPONENT_COUNT * i) + 2] = (float)z;

            System.out.println(x + ", " + y);
        }

        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(ColourShaderProgram colourShaderProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                colourShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw(){
        int count = VERTEX_DATA.length /
                (POSITION_COMPONENT_COUNT);
        glDrawArrays(GL_POINTS, 0, count);
    }
}
