package com.christianbenner.crispinandroid.util;

import com.christianbenner.crispinandroid.render.model.RendererModel;

// Use in collision checking mechanisms
public class Hitbox2D {
    private RendererModel model = null;
    private float x;
    private float y;
    private float w;
    private float h;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;

    // Create a 2D hitbox
    public Hitbox2D(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // Uses the renderer models position as XY and allows you to define width and height
    public Hitbox2D(RendererModel model, float w, float h) {
        this.model = model;
        this.w = w;
        this.h = h;
    }

    // Uses the renderer models position as XY and allows you to define width and height,
    // this version will allow you to define an offset from the models XY position
    public Hitbox2D(RendererModel model, float offsetX, float offsetY, float w, float h) {
        this.model = model;
        this.w = w;
        this.h = h;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    // Check if the hitbox collides with another
    public boolean checkCollision(Hitbox2D otherHitbox, boolean switchYAndZ) {
        // Determine the dimensions of both hitboxes
        Dimension2D localDimension;
        Dimension2D otherDimension;

        if(switchYAndZ)
        {
            localDimension = model != null ? new Dimension2D(model.getPosition().x, model.getPosition().z, w, h) :
                    new Dimension2D(x, y, w, h);
            otherDimension = otherHitbox.model != null ? new Dimension2D(otherHitbox.model.getPosition().x, otherHitbox.model.getPosition().z, otherHitbox.w, otherHitbox.h) :
                    new Dimension2D(otherHitbox.x, otherHitbox.y, otherHitbox.w, otherHitbox.h);
        }
        else
        {
            localDimension = model != null ? new Dimension2D(model.getPosition(), w, h) :
                    new Dimension2D(x, y, w, h);
            otherDimension = otherHitbox.model != null ? new Dimension2D(otherHitbox.model.getPosition(), otherHitbox.w, otherHitbox.h) :
                    new Dimension2D(otherHitbox.x, otherHitbox.y, otherHitbox.w, otherHitbox.h);
        }

        localDimension.x += offsetX;
        localDimension.w += offsetX;
        localDimension.y += offsetY;
        localDimension.h += offsetY;

        otherDimension.x += otherHitbox.offsetX;
        otherDimension.w += otherHitbox.offsetX;
        otherDimension.y += otherHitbox.offsetY;
        otherDimension.h += otherHitbox.offsetY;

        return ((localDimension.x > otherDimension.x && localDimension.x < otherDimension.x + otherDimension.w) ||
                (localDimension.x + localDimension.w > otherDimension.x && localDimension.x + localDimension.w < otherDimension.x + otherDimension.w)) &&
                ((localDimension.y > otherDimension.y && localDimension.y < otherDimension.y + otherDimension.h) ||
                        (localDimension.y + localDimension.h > otherDimension.y && localDimension.y + localDimension.h < otherDimension.y + otherDimension.h));
    }

    // Check if the hitbox collides with another
    public boolean checkCollision(Hitbox2D otherHitbox) {
        return checkCollision(otherHitbox, false);
    }
}