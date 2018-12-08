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
import com.christianbenner.crispinandroid.ui.UIDimension;
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
    private Map map;

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
        super(context, texture, movementSpeed);
        setPosition(position);
        this.player = humanoid;
        this.map = map;

        this.healthbar = new Image(new UIDimension(0, 0, HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT),
                new Colour(1.0f, 0.0f, 0.0f, 0.0f));
        this.healthbarLife = new Image(new UIDimension(0, 0, HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT),
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

        healthbarLife.setDimensions(new UIDimension(
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

    public void update(float deltaTime)
    {
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
            Geometry.Vector tilePosOffset = new Geometry.Vector(0.25f, 0.0f, -0.25f);

            Geometry.Point tilePos;
            if(path.size() - 2 < 0)
            {
                // We are at the players cell
                tilePos = map.getModelPosition(path.getLast()).translate(tilePosOffset);
                lastValid = path.getLast();
            }
            else
            {
                tilePos = map.getModelPosition(path.get(path.size()-2)).translate(tilePosOffset);
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
                Geometry.Vector tilePosOffset = new Geometry.Vector(0.25f, 0.0f, -0.25f);
                Geometry.Point tilePos = map.getModelPosition(lastValid).translate(tilePosOffset);
                Geometry.Vector difference = new Geometry.Vector(tilePos.x - position.x,
                        0.0f, tilePos.z - position.z);
          //      System.out.println("Difference: " + difference);

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

       //         System.out.println("Direction: " + direction);

                velocity = direction.scale(movementSpeed);
      //          System.out.println("Velocity: " + velocity);
                translate(velocity);
            }

        }

       /* Geometry.Vector velocity = new Geometry.Vector(0.0f, 0.0f, 0.0f);
        if(path != null)
        {
            Geometry.Vector tileOffset = new Geometry.Vector(-0.5f, 0.0f, 1.0f);
            Geometry.Point tilePos = map.getModelPosition(path.getLast()).translate(tileOffset);

            Geometry.Vector difference = new Geometry.Vector(tilePos.x - position.x,
                    0.0f, tilePos.z - position.z);

            System.out.println("Difference: " + difference);

            // If the difference is a certain amount, stop moving towards the player and play attack animation


            Geometry.Vector direction = difference.scale(1.0f / difference.length());
            velocity = direction.scale(movementSpeed);

            Geometry.Vector velocity2 = new Geometry.Vector(-velocity.z, 0.0f, velocity.x);
            translate(velocity2);

            System.out.println("Velocity: " + velocity2);
        }*/


        // Move to the player
        // Calculate direction from player location and zombie location
        Geometry.Point playerPosition = player.getPosition();
        Geometry.Vector difference = new Geometry.Vector(playerPosition.x - position.x,
                0.0f, playerPosition.z - position.z);

        // If the difference is a certain amount, stop moving towards the player and play attack animation


        Geometry.Vector direction = difference.scale(1.0f / difference.length());
   //     Geometry.Vector velocity = direction.scale(movementSpeed);
       // translate(velocity);

        // Work out the facing angle from the velocity
        final float ANGLE_RADS = (float)Math.atan2(-velocity.z, velocity.x);
        if(ANGLE_RADS != 0.0)
        {
            // Convert angle to degrees - this is the target direction we should turn to at
            // a specific rate
            final float TARGET_ANGLE_DEGREES = ((ANGLE_RADS / (float)Math.PI) * 180.0f) + 90.0f;



         //   System.out.println("FACING ANGLE: " + facingAngle + ", TARGET ANGLE: " + TARGET_ANGLE_DEGREES);
            final int NUM_REPEATS = (int)facingAngle / 360;
         //   System.out.println("NUM REPEATS: " + NUM_REPEATS);

            facingAngle -= facingAngle * Math.abs(NUM_REPEATS);

            if(facingAngle < 0.0f)
            {
                facingAngle = 360.0f + facingAngle;
            }

            final boolean INCREASE_ANGLE = (TARGET_ANGLE_DEGREES - facingAngle) < (TARGET_ANGLE_DEGREES / 2.0f);

            final float ANGLE_OPTION_ONE = Math.abs(facingAngle - TARGET_ANGLE_DEGREES);
            final float ANGLE_OPTION_TWO = Math.abs((facingAngle + 360.0f) - TARGET_ANGLE_DEGREES);

            final float ao1 = facingAngle - TARGET_ANGLE_DEGREES;
            final float ao2 = 360 - Math.abs(ao1);
            boolean increase = false;
            if(Math.abs(ao1) < ao2)
            {
                if(ao1 < 0.0f)
                {
                    increase = false;
                }
                else
                {
                    increase = true;
                }
            }
            else
            {
                increase = false;
            }

            if(ANGLE_OPTION_ONE < ANGLE_OPTION_TWO)
            {

            }

          //  final boolean SHOULD_INCREASE_ANGLE = >
          //          Math.abs((facingAngle + 360.0f) - TARGET_ANGLE_DEGREES);




            if(Math.abs(TARGET_ANGLE_DEGREES - facingAngle) > 5.5f)
            {
                if(increase)
                {
                    facingAngle += 5.0f;
                }
                else
                {
                    facingAngle -= 5.0f;
                }
            }
            else
            {
                facingAngle = TARGET_ANGLE_DEGREES;
            }
            facingAngle = TARGET_ANGLE_DEGREES;
           /* if(Math.abs(TARGET_ANGLE_DEGREES - facingAngle) < 2.0f)
            {
                if(TARGET_ANGLE_DEGREES > facingAngle)
                {
                    facingAngle += 1.0f;
                } else if (TARGET_ANGLE_DEGREES < facingAngle)
                {
                    facingAngle -= 1.0f;
                }
            }
            else
            {
                facingAngle = TARGET_ANGLE_DEGREES;
            }*/


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
            arm_left.rotateAroundPos(leftArmRotationAxis, ARM_ANGLE, 1.0f, 0.0f, 0.0f);
            arm_right.rotateAroundPos(rightArmRotationAxis, ARM_ANGLE, 1.0f, 0.0f, 0.0f);
        }
        else
        {
            limbAngle = 0.0f;
        }
    }
}