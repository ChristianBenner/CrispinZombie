package com.christianbenner.zombie;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.christianbenner.crispinandroid.util.SceneSwitcher;
import com.christianbenner.zombie.Scenes.DemoMap;
import com.christianbenner.zombie.Scenes.FromScratch;
import com.christianbenner.zombie.Scenes.SceneGame;
import com.christianbenner.zombie.Scenes.SceneIntro;
import com.christianbenner.zombie.Scenes.SceneMenuRemake;
import com.christianbenner.zombie.Scenes.TestScene;

import java.util.concurrent.Callable;

/**
 * Created by Christian Benner on 12/11/2017.
 */

public class GameActivity extends Activity
{
    private GLSurfaceView glSurfaceView;
    private SceneSwitcher sceneSwitcher;
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

            // The application context available to the callable below
            final Context APPLICATION_CONTEXT = this;

            // Add renderer to the surface view
            sceneSwitcher = new SceneSwitcher(new FromScratch(APPLICATION_CONTEXT), new Callable<Integer>() {
                @Override
                public Integer call() {
                    // Allocate a new scene
                    switch (sceneSwitcher.getSceneToLoad())
                    {
                        case Constants.INTRO_ID:
                            sceneSwitcher.setCurrentScene(new SceneIntro(APPLICATION_CONTEXT));
                            break;
                        case Constants.MENU_ID:
                            sceneSwitcher.setCurrentScene(new SceneMenuRemake(APPLICATION_CONTEXT));
                            break;
                        case Constants.GAME_ID:
                            sceneSwitcher.setCurrentScene(new SceneGame(APPLICATION_CONTEXT));
                            break;
                        case Constants.TEST_ID:
                            sceneSwitcher.setCurrentScene(new TestScene(APPLICATION_CONTEXT));
                            break;
                    }

                    return 0;
                }
            });

            glSurfaceView.setRenderer(sceneSwitcher);
            rendererSet = true;

            glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    if(event!=null) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                sceneSwitcher.motionEvent(v, event);
                            }
                        });
                    }

                    return true;
                }
            });

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
            sceneSwitcher.onPause();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(rendererSet){
            glSurfaceView.onResume();
            sceneSwitcher.onResume();
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();

        if(rendererSet)
        {
            glSurfaceView.onPause();
            sceneSwitcher.onRestart();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        if(rendererSet){
            glSurfaceView.onPause();
            sceneSwitcher.onStop();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(rendererSet)
        {
            glSurfaceView.destroyDrawingCache();
            sceneSwitcher.onDestroy();
        }
    }
}
