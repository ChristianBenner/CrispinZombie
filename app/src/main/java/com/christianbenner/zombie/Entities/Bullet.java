package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.render.model.Model;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.zombie.R;

public class Bullet {
    // The speed of the bullet
    private float speed;

    // The life of the bullet, with no unit of time
    private float life;

    // The current position of the bullet
    private Geometry.Point position;

    // The current direction of the bullet
    private Geometry.Vector direction;

    // The renderer model
    private RendererModel model;

    // Create a bullet that needs to be updated to be projected with a specific speed and
    // direction. The life value is an amount that decays over time. Actual time can't be used
    // here because if the game runs slower on some devices the bullets will de-spawn at
    // different rates (without actual time we can use delta time which accounts for the change
    // in time).
    public Bullet(Context context, Geometry.Point startPos, Geometry.Vector direction,
                  float speed, float life)
    {
        // Setup class variables
        this.position = startPos;
        this.direction = direction;
        this.speed = speed;
        this.life = life;

        // Setup the model
        model = new RendererModel(context, R.raw.tile,
                TextureHelper.loadTexture(context, R.drawable.bullet, true),
                Model.AllowedData.VERTEX_TEXEL_NORMAL);
        model.newIdentity();
        model.setPosition(position);
        model.setScale(0.1f);
    }

    public void update(float deltaTime)
    {
        // Move the bullet by the velocity
        position.x += direction.scale(speed).x;
        position.z -= direction.scale(speed).y;
        //position = position.translate(direction.scale(velocity));

        // Cannot change to seconds because if the game runs slower on some devices
        // the bullets will de-spawn at different rates.
        this.life -= 1.0f * deltaTime;

        model.newIdentity();
        model.setPosition(new Geometry.Point(position.x, 1.0f, position.z));
        model.setScale(0.1f);
    }

    // Return the renderer model
    public RendererModel getModel()
    {
        return this.model;
    }

    // Return the velocity vector of the bullet
    public float getVelocity()
    {
        return speed;
    }

    // Return the direction of the bullet
    public Geometry.Vector getDirection() {
        return direction;
    }

    // Return boolean if the bullet is still active and not ready to de-spawn (expired?)
    public boolean isAlive()
    {
        return life > 0.0f;
    }
}
