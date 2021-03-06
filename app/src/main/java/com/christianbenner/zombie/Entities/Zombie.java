package com.christianbenner.zombie.Entities;

import android.content.Context;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.UIRendererGroup;
import com.christianbenner.crispinandroid.ui.Image;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.zombie.Map.Cell;
import com.christianbenner.zombie.Map.Map;

import java.util.LinkedList;

/**
 * Created by Christian Benner on 19/05/2018.
 */

public class Zombie extends Humanoid {
    private Humanoid player;
    private final float ARM_ANGLE = -90.0f;

    private Image healthbar;
    private Image healthbarLife;
    private final int HEALTHBAR_WIDTH = 100;
    private final int HEALTHBAR_HEIGHT = 16;

    private float life = 100.0f;

    // Temp
    private long time = 0;
    private final long PATH_UPDATE_FREQUENCY = 5;
    private final long PATH_UPDATE_WAIT_MILLIS = 1000 / PATH_UPDATE_FREQUENCY;
    public  boolean hasPath = false;

    public Zombie(Context context, Texture texture, float movementSpeed, Humanoid humanoid, Geometry.Point position, Map map)
    {
        super(context, texture, movementSpeed, map);
        setPosition(position);
        this.player = humanoid;

        this.healthbar = new Image(new Dimension2D(0, 0, HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT),
                new Colour(1.0f, 0.0f, 0.0f, 0.0f));
        this.healthbarLife = new Image(new Dimension2D(0, 0, HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT),
            new Colour(0.0f, 1.0f, 0.0f, 0.0f));
    }

    public float getLife()
    {
        return this.life;
    }

    private LinkedList<Cell> path = null;
    Cell lastValid = null;

    public void kill()
    {
        life = 0.0f;
    }

    public void damage(float value)
    {
        life -= value;

        if(life <= 0.0f)
        {
            kill();
        }

    }

    public boolean isAlive()
    {
        return life > 0.0f;
    }

    final float[] HEALTHBAR_CENTER_POS =
    {
            0.0f, 1.0f, 0.0f, 1.0f
    };

    public void updateHealthbar(Camera camera, float uiCanvasWidth, float uiCanvasHeight)
    {
        // Update the healthbar position to the zombies position
        float[] ndcCoordinates = new float[4];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        Matrix.multiplyMM(modelViewMatrix, 0, camera.getViewMatrix(), 0, getModelMatrix(), 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, camera.getProjectionMatrix(), 0, modelViewMatrix, 0);
        Matrix.multiplyMV(ndcCoordinates, 0, modelViewProjectionMatrix, 0, HEALTHBAR_CENTER_POS, 0);
        ndcCoordinates[0] /= ndcCoordinates[3];
        ndcCoordinates[1] /= ndcCoordinates[3];
        ndcCoordinates[2] /= ndcCoordinates[3];
        healthbar.setPosition(new Geometry.Point((((ndcCoordinates[0] + 1.0f) / 2.0f) *
                uiCanvasWidth) - (HEALTHBAR_WIDTH / 2.0f),
                (((ndcCoordinates[1] + 1.0f) / 2.0f) * uiCanvasHeight) + (HEALTHBAR_HEIGHT / 2.0f), 0.0f));
        healthbar.setAlpha(1.0f);

        final int LIFE_PIXELS = (int)((this.life / 100.0f) * HEALTHBAR_WIDTH);

        healthbarLife.setDimensions(new Dimension2D(
                (((ndcCoordinates[0] + 1.0f) / 2.0f) * uiCanvasWidth) - (HEALTHBAR_WIDTH / 2.0f),
                (((ndcCoordinates[1] + 1.0f) / 2.0f) * uiCanvasHeight) + (HEALTHBAR_HEIGHT / 2.0f),
                LIFE_PIXELS, HEALTHBAR_HEIGHT));
        healthbarLife.setAlpha(1.0f);
    }

    public void addToRenderer(Renderer renderer, UIRendererGroup uiRendererGroup)
    {
        super.addToRenderer(renderer);
        uiRendererGroup.addUI(healthbar);
        uiRendererGroup.addUI(healthbarLife);
    }

    public void removeFromRenderer(Renderer renderer, UIRendererGroup uiRendererGroup)
    {
        super.removeFromRenderer(renderer);
        uiRendererGroup.removeUI(healthbar);
        uiRendererGroup.removeUI(healthbarLife);
    }

    // Returns the same angle in the range of 0 to 360 degrees
    private float capAngle(float angle)
    {
        float cappedAngle = angle;

        // If the angle is from -360 to 0 degrees, add 360 degrees
        if(cappedAngle < 0.0f && cappedAngle >= -360.0f)
        {
            cappedAngle += 360.0f;
        }
        else
        {
            // How many turns has the angle exceeded 360 degrees by
            cappedAngle -= 360.0f * Math.abs((int)angle / 360);
        }

        return cappedAngle;
    }

    public void setXZ(float x, float z)
    {
        position.x = x;
        position.z = z;
    }

/*    private boolean removeFromRenderer = false;
    private float deathTimer = 60.0f;
    public boolean isDeathCycleOver()
    {
        return removeFromRenderer;
    }*/

    public void update(float deltaTime)
    {
/*        if(life <= 0.0f)
        {
            setColour(new Colour(1.0f, 0.0f, 0.0f));
            deathTimer -= 1.0f * deltaTime;
            if(deathTimer <= 0.0f)
            {
                removeFromRenderer = true;
                deathTimer = 0.0f;
            }
            setAlpha(deathTimer / 60.0f);
        }*/

        // Get path to player DEBUG
        if(SystemClock.uptimeMillis() - time > PATH_UPDATE_WAIT_MILLIS) {
            time = SystemClock.uptimeMillis();
            path = map.findShortestPath((int)(getPosition().x * 2f), (int)((getPosition().z * 2f) + 1.0f),
                    (int)(player.getPosition().x * 2f), (int)((player.getPosition().z * 2f) + 1.0f));
            hasPath = true;
        }

        Geometry.Vector velocity = new Geometry.Vector(0.0f, 0.0f, 0.0f);
        if(path != null)
        {
            Geometry.Point tilePos;
            if(path.size() - 2 < 0)
            {
                // We are at the players cell
                tilePos = map.getModelPosition(path.getLast()).translate(map.TILE_POSITION_OFFSET);
                lastValid = path.getLast();
            }
            else
            {
                tilePos = map.getModelPosition(path.get(path.size()-2)).translate(map.TILE_POSITION_OFFSET);
                lastValid = path.get(path.size()-2);
            }

        //    System.out.println("Tile Pos: " + tilePos);

        //    System.out.println("Zombie Pos: " + getPosition());

            Geometry.Vector difference = new Geometry.Vector(tilePos.x - position.x,
                    0.0f, tilePos.z - position.z);
       //     System.out.println("Difference: " + difference);

            // Need to check if dividing my 0
            Geometry.Vector direction;
            if(difference.length() == 0)
            {
                direction = new Geometry.Vector(0.0f, 0.0f, 0.0f);
            }
            else
            {
                direction = difference.scale(1.0f / difference.length());
            }

         //   System.out.println("Direction: " + direction);

            velocity = direction.scale(movementSpeed);
       //     System.out.println("Velocity: " + velocity);


         //   Geometry.Vector v = new Geometry.Vector(0.0f, 0.0f, 0.0f);
            translate(velocity);
        }
        else
        {
            // Go to last valid cell
            if(lastValid != null)
            {
                final Geometry.Vector TILE_POS_OFFSET = new Geometry.Vector(0.25f, 0.0f, -0.25f);
                Geometry.Point tilePos = map.getModelPosition(lastValid).translate(TILE_POS_OFFSET);
                Geometry.Vector difference = new Geometry.Vector(tilePos.x - position.x,
                        0.0f, tilePos.z - position.z);

                // Need to check if dividing my 0
                Geometry.Vector direction;
                if(difference.length() == 0)
                {
                    direction = new Geometry.Vector(0.0f, 0.0f, 0.0f);
                }
                else
                {
                    direction = difference.scale(1.0f / difference.length());
                }

                velocity = direction.scale(movementSpeed);
                translate(velocity);
            }
        }

        // This part calculates where the zombie should be facing and smoothly rotates it
        // Work out the facing angle from the velocity
        final float ANGLE_RADS = (float)Math.atan2(-velocity.z, velocity.x);
        if(ANGLE_RADS != 0.0)
        {
            final float TARGET_ANGLE_DEGREES = capAngle(((ANGLE_RADS / (float)Math.PI) * 180.0f) + 90.0f);
            facingAngle = capAngle(facingAngle);

            if(Math.abs(facingAngle - Math.abs(TARGET_ANGLE_DEGREES)) <= 5.0f)
            {
                facingAngle = TARGET_ANGLE_DEGREES;
            }
            else
            {
                // At this point the facing angle is in range 0-360 degrees and we need to move it to
                // the target angle in the direction closest
                final float MAX_ANGLE = Math.max(facingAngle, TARGET_ANGLE_DEGREES);
                final float MIN_ANGLE = Math.min(facingAngle, TARGET_ANGLE_DEGREES);
                final float ANGLE_TO_MOVE = MAX_ANGLE - MIN_ANGLE;

                boolean clockwiseMotion = false;
                if(MAX_ANGLE == TARGET_ANGLE_DEGREES)
                {
                    clockwiseMotion = true;
                }

                if(ANGLE_TO_MOVE > 180)
                {
                    clockwiseMotion = !clockwiseMotion;
                }

                if(clockwiseMotion)
                {
                    facingAngle += 5.0f;
                }
                else
                {
                    facingAngle -= 5.0f;
                }
            }
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
        head.rotate(facingAngle, 0.0f, 1.0f, 0.0f);

        // Rotate the arms and legs to move
        if(velocity.length() > 0.0f)
        {
            leg_left.rotateAroundPos(leftLegRotationAxis, limbAngle, 1.0f, 0.0f, 0.0f);
        //    leg_right.rotateAroundPos(leftLegRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);
            leg_right_lower.rotateAroundPos(leftLegRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);
            //leg_right_upper.rotateAroundPos(leftLegRotationAxis, -limbAngle, 1.0f, 0.0f, 0.0f);
            arm_left.rotateAroundPos(leftArmRotationAxis, ARM_ANGLE, 1.0f, 0.0f, 0.0f);
            arm_right.rotateAroundPos(rightArmRotationAxis, ARM_ANGLE, 1.0f, 0.0f, 0.0f);
        }
        else
        {
            limbAngle = 0.0f;
        }
    }
}