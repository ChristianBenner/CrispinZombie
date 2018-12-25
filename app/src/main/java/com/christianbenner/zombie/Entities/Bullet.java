package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Colour;
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

    // The damage of the bullet
    private float damage;

    // The current position of the bullet
    private Geometry.Point position;

    // The current direction of the bullet
    private Geometry.Vector direction;

    // The renderer model
    private RendererModel model;

    // The width of the bullet
    public final float WIDTH_MULTIPLIER = 0.1f;

    // The height of the bullet
    public final float DEPTH_MULTIPLIER = 0.2f;

    // The type of bullet
    public enum BulletType
    {
        DEFAULT,
        FISTS,
        RPG
    }

    private BulletType bulletType = BulletType.DEFAULT;

    // Create a bullet that needs to be updated to be projected with a specific speed and
    // direction. The life value is an amount that decays over time. Actual time can't be used
    // here because if the game runs slower on some devices the bullets will de-spawn at
    // different rates (without actual time we can use delta time which accounts for the change
    // in time).
    public Bullet(Context context, Geometry.Point startPos, Geometry.Vector direction,
                  float speed, float life, float damage)
    {
        // Setup class variables
        this.position = startPos;
        this.direction = direction;
        this.speed = speed;
        this.life = life;
        this.damage = damage;

        // Setup the model
        model = new RendererModel(context, R.raw.tile,
                TextureHelper.loadTexture(context, R.drawable.bullet, true),
                Model.AllowedData.VERTEX_TEXEL_NORMAL);
        model.newIdentity();
        model.setPosition(position);
        model.setScale(WIDTH_MULTIPLIER);
    }

    public void setBulletType(BulletType type)
    {
        this.bulletType = type;
    }

    public void setAlpha(float alpha)
    {
        this.model.setAlpha(alpha);
    }

    public BulletType getType()
    {
        return this.bulletType;
    }

    public float getDamage()
    {
        return this.damage;
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
        model.setScale(WIDTH_MULTIPLIER);
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

    // Kill the bullet
    public void endLife()
    {
        life = 0.0f;
    }

    // Return the current position of the bullet
    public Geometry.Point getPosition() {
        return position;
    }

    // Circular collision test against humanoid
    public boolean collidesWith(Humanoid humanoid)
    {
        // Spherical collision between humanoid and bullet
        final float BULLET_RADIUS = DEPTH_MULTIPLIER / 2.0f;
        final float HUMANOID_RADIUS = 0.5f / 2.0f;

        // Bullet Center Position
        final Geometry.Point BULLET_CENTER = new Geometry.Point(
                position.x + BULLET_RADIUS,
                position.y + BULLET_RADIUS,
                position.z + BULLET_RADIUS
        );

        // Player Center Position
        final Geometry.Point HUMANOID_CENTER = new Geometry.Point(
                humanoid.position.x + HUMANOID_RADIUS,
                humanoid.position.y + HUMANOID_RADIUS,
                humanoid.position.z + HUMANOID_RADIUS
        );

        // Difference
        final Geometry.Point DIFFERENCE = new Geometry.Point(
                Math.abs(BULLET_CENTER.x - HUMANOID_CENTER.x),
                Math.abs(BULLET_CENTER.y - HUMANOID_CENTER.y),
                Math.abs(BULLET_CENTER.z - HUMANOID_CENTER.z)
        );

        // If the difference is less than the radii sum
        if(DIFFERENCE.x < HUMANOID_RADIUS + BULLET_RADIUS &&
                DIFFERENCE.y < HUMANOID_RADIUS + BULLET_RADIUS &&
                DIFFERENCE.z < HUMANOID_RADIUS + BULLET_RADIUS)
        {
            return true;
        }

        return false;
    }
}
