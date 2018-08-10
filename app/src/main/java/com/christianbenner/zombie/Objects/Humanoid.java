package com.christianbenner.zombie.Objects;

import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.data.objects.Cube;
import com.christianbenner.crispinandroid.programs.ChangingColourShaderProgram;
import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.programs.ColourShaderProgram;
import com.christianbenner.crispinandroid.util.Camera;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.ObjectBuilder;

import java.util.List;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static com.christianbenner.crispinandroid.util.ObjectBuilder.sizeOfCuboidInVertices;
import static com.christianbenner.crispinandroid.util.ObjectBuilder.sizeOfOpenCylinderInVertices;

/**
 * Created by Christian Benner on 09/01/2018.
 */

public class Humanoid {
    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float height = 2.0f;
    public final float width = 0.5f;
    public final float depth = 0.2f;

    private float colourTime = 0.0f;
    private Colour colour;

    private Geometry.Point position = new Geometry.Point(0.0f, 0.0f, 0.0f);

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;


    // WIP
    private Cube cube;

    public Humanoid()
    {
        ObjectBuilder.GeneratedData generatedData = createHumanoid(width, height, depth, 32);

        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
        colour = new Colour(1.0f, 0.0f, 0.0f);

        cube = new Cube();
    }

    public void draw()
    {
        for(ObjectBuilder.DrawCommand drawCommand : drawList)
        {
            drawCommand.draw();
        }
    }

    public void bindData(ColourShaderProgram colourShaderProgram)
    {
        vertexArray.setVertexAttribPointer(0,
                colourShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }


    public void bindData(ChangingColourShaderProgram changingColourShader)
    {
        vertexArray.setVertexAttribPointer(0,
                changingColourShader.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }

    private float[] modelMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];
    public void render(Camera camera, ChangingColourShaderProgram shader)
    {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, position.x, position.y + (height / 2.0f),
                position.z);
        multiplyMM(modelViewProjectionMatrix, 0, camera.getViewProjectionMatrix(),
                0, modelMatrix, 0);

        shader.useProgram();
        shader.setUniforms(modelViewProjectionMatrix, colourTime, colour);
        bindData(shader);
        draw();
    }

    private float[] viewProjectionMatrix = new float[16];
    public void newRender(Camera camera, ChangingColourShaderProgram shader)
    {
        viewProjectionMatrix = camera.getViewProjectionMatrix();
        shader.useProgram();
        cube.bindData(shader);

        // position

        // Cube specific model matrix
        // translate by cube position * scale
        /*Geometry.Point cubePosition = new Geometry.Point()
        setIdentityM(modelMatrix, 0);
        scaleM(modelMatrix, 0, scale, scale, scale);
        translateM(modelMatrix, 0,
                position.x + ( * scale),
                position.y + ((height * scale) / 2.0f) / scale,
                position.z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);*/

        // set uniforms

        // Render cubes for legs and arms etc

    }

    public void setPosition(Geometry.Point position)
    {
        this.position = position;
    }

    public void setColourTime(float time)
    {
        this.colourTime = time;
    }

    public void setColour(Colour colour)
    {
        this.colour = colour;
    }

    // only build objects if they are going to be static
    static ObjectBuilder.GeneratedData createHumanoid(float width, float height, float depth,
                                                      int numPoints)
    {
        int size = (sizeOfOpenCylinderInVertices(numPoints) * 2)
                + (sizeOfCuboidInVertices() * 2);
        ObjectBuilder objectBuilder = new ObjectBuilder(size);

        Geometry.Point leftLegCenter = new Geometry.Point(-width / 4.0f,
                -(3.0f * height) / 10.0f, 0.0f);
        Geometry.Point rightLegCenter = new Geometry.Point(width / 4.0f,
                -(3.0f * height) / 10.0f, 0.0f);

        // Generate the vertex data
        // center, radius, height
        Geometry.Cylinder leftLeg = new Geometry.Cylinder(leftLegCenter,
                depth / 2.0f, (2 * height) / 5.0f);
        Geometry.Cylinder rightLeg = new Geometry.Cylinder(rightLegCenter,
                depth / 2.0f, (2 * height) / 5.0f);

     //   float radius = depth / 2.0f;
     //   Geometry.Cylinder leftArm = new Geometry.Cylinder(new Geometry.Point(-(width/2.0f) - radius, 1.0f / 5.0f, 0.0f),
     //           radius, (1.0f * height) / 5.0f);
     //   Geometry.Cylinder rightArm = new Geometry.Cylinder(new Geometry.Point((width/2.0f) + radius, 1.0f / 5.0f, 0.0f),
     //           radius, (2.0f * height) / 5.0f);

        Geometry.Cuboid body = new Geometry.Cuboid(new Geometry.Point(0.0f, 1.0f / 5.0f, 0.0f),
                width, (2.0f * height) / 5.0f, depth);

        float headHeight = 0.3f;
        float headCenter = (1.0f / 5.0f) + (height / 5.0f) + (headHeight / 2.0f);
        Geometry.Cuboid head = new Geometry.Cuboid(new Geometry.Point(0.0f, headCenter, 0.0f),
                width / 1.5f, headHeight, depth / 1.5f);

        objectBuilder.appendOpenCylinder(leftLeg, numPoints);
        objectBuilder.appendOpenCylinder(rightLeg, numPoints);
        objectBuilder.appendCuboid(body);
        objectBuilder.appendCuboid(head);
      //  objectBuilder.appendOpenCylinder(leftArm, numPoints);
      //  objectBuilder.appendOpenCylinder(rightArm, numPoints);

        return objectBuilder.build();
    }
}
