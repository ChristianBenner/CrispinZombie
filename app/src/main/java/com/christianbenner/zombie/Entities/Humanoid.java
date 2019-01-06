package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.zombie.Map.Cell;
import com.christianbenner.zombie.Map.Map;
import com.christianbenner.zombie.R;

import java.util.LinkedList;

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
 //   protected RendererModel leg_right;
    protected RendererModel leg_right_lower;
    protected RendererModel leg_right_upper;
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
    private final float RADII = 0.2f;

    protected Map map;

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

    public Humanoid(Context context, Texture texture, float movementSpeed, Map map)
    {
        this.context = context;
        this.movementSpeed = movementSpeed;
        this.texture = texture;

        this.map = map;

        position = new Geometry.Point(0.0f, 0.0f, 0.0f);
        velocity = new Geometry.Vector(0.0f, 0.0f, 0.0f);
        facingAngle = 0.0f;
        createParts();
    }

    public void setColour(Colour colour)
    {
        body.setColour(colour);
        head.setColour(colour);
    }

    public void setAlpha(float alpha)
    {
        body.setAlpha(alpha);
        head.setAlpha(alpha);
    }

    public void checkHumanoidCollision(final Humanoid other)
    {
        // Calculate distance to player
        final float DISTANCE = position.distance(other.getPosition());

        if(DISTANCE < RADII + RADII)
        {
            // Move the zombie away from the player
            final Geometry.Vector DIRECTION = new Geometry.Vector(
                    other.getPosition().x - position.x, other.getPosition().y - position.y, other.getPosition().z - position.z);

            // Scale the vector to 0-1
            final Geometry.Vector MAG_VECTOR = DIRECTION.scale(-1.0f / DIRECTION.length());
            final Geometry.Vector MOVEMENT_VECTOR = MAG_VECTOR.scale(RADII + RADII - DISTANCE);

            // Add the movement to the position
            position.x += MOVEMENT_VECTOR.x;
            position.z += MOVEMENT_VECTOR.z;
        }
    }

    private final float DOOR_BUY_RADII = 0.8f;
    public boolean checkDoorCollision(Door door)
    {
        if(!door.isOpen())
        {
            // Calculate distance to player
            final Geometry.Point DOOR_POS = door.getPosition().
                    translate(new Geometry.Vector(0.25f, 0.0f, -0.25f));
            final float DISTANCE = position.distance(DOOR_POS);

            // If the door isn't open, push the player away
            if(DISTANCE < RADII + RADII)
            {
                // Move the zombie away from the player
                final Geometry.Vector DIRECTION = new Geometry.Vector(
                        DOOR_POS.x - position.x, DOOR_POS.y - position.y, DOOR_POS.z - position.z);

                // Scale the vector to 0-1
                final Geometry.Vector MAG_VECTOR = DIRECTION.scale(-1.0f / DIRECTION.length());
                final Geometry.Vector MOVEMENT_VECTOR = MAG_VECTOR.scale(RADII + RADII - DISTANCE);

                // Add the movement to the position
                position.x += MOVEMENT_VECTOR.x;
                position.z += MOVEMENT_VECTOR.z;
            }

            // If the player is in range to buy the door, return true so that the scene can display
            // the buy UI
            if(DISTANCE < RADII + DOOR_BUY_RADII)
            {
                return true;
            }
        }

        return false;
    }

    public void checkTileCollisions()
    {
        // Get the tiles surrounding the humanoid
        LinkedList<Cell> surroundingTiles = map.getSurroundingCollidableTiles(
                (int)(getPosition().x * 2f), (int)((getPosition().z * 2f) + 1.0f));

        // If there are no tiles
        if(surroundingTiles == null)
        {
            return;
        }

        // Run the collision on each cell
        for(Cell cell : surroundingTiles)
        {
            // Determine the position of the cell
            final Geometry.Point CELL_POSITION = map.getModelPosition(cell);

            // Calculate distance to player
            final float DISTANCE = position.distance(CELL_POSITION);

            if(DISTANCE < Map.TILE_SIZE + RADII)
            {
                // Move the zombie away from the player
                final Geometry.Vector DIRECTION = new Geometry.Vector(
                        CELL_POSITION.x - position.x, CELL_POSITION.y - position.y, CELL_POSITION.z - position.z);

                // Scale the vector to 0-1
                final Geometry.Vector MAG_VECTOR = DIRECTION.scale(-1.0f / DIRECTION.length());
                final Geometry.Vector MOVEMENT_VECTOR = MAG_VECTOR.scale(RADII + Map.TILE_SIZE - DISTANCE);

                // Add the movement to the position
                position.x += MOVEMENT_VECTOR.x;
                position.z += MOVEMENT_VECTOR.z;
            }
        }
    }

    public void removeFromRenderer(Renderer renderer)
    {
        renderer.removeModel(leg_left);
     //   renderer.removeModel(leg_right);
        renderer.removeModel(leg_right_lower);
        renderer.removeModel(leg_right_upper);
        renderer.removeModel(arm_left);
        renderer.removeModel(arm_right);
        renderer.removeModel(body);
        renderer.removeModel(head);
    }

    private void createParts()
    {
        leg_left = new RendererModel(context, R.raw.left_leg_clean, texture);
     //   leg_right = new RendererModel(context, R.raw.right_leg_clean, texture);
        leg_right_lower = new RendererModel(context, R.raw.wilbert_right_leg_lower, TextureHelper.loadTexture(context, R.drawable.box));
        leg_right_upper = new RendererModel(context, R.raw.wilbert_right_leg_upper, TextureHelper.loadTexture(context, R.drawable.box));
        arm_left = new RendererModel(context, R.raw.left_arm_clean, texture);
        arm_right = new RendererModel(context, R.raw.right_arm_clean, texture);
        body = new RendererModel(context, R.raw.wilbert_torso, TextureHelper.loadTexture(context, R.drawable.box));
        head = new RendererModel(context, R.raw.zhead, TextureHelper.loadTexture(context, R.drawable.box));
    }

    public void addToRenderer(Renderer renderer)
    {
     //   renderer.addModel(leg_left);
     //   renderer.addModel(leg_right);
     //   renderer.addModel(arm_left);
     //   renderer.addModel(arm_right);
     //   renderer.addModel(leg_right_upper);
     //   renderer.addModel(leg_right_lower);
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
    //    leg_right.newIdentity();
        leg_right_upper.newIdentity();
        leg_right_lower.newIdentity();
        arm_left.newIdentity();
        arm_right.newIdentity();
        body.newIdentity();
        head.newIdentity();
        leg_left.setPosition(position);
     //   leg_right.setPosition(position);
        leg_right_upper.setPosition(position.translate(new Geometry.Vector(-0.06f, 0.35f, -0.04f)));
        leg_right_lower.setPosition(position.translate(new Geometry.Vector(0.0f, 0.0f, 0.05f)));
        arm_left.setPosition(position);
        arm_right.setPosition(position);
        body.setPosition(position.translateY(0.65f));
        head.setPosition(position.translateY(0.95f));

        leg_right_upper.setScale(0.006f);
        leg_right_lower.setScale(0.008f);
        head.setScale(0.01f);
        body.setScale(0.007f);
    }

    public void setVelocity(Geometry.Vector vector)
    {
        this.velocity = vector.scale(movementSpeed);
    }

    public void translate(Geometry.Vector vector)
    {
        setPosition(this.position.translate(vector));
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
    //    leg_right.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        leg_right_lower.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        leg_right_upper.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        arm_left.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        arm_right.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        body.rotate(facingAngle, 0.0f, 1.0f, 0.0f);
        head.rotate(facingAngle + 180.0f, 0.0f, 1.0f, 0.0f);

        // Rotate the arms and legs to move
        if(velocity.length() > 0.0f)
        {
            leg_left.rotateAroundPos(leftLegRotationAxis, limbAngle, 1.0f, 0.0f, 0.0f);
           // leg_right.rotateAroundPos(leftLegRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);
            leg_right_lower.rotateAroundPos(leftLegRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);
           // leg_right_upper.rotateAroundPos(leftLegRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);
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
