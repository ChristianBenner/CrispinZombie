package com.christianbenner.zombie.Entities;

import android.content.Context;

import com.christianbenner.crispinandroid.render.model.Model;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.zombie.R;

public class Bullet {
    private RendererModel model;
    private float velocity, life;
    private Geometry.Point position;
    private Geometry.Vector direction;

    public Bullet(Context context, Geometry.Point startPos, Geometry.Vector direction,
                  float velocity, float life)
    {
        this.position = startPos;
        this.direction = direction;
        this.velocity = velocity;
        this.life = life;

        int objectResourceId = R.raw.tile;
        int textureId = R.drawable.bullet;

        // Setup the model
        model = new RendererModel(context, objectResourceId,
                TextureHelper.loadTexture(context, textureId, true),
                Model.AllowedData.VERTEX_TEXEL_NORMAL);
        model.newIdentity();
        model.setPosition(position);
        model.setScale(0.1f);
    }

    public void update(float deltaTime)
    {
        // Move the bullet by the velocity
        position.x += direction.scale(velocity).x;
        position.z -= direction.scale(velocity).y;
        //position = position.translate(direction.scale(velocity));

        // Change to seconds
        this.life -= 1.0f * deltaTime;

        model.newIdentity();
        model.setPosition(new Geometry.Point(position.x, 1.0f, position.z));
        model.setScale(0.1f);
    }

    public RendererModel getModel()
    {
        return this.model;
    }

    public float getVelocity()
    {
        return velocity;
    }

    public Geometry.Vector getDirection() {
        return direction;
    }

    public boolean isAlive()
    {
        return life > 0.0f;
    }
}
