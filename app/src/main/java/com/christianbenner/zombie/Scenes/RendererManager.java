package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.AStarTesting.AStarDemo;

import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Christian Benner on 19/11/2017.
 */

public class RendererManager implements GLSurfaceView.Renderer {
    private HashMap<Integer, Scene> renderers = new HashMap<Integer, Scene>();
    private Context context;
    private int currentRendererID = 0;
    private boolean currentRendererInit = false;

    private int width;
    private int height;

    Scene scene;

    public RendererManager(Context context)
    {
        this.context = context;
       // scene = new SceneIntro(context);
       // addRenderer(RendererIDConstants.INTRO_ID, scene, true);
     //   scene = new SceneGameTest(context);
     //   addRenderer(RendererIDConstants.GAME_TEST_ID, scene, true);
        scene = new SceneGame(context);
        addRenderer(RendererIDConstants.GAME_ID, scene, true);
    }

    private void addRenderer(int id, Scene renderer, boolean currentRenderer)
    {
        // If the added scene wants to be the current rendered scene
        currentRendererID = currentRenderer ? id : currentRendererID;
        renderers.put(id, renderer);

        if(currentRenderer)
        {
            currentRendererInit = renderers.get(id).isInit();
        }
    }

    private void changeRenderer(int id)
    {
        currentRendererInit = renderers.get(id).isInit();
        currentRendererID = id;
    }

    // This will only get used once? Maybe it's a good idea to have an init
    protected void surfaceCreated() {

    }

    protected void surfaceChanged(int width, int height) {
        this.width = width;
        this.height = height;

        renderers.get(currentRendererID).surfaceChanged(width, height);
    }

    protected void draw() {
        renderers.get(currentRendererID).draw();
    }

    // in the update function make sure that the right renderer is being used
    protected void update(float deltaTime) {
        if(renderers.get(currentRendererID).isSceneFinished())
        {
            moveOntoNextRenderer();
        }

        if(currentRendererInit == false)
        {
            renderers.get(currentRendererID).surfaceChanged(width, height);
            renderers.get(currentRendererID).surfaceCreated();
            renderers.get(currentRendererID).setInit(true);
            currentRendererInit = true;
        }

        renderers.get(currentRendererID).update(deltaTime);
    }

    private void moveOntoNextRenderer()
    {
        currentRendererID = renderers.get(currentRendererID).getNextSceneId();
        switch (currentRendererID)
        {
            case RendererIDConstants.INTRO_ID:
                System.out.println("Switching to intro renderer");
                scene = new SceneIntro(context);
                addRenderer(RendererIDConstants.INTRO_ID, scene, true);
                break;
            case RendererIDConstants.MENU_ID:
                System.out.println("Switching to menu renderer");
                scene = new SceneMenu(context);
                addRenderer(RendererIDConstants.MENU_ID, scene, true);
                break;
            case RendererIDConstants.START_WARS_TEST_ID:
                System.out.println("Switching to test bench renderer");
                scene = new SceneStarWarsTest(context);
                addRenderer(RendererIDConstants.START_WARS_TEST_ID, scene, true);
                break;
            case RendererIDConstants.GAME_TEST_ID:
                System.out.println("Switching to game test scene");
                scene = new SceneGameTest(context);
                addRenderer(RendererIDConstants.GAME_TEST_ID, scene, true);
                break;
            case RendererIDConstants.A_STAR_DEMO:
                System.out.println("Switching to a star demo scene");
                scene = new AStarDemo(context);
                addRenderer(RendererIDConstants.A_STAR_DEMO, scene, true);
                break;
            case RendererIDConstants.GAME_ID:
                System.out.println("Switching to game scene");
                scene = new SceneGame(context);
                addRenderer(RendererIDConstants.GAME_ID, scene, true);
                break;
        }
    }

    public void onPause() {
        pauseNanoTime = System.nanoTime();
        renderers.get(currentRendererID).onPause();
        currentRendererInit = false;
    }

    public void onResume() {
        timePausedInNanos = System.nanoTime() - pauseNanoTime;
        renderers.get(currentRendererID).onResume(context);
    }

    public void onRestart() {
        renderers.get(currentRendererID).onRestart();
        currentRendererInit = false;
    }

    public void onDestroy() {
        renderers.get(currentRendererID).onDestroy();
        currentRendererInit = false;
    }

    public void motionEvent(View view, MotionEvent event) {
        renderers.get(currentRendererID).motionEvent(view, event);
    }

    private static final int FRAMES_TO_CALCULATE = 15;
    private static final float TARGET_UPDATE_RATE = 60.0f;
    private static final float MAX_DELTA = 1.0f;

    private long pauseNanoTime;
    private long timePausedInNanos;

    private long startNanoTime;
    private int updateCount;
    private float deltaTime;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        surfaceCreated();

        // Setup the timing variables
        deltaTime = MAX_DELTA;
        updateCount = 0;
        startNanoTime = System.nanoTime();
        timePausedInNanos = 0;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        timeUpdate();
        draw();
    }

    // This method calls the update functionality with a delta time value
    // so that all things updated move at the same speed independent on the
    // rate the update function is called
    private void timeUpdate()
    {
        // Calculate time taken to update
        if(updateCount == 15)
        {
            long totalNanoCalc = System.nanoTime() - startNanoTime;
            totalNanoCalc -= timePausedInNanos;
            long ms = totalNanoCalc / 1000000;
            startNanoTime = System.nanoTime();

            deltaTime = TARGET_UPDATE_RATE / (1000 /
                    ((float)ms / (float)FRAMES_TO_CALCULATE));

          //  System.out.println("Debug : DeltaTime (" + deltaTime + ")");
            updateCount = 0;
        }

        updateCount++;
        update(deltaTime);
    }
}
