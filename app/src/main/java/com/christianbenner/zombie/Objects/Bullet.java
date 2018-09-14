package com.christianbenner.zombie.Objects;

import android.content.Context;

import com.christianbenner.crispinandroid.data.objects.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.zombie.R;

public class Bullet extends RendererModel {
    private float x, y, velocity, life;
    private Geometry.Vector direction;

    public Bullet(Context context, float x, float y, Geometry.Vector direction, float velocity,
                  float life)
    {
        super(context, R.raw.tile, TextureHelper.loadTexture(context, R.drawable.bullet));
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.velocity = velocity;
        this.life = life;
    }

    public RendererModel getModel()
    {
        return mo
    }

    public void update(float deltaTime)
    {
        this.x += direction.scale(velocity).x;
        this.y += direction.scale(velocity).y;

        // Change to seconds
        this.life -= 1.0f * deltaTime;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public float getVelocity()
    {
        return velocity;
    }

    public Geometry.Vector getDirection() {
        return direction;
    }
}
