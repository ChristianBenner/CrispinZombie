package com.christianbenner.crispinandroid.ui;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.christianbenner.crispinandroid.render.util.VertexArray;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.christianbenner.crispinandroid.render.util.TextureHelper;

/**
 * Created by Christian Benner on 03/04/2018.
 */

public class MoveController extends Button
{
    private VertexArray moveVertexArray;
    private int moveVertexCount;
    private Geometry.Point outerOffset;
    private float radius;
    private BaseController baseController;

    private final float[] vertexData = {
            // XYST Triangles
            0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f,
            1f, 1f, 1f, 0f,
            1f, 1f, 1f, 0f,
            0f, 0f, 0f, 1f,
            1f, 0f, 1f, 1f
    };

    public MoveController(Context context, BaseController controller, int textureResource)
    {
        super(new UIDimension(
                controller.getPosition().x + controller.getRadius() -
                        (controller.getRadius() / 2.0f),
                        controller.getPosition().y + controller.getRadius() -
                                (controller.getRadius() / 2.0f),
                        (controller.getRadius() / 2.0f) * 2.0f,
                        (controller.getRadius() / 2.0f) * 2.0f),
                TextureHelper.loadTexture(context, textureResource,
                        true));

        this.baseController = controller;
        moveVertexArray = new VertexArray(vertexData);
        moveVertexCount = vertexData.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);
        outerOffset = new Geometry.Point(0.0f, 0.0f, 0.0f);
        radius = controller.getRadius() / 2.0f;
    }

    public void setOuterOffset(Geometry.Point offset)
    {
        this.outerOffset = offset;
        this.dimensions.x = baseController.getPosition().x +
                baseController.getRadius() - radius + offset.x;
        this.dimensions.y = baseController.getPosition().y +
                baseController.getRadius() - radius + offset.y;
    }

    public Geometry.Point getOuterOffset()
    {
        return this.outerOffset;
    }

    @Override
    public void bindData(ShaderProgram shader) {
        moveVertexArray.setVertexAttribPointer(
                0,
                shader.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        moveVertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                shader.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public Geometry.Vector getDirection()
    {
        float xDirection = ((dimensions.x + radius) - baseController.getPosition().x -
                baseController.getRadius()) / baseController.getRadius();
        float yDirection = ((dimensions.y + radius) - baseController.getPosition().y -
                baseController.getRadius()) / baseController.getRadius();
        Geometry.Vector direction = new Geometry.Vector(xDirection, yDirection, 0.0f);
        return direction;
    }

    @Override
    public void draw()
    {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, moveVertexCount);
    }

    @Override
    public void drag(PointF pos)
    {
        if(isButtonDown())
        {
            // Calculate offset
            float xOffset = pos.x - baseController.getPosition().x - baseController.getRadius();
            float yOffset = pos.y - baseController.getPosition().y - baseController.getRadius();
            Geometry.Vector drag = new Geometry.Vector(xOffset, yOffset, 0.0f);
            if(drag.length() > baseController.getRadius())
            {
                Geometry.Vector newVec = drag.scale(baseController.getRadius() / drag.length());
                dimensions.x = newVec.x + baseController.getPosition().x +
                        baseController.getRadius() - radius;
                dimensions.y = newVec.y + baseController.getPosition().y +
                        baseController.getRadius() - radius;
            }
            else
            {
                dimensions.x = pos.x - radius;
                dimensions.y = pos.y - radius;
            }
        }
    }

    @Override
    public void release(PointF position)
    {
        if(buttonDown)
        {
            // Set position back to original offset
            setOuterOffset(new Geometry.Point(0.0f, 0.0f, 0.0f));

            // Send the release event
            super.sendReleaseEvent(position);

            buttonDown = false;
        }
    }
}
