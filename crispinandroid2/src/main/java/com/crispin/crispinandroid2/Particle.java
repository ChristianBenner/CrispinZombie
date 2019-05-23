package com.crispin.crispinandroid2;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.util.Geometry;

// This should be a data only class that contains values such as direction, speed
public class Particle {
    public float maxLifeTime;
    public float currentLifeTime;
    public Geometry.Point position;
    public float size; // size of the side of the square
    public Geometry.Vector velocity;
    public Colour colour;
    public float angle;
    // Particle should have a circle texture (square for now though)

    // Square

    public Particle(Geometry.Point startPosition, Geometry.Vector startVelocity, float size, float life, Colour colour, float angle)
    {
        this.position = startPosition;
        this.velocity = startVelocity;
        this.size = size;
        this.maxLifeTime = life;
        this.currentLifeTime = life;
        this.colour = colour;
        this.angle = angle;
    }
}
