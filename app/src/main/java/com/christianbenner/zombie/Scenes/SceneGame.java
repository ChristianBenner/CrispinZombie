package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Light;
import com.christianbenner.crispinandroid.render.data.RendererGroupType;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.render.shaders.ColourShaderProgram;
import com.christianbenner.crispinandroid.render.shaders.PerFragLightingShader;
import com.christianbenner.crispinandroid.render.shaders.PerFragMultiLightingShader;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.RendererGroup;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.render.util.UIRendererGroup;
import com.christianbenner.crispinandroid.ui.BaseController;
import com.christianbenner.crispinandroid.ui.Button;
import com.christianbenner.crispinandroid.ui.Font;
import com.christianbenner.crispinandroid.ui.Image;
import com.christianbenner.crispinandroid.ui.MoveController;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.ui.Text;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.Entities.Bullet;
import com.christianbenner.zombie.Entities.Door;
import com.christianbenner.zombie.Entities.Player;
import com.christianbenner.zombie.Entities.Weapon;
import com.christianbenner.zombie.Entities.Zombie;
import com.christianbenner.zombie.Map.Map;
import com.christianbenner.zombie.R;

import java.util.ArrayList;
import java.util.Random;

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
            new Geometry.Point(3.0f, 0.0f, 3.5f);
    static final float CROSSHAIR_OFFSET = 100.0f;
    private final int MUSIC_TRACKS = 2;

    // Android Context
    private int viewWidth;
    private int viewHeight;

    // Game Objects
    private Light TEST_LIGHT;
    private Light TEST_LIGHT2;
    private RendererModel box;
    private RendererModel zombiehead;


    private Player player;
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
    private UIRendererGroup healthbarsUIGroup;

    private PerFragMultiLightingShader shader;
    private TextureShaderProgram uiShader;
    private ColourShaderProgram colourShader;
    private PerFragLightingShader lightingShader;

    // UI
    private Button debug_camera_button_up;
    private Button debug_camera_button_down;
    private Button switch_camera_button;
    private Button wave_button;
    private Button switch_weapon_button;
    private Image hotbar;
    private Text cameraText;
    private BaseController baseMoveController;
    private MoveController moveController;
    private BaseController baseAimController;
    private MoveController aimController;
    private Image crosshair;
    private Button buy_door_button;
    boolean showBuyUI = false;
    boolean buyDoorButtonInRenderer = false;

    // Properties and timers
    private boolean debugView;
    private float cameraTextTimer = 1.0f;

    // Music queue
    private int[] musicQueue = new int[MUSIC_TRACKS];
    private int musicQueueIndex = 0;

    // Bullets
    private ArrayList<Bullet> bullets;
    private RendererGroup bulletsGroup;
    // Map
    private Map demoMap;

    private Light muzzleFlareLight = null;
    private float muzzleFlareLightTimer;
    private boolean muzzleFlareProcess = false;
    private final int MUSIC_SELECTION;

    private ArrayList<RendererModel> zheads;

    private RendererGroup ztorso;

    public SceneGame(Context context) {
        super(context, Constants.GAME_ID);

        // Create the demo map
        demoMap = new Map(context, R.raw.demo_map4);
        demoMap.printMap();

        // Sets up whether or not we start on the debug camera
        debugView = STARTUP_ON_DEBUG_VIEW;

        // Create the camera
        camera = new Camera();
        camera.setAnglesRads(3.141592f, -(3.141592f/2.0f));
        camera.setPosition(CAMERA_START_POSITION);

        // Create the debug camera
        debugCamera = new Camera();
        debugCamera.setPosition(DEBUG_CAMERA_START_POSITION);

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
                        debugCamera.setAnglesRads(-cameraX, cameraY);
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
      //  zombiehead = new RendererModel(context, R.raw.zhead, TextureHelper.loadTexture(context, R.drawable.button_go));

        bullets = new ArrayList<>();
        bulletsGroup = new RendererGroup(RendererGroupType.SAME_BIND_SAME_TEX);

        player = new Player(context, TextureHelper.loadTexture(context, R.drawable.player, true),
                MOVE_SPEED, bullets, bulletsGroup, demoMap);

        player.setPosition(PLAYER_START_POSITION);

        zombies = new ArrayList<>();
        zombies.add(new Zombie(context,
                TextureHelper.loadTexture(context, R.drawable.zombie, true),
                MOVE_SPEED / 2.0f, player,
                new Geometry.Point(4.0f, 0.0f, 0.0f), demoMap));
        zombies.add(new Zombie(context,
                TextureHelper.loadTexture(context, R.drawable.zombie, true),
                MOVE_SPEED / 1.9f, player,
                new Geometry.Point(6.0f, 0.0f, 0.0f), demoMap));

        zombies.add(new Zombie(context,
                TextureHelper.loadTexture(context, R.drawable.zombie, true),
                MOVE_SPEED / 1.8f, player,
                new Geometry.Point(8.0f, 0.0f, 0.0f), demoMap));
        zombies.add(new Zombie(context,
                TextureHelper.loadTexture(context, R.drawable.zombie, true),
                MOVE_SPEED / 1.7f, player,
                new Geometry.Point(10.0f, 0.0f, 0.0f), demoMap));
        zombies.add(new Zombie(context,
                TextureHelper.loadTexture(context, R.drawable.zombie, true),
                MOVE_SPEED / 1.5f, player,
                new Geometry.Point(12.0f, 0.0f, 0.0f), demoMap));
        zombies.add(new Zombie(context,
                TextureHelper.loadTexture(context, R.drawable.zombie, true),
                MOVE_SPEED / 1.2f, player,
                new Geometry.Point(14.0f, 0.0f, 0.0f), demoMap));

        TEST_LIGHT = new Light();
        TEST_LIGHT2 = new Light();
        TEST_LIGHT.setColour(new Colour(0.89f, 0.89f, 0.89f));
        TEST_LIGHT2.setColour(new Colour(1.0f, 0.2f, 0.2f));
        TEST_LIGHT2.setAmbienceIntensity(0.3f);
        TEST_LIGHT2.setMaxAmbience(40f);
        TEST_LIGHT2.setAttenuation(4f);
        TEST_LIGHT.setAttenuation(0.001f);

        muzzleFlareLight = new Light();
        muzzleFlareLight.setColour(new Colour(1.0f, 1.0f, 0.0f));
        muzzleFlareLight.setAmbienceIntensity(0.0f);
        muzzleFlareLight.setAttenuation(0.6f);
        muzzleFlareLight.setMaxAmbience(0.0f);

        shader = new PerFragMultiLightingShader(context);
        renderer = new Renderer(shader, camera);

        ztorso = new RendererGroup(RendererGroupType.SAME_BIND_SAME_TEX);

        zheads = new ArrayList<>();
/*        for(int i = 0; i < 100; i++)
        {
            RendererModel model = new RendererModel(context, R.raw.wilbert_torso_test2, TextureHelper.loadTexture(context, R.drawable.button_go));
            zheads.add(model);
            ztorso.addModel(model);
        }*/

        for(int i = 0; i < zheads.size(); i++)
        {
            zheads.get(i).newIdentity();
            zheads.get(i).setPosition(new Geometry.Point(1f + (i * 2.0f), 2.0f, 0.0f));
            zheads.get(i).rotate(boxRotationAngle, 0.0f, 1.0f, 0.0f);
            zheads.get(i).setScale(0.1f);
        }

        renderer.addGroup(ztorso);

        //renderer.addModel(sniper);
        renderer.addModel(box);
    //    renderer.addModel(zombiehead);
        renderer.addLight(TEST_LIGHT);
        renderer.addLight(TEST_LIGHT2);
        renderer.addLight(muzzleFlareLight);
        renderer.addLight(muzzleFlareLight);
        player.addToRenderer(renderer);

        renderer.addGroup(bulletsGroup);

        demoMap.addToRenderer(renderer);

        uiRenderer = new UIRenderer();
        debugViewUIGroup = new UIRendererGroup(uiRenderer, debugView);
        healthbarsUIGroup = new UIRendererGroup(uiRenderer);

        for(Zombie z : zombies)
        {
            z.addToRenderer(renderer, healthbarsUIGroup);
        }

        musicQueue[0] = R.raw.button_click;
        musicQueue[1] = R.raw.button_click;

        final Random MUSIC_RANDOMIZER = new Random();
        MUSIC_SELECTION = MUSIC_RANDOMIZER.nextInt(2);
    }

    @Override
    protected void surfaceCreated()
    {
        shader.onSurfaceCreated();
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

    //    playMusic(context, R.raw.zombies);
        switch(MUSIC_SELECTION)
        {
            case 0:
               // playMusic(context, R.raw.gameplay_decay);
                break;
            case 1:
               // playMusic(context, R.raw.gameplay_deep_noise);
                break;
        }
    }

    private Text text;
    @Override
    public void surfaceChanged(int width, int height) {
        viewWidth = width;
        viewHeight = height;
        camera.viewChanged(width, height);
        debugCamera.viewChanged(width, height);

        uiRenderer.setCanvasSize(width, height);
        uiShader = new TextureShaderProgram(context);
        uiShader.onSurfaceCreated();
        uiRenderer.setShader(uiShader);

        colourShader = new ColourShaderProgram(context);
        healthbarsUIGroup.setShader(colourShader);

        // If Debug Enabled
        // Camera Text
        Font font = new Font(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        if(debugView)
        {
            cameraText = new Text("Debug Camera", 2, font, true);
        }
        else
        {
            cameraText = new Text("Birds Eye Camera", 2, font, true);
        }

        cameraText.setPosition(new Geometry.Point(0.0f, height - cameraText.getHeight(), 0.0f));
        uiRenderer.addUI(cameraText);

        initUI();

        shader = new PerFragMultiLightingShader(context);
        renderer.setShader(shader);
        shader.onSurfaceCreated();

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
/*
        zombiehead.newIdentity();
        zombiehead.setPosition(new Geometry.Point(1f, 2.0f, 0.0f));
        zombiehead.rotate(boxRotationAngle, 0.0f, 1.0f, 0.0f);
        zombiehead.setScale(0.1f);*/

        renderer.render();

        colourShader.useProgram();

        for(int i = 0; i < zombies.size(); i++)
        {
            if(zombies.get(i).isAlive())
            {
                if (debugView) {
                    zombies.get(i).updateHealthbar(debugCamera, uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight());
                } else {
                    zombies.get(i).updateHealthbar(camera, uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight());
                }
            }
            else
            {
                // The zombie is dead so remove it from processing
                zombies.remove(i);
                i--;
            }
        }

        uiRenderer.render();
    }

    // Position Lights (Rotate around a point)
    private float[] mLightModelMatrix = new float[16];
    private float[] mLightPosInWorldSpace = new float[4];
    private float[] mLightPosInEyeSpace = new float[4];
    private float[] mLightPosInModelSpace = { 0.0f, 0.0f, 0.0f, 1.0f };
    private float lightRotationAngle = 0.0f;
    private float[] lightInModelSpace = new float[4];
    private void positionLight(float z) {
        setIdentityM(mLightModelMatrix, 0);
        translateM(mLightModelMatrix, 0, 7.0f, 1.0f, 4.0f);
        rotateM(mLightModelMatrix, 0, lightRotationAngle, 0.0f, 1.0f, 0.0f);
        translateM(mLightModelMatrix, 0, 0.0f, 0.0f, z);
        multiplyMV(lightInModelSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        if (debugView) {
            multiplyMV(mLightPosInEyeSpace, 0, debugCamera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        } else {
            multiplyMV(mLightPosInEyeSpace, 0, camera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        }
    }

    private void positionLight(Geometry.Point point) {
        System.out.println("POSITIONING");
        setIdentityM(mLightModelMatrix, 0);
        translateM(mLightModelMatrix, 0, point.x, point.y, point.z);
        multiplyMV(lightInModelSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        if (debugView) {
            multiplyMV(mLightPosInEyeSpace, 0, debugCamera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        } else {
            multiplyMV(mLightPosInEyeSpace, 0, camera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        }
    }

    private boolean updateDoor = false;

    @Override
    public void update(float deltaTime) {
    //    System.out.println("DEBUG CAM POS: " + debugCamera.getPosition());
    //    System.out.println("LOOKING AT: " + debugCamera.getHorizontalAngle() + ", " + debugCamera.getVerticalAngle());
    ////    System.out.println("HUMAN: " + player.getPosition());

        /*for(int i = 0; i < 5; i++)
        {
            System.out.println("At [" + i + "]: " + mActivePointers.get(mActivePointers.keyAt(i), new PointF(0.0f, 0.0f)));
        }*/
/*        if(!spawnedBullet)
        {

            spawnedBullet = true;
            Bullet temp = new Bullet(context, humanoid.getPosition().x,
                    humanoid.getPosition().y, new Geometry.Vector(1.0f, 0.0f, 0.0f),
                    0.01f, 500000.0f);

            // temp.setScale(0.05f);
            bullets.add(temp);
            renderer.addModel(temp);

        }*/

        moveController.update(deltaTime);
        aimController.update(deltaTime);

        Geometry.Vector moveVector = moveController.getDirection();
        player.setVelocity(new Geometry.Vector(moveVector.x, 0.0f, -moveVector.y));

        if(debugView)
        {
            debug_camera_button_up.update(deltaTime);
            debug_camera_button_down.update(deltaTime);
        }
        switch_weapon_button.update(deltaTime);
        switch_camera_button.update(deltaTime);
        wave_button.update(deltaTime);
        buy_door_button.update(deltaTime);

        if(cameraTextTimer > 0.0f)
        {
            cameraTextTimer -= CAMERA_INFO_TEXT_FADEOUT_SPEED * deltaTime;
            if(cameraTextTimer < 0.0f)
            {
                cameraTextTimer = 0.0f;
            }

            cameraText.setColour(new Colour(1.0f, 0.0f, 0.0f, cameraTextTimer));
        }

        // Update zombies
        for(Zombie zombie : zombies)
        {
            zombie.update(deltaTime);
        }

        player.update(deltaTime);
        player.checkTileCollisions();

        // This part pushes the zombie away from other zombies, players and walls
        // Calculate how far the zombie is to the player
        for(Zombie z : zombies)
        {
            z.checkHumanoidCollision(player);
            z.checkTileCollisions();

            for(Zombie other : zombies)
            {
                if(z != other)
                {
                    z.checkHumanoidCollision(other);
                }
            }
        }

        final ArrayList<Door> DOORS = demoMap.getDoors();
        showBuyUI = false;
        for(Door door : DOORS)
        {
            for(Zombie zombie : zombies)
            {
                zombie.checkDoorCollision(door);
            }

            if(player.checkDoorCollision(door))
            {
                // Show buy icon
                showBuyUI = true;
            }

            door.update(deltaTime);
        }

        if(showBuyUI)
        {
            // Activate the buy UI
            if(buyDoorButtonInRenderer == false)
            {
                uiRenderer.addUI(buy_door_button);
                buyDoorButtonInRenderer = true;
            }
        }
        else
        {
            // De-activate the buy UI
            if(buyDoorButtonInRenderer)
            {
                uiRenderer.removeUI(buy_door_button);
                buyDoorButtonInRenderer = false;
            }
        }

        // Check if the player interacts with weapons on the floor
     //   for(Weapon weapons : demoMap.getWeapons())
      //  {
      //      if(weapons)
     //   }

        Weapon weaponPickup = demoMap.weaponPickupCollision(player.getHitbox());
        if(weaponPickup != null)
        {
            if(player.getCurrentWeapon() != weaponPickup.getType())
            {
                System.out.println("Collided with weapon pick-up, switching weapon to: " + weaponPickup.getType());
                player.switchWeapon(weaponPickup.getType());
              //  audio.playSound(R.raw.pickup, 1);
            }
        }

        // Update bullets
        for (int n = 0; n < bullets.size(); n++) {
            Bullet bullet = bullets.get(n);

            bullet.update(deltaTime);

            // todo: Check the bullet collisions with zombies
            for(Zombie zombie : zombies)
            {
                if(bullet.collidesWith(zombie))
                {
                    zombie.damage(bullet.getDamage());
                    bullet.endLife();

                    if(bullet.getType() == Bullet.BulletType.FISTS)
                    {
                      //  audio.playSound(R.raw.temp_punch_hit);
                    }
                    else {
                      //  audio.playSound(R.raw.hit, 1);
                    }

                    if(!zombie.isAlive())
                    {
                     //   audio.playSound(R.raw.zombie_hit);
                        zombie.removeFromRenderer(renderer, healthbarsUIGroup);
                    }
                }
            }

            // Check bullet collision with the map
            if(demoMap.checkCollision(bullet))
            {
                // Collision sound effect
                audio.playSound(R.raw.wood_collide);
                bullet.endLife();
            }

            // If the bullets have run out of life, remove them
            if (bullet.isAlive() == false) {
                bulletsGroup.removeModel(bullet.getModel());
                bullets.remove(n--);
            }
        }

     //   sniper.setPosition(new Geometry.Point(humanoid.getPosition().x, humanoid.getPosition().y, humanoid.getPosition().z));
      //  sniper.setPosition(new Geometry.Point(humanoid.getPosition().x, 1.0f, 1.0f));

   //     sniper.newIdentity();
    //    sniper.setPosition(new Geometry.Point(3.0f, 0.1f, 4.0f));
    //    sniper.setScale(0.1f);
    //    sniper.rotate(angleS, 0.0f, 1.0f, 0.0f);
        camera.setPosition(new Geometry.Point(player.getPosition().x, CAMERA_START_POSITION.y, player.getPosition().z));

        boxRotationAngle += 1f * deltaTime;
        lightRotationAngle += 0.5f * deltaTime;
        //positionLight(5.0f);
       // TEST_LIGHT.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
       //         mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));
        positionLight(-5.0f);
        TEST_LIGHT2.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
                 mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));

        if(muzzleFlareProcess)
        {
            muzzleFlareLightTimer += deltaTime;

            if(muzzleFlareLightTimer >= 5.0f)
            {
                muzzleFlareLight.setAmbienceIntensity(0.0f);
                muzzleFlareLight.setMaxAmbience(0.0f);
                muzzleFlareLightTimer = 0.0f;
                muzzleFlareProcess = false;
            }
        }
    }

    // Process touch
    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent event)
    {
        switch(event){
            case CLICK:
                // Look through the UI and check if the pointer interacts with them
                if(handlePointerControl(switch_camera_button, pointer)) { return; }
                if(handlePointerControl(switch_weapon_button, pointer)) { return; }
                if(handlePointerControl(wave_button, pointer)) { return; }
                if(handlePointerControl(moveController, pointer)) { return; }
                if(handlePointerControl(aimController, pointer)) { return; }
                if(showBuyUI && handlePointerControl(buy_door_button, pointer)) { return; }
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

    private boolean handlePointerControl(Button button, Pointer pointer)
    {
        if(button.interacts(pointer))
        {
            pointer.setControlOver(button);
            return true;
        }

        return false;
    }


    private void initUI()
    {
        Texture hotbar_texture = TextureHelper.loadTexture(context, R.drawable.hotbar_scaled, true);
        hotbar = new Image(new Dimension2D((viewWidth / 2.0f) - (hotbar_texture.getWidth() / 2.0f), 10.0f,
                hotbar_texture.getWidth(), hotbar_texture.getHeight()), hotbar_texture);

        crosshair = new Image(
                new Dimension2D((viewWidth/2.0f) - 25, (viewHeight/2.0f) - 25, 50, 50),
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
                            player.setVelocity(new Geometry.Vector(velocity.x, 0.0f, -velocity.y));
                        }
                        break;
                    case RELEASE:
                        player.setVelocity(new Geometry.Vector(0.0f, 0.0f, 0.0f));
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
                        // Fetch the direction from the joystick, divide by magnitude to get the
                        // unit vector.
                        Geometry.Vector unitVectorDirection = aimController.getDirection().
                                scale(1.0f / aimController.getDirection().length());

                        // Scale the unit vector by the cross-hair offset to set distance from the
                        // player
                        Geometry.Vector offset = unitVectorDirection.scale(CROSSHAIR_OFFSET);

                        // Set the cross-hair position
                        crosshair.setPosition(new Geometry.Point((viewWidth/2.0f) - 25 + offset.x,
                                (viewHeight/2.0f) - 25 + offset.y, 0.0f));

                        // Tell the player that the user has used the fire action
                        if(player.fireAction(unitVectorDirection))
                        {
                            muzzleFlareProcess = true;
                            muzzleFlareLight.setAmbienceIntensity(0.02f);
                            muzzleFlareLight.setMaxAmbience(10.0f);
                            positionLight(player.getPosition());
                            muzzleFlareLight.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
                                    mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));
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

        wave_button = new Button(new Dimension2D(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
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
                        player.setWaving(!player.isWaving());

                        final ArrayList<Door> doors = demoMap.getDoors();
                        for(Door door : doors)
                        {
                            door.setOpen(false);
                        }
                        break;
                }
            }
        });

        switch_camera_button = new Button(
                new Dimension2D(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
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
                            player.setVelocity(new Geometry.Vector(0.0f, 0.0f, 0.0f));
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

        switch_weapon_button = new Button(
                new Dimension2D(viewWidth - BUTTON_PADDING - BUTTON_SIZE - BUTTON_PADDING -
                        BUTTON_SIZE,
                        viewHeight - BUTTON_SIZE - BUTTON_PADDING,
                        BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_switch_weapon));
        switch_weapon_button.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        player.switchWeaponTemp();
                        break;
                }
            }
        });

        debug_camera_button_up = new Button(
                new Dimension2D(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
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

        debug_camera_button_down = new Button(
                new Dimension2D(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
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

        buy_door_button = new Button(new Dimension2D(viewWidth - BUTTON_PADDING - BUTTON_SIZE - BUTTON_PADDING -
                BUTTON_SIZE - BUTTON_PADDING - BUTTON_SIZE,
                viewHeight - BUTTON_SIZE - BUTTON_PADDING,
                BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_buy_door));
        buy_door_button.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        final ArrayList<Door> DOORS = demoMap.getDoors();
                        for(Door door : DOORS)
                        {
                            if(player.checkDoorCollision(door))
                            {
                                door.setOpen(true);
                               // playSound(context, R.raw.buyitem, 0);
                               // playSound(context, R.raw.doorsound, 0);
                            }
                        }
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
        uiRenderer.addUI(switch_weapon_button);
        uiRenderer.addUI(wave_button);
        uiRenderer.addRendererGroup(healthbarsUIGroup);
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
