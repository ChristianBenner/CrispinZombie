package com.christianbenner.zombie.AStarTesting;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.data.objects.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Renderer;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.zombie.R;

/**
 * Created by Christian Benner on 19/06/2018.
 */

public class AStarPlayer {
        private final float VELOCITY = 0.04f;
        private float tileSize = 0.5f;

        public AStarPlayer(float tileSize, float x, float z, Renderer renderer, Context context)
        {
            this.tileSize = tileSize;

            model = new RendererModel(context, R.raw.box,
                    TextureHelper.loadTexture(context, R.drawable.box));
            model.setColour(new Colour(1.0f, 0.0f, 0.0f, 0.5f));

            setPosition(x, z);
            renderer.addModel(model);
        }

        // Encapsulate the position so that the point is top left of the model
        public void setPosition(float x, float z)
        {
            this.x = x + (tileSize / 2.0f);
            this.z = z - (tileSize / 2.0f);

            // Position the model
            model.newIdentity();                                                            // Reset the position matrix

            Geometry.Point positionOffset = new Geometry.Point(tileSize / 2.0f, tileSize / 2.0f, -tileSize / 2.0f);
            model.setPosition(new Geometry.Point((x*tileSize) + positionOffset.x, positionOffset.y,
                    (z * tileSize) + positionOffset.z));
            model.setScale(1.0f);                                                // Set scale
        }

        public void update()
        {
            x += directionX * 0.04f;
            z += directionY * 0.04f;

            // Position the model
            model.newIdentity();                                                            // Reset the position matrix
            model.setPosition(new Geometry.Point(this.x, this.tileSize / 2.0f, this.z));    // Set position
            model.setScale(tileSize / 2.0f);                                                // Set scale
        }

        public float getX() { return x; }

        public float getZ()
        {
            return z;
        }

        // The current cell that the player is on
        public int getCellX()
        {
            return Math.round(x - tileSize);
        }

        public int getCellZ()
        {
            return Math.round(z);
        }

        private float x, z;
        private RendererModel model;

        public int directionX = 0, directionY = 0;
}
