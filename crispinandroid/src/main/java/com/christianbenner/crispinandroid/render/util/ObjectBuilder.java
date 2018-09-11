package com.christianbenner.crispinandroid.render.util;

import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Geometry.Circle;
import com.christianbenner.crispinandroid.util.Geometry.Cylinder;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Created by Christian Benner on 11/08/2017.
 */

public class ObjectBuilder {
    public static interface DrawCommand {
        void draw();
    }

    public static class GeneratedData{
        public final float[] vertexData;
        public final List<DrawCommand> drawList;

        GeneratedData(float[] vertexData, List<DrawCommand> drawList){
            this.vertexData = vertexData;
            this.drawList = drawList;
        }
    }

    public GeneratedData build(){
        return new GeneratedData(vertexData, drawList);
    }

    private static final int FLOATS_PER_VERTEX = 3;
    private final float[] vertexData;
    private int offset = 0;
    private final List<DrawCommand> drawList = new ArrayList<DrawCommand>();

    public ObjectBuilder(int sizeInVertices){
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }

    public static int sizeOfCirlceInVertices(int numPoints){
        return 1 + (numPoints + 1); // Two vertices to start triangle fan
    }

    public static int sizeOfOpenCylinderInVertices(int numPoints){
        return (numPoints + 1) * 2;
    }

    public static int sizeOfCuboidInVertices()
    {
        final int FACES = 3;
        final int VERTEX_PER_FACE = 6;
        final int FLOATS_PER_VERTEX = 3;
        return FACES * VERTEX_PER_FACE * FLOATS_PER_VERTEX;
    }

    public void appendOpenCylinder(Cylinder cylinder, final int numPoints){
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
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }

    // Build a circle to be drawn with a triangle fan
    public void appendCircle(Circle circle, final int numPoints){
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCirlceInVertices(numPoints);

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });

        // Center point of fan
        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;

        // Fan around the center point
        for(int i = 0; i <= numPoints; i++){
            float angleInRadians =
                    ((float) i / (float) numPoints)
                    * ((float) Math.PI * 2f);

            vertexData[offset++] =
                    (float) (circle.center.x
                                    + circle.radius * Math.cos(angleInRadians));
            vertexData[offset++] = circle.center.y;
            vertexData[offset++] =
                    (float) (circle.center.z
                                    + circle.radius * Math.sin(angleInRadians));
        }
    }

    public void appendCuboid(Geometry.Cuboid cuboid)
    {
        final int FACES = 3;
        final int VERTEX_PER_FACE = 6;
        final int FLOATS_PER_VERTEX = 3;

        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = FACES * VERTEX_PER_FACE;

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });

        // Sort vertex data to suit width and height
        float xUpperBound = cuboid.center.x + (cuboid.width / 2.0f);
        float xLowerBound = cuboid.center.x - (cuboid.width / 2.0f);
        float yUpperBound = cuboid.center.y + (cuboid.height / 2.0f);
        float yLowerBound = cuboid.center.y - (cuboid.height / 2.0f);
        float zUpperBound = cuboid.center.z + (cuboid.depth / 2.0f);
        float zLowerBound = cuboid.center.z - (cuboid.depth / 2.0f);

        // XYZ
        // Front Face
        addFace(new Geometry.Point(xLowerBound, yUpperBound, zUpperBound),
                new Geometry.Point(xLowerBound, yLowerBound, zUpperBound),
                new Geometry.Point(xUpperBound, yUpperBound, zUpperBound),
                new Geometry.Point(xUpperBound, yLowerBound, zUpperBound));

        // Right Face
        addFace(new Geometry.Point(xUpperBound, yUpperBound, zUpperBound),
                new Geometry.Point(xUpperBound, yLowerBound, zUpperBound),
                new Geometry.Point(xUpperBound, yUpperBound, zLowerBound),
                new Geometry.Point(xUpperBound, yLowerBound, zLowerBound));

        // Back Face
        addFace(new Geometry.Point(xLowerBound, yUpperBound, zLowerBound),
                new Geometry.Point(xLowerBound, yLowerBound, zLowerBound),
                new Geometry.Point(xUpperBound, yUpperBound, zLowerBound),
                new Geometry.Point(xUpperBound, yLowerBound, zLowerBound));

        // Left Face
        addFace(new Geometry.Point(xLowerBound, yUpperBound, zUpperBound),
                new Geometry.Point(xLowerBound, yLowerBound, zUpperBound),
                new Geometry.Point(xLowerBound, yUpperBound, zLowerBound),
                new Geometry.Point(xLowerBound, yLowerBound, zLowerBound));

        // Bottom Face
        addFace(new Geometry.Point(xLowerBound, yLowerBound, zUpperBound),
                new Geometry.Point(xLowerBound, yLowerBound, zLowerBound),
                new Geometry.Point(xUpperBound, yLowerBound, zUpperBound),
                new Geometry.Point(xUpperBound, yLowerBound, zLowerBound));

        // Top Face
        addFace(new Geometry.Point(xLowerBound, yUpperBound, zUpperBound),
                new Geometry.Point(xLowerBound, yUpperBound, zLowerBound),
                new Geometry.Point(xUpperBound, yUpperBound, zUpperBound),
                new Geometry.Point(xUpperBound, yUpperBound, zLowerBound));
    }

    private void addFace(Geometry.Point topLeft, Geometry.Point bottomLeft,
                          Geometry.Point topRight, Geometry.Point bottomRight)
    {
        vertexData[offset++] = topLeft.x;
        vertexData[offset++] = topLeft.y;
        vertexData[offset++] = topLeft.z;

        vertexData[offset++] = bottomLeft.x;
        vertexData[offset++] = bottomLeft.y;
        vertexData[offset++] = bottomLeft.z;

        vertexData[offset++] = topRight.x;
        vertexData[offset++] = topRight.y;
        vertexData[offset++] = topRight.z;

        vertexData[offset++] = topRight.x;
        vertexData[offset++] = topRight.y;
        vertexData[offset++] = topRight.z;

        vertexData[offset++] = bottomLeft.x;
        vertexData[offset++] = bottomLeft.y;
        vertexData[offset++] = bottomLeft.z;

        vertexData[offset++] = bottomRight.x;
        vertexData[offset++] = bottomRight.y;
        vertexData[offset++] = bottomRight.z;
    }
}
