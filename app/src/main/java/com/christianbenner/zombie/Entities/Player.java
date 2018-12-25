package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.util.RendererGroup;
import com.christianbenner.crispinandroid.util.Audio;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Hitbox2D;
import com.christianbenner.zombie.Map.Map;
import com.christianbenner.zombie.R;

import java.util.ArrayList;
import java.util.Random;

public class Player extends Humanoid
{
    private ArrayList<Bullet> bulletListReference;
    private RendererGroup bulletModelsReference;
    private Weapon.WeaponType currentWeapon;

    private Audio audio;

    private Random randomNumGenerator;
    private Hitbox2D hitbox;

    public Player(Context context,
                  Texture texture,
                  float movementSpeed,
                  ArrayList<Bullet> bullets,
                  RendererGroup bulletModels, Map map)
    {
        super(context, texture, movementSpeed, map);
        this.bulletListReference = bullets;
        this.bulletModelsReference = bulletModels;
        this.currentWeapon = Weapon.WeaponType.HANDS;
        this.audio = Audio.getInstance();

        this.randomNumGenerator = new Random();
        this.hitbox = new Hitbox2D(head, -0.5f, 0.5f, 1.0f, 1.0f);
    }

    public Hitbox2D getHitbox() {
        return hitbox;
    }

    int bulletWaitCount = 0;
    public boolean fireAction(Geometry.Vector unitVectorDirection)
    {
        boolean muzzleFlare = false;

        bulletWaitCount++;
        // Todo: On gunshot spawn a light for a couple ms

        Geometry.Point bulletSpawnPos = getPosition().translate(new Geometry.Vector(-0.1f, 0.0f, 0.1f));

        Bullet[] bulletsToAdd = null;

        // Do different things for different weapons that may be equipped
        switch (currentWeapon)
        {
            case HANDS:
                if(bulletWaitCount > 30) {
                    audio.playSound(R.raw.temp_punch, 1);
                    bulletWaitCount = 0;

                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.08f, 7.0f, 25.0f);
                    bulletsToAdd[0].setAlpha(0.0f);
                }
                break;
            case PISTOL:
                if(bulletWaitCount > 20) {
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.04f, 150.0f, 25.0f);

                    audio.playSound(R.raw.temp_pistol, 1);
           //         audio.playSound(R.raw.ting, 2);
                    bulletWaitCount = 0;
                    muzzleFlare = true;
                }
                break;
            case SNIPER:
                if(bulletWaitCount > 70) {
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.7f, 150.0f, 100.0f);

                    audio.playSound(R.raw.temp_sniper, 1);
             //       audio.playSound(R.raw.ting, 2);
                    bulletWaitCount = 0;
                    muzzleFlare = true;
                }
                break;
            case SHOTGUN:
                if(bulletWaitCount > 60) {
                    bulletsToAdd = new Bullet[5];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.05f, 40.0f, 50.0f);
                    bulletsToAdd[1] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.04f, 40.0f, 50.0f);
                    bulletsToAdd[2] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.03f, 40.0f, 50.0f);
                    bulletsToAdd[3] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.02f, 40.0f, 50.0f);
                    bulletsToAdd[4] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.01f, 40.0f, 50.0f);

                    audio.playSound(R.raw.temp_shotgun, 1);
          //          audio.playSound(R.raw.ting, 2);
                    bulletWaitCount = 0;
                    muzzleFlare = true;
                }

                break;
            case MACHINE_GUN:
                if(bulletWaitCount > 8) {
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.4f, 150.0f, 20.0f);

                    audio.playSound(R.raw.temp_assault_rifle, 1);
                  /*  final int FILE_TO_PLAY = randomNumGenerator.nextInt(4);
                    switch (FILE_TO_PLAY)
                    {
                        case 0:
                            audio.playSound(R.raw.ar1, 1);
                            break;
                        case 1:
                            audio.playSound(R.raw.ar2, 1);
                            break;
                        case 2:
                            audio.playSound(R.raw.ar3, 1);
                            break;
                        case 3:
                            audio.playSound(R.raw.ar4, 1);
                            break;
                    }*/

                  //  audio.playSound(R.raw.temp_assault_rifle, 1);
            //        audio.playSound(R.raw.ting, 2);
                    bulletWaitCount = 0;
                    muzzleFlare = true;
                }

                break;
            case ROCKET_LAUNCHER:
                if(bulletWaitCount > 90) {
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.08f, 85.0f, 100.0f);
                    audio.playSound(R.raw.temp_rpg, 1);

                    bulletWaitCount = 0;
                    muzzleFlare = true;
                }

                break;
            case SUB_MACHINE_GUN:
                if(bulletWaitCount > 5) {
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.3f, 150.0f, 15.0f);
                    audio.playSound(R.raw.temp_smg, 1);
               //     audio.playSound(R.raw.ting, 2);
                    bulletWaitCount = 0;
                    muzzleFlare = true;
                }

                break;
            case GRENADE_LAUNCHER:
                if(bulletWaitCount > 30) {
                    bulletsToAdd = new Bullet[1];
                    bulletsToAdd[0] = new Bullet(context, bulletSpawnPos,
                            unitVectorDirection, 0.02f, 150.0f, 100.0f);
                    audio.playSound(R.raw.temp_grenade_launcher, 1);

                    bulletWaitCount = 0;
                }

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
                bulletListReference.add(bulletsToAdd[i]);
                bulletModelsReference.addModel(bulletsToAdd[i].getModel());
            }
        }

        return muzzleFlare;
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

    public void switchWeapon(Weapon.WeaponType type)
    {
        currentWeapon = type;
    }

    public Weapon.WeaponType getCurrentWeapon() {
        return currentWeapon;
    }
}
