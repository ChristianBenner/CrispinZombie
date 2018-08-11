package com.christianbenner.zombie.Objects;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.objects.Model;
import com.christianbenner.crispinandroid.data.objects.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.zombie.R;

/**
 * Created by Christian Benner on 19/05/2018.
 */

public class Weapon {
    private RendererModel model;
    private Texture uiImage;
    private int maxAmmo;
    private int currentAmmo;

    // If state is drop, rotate on the floor
    private enum state {
        DROP,
        EQUIPPED
    }

    public enum WeaponType
    {
        PISTOL,
        SNIPER,
        SHOTGUN,
        MACHINE_GUN,
        SUB_MACHINE_GUN,
        GRENADE_LAUNCHER,
        ROCKET_LAUNCHER
    }

    private WeaponType type;

    public Weapon(Context context, WeaponType type, Geometry.Point startPos)
    {
        int objectResourceId = R.raw.tile;
        int textureId = R.drawable.unknown_texture;

        this.type = type;

        // Point towards the correct models
        switch (type)
        {
            case PISTOL:

                break;
            case SNIPER:

                break;
            case SHOTGUN:

                break;
            case MACHINE_GUN:

                break;
            case SUB_MACHINE_GUN:

                break;
            case GRENADE_LAUNCHER:

                break;
            case ROCKET_LAUNCHER:

                break;
        }

        // Setup the model
        model = new RendererModel(context, objectResourceId,
            TextureHelper.loadTexture(context, textureId, true),
                Model.AllowedData.VERTEX_TEXEL_NORMAL);
        model.newIdentity();
        model.setPosition(startPos);
    }

    @Override
    public String toString()
    {
        return "Weapon[" + type + "], " + this.model.getPosition();
    }
}
