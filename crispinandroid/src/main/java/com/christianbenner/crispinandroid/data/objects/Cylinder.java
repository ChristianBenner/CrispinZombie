package com.christianbenner.crispinandroid.data.objects;

import com.christianbenner.crispinandroid.programs.ChangingColourShaderProgram;
import com.christianbenner.crispinandroid.util.Geometry;

/**
 * Created by chris on 10/01/2018.
 */

public class Cylinder {
    //private final Geometry.Point center;
    //private final float radius;
    //private final float height;

    public Cylinder(Geometry.Point center, float radius, float height)
    {
       /* this.center = center;
        this.radius = radius;
        this.height = height;

        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
        final float yStart = cylinder.center.y - (cylinder.height / 2f);
        final float yEnd = cylinder.center.y + (cylinder.height / 2f);

        for (int i = 0; i <= numPoints; i++){
            float angleInRadians =
                    ((float) i / (float) numPoints)
                            * ((float) Math.PI * 2f);

            float xPosition = (float) (cylinder.center.x
                    + cylinder.radius * Math.cos(angleInRadians));

            float zPosition =
                    (float) (cylinder.center.z
                            + cylinder.radius * Math.sin(angleInRadians));

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yStart;
            vertexData[offset++] = zPosition;

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yEnd;
            vertexData[offset++] = zPosition;
        }*/
    }

    void bindData(ChangingColourShaderProgram colourShaderProgram)
    {

    }

   // void draw()
   // {
   //     glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
   // }
}
