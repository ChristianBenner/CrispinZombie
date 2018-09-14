package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.render.data.RendererGroupType;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.RendererGroup;
import com.christianbenner.crispinandroid.util.Audio;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.zombie.R;

import java.util.ArrayList;

/**
 * Created by chris on 10/01/2018.
 */

public class Human {
    private Context context;
    private Audio audio;

    protected RendererModel leg_left;
    protected RendererModel leg_right;
    protected RendererModel arm_right;
    protected RendererModel arm_left;
    protected RendererModel body;
    protected RendererModel head;

    private RendererGroup bulletsGroup;

    private ArrayList<Bullet> bullets;

    private Texture texture_human;

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

    private Weapon.WeaponType currentWeapon;

    public Human(Context context, Texture texture, float movementSpeed)
    {
        this.context = context;
        this.audio = Audio.getInstance();
        this.currentWeapon = Weapon.WeaponType.HANDS;

        texture_human = texture;
        this.position = new Geometry.Point(0.0f, 0.0f, 0.0f);
        this.velocity = new Geometry.Vector(0.0f, 0.0f, 0.0f);
        facingAngle = 0.0f;
        desiredAngle = 0.0f;
        createParts();
        this.movementSpeed = movementSpeed;

        bullets = new ArrayList<>();
        bulletsGroup = new RendererGroup(RendererGroupType.SAME_BIND_SAME_TEX);
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
        renderer.addGroup(bulletsGroup);
    }

    int bulletWaitCount = 0;
    public void fireAction(Geometry.Vector unitVectorDirection)
    {
        if(bulletWaitCount > 30)
        {
            bulletWaitCount = 0;

            // Todo: On gunshot spawn a light for a couple ms

            Geometry.Point bulletSpawnPos = getPosition().translate(new Geometry.Vector(0.0f, 0.5f, 0.0f));

            Bullet[] bulletsToAdd = null;

            // Do different things for different weapons that may be equipped
            switch (currentWeapon)
            {
                case HANDS:
                    audio.playSound(R.raw.temp_punch, 1);
                    break;
                case PISTOL:
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.4f, 150.0f);

                    audio.playSound(R.raw.temp_pistol, 1);

                    break;
                case SNIPER:
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.7f, 150.0f);

                    audio.playSound(R.raw.temp_sniper, 1);
                    break;
                case SHOTGUN:
                    bulletsToAdd = new Bullet[5];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.05f, 40.0f);
                    bulletsToAdd[1] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.04f, 40.0f);
                    bulletsToAdd[2] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.03f, 40.0f);
                    bulletsToAdd[3] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.02f, 40.0f);
                    bulletsToAdd[4] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.01f, 40.0f);

                    audio.playSound(R.raw.temp_shotgun, 1);
                    break;
                case MACHINE_GUN:
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.4f, 150.0f);
                    audio.playSound(R.raw.temp_assault_rifle, 1);
                    break;
                case ROCKET_LAUNCHER:
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.08f, 85.0f);
                    audio.playSound(R.raw.temp_rpg, 1);
                    break;
                case SUB_MACHINE_GUN:
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.3f, 150.0f);
                    audio.playSound(R.raw.temp_smg, 1);
                    break;
                case GRENADE_LAUNCHER:
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.02f, 150.0f);
                    audio.playSound(R.raw.temp_grenade_launcher, 1);
                    break;
                default:
                    System.out.println("Not implemented weapon mechanics yet.");
                    break;
            }

            if(bulletsToAdd != null)
            {
                for(int i = 0; i < bulletsToAdd.length; i++)
                {
                    // Spawn bullet
                    bullets.add(bulletsToAdd[i]);
                    bulletsGroup.addModel(bulletsToAdd[i].getModel());
                }
            }
        }
        bulletWaitCount++;
    }

    // Debug function iterates through the weapon types list
    public void switchWeaponTemp()
    {
        if(currentWeapon == Weapon.WeaponType.HANDS)
        {
            currentWeapon = Weapon.WeaponType.values()[0];
        }
        else
        {
            currentWeapon = Weapon.WeaponType.values()[currentWeapon.ordinal() + 1];
        }
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

        // Update bullets
        for (int n = 0; n < bullets.size(); n++) {
            bullets.get(n).update(deltaTime);

            // If the bullets have run out of life, remove them
            if (bullets.get(n).isAlive() == false) {
                bulletsGroup.removeModel(bullets.get(n).getModel());
                bullets.remove(n--);
            }
        }
    }
}
