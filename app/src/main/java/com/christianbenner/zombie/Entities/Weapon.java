package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.model.Model;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Hitbox2D;
import com.christianbenner.zombie.Map.Map;
import com.christianbenner.zombie.R;

/**
 * Created by Christian Benner on 19/05/2018.
 */

public class Weapon {
    private RendererModel model;
    private Texture uiImage;
    private int maxAmmo;
    private int currentAmmo;
    private Hitbox2D hitbox;

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
        ROCKET_LAUNCHER,
        HANDS
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
                textureId = R.drawable.pistol;
                break;
            case SNIPER:
                textureId = R.drawable.sniper_hotbar;
                break;
            case SHOTGUN:

                break;
            case MACHINE_GUN:
                textureId = R.drawable.assualt_rifle_hotbar;
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
        model.setScale(0.3f);

        this.hitbox = new Hitbox2D(model, Map.TILE_SIZE, Map.TILE_SIZE);
    }

    public RendererModel getModel()
    {
        return this.model;
    }

    public WeaponType getType()
    {
        return this.type;
    }

    public Hitbox2D getHitbox() {
        return hitbox;
    }

    @Override
    public String toString()
    {
        return "Weapon[" + type + "], " + this.model.getPosition();
    }
}
