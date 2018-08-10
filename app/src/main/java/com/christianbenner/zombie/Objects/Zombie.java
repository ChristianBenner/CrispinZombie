package com.christianbenner.zombie.Objects;

import android.content.Context;
import android.os.SystemClock;

import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.zombie.Cell;
import com.christianbenner.zombie.Map;

import java.util.LinkedList;

/**
 * Created by Christian Benner on 19/05/2018.
 */

public class Zombie extends Human {
    private Human player;
    private final float ARM_ANGLE = -90.0f;
    private Map map;

    // Temp
    private long time = 0;
    private final long PATH_UPDATE_FREQUENCY = 5;
    private final long PATH_UPDATE_WAIT_MILLIS = 1000 / PATH_UPDATE_FREQUENCY;
    public  boolean hasPath = false;

    public Zombie(Context context, Texture texture, float movementSpeed, Human human, Geometry.Point position, Map map)
    {
        super(context, texture, movementSpeed);
        setPosition(position);
        this.player = human;
        this.map = map;
    }

    private LinkedList<Cell> path = null;
    Cell lastValid = null;
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
            arm_left.rotateAroundPos(leftArmRotationAxis, ARM_ANGLE, 1.0f, 0.0f, 0.0f);
            arm_right.rotateAroundPos(rightArmRotationAxis, ARM_ANGLE, 1.0f, 0.0f, 0.0f);
        }
        else
        {
            angle = 0.0f;
        }


        count++;
        if(count >= 60)
        {
            count = 0;
            // Path find to the player
            // Find the current tile that we are on
            int currentTileX = (int)(position.x / 2.0f);
            int currentTileZ = (int)((position.z / 2.0f) + 1.0f);

        //    ArrayList<Tile> path = map.newPathFind(currentTileX, currentTileZ, (int)(playerPosition.x / 2.0f), (int)((playerPosition.z / 2.0f) + 1.0f));

            //ArrayList<Tile> path = map.pathfind(currentTileX, currentTileZ, (int)(playerPosition.x / 2.0f), (int)((playerPosition.z / 2.0f) + 1.0f));
            //map.nextMove(currentTileX, currentTileZ, (int)playerPosition.x, (int)(playerPosition.z + 1.0f));

  //          System.out.println("Current Tile: " + currentTileX + ", " + currentTileZ);

    //        System.out.println("Path {");
      //      for(Tile tile : path)
      //      {
      //          System.out.println("\tTile x[" + tile.getPosition().x + ", y[" + tile.getPosition().y + "]");
     //       }
    //        System.out.println("}");
        }
    }

    int count = 0;
}