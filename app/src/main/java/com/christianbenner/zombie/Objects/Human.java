package com.christianbenner.zombie.Objects;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.objects.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Renderer;
import com.christianbenner.zombie.R;

/**
 * Created by chris on 10/01/2018.
 */

public class Human {
    protected RendererModel leg_left;
    protected RendererModel leg_right;
    protected RendererModel arm_right;
    protected RendererModel arm_left;
    protected RendererModel body;
    protected RendererModel head;

    private Texture texture_human;

    private Context context;

    private static final int DEFAULT_GROUP_HUMAN = 200;
    private int rendererGroup = DEFAULT_GROUP_HUMAN;

    protected Geometry.Point position;
    private Geometry.Vector velocity;
    protected float facingAngle;
    private float desiredAngle;

    protected float movementSpeed;

    protected final Geometry.Point leftLegRotationAxis =
            new Geometry.Point(-0.05f, 0.35f, 0f );
    protected final Geometry.Point rightLegRotationAxis =
            new Geometry.Point(0.05f, 0.35f, 0f );
    protected final Geometry.Point leftArmRotationAxis =
            new Geometry.Point(-0.1f, 0.7f, 0f );
    protected final Geometry.Point rightArmRotationAxis =
            new Geometry.Point(0.1f, 0.7f, 0f );
    protected final Geometry.Point rightArmWaveRotationAxis =
            new Geometry.Point(0.15f, 0.65f, 0f );

    public Human(Context context, Texture texture, float movementSpeed)
    {
        this.context = context;
        texture_human = texture;
        this.position = new Geometry.Point(0.0f, 0.0f, 0.0f);
        this.velocity = new Geometry.Vector(0.0f, 0.0f, 0.0f);
        facingAngle = 0.0f;
        desiredAngle = 0.0f;
        createParts();
        this.movementSpeed = movementSpeed;
    }

    private void createParts()
    {
        leg_left = new RendererModel(context, R.raw.left_leg_clean, texture_human);
        leg_right = new RendererModel(context, R.raw.right_leg_clean, texture_human);
        arm_left = new RendererModel(context, R.raw.left_arm_clean, texture_human);
        arm_right = new RendererModel(context, R.raw.right_arm_clean, texture_human);
        body = new RendererModel(context, R.raw.body_clean, texture_human);
        head = new RendererModel(context, R.raw.head_clean, texture_human);
    }

    public void addToRenderer(Renderer renderer)
    {
        renderer.addModel(leg_left);
        renderer.addModel(leg_right);
        renderer.addModel(arm_left);
        renderer.addModel(arm_right);
        renderer.addModel(body);
        renderer.addModel(head);
    }

    public int getRendererGroup()
    {
        return rendererGroup;
    }

    public Geometry.Point getPosition()
    {
        return this.position;
    }

    public void setPosition(Geometry.Point position)
    {
        this.position = position;
        leg_left.newIdentity();
        leg_right.newIdentity();
        arm_left.newIdentity();
        arm_right.newIdentity();
        body.newIdentity();
        head.newIdentity();
        leg_left.setPosition(position);
        leg_right.setPosition(position);
        arm_left.setPosition(position);
        arm_right.setPosition(position);
        body.setPosition(position);
        head.setPosition(position);
    }

    public void setVelocity(Geometry.Vector vector)
    {
        this.velocity = vector.scale(movementSpeed);
    }

    public void translate(Geometry.Vector vector)
    {
        setPosition(this.position.translate(vector));
    }

    private void rotate(float angle)
    {
        leg_left.rotate(angle, 0.0f, 1.0f, 0.0f);
        leg_right.rotate(angle, 0.0f, 1.0f, 0.0f);
        arm_left.rotate(angle, 0.0f, 1.0f, 0.0f);
        arm_right.rotate(angle, 0.0f, 1.0f, 0.0f);
        body.rotate(angle, 0.0f, 1.0f, 0.0f);
        head.rotate(angle, 0.0f, 1.0f, 0.0f);
    }

    boolean up = false;
    float angle = 0.0f;

    boolean up_wave = false;
    float angle_wave = 0.0f;

    protected final float MAX_LIMB_MOVEMENT = 45.0f;
    protected final float MAX_LIMB_MOVEMENT_SPEED = 5.0f;
    private boolean waving = false;

    public void setWaving(boolean state)
    {
        waving = state;
    }

    public boolean isWaving()
    {
        return waving;
    }

    public void update(float deltaTime)
    {
        translate(velocity.scale(deltaTime));

        // Work out the facing angle from the velocity
        float angleRads = (float)Math.atan2(-velocity.z, velocity.x);
        if(angleRads != 0.0)
        {
            // Convert to Degrees
            facingAngle = ((angleRads / (float)Math.PI) * 180.0f) + 90.0f;
        }

        // How much the limbs should move (depends on how far the joystick is dragged)
        float limbMovementFactor = (velocity.length() / movementSpeed);

        if(angle > MAX_LIMB_MOVEMENT * limbMovementFactor)
        {
            up = false;
        }
        else if(angle < -MAX_LIMB_MOVEMENT * limbMovementFactor)
        {
            up = true;

        }

        if(up)
        {
            angle += (MAX_LIMB_MOVEMENT_SPEED * limbMovementFactor);
        }else
        {
            angle -= (MAX_LIMB_MOVEMENT_SPEED * limbMovementFactor);
        }

        // Rotate all the parts to face the right way
        leg_left.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        leg_right.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        arm_left.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        arm_right.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        body.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        head.rotate(facingAngle, 0.0f, 1.0f, 0.0f);

        // Rotate the arms and legs to move
        if(velocity.length() > 0.0f)
        {
            leg_left.rotateAroundPos(leftLegRotationAxis, angle, 1.0f, 0.0f, 0.0f);
            leg_right.rotateAroundPos(leftLegRotationAxis, -angle, 1.0f, 0.0f, 0.0f);
            arm_left.rotateAroundPos(leftArmRotationAxis, -angle, 1.0f, 0.0f, 0.0f);

            if(!waving)
            {
                arm_right.rotateAroundPos(rightArmRotationAxis, angle, 1.0f, 0.0f, 0.0f);
            }
        }
        else
        {
            angle = 0.0f;
        }


        if(angle_wave > 75.0f)
        {
            up_wave = false;
        }
        else if(angle_wave < 0.0f)
        {
            up_wave = true;
        }

        if(up_wave)
        {
            angle_wave += 5.0f;
        }
        else
        {
            angle_wave -= 5.0f;
        }

        if(waving)
        {
            arm_right.rotateAroundPos(rightArmWaveRotationAxis, angle_wave + 90.0f, 0.0f, 0.0f, 1.0f);
        }

     /*   if(waving) {
            // rotate arm upwards to a certain point
            //  Geometry.Point pos = arm_right.getPosition();
            //   Geometry.Point newPos = new Geometry.Point(0.0f, 0.0f, 0.0f);
            // arm_right.setPosition(newPos);
            arm_right.newIdentity();

        }*/

  //      rotate(facingAngle);
/*
        if(waving)
        {
            //arm_right.setPosition(new Geometry.Point(-0.1f, -0.7f, 0.0f));

            arm_right.newIdentity();

            Geometry.Point positionOfArm = new Geometry.Point(0.1f * (float)Math.cos(facingAngle), 0.7f * (float)Math.cos(facingAngle), 0.0f);
            Geometry.Point pointOfRotation = new Geometry.Point(
                    positionOfArm.x + 0.1f,
                    positionOfArm.y + 0.0f,
                    positionOfArm.z + 0.0f);

            // Position
            System.out.println(facingAngle);
            arm_right.setPosition(new Geometry.Point(
                    positionOfArm.x + position.x,
                    positionOfArm.y + position.y,
                    positionOfArm.z + position.z));
            arm_right.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
            arm_right.rotate(angle, 0.0f, 0.0f, 1.0f);
            // Position after rotation?
            arm_right.setPosition(new Geometry.Point(-pointOfRotation.x, -pointOfRotation.y, -pointOfRotation.z));

           // arm_right.setPosition(new Geometry.Point(0.1f + position.x,
         //         0.7f + position.y, 0.0f + position.z));
         //   arm_right.rotate(facingAngle + angle, 0.0f, 0.0f, 1.0f);
          //  arm_right.setPosition(new Geometry.Point(-0.2f, -0.7f, 0.0f));

            angle += 5.0f * deltaTime;

            if(angle >= 150.0f && moving_up)
            {
                moving_up = false;
                moving_down = true;
            }

            if(angle <= 100.0f && moving_down)
            {
                moving_down = false;
                moving_up = true;
            }

            if(moving_down)
            {
                angle -= 2.0f * deltaTime;
            }
            else if(moving_up)
            {
                angle += 2.0f * deltaTime;
            }
        }*/
    }
}
