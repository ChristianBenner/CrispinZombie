package com.christianbenner.crispinandroid.util;

import android.opengl.Matrix;

import static java.lang.Math.sin;

/**
 * Created by Christian Benner on 11/08/2017.
 */

public class Geometry {
    // Code is immutable - whenever we make a change we return a new object,
    // it may be better to mutate floating point arrays with statis functions for top performance
    public static class Point {
        public float x, y, z;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point translate(Vector vector)
        {
            return new Point(
                    x + vector.x,
                    y + vector.y,
                    z + vector.z);
        }


        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }

        public Point invert()
        {
            return new Point(-x, -y, -z);
        }

        public float distance(Point target)
        {
            final float dX = target.x - x;
            final float dY = target.y - y;
            final float dZ = target.z - z;

            return (float)Math.sqrt((double)((dX * dX) + (dY * dY) + (dZ * dZ)));
        }

        @Override
        public String toString()
        {
            return "Point[x:" + x + ",y:" + y + ",z:" + z + "]";
        }
    }

    public static class Quaternion
    {
        private float[] rotationMatrix = new float[16];
        private Point rotation;
        public Quaternion(Point rotationAxis, Point rotationAngles)
        {
            this.rotation = new Point(rotationAxis.x * (float)sin(rotationAngles.x / 2.0f),
                    rotationAxis.y * (float)sin(rotationAngles.y / 2.0f),
                    rotationAxis.z * (float)sin(rotationAngles.z / 2.0f));
        }

 /*       // Returns a 16 float array
        float[] createRotation()
        {
            float[] m1 = {

            };

            float[] m2 = {

            };

            Matrix m;
            m = new Matrix();
            Matrix.
        }*/

        public float[] getRotationMatrix()
        {
            Matrix.setRotateEulerM(rotationMatrix, 0, rotation.x, rotation.y, rotation.z);
            return rotationMatrix;
        }

        @Override
        public String toString()
        {
            return "Quat[x:" + rotation.x + ",y:" + rotation.y + ",z:" + rotation.z + "]";
        }
    }

    public static class Cuboid {
        public final Point center;
        public float height;
        public float width;
        public float depth;

        public Cuboid(Point center, float width, float height, float depth)
        {
            this.center = center;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
    }

    public static class Circle {
        public final Point center;
        public final float radius;

        public Circle(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }

        public Circle scale(float scale) {
            return new Circle(center, radius * scale);
        }
    }

    public static class Cylinder {
        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height){
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }

    public static class Vector {
        public final float x, y, z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length(){
            return (float)Math.sqrt(
                    x * x
                    + y * y
                    + z*z);
        }

        // http://en.wikipedia.org/wiki/Cross_product
        public Vector crossProduct(Vector other) {
            return new Vector(
                    (y * other.z) - (z * other.y),
                    (z * other.x) - (x * other.z),
                    (x * other.y) - (y * other.x));
        }

        public float dotProduct(Vector other)
        {
            return (x * other.x) + (y * other.y) + (z * other.z);
        }

        public Vector scale(float f)
        {
            return new Vector(x * f, y * f, z * f);
        }

        @Override
        public String toString()
        {
            return "x: " + x + ", y: " + y + ", z: " + y;
        }
    }


    public static Vector vectorBetween(Point from, Point to) {
        return new Vector(
                to.x - from.x,
                to.y - from.y,
                to.z - from.z);
    }

    public static class Ray {
        public final Point point;
        public final Vector vector;

        public Ray(Point point, Vector vector) {
            this.point = point;
            this.vector = vector;
        }
    }

    public static class Sphere {
        public final Point center;
        public final float radius;

        public Sphere(Point center, float radius)
        {
            this.center = center;
            this.radius = radius;
        }
    }

    public static boolean intersects(Sphere sphere, Ray ray)
    {
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    public static float distanceBetween(Point point, Ray ray)
    {
        Vector p1ToPoint = vectorBetween(ray.point, point);
        Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);


        float areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length();
        float lengthOfBase = ray.vector.length();

        // area of triangle = base * height so height = triangle / base
        float distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase;
        return distanceFromPointToRay;
    }

    public static class Plane
    {
        public final Point point;
        public final Vector normal;

        public Plane(Point point, Vector normal)
        {
            this.point = point;
            this.normal = normal;
        }
    }

    public static Point intersectionPoint(Ray ray, Plane plane)
    {
        Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);

        float scaleFactor = rayToPlaneVector.dotProduct(plane.normal)
                / ray.vector.dotProduct(plane.normal);

        Point intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor));
        return intersectionPoint;
    }
}

