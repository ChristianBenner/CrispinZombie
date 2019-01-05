package com.christianbenner.crispinandroid.util;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.Callable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

public class SceneSwitcher implements GLSurfaceView.Renderer {
    private static final int FRAMES_TO_CALCULATE = 15;
    private static final float TARGET_UPDATE_RATE = 60.0f;

    private Scene currentScene;

    private int viewWidth;
    private int viewHeight;

    private float deltaTime;
    private long startNanoTime;
    private int updateCount;

    private final Callable<Integer> initSceneFunc;

    public SceneSwitcher(Scene startScene, Callable<Integer> initSceneFunc)
    {
        this.initSceneFunc = initSceneFunc;

        this.deltaTime = 1.0f;
        this.startNanoTime = System.nanoTime();
        this.updateCount = 0;

        this.viewWidth = 0;
        this.viewHeight = 0;

        this.currentScene = startScene;
    }

    // Call the destroy function on the current scene
    public void destroyCurrentScene()
    {
        // If the current scene is loaded, destroy it first
        if(currentScene != null)
        {
            currentScene.onDestroy();
        }
    }

    // If the scene is finished, get the ID of the next, if not, get the current ID
    public int getSceneToLoad()
    {
        if(currentScene != null)
        {
            if(currentScene.isSceneFinished())
            {
                return currentScene.getNextSceneId();
            }

            return currentScene.getSceneId();
        }

        return 0;
    }

    // Set the current scene
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

        this.deltaTime = 1.0f;

        currentScene.surfaceChanged(width, height);
    }

    private long timeStart = 0;
    private int frames = 0;

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

            if(timeStart == 0)
            {
                timeStart = System.currentTimeMillis();
            }
            else
            {
                frames++;

                if(System.currentTimeMillis() - timeStart >= 1000)
                {
                    System.out.println("FPS: " + frames);
                    timeStart = System.currentTimeMillis();
                    frames = 0;
                }
            }

            updateCount++;
            currentScene.update(deltaTime);
            currentScene.draw();

            if(currentScene.getNextSceneId() != 0)
            {
                currentScene.destroy();

                try {
                    initSceneFunc.call();
                } catch (Exception e) {
                    System.err.println("SceneSwitcher: ERROR FAILED TO SWITCH SCENE");
                    e.printStackTrace();
                    System.exit(1);
                }

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

    public void onPause(){
        currentScene.pause();
    }

    public void onResume(){
        currentScene.resume();
    }

    public void onRestart(){
        currentScene.restart();
    }

    public void onStop(){
        currentScene.destroy();
    }

    public void onDestroy(){
        currentScene.destroy();
    }
}
