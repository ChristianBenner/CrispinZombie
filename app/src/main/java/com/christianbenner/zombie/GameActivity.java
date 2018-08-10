package com.christianbenner.zombie;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.christianbenner.zombie.Scenes.RendererManager;

/**
 * Created by Christian Benner on 12/11/2017.
 */

public class GameActivity extends Activity
{
    private GLSurfaceView glSurfaceView;
    private RendererManager renderer;
    private boolean rendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);

        if(supportGLES())
        {
            // Set the program to use GL ES 2.0
            glSurfaceView.setEGLContextClientVersion(2);

            // Add renderer to the surface view
            renderer = new RendererManager(this);
            glSurfaceView.setRenderer(renderer);
            rendererSet = true;

            glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    if(event!=null) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                renderer.motionEvent(v, event);
                            }
                        });
                    }

                    return true;
                }
            });

          //  glSurfaceView.dispatchTouchEvent(event);
 /*           glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    if(event!=null) {

                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                renderer.motionEvent(v, event);
                            }
                        });


                        return true;
                    }

                    return false;




                   // return true;
*//*
                    if(event != null)
                    {
                        int action = MotionEventCompat.getActionMasked(event);
                        int index = MotionEventCompat.getActionIndex(event);
                        int xPos = -1;
                        int yPos = -1;
                        if(event.getPointerCount() > 1)
                        {
                            System.err.print("Multi Touch: ");
                            xPos = (int)MotionEventCompat.getX(event, index);
                            yPos = (int)MotionEventCompat.getY(event, index);
                            System.err.printf("Pointer[%d], x: %d, y: %d\n", index, xPos, yPos);
                        }
                        else
                        {
                            System.err.print("Single Touch: ");
                            xPos = (int)MotionEventCompat.getX(event, index);
                            yPos = (int)MotionEventCompat.getY(event, index);
                            System.err.printf("Pointer[%d], x: %d, y: %d\n", index, xPos, yPos);
                        }

                        return true;*//*


                }
            });
*/
            setContentView(glSurfaceView);
        }
        else
        {
            Toast.makeText(this, "Error : This device does not support OpenGL ES 2.0",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Determines if the device GL ES version is 2 or more
    private boolean supportGLES()
    {
        return ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))
                .getDeviceConfigurationInfo().reqGlEsVersion >= 0x20000;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(rendererSet){
            glSurfaceView.onPause();
            renderer.onPause();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(rendererSet){
            glSurfaceView.onResume();
            renderer.onResume();
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();

        if(rendererSet)
        {
            glSurfaceView.onPause();
            renderer.onRestart();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        if(rendererSet){
            glSurfaceView.onPause();
            renderer.onPause();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(rendererSet)
        {
            glSurfaceView.destroyDrawingCache();
            renderer.onDestroy();
        }
    }
}
