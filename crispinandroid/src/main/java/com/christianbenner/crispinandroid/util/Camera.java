package com.christianbenner.crispinandroid.util;

import android.graphics.PointF;
import android.opengl.Matrix;

import com.christianbenner.crispinandroid.ui.Interactive;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;

import java.util.ArrayList;

import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by Christian Benner on 13/01/2018.
 */

public class Camera implements Interactive {
    private ArrayList<TouchListener> touchListeners = new ArrayList<>();
    public void addTouchListener(TouchListener listener) { touchListeners.add(listener); }
    public void removeTouchListener(TouchListener listener) { touchListeners.remove(listener); }

    // Matrix's
    private float[] viewMatrix;
    private float[] viewProjectionMatrix;
 //   private final float[] modelViewProjectionMatrix = new float[16];
    private float[] projectionMatrix;
 //   private final float[] modelMatrix = new float[16];

    // Camera Look at
    private float eyeX;
    private float eyeY;
    private float eyeZ;
    private float centerX;
    private float centerY;
    private float centerZ;
    private float upX;
    private float upY;
    private float upZ;

    private Geometry.Point position;
    private float horizontalAngle;
    private float verticalAngle;
    private float fov;
    private float speed;
    private float mouseSpeed;
    private boolean touchFocus;

    public Camera()
    {
        this.position = new Geometry.Point(0.0f, 0.0f, 0.0f);
        this.direction = new Geometry.Vector(0.0f, 0.0f, -1.0f);
        this.right = new Geometry.Vector(1.0f, 0.0f, 0.0f);

        this.viewMatrix = new float[16];
        this.viewProjectionMatrix = new float[16];
        this.projectionMatrix = new float[16];

        this.eyeX = 0.0f;
        this.eyeY = 0.0f;
        this.eyeZ = 0.0f;
        this.centerX = 0.0f;
        this.centerY = 0.0f;
        this.centerZ = 0.0f;
        this.upX = 0.0f;
        this.upY = 0.0f;
        this.upZ = 0.0f;
        this.horizontalAngle = (float)Math.PI;
        this.verticalAngle = 0.0f;
        this.fov = 45.0f;
        this.speed = 3.0f;
        this.mouseSpeed = 0.005f;
        this.touchFocus = false;
    }

    public void viewChanged(int width, int height)
    {
        // Set the OpenGL viewport to fill the entire surface
        glViewport(0, 0, width, height);

        /* Use OpenGL or MatrixHelper class
        PerspectiveM creates a frustrum matrix that allows objects within
        a specific and realistic viewpoint to be rendered - the further
        things are, the smaller
          */
        Matrix.perspectiveM(projectionMatrix, 0, 45, (float) width
                / (float) height, 0.1f, 50f);

        updateLookUp();
    }

    public void setPosition(Geometry.Point position)
    {
        this.position = position;
        updateLookUp();
    }

    public Geometry.Point getPosition()
    {
        return this.position;
    }

    private Geometry.Vector direction;
    private Geometry.Vector right;
    private void updateLookUp()
    {
        Geometry.Vector up = right.crossProduct(direction);

        Matrix.setLookAtM(viewMatrix, 0,
                // x y z
                position.x, position.y, position.z, // eye position
                position.x + direction.x, position.y + direction.y, position.z + direction.z, // center position
                up.x, up.y, up.z); // up position?

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    public boolean hasTouchFocus()
    {
        return touchFocus;
    }

    @Override
    public void click(PointF position)
    {
        touchFocus = true;

        // Send touch event
        TouchEvent be = new TouchEvent(this);
        be.setEvent(TouchEvent.Event.CLICK);
        for(TouchListener bl : touchListeners)
        {
            bl.touchEvent(be, position);
        }
    }

    @Override
    public void release(PointF position)
    {
        if(touchFocus)
        {
            // Send release event
            TouchEvent be = new TouchEvent(this);
            be.setEvent(TouchEvent.Event.RELEASE);
            for(TouchListener bl : touchListeners)
            {
                bl.touchEvent(be, position);
            }

            touchFocus = false;
        }
    }

    @Override
    public void drag(PointF position)
    {
        // Send the drag touch event
        TouchEvent be = new TouchEvent(this);
        be.setEvent(TouchEvent.Event.DOWN);
        for(TouchListener bl : touchListeners)
        {
            bl.touchEvent(be, position);
        }
    }

    public enum Direction
    {
        FORWARD,
        RIGHT,
        UP
    }

    // Currently only supports X-Z movement
    public void translate(Geometry.Vector velocity)
    {
        float hAngle = horizontalAngle - (float) Math.PI;
        Geometry.Vector moveVector = new Geometry.Vector(
                (velocity.x * (float)cos(hAngle)) + (velocity.z * (float)sin(hAngle)),
                0.0f,
                (velocity.z * (float)cos(hAngle)) + (velocity.x * -(float)sin(hAngle)));

        this.position = this.position.translate(moveVector);
        updateLookUp();
    }

    public void translate(float speed, Direction direction)
    {
        float movementZ = ((float)cos(horizontalAngle) * speed);
        float movementX = ((float)sin(horizontalAngle) * speed);

        if(direction == Direction.FORWARD) {
            this.position = this.position.translate(new Geometry.Vector(movementX, 0.0f, movementZ));
        }
        else if(direction == Direction.RIGHT)
        {
            this.position = this.position.translate(new Geometry.Vector(-movementZ, 0.0f, movementX));
        }
        else if(direction == Direction.UP)
        {
            this.position = this.position.translate(new Geometry.Vector(0.0f, speed, 0.0f));
        }

        updateLookUp();
    }

    public void setAngles(float horizontalAngle, float verticalAngle)
    {
        this.horizontalAngle = horizontalAngle;
        this.verticalAngle = verticalAngle;

        direction = new Geometry.Vector(
                (float)(cos(verticalAngle) * sin(horizontalAngle)),
                (float)(sin(verticalAngle)),
                (float)(cos(verticalAngle) * cos(horizontalAngle))
        );

        right = new Geometry.Vector(
                (float)(sin(horizontalAngle - 3.14f/2.0f)),
                0.0f,
                (float)(cos(horizontalAngle - 3.14f/2.0f))
        );

        updateLookUp();
    }

    public float[] getViewProjectionMatrix()
    {
        return viewProjectionMatrix;
    }
    public float[] getViewMatrix() { return viewMatrix; }
    public float[] getProjectionMatrix() { return projectionMatrix; }

    public void rotateViewProjection(float a, float x, float y, float z)
    {
        rotateM(viewProjectionMatrix, 0, a, x, y, z);
    }

    public void setLookAt(float eyeX, float eyeY, float eyeZ,
                          float centerX, float centerY, float centerZ,
                          float upX, float upY, float upZ)
    {
        this.eyeX = eyeX;
        this.eyeY = eyeY;
        this.eyeZ = eyeZ;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.upX = upX;
        this.upY = upY;
        this.upZ = upZ;
        updateLookUp();
    }

    public void setEye(float eyeX, float eyeY, float eyeZ)
    {
        this.eyeX = eyeX;
        this.eyeY = eyeY;
        this.eyeZ = eyeZ;
        updateLookUp();
    }

    public void setCenter(float centerX, float centerY, float centerZ)
    {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        updateLookUp();
    }

    public void setUp(float upX, float upY, float upZ)
    {
        this.upX = upX;
        this.upY = upY;
        this.upZ = upZ;
        updateLookUp();
    }

    public void moveCamera(float speed)
    {
        // view vector
        float x = this.centerX - this.eyeX;
        float y = this.centerY - this.eyeY;
        float z = this.centerZ - this.eyeZ;

        this.eyeX = eyeX + x * speed;
        this.eyeZ = eyeZ + z * speed;
        this.centerX = centerX + x * speed;
        this.centerZ = centerZ + z * speed;
    }

    public void rotateView(float speed)
    {
        // view vector
        float x = this.centerX - this.eyeX;
        float y = this.centerY - this.eyeY;
        float z = this.centerZ - this.eyeZ;

        this.centerZ = (float)(eyeZ + sin(speed)*x + cos(speed)*z);
        this.centerX = (float)(eyeX + cos(speed)*x - sin(speed)*z);
    }

    public void rotatePosition(float speed)
    {
        // view vector
        float x = this.centerX - this.eyeX;
        float y = this.centerY - this.eyeY;
        float z = this.centerZ - this.eyeZ;

        eyeZ = (float)(centerZ + sin(speed)*x + cos(speed)*z);
        eyeX = (float)(centerX + cos(speed)*x + sin(speed)*z);

        centerY += speed;
        updateLookUp();
    }

    public void strafe(float speed)
    {
        // view vector
        float x = this.centerX - this.eyeX;
        float y = this.centerY - this.eyeY;
        float z = this.centerZ - this.eyeZ;

        float oX = -z;
        float oZ = x;

        // mPos = eye, mView = center
        eyeX = eyeX + oX * speed;
        eyeZ = eyeZ + oZ * speed;
        centerX = centerX + oX * speed;
        centerZ = centerZ + oZ * speed;
    }
}
