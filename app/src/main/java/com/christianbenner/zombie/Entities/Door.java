package com.christianbenner.zombie.Entities;

import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;

public class Door
{
    public enum TYPE
    {
        WOOD,
        STEEL
    }

    private TYPE type;
    private Geometry.Point position;
    private int rotation;
    private int cost;
    private boolean open;
    private RendererModel model;

    public Door(TYPE type, Geometry.Point position, int rotation, int cost, RendererModel model)
    {
        // NOTE: The width of a door is 0.4 NDC

        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.cost = cost;
        this.model = model;

        this.model.newIdentity();
        this.model.setPosition(position);

/*        if(rotation == 0)
        {
            this.model.rotate(90.0f, 0.0f, -1.0f, 0.0f);
        }*/

        this.model.setScale(0.5f);
    }

    public boolean isOpen()
    {
        return this.open;
    }

    public void setOpen(boolean state)
    {
        this.open = state;
    }

    public TYPE getType() {
        return type;
    }

    public Geometry.Point getPosition() {
        return position;
    }

    public int getRotation() {
        return rotation;
    }

    public int getCost()
    {
        return cost;
    }

    public RendererModel getModel()
    {
        return model;
    }

    public void resetAngle()
    {
        angle = 0.0f;
    }

    private final float SCALE = 0.5f;
    private float angle = 0.0f;
    public void update(float deltaTime)
    {
        if(open)
        {
            this.model.newIdentity();
            this.model.setPosition(position);

            final float CENTER_Z_LOWER = -0.4f * SCALE;
            final float CENTER_Z_UPPER = -0.6f * SCALE;

            // Depending on which way the door is rotating, the axis of rotation is different
            // Also depends on the scale of the door because otherwise the point will be off
            final Geometry.Point POINT_OF_ROTATION;
            final float ROTATION_Y;
            switch (rotation)
            {
                case 0:
                    POINT_OF_ROTATION = new Geometry.Point(0.0f, 0.0f, CENTER_Z_LOWER);
                    ROTATION_Y = -1.0f;
                    break;
                case 1:
                    //pointOfRotation = position.translate(new Geometry.Vector(0.0f, 0.0f, -1.0f + (0.4f * SCALE)));
                    POINT_OF_ROTATION = new Geometry.Point(0.0f, 0.0f, CENTER_Z_UPPER);
                    ROTATION_Y = 1.0f;
                    break;
                default:
                    POINT_OF_ROTATION = position.translate(new Geometry.Vector(0.0f, 0.0f, -(0.4f * SCALE)));
                    ROTATION_Y = -1.0f;
                    break;
            }

            System.out.println("Rotation: " + rotation + ", Established Rotation Axis: " + POINT_OF_ROTATION);

            if(angle <= 90.0f)
            {
                angle += 2.0f * deltaTime;

                this.model.rotateAroundPos(POINT_OF_ROTATION, angle, 0.0f, ROTATION_Y, 0.0f);
            }

            if(angle > 90.0f)
            {
                angle = 90.0f;
            }

            this.model.setScale(SCALE);
        }
    }
}
