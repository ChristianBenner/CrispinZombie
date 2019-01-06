package com.christianbenner.zombie.Entities;

import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;

public class Door
{
    // Rotation Details:
    // 0 - Horizontal Door BL
    // 1 - Horizontal Door TL
    // 2 - Horizontal Door BR
    // 3 - Horizontal Door TR
    // 4 - Vertical Door BL
    // 5 - Vertical Door TL
    // 6 - Vertical Door BR
    // 7 - Vertical Door TR

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

    private final float START_ANGLE;
    private final Geometry.Point POINT_OF_ROTATION;
    private final float ROTATION_Y;

    private static final float SCALE = 0.5f;
    private static final float HORIZONTAL_Z_LOWER = -0.4f * SCALE;
    private static final float HORIZONTAL_Z_UPPER = -0.6f * SCALE;
    private static final float HORIZONTAL_X_LEFT = 0.0f;
    private static final float HORIZONTAL_X_RIGHT = 1.0f * SCALE;
    private static final float VERTICAL_Z_LOWER = -HORIZONTAL_X_LEFT;
    private static final float VERTICAL_Z_UPPER = -HORIZONTAL_X_RIGHT;
    private static final float VERTICAL_X_LEFT = -HORIZONTAL_Z_LOWER;
    private static final float VERTICAL_X_RIGHT = -HORIZONTAL_Z_UPPER;

    public Door(TYPE type, Geometry.Point position, int rotation, int cost, RendererModel model)
    {
        // NOTE: The width of a door is 0.4 NDC

        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.cost = cost;
        this.model = model;

        // Depending on which way the door is rotating, the axis of rotation is different
        // Also depends on the scale of the door because otherwise the point will be off
        switch (rotation)
        {
            case 0:
                POINT_OF_ROTATION = new Geometry.Point(HORIZONTAL_X_LEFT, 0.0f, HORIZONTAL_Z_LOWER);
                ROTATION_Y = -1.0f;
                break;
            case 1:
                POINT_OF_ROTATION = new Geometry.Point(HORIZONTAL_X_LEFT, 0.0f, HORIZONTAL_Z_UPPER);
                ROTATION_Y = 1.0f;
                break;
            case 2:
                POINT_OF_ROTATION = new Geometry.Point(HORIZONTAL_X_RIGHT, 0.0f, HORIZONTAL_Z_LOWER);
                ROTATION_Y = 1.0f;
                break;
            case 3:
                POINT_OF_ROTATION = new Geometry.Point(HORIZONTAL_X_RIGHT, 0.0f, HORIZONTAL_Z_UPPER);
                ROTATION_Y = -1.0f;
                break;
            case 4:
                POINT_OF_ROTATION = new Geometry.Point(VERTICAL_X_LEFT, 0.0f, VERTICAL_Z_LOWER);
                ROTATION_Y = 1.0f;
                break;
            case 5:
                POINT_OF_ROTATION = new Geometry.Point(VERTICAL_X_LEFT, 0.0f, VERTICAL_Z_UPPER);
                ROTATION_Y = -1.0f;
                break;
            case 6:
                POINT_OF_ROTATION = new Geometry.Point(VERTICAL_X_RIGHT, 0.0f, VERTICAL_Z_LOWER);
                ROTATION_Y = -1.0f;
                break;
            case 7:
                POINT_OF_ROTATION = new Geometry.Point(VERTICAL_X_RIGHT, 0.0f, VERTICAL_Z_UPPER);
                ROTATION_Y = 1.0f;
                break;
            default:
                POINT_OF_ROTATION = new Geometry.Point(HORIZONTAL_X_LEFT, 0.0f, HORIZONTAL_Z_LOWER);
                ROTATION_Y = -1.0f;
                break;
        }

        this.model.newIdentity();
        this.model.setPosition(position);

        // If the door starts vertical
        switch (rotation)
        {
            case 4:
            case 5:
            case 6:
            case 7:
                // rotate around the mid point
                this.model.rotateAroundPos(new Geometry.Point(0.25f, 0.0f, -0.25f), 90.0f, 0.0f, -1.0f, 0.0f);
                START_ANGLE = 90.0f;
                break;
            default:
                START_ANGLE = 0.0f;
                break;
        }

        this.model.setScale(0.5f);
    }

    public boolean isHorizontal()
    {
        switch (getRotation())
        {
            // VERTICAL DOORS
            case 4:
            case 5:
            case 6:
            case 7:
                return false;
            default:
                return true;
        }
    }

    public boolean isOpen()
    {
        return this.open;
    }

    public void setOpen(boolean state)
    {
        this.open = state;
        if(state == false)
        {
            angle = 0.0f;
            this.model.newIdentity();
            this.model.setPosition(position);
            // If the door starts vertical
            switch (rotation)
            {
                case 4:
                case 5:
                case 6:
                case 7:
                    // rotate around the mid point
                    this.model.rotateAroundPos(new Geometry.Point(0.25f, 0.0f, -0.25f), 90.0f, 0.0f, -1.0f, 0.0f);
                    break;
                default:
                    break;
            }
            this.model.setScale(0.5f);
        }
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


    private float angle = 0.0f;
    public void update(float deltaTime)
    {
        if(open)
        {
            this.model.newIdentity();
            this.model.setPosition(position);

            if(angle <= 90.0f)
            {
                angle += 2.0f * deltaTime;
            }

            if(angle > 90.0f)
            {
                angle = 90.0f;
            }

            this.model.rotateAroundPos(POINT_OF_ROTATION, angle, 0.0f, ROTATION_Y, 0.0f);
            switch (rotation)
            {
                case 4:
                case 5:
                case 6:
                case 7:
                    this.model.rotateAroundPos(new Geometry.Point(0.25f, 0.0f, -0.25f), 90.0f, 0.0f, -1.0f, 0.0f);
                    break;
            }

            this.model.setScale(SCALE);
        }
    }
}
