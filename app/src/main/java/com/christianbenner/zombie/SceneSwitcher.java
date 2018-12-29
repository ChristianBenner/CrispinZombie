package com.christianbenner.zombie;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Scenes.SceneGame;
import com.christianbenner.zombie.Scenes.SceneIntro;
import com.christianbenner.zombie.Scenes.SceneMenu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

public class SceneSwitcher implements GLSurfaceView.Renderer {
    private final static int STARTING_SCENE = Constants.INTRO_ID;
    private static final int FRAMES_TO_CALCULATE = 15;
    private static final float TARGET_UPDATE_RATE = 60.0f;

    private int currentSceneType;
    private Scene currentScene;
    private final Context context;

    private int viewWidth;
    private int viewHeight;

    private float deltaTime;
    private long startNanoTime;
    private int updateCount;

    private final Callable<Integer> initSceneFunc;

    public SceneSwitcher(Context context, Scene startScene, Callable<Integer> initSceneFunc)
    {
        this.context = context;
        this.initSceneFunc = initSceneFunc;

        this.deltaTime = 1.0f;
        this.startNanoTime = System.nanoTime();
        this.updateCount = 0;

        this.viewWidth = 0;
        this.viewHeight = 0;

        this.currentScene = startScene;
        this.currentSceneType = this.STARTING_SCENE;
    }

    public void initScene()
    {
        try {
            initSceneFunc.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroyCurrentScene()
    {
        // If the current scene is loaded, destroy it first
        if(currentScene != null)
        {
            currentScene.onDestroy();
        }
    }

    public int getNextScene()
    {
        if(currentScene != null)
        {
            return currentScene.getNextSceneId();
        }

        return 0;
    }

    public void setCurrentScene(Scene scene)
    {
        this.currentScene = scene;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        currentScene.surfaceCreatedCall();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;

        currentScene.surfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Draw call functionality
        if(currentScene != null)
        {
            // This method calls the update functionality with a delta time value
            // so that all things updated move at the same speed independent on the
            // rate the update function is called
            // Calculate time taken to update
            if(updateCount == 15)
            {
                // long totalNanoCalc = System.nanoTime() - startNanoTime;
                // totalNanoCalc -= timePausedInNanos;
                // long ms = totalNanoCalc / 1000000;

                deltaTime = TARGET_UPDATE_RATE / (1000 /
                        (((System.nanoTime() - startNanoTime) / 1000000) / (float)FRAMES_TO_CALCULATE));
                startNanoTime = System.nanoTime();

                //  System.out.println("Debug : DeltaTime (" + deltaTime + ")");
                updateCount = 0;
            }

            updateCount++;
            currentScene.update(deltaTime);
            currentScene.draw();

            if(currentScene.getNextSceneId() != 0)
            {
                initScene();
                currentSceneType = currentScene.getNextSceneId();
                currentScene.surfaceCreatedCall();
                currentScene.surfaceChanged(viewWidth, viewHeight);
            }
        }
        else
        {
            // If the scene is not ready or isn't initialised
            System.err.println("The current scene is not initialised therefor there is nothing to draw or update...");
            glClear(GL_COLOR_BUFFER_BIT);
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        }

    }

    public void motionEvent(View view, MotionEvent event) {
        if(currentScene != null)
        {
            currentScene.motionEvent(view, event);
        }
        else
        {
            System.err.println("There is nothing to handle the motion event. No scene is running");
        }
    }
}
