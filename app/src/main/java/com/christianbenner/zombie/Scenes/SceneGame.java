package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.objects.RendererModel;
import com.christianbenner.crispinandroid.programs.PerFragMultiLightingShader;
import com.christianbenner.crispinandroid.programs.TextureShaderProgram;
import com.christianbenner.crispinandroid.ui.BaseController;
import com.christianbenner.crispinandroid.ui.GLButton;
import com.christianbenner.crispinandroid.ui.GLFont;
import com.christianbenner.crispinandroid.ui.GLImage;
import com.christianbenner.crispinandroid.ui.GLText;
import com.christianbenner.crispinandroid.ui.MoveController;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;
import com.christianbenner.crispinandroid.ui.UIDimension;
import com.christianbenner.crispinandroid.util.Camera;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Light;
import com.christianbenner.crispinandroid.util.Renderer;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.crispinandroid.util.UIRenderer;
import com.christianbenner.crispinandroid.util.UIRendererGroup;
import com.christianbenner.zombie.Map;
import com.christianbenner.zombie.Objects.Bullet;
import com.christianbenner.zombie.Objects.Human;
import com.christianbenner.zombie.Objects.Zombie;
import com.christianbenner.zombie.R;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Christian Benner on 15/02/2018.
 */

public class SceneGame extends Scene {
    private final boolean STARTUP_ON_DEBUG_VIEW = false;
    private final float BUTTON_SIZE = 150.0f;
    private final float BUTTON_PADDING = 20.0f;
    private final float MOVE_SPEED = 0.04f;
    private final float CAMERA_INFO_TEXT_FADEOUT_SPEED = 0.02f;
    private final Geometry.Point CAMERA_START_POSITION =
            new Geometry.Point(4.0f, 8.0f, 0.0f);
    private final Geometry.Point DEBUG_CAMERA_START_POSITION =
            new Geometry.Point(0.0f, 1.0f, 0.0f);
    private final Geometry.Point PLAYER_START_POSITION =
            new Geometry.Point(5.0f, 0.0f, 5.0f);
    static final float CROSSHAIR_OFFSET = 100.0f;
    private final int MUSIC_TRACKS = 2;

    // Android Context
    private int viewWidth;
    private int viewHeight;

    // Game Objects
    private Light TEST_LIGHT;
    private Light TEST_LIGHT2;
    private RendererModel box;
    private Human humanoid;
    //private RendererModel sniper;

    // Hold all the zombies
    private ArrayList<Zombie> zombies;

    // Cameras and Rendering
    private Camera camera;
    private Camera debugCamera;

    // Some of these probably aren't necessary - investigate
    private float cameraX = 3.141592f;
    private float cameraY = 0.0f;
    private float previousX = 0.0f;
    private float previousY = 0.0f;
    private boolean doneYet = false;

    private Renderer renderer;
    private UIRenderer uiRenderer;
    private UIRendererGroup debugViewUIGroup;
    private PerFragMultiLightingShader shader;
    private TextureShaderProgram uiShader;

    // UI
    private GLButton debug_camera_button_up;
    private GLButton debug_camera_button_down;
    private GLButton switch_camera_button;
    private GLButton wave_button;
    private GLImage hotbar;
    private GLText cameraText;
    private BaseController baseMoveController;
    private MoveController moveController;
    private BaseController baseAimController;
    private MoveController aimController;
    private GLImage crosshair;

    // Properties and timers
    private boolean debugView;
    private float cameraTextTimer = 1.0f;

    // Music queue
    private int[] musicQueue = new int[MUSIC_TRACKS];
    private int musicQueueIndex = 0;

    // Map
    private Map demoMap;

    private ArrayList<Bullet> bullets;

    public SceneGame(Context context) {
        super(context);

        // Create the demo map
        demoMap = new Map(context, R.raw.demo_map4);
        demoMap.printMap();

        // Sets up whether or not we start on the debug camera
        debugView = STARTUP_ON_DEBUG_VIEW;

        // Create the camera
        camera = new Camera();
        camera.setAngles(3.141592f, -(3.141592f/2.0f));
        camera.setPosition(CAMERA_START_POSITION);

        // Create the debug camera
        debugCamera = new Camera();
        debugCamera.setPosition(DEBUG_CAMERA_START_POSITION);

        bullets = new ArrayList<>();

        // Add a touch listener so that we can pick up touches on the camera and handle them
        debugCamera.addTouchListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    // Allows for the user to look around by dragging there finger
                    case DOWN:
                        float normalizedX = (e.getPosition().x / (float) viewWidth) * 2 - 1;
                        float normalizedY = ((e.getPosition().y / (float) viewHeight) * 2 - 1);

                        if(doneYet == false)
                        {
                            previousX = normalizedX;
                            previousY = normalizedY;
                            doneYet = true;
                        }

                        cameraX -= previousX - normalizedX;
                        cameraY -= previousY - normalizedY;
                        previousX = normalizedX;
                        previousY = normalizedY;
                        debugCamera.setAngles(-cameraX, cameraY);
                        break;
                    case RELEASE:
                        doneYet = false;
                        break;
                }
            }
        });

        // Init renderer and create objects
     //   sniper = new RendererModel(context, R.raw.sniper, TextureHelper.loadTexture(context, R.drawable.sniper, true));
       // sniper.setPosition(new Geometry.Point(2.0f, 1.0f, 5.0f));
      //  sniper.setScale(0.1f);

        box = new RendererModel(context, R.raw.box, TextureHelper.loadTexture(context, R.drawable.box));
        humanoid = new Human(context,
                TextureHelper.loadTexture(context, R.drawable.player, true), MOVE_SPEED);
        humanoid.setPosition(PLAYER_START_POSITION);

        zombies = new ArrayList<>();
        zombies.add(new Zombie(context,
                TextureHelper.loadTexture(context, R.drawable.zombie, true),
                MOVE_SPEED / 2.0f, humanoid,
                new Geometry.Point(4.0f, 0.0f, 0.0f), demoMap));

        TEST_LIGHT = new Light();
        TEST_LIGHT2 = new Light();
        TEST_LIGHT.setColour(new Colour(0.89f, 0.89f, 0.89f));
        TEST_LIGHT2.setColour(new Colour(1.0f, 1.0f, 0.0f));
        TEST_LIGHT2.setAmbienceIntensity(0.3f);
        TEST_LIGHT.setAttenuation(0.001f);

        shader = new PerFragMultiLightingShader(context);
        renderer = new Renderer(shader, camera);
        //renderer.addModel(sniper);
        renderer.addModel(box);
        renderer.addLight(TEST_LIGHT);
        renderer.addLight(TEST_LIGHT2);
        humanoid.addToRenderer(renderer);

        for(int i = 0; i < zombies.size(); i++)
        {
            zombies.get(i).addToRenderer(renderer);
        }

        demoMap.addToRenderer(renderer);

        uiRenderer = new UIRenderer(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        debugViewUIGroup = new UIRendererGroup(uiRenderer, debugView);

        musicQueue[0] = R.raw.button_click;
        musicQueue[1] = R.raw.button_click;
    }

    @Override
    protected void surfaceCreated()
    {
      //  cameraText.setColour(new Colour(1.0f, 0.0f, 0.0f, 1.0f));
       // cameraText.setPosition(-1.0f, -1.0f);

       // playMusic(context, R.raw.zombies);
        //playSound(context, R.raw.scarysounds, -1);
       /* audio.setOnMusicComlete(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                System.out.println("Switching to next music track");
                if(musicQueueIndex >= TRACKS)
                {
                    musicQueueIndex = 0;
                }
                playMusic(context, musicQueue[musicQueueIndex++]);
            }
        });*/
    }
    private GLText text;
    @Override
    public void surfaceChanged(int width, int height) {
        viewWidth = width;
        viewHeight = height;
        uiRenderer.createUICanvas(width, height);
        camera.viewChanged(width, height);
        debugCamera.viewChanged(width, height);

        uiRenderer.createUICanvas(width, height);
        uiShader = new TextureShaderProgram(context);
        uiRenderer.setShader(uiShader);

        // If Debug Enabled
        // Camera Text
        GLFont font = new GLFont(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        if(debugView)
        {
            cameraText = new GLText("Debug Camera", 2, font, width, uiRenderer, true);
        }
        else
        {
            cameraText = new GLText("Birds Eye Camera", 2, font, width, uiRenderer, true);
        }

        cameraText.setPosition(new Geometry.Point(0.0f, height - cameraText.getHeight(), 0.0f));
        uiRenderer.addUI(cameraText);

        initUI();

        shader = new PerFragMultiLightingShader(context);
        renderer.setShader(shader);
        if(debugView)
        {
            renderer.setCamera(debugCamera);
        }
        else
        {
            renderer.setCamera(camera);
        }
    }

    private float boxRotationAngle = 0.0f;
    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        box.newIdentity();
        box.setPosition(new Geometry.Point(1f, 0.0f, 0.0f));
        box.rotate(boxRotationAngle, 1.0f, 0.0f, 0.0f);
        box.setScale(0.25f);

        renderer.render();
        uiRenderer.render();

        glDisable(GL_DEPTH_TEST);
    }

    // Position Lights (Rotate around a point)
    private float[] mLightModelMatrix = new float[16];
    private float[] mLightPosInWorldSpace = new float[4];
    private float[] mLightPosInEyeSpace = new float[4];
    private float[] mLightPosInModelSpace = { 0.0f, 0.0f, 0.0f, 1.0f };
    private float lightRotationAngle = 0.0f;
    private float[] lightInModelSpace = new float[4];
    private void positionLight(float z)
    {
        setIdentityM(mLightModelMatrix, 0);
        translateM(mLightModelMatrix, 0, 7.0f, 1.0f, 4.0f);
        rotateM(mLightModelMatrix, 0, lightRotationAngle, 0.0f, 1.0f, 0.0f);
        translateM(mLightModelMatrix, 0, 0.0f, 0.0f, z);
        multiplyMV(lightInModelSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        if(debugView)
        {
            multiplyMV(mLightPosInEyeSpace, 0, debugCamera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        }
        else
        {
            multiplyMV(mLightPosInEyeSpace, 0, camera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        }
    }

    float angleS = 0.0f;
    long time = 0;
    boolean done = false;

    float nX = 0.0f;
    float nZ = 0.0f;
    @Override
    public void update(float deltaTime) {
        /*for(int i = 0; i < 5; i++)
        {
            System.out.println("At [" + i + "]: " + mActivePointers.get(mActivePointers.keyAt(i), new PointF(0.0f, 0.0f)));
        }*/

        moveController.update(deltaTime);
        aimController.update(deltaTime);

        Geometry.Vector moveVector = moveController.getDirection();
        humanoid.setVelocity(new Geometry.Vector(moveVector.x, 0.0f, -moveVector.y));

        if(debugView)
        {
            debug_camera_button_up.update(deltaTime);
            debug_camera_button_down.update(deltaTime);
        }
        switch_camera_button.update(deltaTime);
        wave_button.update(deltaTime);

        if(cameraTextTimer > 0.0f)
        {
            cameraTextTimer -= CAMERA_INFO_TEXT_FADEOUT_SPEED * deltaTime;
            if(cameraTextTimer < 0.0f)
            {
                cameraTextTimer = 0.0f;
            }

            cameraText.setColour(new Colour(1.0f, 0.0f, 0.0f, cameraTextTimer));
        }

        humanoid.update(deltaTime);

        for(Zombie zombie : zombies)
        {
            zombie.update(deltaTime);
        }

     //   sniper.setPosition(new Geometry.Point(humanoid.getPosition().x, humanoid.getPosition().y, humanoid.getPosition().z));
      //  sniper.setPosition(new Geometry.Point(humanoid.getPosition().x, 1.0f, 1.0f));

        angleS += 2f * deltaTime;
   //     sniper.newIdentity();
    //    sniper.setPosition(new Geometry.Point(3.0f, 0.1f, 4.0f));
    //    sniper.setScale(0.1f);
    //    sniper.rotate(angleS, 0.0f, 1.0f, 0.0f);
        camera.setPosition(new Geometry.Point(humanoid.getPosition().x, CAMERA_START_POSITION.y, humanoid.getPosition().z));

        boxRotationAngle += 1f * deltaTime;
        lightRotationAngle += 0.5f * deltaTime;
        positionLight(5.0f);
        TEST_LIGHT.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
                mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));
        positionLight(-5.0f);
        TEST_LIGHT2.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
                mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));
    }

    // Process touch
    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent event)
    {
        switch(event){
            case CLICK:
                // Look through the UI and check if the pointer interacts with them
                if(handlePointerControl(switch_camera_button, pointer)) { return; }
                if(handlePointerControl(wave_button, pointer)) { return; }
                if(handlePointerControl(moveController, pointer)) { return; }
                if(handlePointerControl(aimController, pointer)) { return; }
                if(debugView)
                {
                    if(handlePointerControl(debug_camera_button_up, pointer)) { return; }
                    if(handlePointerControl(debug_camera_button_down, pointer)) { return; }

                    if(!debugCamera.hasTouchFocus())
                    {
                        pointer.setControlOver(debugCamera);
                    }
                }
                break;
        }
    }

    private boolean handlePointerControl(GLButton button, Pointer pointer)
    {
        if(button.interacts(pointer))
        {
            pointer.setControlOver(button);
            return true;
        }

        return false;
    }


    int bulletWaitCount = 0;
    private void initUI()
    {
        Texture hotbar_texture = TextureHelper.loadTexture(context, R.drawable.hotbar_scaled, true);
        hotbar = new GLImage(new UIDimension((viewWidth / 2.0f) - (hotbar_texture.getWidth() / 2.0f), 10.0f,
                hotbar_texture.getWidth(), hotbar_texture.getHeight()), hotbar_texture);

        crosshair = new GLImage(
                new UIDimension((viewWidth/2.0f) - 25, (viewHeight/2.0f) - 25, 50, 50),
                new Colour(1.0f, 1.0f, 1.0f, 0.0f),
                TextureHelper.loadTexture(context, R.drawable.crosshair, true));

        // Add the move joystick
        baseMoveController = new BaseController(context,
                new Geometry.Point(80.0f, 70.0f, 0.0f), 200.0f, R.drawable.joy_stick_outer);
        moveController = new MoveController(context, baseMoveController, R.drawable.movement_joystick_inner);
        moveController.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                    case DOWN:
                        if(debugView)
                        {
                            Geometry.Vector velocity = moveController.getDirection().scale(MOVE_SPEED);
                           // debugCamera.translate(new Geometry.Vector(velocity.x, 0.0f, -velocity.y));
                        }
                        else
                        {
                            Geometry.Vector velocity = moveController.getDirection().scale(MOVE_SPEED);
                            humanoid.setVelocity(new Geometry.Vector(velocity.x, 0.0f, -velocity.y));
                        }
                        break;
                    case RELEASE:
                        humanoid.setVelocity(new Geometry.Vector(0.0f, 0.0f, 0.0f));
                        break;
                }
            }
        });

        // Add the aim joystick
        baseAimController = new BaseController(context,
                new Geometry.Point(viewWidth - 80.0f - 400.0f, 70.0f, 0.0f), 200.0f,
                R.drawable.joy_stick_outer);
        aimController = new MoveController(context, baseAimController, R.drawable.aim_joy_stick_inner);
        aimController.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        crosshair.setAlpha(1.0f);
                        break;
                    case DOWN:
                        Geometry.Vector offsetDirection = aimController.getDirection().
                                scale(1.0f / aimController.getDirection().length());
                        Geometry.Vector offset = offsetDirection.scale(CROSSHAIR_OFFSET);
                        crosshair.setPosition(new Geometry.Point((viewWidth/2.0f) - 25 + offset.x,
                                (viewHeight/2.0f) - 25 + offset.y, 0.0f));

                        if(bulletWaitCount > 10)
                        {
                            bulletWaitCount = 0;

                            // Spawn bullet
                            bullets.add(new Bullet(humanoid.getPosition().x,
                                    humanoid.getPosition().y, offsetDirection,
                                    0.01f, 500000.0f));

                        }


                        break;
                    case RELEASE:
                        crosshair.setPosition(new Geometry.Point((viewWidth/2.0f) - 25,
                                (viewHeight/2.0f) - 25, 0.0f));
                        crosshair.setAlpha(0.0f);
                        break;
                }
            }
        });

        wave_button = new GLButton(new UIDimension(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
                viewHeight - BUTTON_SIZE - BUTTON_PADDING - BUTTON_PADDING - BUTTON_SIZE,
                BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_wave));
        wave_button.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        humanoid.setWaving(!humanoid.isWaving());
                        break;
                }
            }
        });

        switch_camera_button = new GLButton(
                new UIDimension(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
                        viewHeight - BUTTON_SIZE - BUTTON_PADDING, BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_camera));
        switch_camera_button.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        debug_camera_button_up.forceRelease();
                        debug_camera_button_down.forceRelease();
                        debugView = !debugView;
                        cameraTextTimer = 1.0f;
                        if(debugView)
                        {
                            Geometry.Point pos = camera.getPosition();
                            pos.y = debugCamera.getPosition().y;
                            debugCamera.setPosition(pos);
                            renderer.setCamera(debugCamera);
                            cameraText.setText("Debug Camera");
                            humanoid.setVelocity(new Geometry.Vector(0.0f, 0.0f, 0.0f));
                            debugViewUIGroup.enableRendering();
                        }
                        else
                        {
                            Geometry.Point suspended =
                                    debugCamera.getPosition();
                            suspended.y = CAMERA_START_POSITION.y;
                            camera.setPosition(suspended);
                            renderer.setCamera(camera);
                            cameraText.setText("Birds Eye Camera");
                            debugViewUIGroup.disableRendering();
                        }
                        break;
                }
            }
        });

        debug_camera_button_up = new GLButton(
                new UIDimension(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
                        BUTTON_PADDING + BUTTON_PADDING + BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_up));
        debug_camera_button_up.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    case DOWN:
                        debugCamera.translate(MOVE_SPEED, Camera.Direction.UP);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        debug_camera_button_down = new GLButton(
                new UIDimension(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
                        BUTTON_PADDING, BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_down));
        debug_camera_button_down.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    case DOWN:
                        debugCamera.translate(-MOVE_SPEED, Camera.Direction.UP);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        // Add to the UI Renderer
        uiRenderer.addUI(hotbar);
        uiRenderer.addUI(crosshair);
        uiRenderer.addUI(baseMoveController);
        uiRenderer.addUI(moveController);
        uiRenderer.addUI(baseAimController);
        uiRenderer.addUI(aimController);
        uiRenderer.addUI(switch_camera_button);
        uiRenderer.addUI(wave_button);
        debugViewUIGroup.addUI(debug_camera_button_down);
        debugViewUIGroup.addUI(debug_camera_button_up);
        uiRenderer.addRendererGroup(debugViewUIGroup);
    }

    @Override
    protected void pause() {

    }

    @Override
    protected void resume() {

    }

    @Override
    protected void restart() {

    }

    @Override
    protected void destroy() {

    }
}
