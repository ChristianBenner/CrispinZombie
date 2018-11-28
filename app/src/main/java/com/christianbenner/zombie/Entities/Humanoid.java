package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.zombie.R;

/**
 * Created by chris on 10/01/2018.
 */

public class Humanoid {
    // Activity context
    protected Context context;

    // Constants
    private static final int DEFAULT_GROUP_HUMAN = 200;
    private int rendererGroup = DEFAULT_GROUP_HUMAN;
    protected final float MAX_LIMB_MOVEMENT = 45.0f;
    protected final float MAX_LIMB_MOVEMENT_SPEED = 5.0f;

    // The renderer models for each part of the human
    protected RendererModel leg_left;
    protected RendererModel leg_right;
    protected RendererModel arm_right;
    protected RendererModel arm_left;
    protected RendererModel body;
    protected RendererModel head;

    // The texture associated to the humanoid
    private Texture texture;

    // The current position
    protected Geometry.Point position;

    // The velocity
    private Geometry.Vector velocity;

    // The angle that the humanoid is facing
    protected float facingAngle;

    // The speed in which the humanoid can move
    protected float movementSpeed;

    // The state of the limb being raised
    boolean limbRaised = false;

    // The angle of the limbs
    float limbAngle = 0.0f;

    boolean upWave = false;
    float waveAngle = 0.0f;

    private boolean waving = false;

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

    public float[] getFirstFloats()
    {
        return head.getFirstFloats();
    }

    public Humanoid(Context context, Texture texture, float movementSpeed)
    {
        this.context = context;
        this.movementSpeed = movementSpeed;
        this.texture = texture;

        position = new Geometry.Point(0.0f, 0.0f, 0.0f);
        velocity = new Geometry.Vector(0.0f, 0.0f, 0.0f);
        facingAngle = 0.0f;
        createParts();
    }

    public void removeFromRenderer(Renderer renderer)
    {
        renderer.removeModel(leg_left);
        renderer.removeModel(leg_right);
        renderer.removeModel(arm_left);
        renderer.removeModel(arm_right);
        renderer.removeModel(body);
        renderer.removeModel(head);
    }

    private void createParts()
    {
        leg_left = new RendererModel(context, R.raw.left_leg_clean, texture);
        leg_right = new RendererModel(context, R.raw.right_leg_clean, texture);
        arm_left = new RendererModel(context, R.raw.left_arm_clean, texture);
        arm_right = new RendererModel(context, R.raw.right_arm_clean, texture);
        body = new RendererModel(context, R.raw.body_clean, texture);
        head = new RendererModel(context, R.raw.head_clean, texture);
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

    public float[] getModelMatrix()
    {
        return head.getModelMatrix();
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

        if(limbAngle > MAX_LIMB_MOVEMENT * limbMovementFactor)
        {
            limbRaised = false;
        }
        else if(limbAngle < -MAX_LIMB_MOVEMENT * limbMovementFactor)
        {
            limbRaised = true;

        }

        if(limbRaised)
        {
            limbAngle += (MAX_LIMB_MOVEMENT_SPEED * limbMovementFactor);
        }else
        {
            limbAngle -= (MAX_LIMB_MOVEMENT_SPEED * limbMovementFactor);
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
            leg_left.rotateAroundPos(leftLegRotationAxis, limbAngle, 1.0f, 0.0f, 0.0f);
            leg_right.rotateAroundPos(leftLegRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);
            arm_left.rotateAroundPos(leftArmRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);

            if(!waving)
            {
                arm_right.rotateAroundPos(rightArmRotationAxis, limbAngle, 1.0f, 0.0f, 0.0f);
            }
        }
        else
        {
            limbAngle = 0.0f;
        }

        if(waveAngle > 75.0f)
        {
            upWave = false;
        }
        else if(waveAngle < 0.0f)
        {
            upWave = true;
        }

        if(upWave)
        {
            waveAngle += 5.0f;
        }
        else
        {
            waveAngle -= 5.0f;
        }

        if(waving)
        {
            arm_right.rotateAroundPos(rightArmWaveRotationAxis, waveAngle + 90.0f, 0.0f, 0.0f,
                    1.0f);
        }
    }
}
