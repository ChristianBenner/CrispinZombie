package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.data.objects.Model;
import com.christianbenner.crispinandroid.data.objects.RendererModel;
import com.christianbenner.crispinandroid.data.objects.Square;
import com.christianbenner.crispinandroid.programs.ChangingColourShaderProgram;
import com.christianbenner.crispinandroid.programs.ColourShaderProgram;
import com.christianbenner.crispinandroid.programs.PerFragLightingShader;
import com.christianbenner.crispinandroid.programs.PerFragLightingTextureShader;
import com.christianbenner.crispinandroid.programs.PerFragMultiLightingShader;
import com.christianbenner.crispinandroid.programs.PerVertexLightingShader;
import com.christianbenner.crispinandroid.programs.PerVertexLightingTextureShader;
import com.christianbenner.crispinandroid.ui.GLButtonOld;
import com.christianbenner.crispinandroid.ui.GLFont;
import com.christianbenner.crispinandroid.ui.GLText;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;
import com.christianbenner.crispinandroid.util.Camera;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Light;
import com.christianbenner.crispinandroid.util.Renderer;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.Map;
import com.christianbenner.zombie.R;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Christian Benner on 08/01/2018.
 */

public class SceneGameTest extends Scene{
    enum GRAPHIC_SETTINGS
    {
        LOW,
        HIGH
    }

    private GRAPHIC_SETTINGS settings;

    // Constants
    private final int BUTTON_PADDING = 20;
    private final int BUTTON_SIZE = 150;
    private final float BUTTON_MOVEMENT = 0.025f;
    private final float MINIMUM_Y = -0.5f;
    private final float MINIMUM_X = 0.0f;

    private Context context;

    // Objects
    private Map map;
   // private Cube cube;
   // private LFModel cubetex;
    private Model cube;
   // private LVModel monkey;
    private Model sphere;
    private Model torus;
    private Square ground;
    private GLText version;
    private GLText fpsText;
    int frames = 0;
    long startTime = 0;

    // Textures
    private Texture textureButtonLeft;
    private Texture textureButtonRight;
    private Texture textureButtonUp;
    private Texture textureButtonDown;
    private Texture repeatedGrassTexture;
    private Texture cubetexTexture;
    private Texture stone;

    // Shaders
    private ChangingColourShaderProgram colourShaderProgram;
    private PerFragLightingShader colourLightingShader;
    private PerVertexLightingShader colourLightingShader2;
    private ColourShaderProgram lineShader;
    private PerVertexLightingTextureShader textureLightingShader;
    private PerFragLightingTextureShader pfltShader;
    private PerFragLightingShader pflShader;
    private PerFragMultiLightingShader TEST_SHADER;

    // MATRIX
    private Camera camera;

    private GLButtonOld uiButtonLeft;
    private GLButtonOld uiButtonRight;
    private GLButtonOld uiButtonUp;
    private GLButtonOld uiButtonDown;
    private GLButtonOld uiButtonZoomIn;
    private GLButtonOld uiButtonZoomOut;

    private Light sunlight;
    private Light cameraLight;
    private Light TEST_LIGHT;
    private Light TEST_LIGHT2;
    private Light TEST_LIGHT3;
    private ArrayList<Light> lights = new ArrayList<>();
    private Texture texture_ship;
    private Texture texture_box;
    private Texture texture_grass;

    private int viewWidth = 0;
    private int viewHeight = 0;
    private final float UI_LEFT_TOP_MARGIN = BUTTON_PADDING + BUTTON_PADDING + BUTTON_SIZE +
            BUTTON_SIZE + BUTTON_PADDING;
    private final float UI_LEFT_RIGHT_MARGIN = BUTTON_PADDING + BUTTON_PADDING + BUTTON_SIZE +
            BUTTON_SIZE + BUTTON_PADDING;
    private Light testLight = new Light();

    private Renderer TEST_RENDERER;
    private RendererModel TEST_MODEL_1;
    private RendererModel TEST_MODEL_2;
    private RendererModel TEST_MODEL_3;
    private RendererModel TEST_MODEL_4;
    private RendererModel TEST_MODEL_5;

    public SceneGameTest(Context context)
    {
        this.context = context;
        this.settings = GRAPHIC_SETTINGS.HIGH;
       // this.camera = null;
        camera = new Camera();

        TEST_MODEL_1 = new RendererModel(context, R.raw.box);
        TEST_MODEL_2 = new RendererModel(context, R.raw.box);
        TEST_MODEL_3 = new RendererModel(context, R.raw.box);
        TEST_MODEL_4 = new RendererModel(context, R.raw.tile);
        TEST_MODEL_5 = new RendererModel(context, R.raw.box);

        TEST_MODEL_5.setColour(new Colour(0.2f, 0.2f, 0.2f));
        TEST_MODEL_5.setScale(3.0f);

        TEST_LIGHT = new Light();
        TEST_LIGHT2 = new Light();
        TEST_LIGHT3 = new Light();

        TEST_RENDERER = new Renderer(new PerFragMultiLightingShader(context), camera);
        TEST_RENDERER.addModel(TEST_MODEL_1);
        TEST_RENDERER.addModel(TEST_MODEL_2);
        TEST_RENDERER.addModel(TEST_MODEL_3);
        TEST_RENDERER.addModel(TEST_MODEL_4);
        TEST_RENDERER.addModel(TEST_MODEL_5);
        TEST_RENDERER.addLight(TEST_LIGHT);
        TEST_RENDERER.addLight(TEST_LIGHT2);
        TEST_RENDERER.addLight(TEST_LIGHT3);

        this.cameraUpdated = true;
    }

    @Override
    public void surfaceCreated() {
        System.err.println("surface created");
        // wip
        lineXVertexArray = new VertexArray(lineData);
        lineData[3] = 0.0f;
        lineData[4] = 1.0f;
        lineYVertexArray = new VertexArray(lineData);
        lineData[4] = 0.0f;
        lineData[5] = 1.0f;
        lineZVertexArray = new VertexArray(lineData);
        lineShader = new ColourShaderProgram(context);

        // Setup shader
        colourShaderProgram = new ChangingColourShaderProgram(context);
    //    colourLightingShader = new PerFragLightingShader(context);
        colourLightingShader2 = new PerVertexLightingShader(context);
        textureLightingShader = new PerVertexLightingTextureShader(context);
        pfltShader = new PerFragLightingTextureShader(context);
        pflShader = new PerFragLightingShader(context);
        TEST_SHADER = new PerFragMultiLightingShader(context);

        lights.add(TEST_LIGHT);
        lights.add(TEST_LIGHT2);
        TEST_SHADER.setLights(lights);

    //    lights.add(testLight);
      //  pflShader.setLights(lights);
     //   pfltShader.setLights(lights);
   //     pfltShader.setLights(lights);

        sunlight = new Light();
        sunlight.setAttenuation(0.8f);
        sunlight.setMaxAmbience(0.8f);
        sunlight.setAmbienceIntensity(30.0f);
        sunlight.setColour(new Colour(253f/255f, 184f/255f, 19f/255f));
    //    lights.add(sunlight);

        cameraLight = new Light();
        cameraLight.setColour(new Colour(0.0f, 0.0f, 1.0f));
        cameraLight.setMaxAmbience(1.5f);
        cameraLight.setAttenuation(0.0f);
        cameraLight.setAmbienceIntensity(30.0f);
   //     lights.add(cameraLight);

    //   tile.setLights(lights);
     //   tile.setTexture(R.drawable.stone_tile);

        // Set up objects
   //     monkey = new ShadedModel(context, ShaderType.L_FRAG, R.raw.monkey,
  //              Model.AllowedData.VERTEX_TEXEL_NORMAL);
  //      monkey.setPosition(new Geometry.Point(7.0f, 2.0f, -3.0f));
  //      monkey.setColour(new Colour(0.0f, 1.0f, 0.0f));
  //      monkey.setLights(lights);



       // sphere = new Model(context, R.raw.sphere);
      //  sphere.setPosition(new Geometry.Point(9.5f, 2.0f, -3.0f));
      //  sphere.setColour(new Colour(0.0f, 0.0f, 1.0f));
      //  torus = new Model(context, R.raw.torus);
      //  torus.setPosition(new Geometry.Point(4.0f, 2.0f, -3.0f));
      //  torus.setColour(new Colour(1.0f, 0.0f, 0.0f));
        ground = new Square();

        map = new Map(this.context, R.raw.demo_map);

        // Setup text
        GLFont font = new GLFont(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        version = new GLText(context, "Version " + Constants.VERSION_STRING, 1, font, 1920f, 1080f, 1.0f);
        version.setPosition(-1.0f, -1.0f);
        version.setColour(new Colour(1.0f, 0.0f, 0.0f));

        fpsText = new GLText(context, "FPS: x", 2, font, 1.0f, 1920f, 1080f, true);
        fpsText.setColour(new Colour(1.0f, 0.0f, 0.0f, 0.5f));
        fpsText.setPosition(-1.0f, -1.0f);

        // Setup textures
        textureButtonLeft = TextureHelper.loadTexture(context, R.drawable.button_left);
        textureButtonRight = TextureHelper.loadTexture(context, R.drawable.button_right);
        textureButtonUp = TextureHelper.loadTexture(context, R.drawable.button_up);
        textureButtonDown = TextureHelper.loadTexture(context, R.drawable.button_down);
        repeatedGrassTexture = TextureHelper.loadTexture(context, R.drawable.grass_tile);
     //   cubetexTexture = TextureHelper.loadTexture(context, R.drawable.cubetex);
        stone = TextureHelper.loadTexture(context, R.drawable.stone_tile);

        // Setup UI
        uiButtonLeft = new GLButtonOld(context);
        uiButtonLeft.setTexture(textureButtonLeft);
        uiButtonLeft.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e, PointF position) {
                switch (e.event)
                {
                    case DOWN:
                        camera.translate(-BUTTON_MOVEMENT, Camera.Direction.RIGHT);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        uiButtonRight = new GLButtonOld(context);
        uiButtonRight.setTexture(textureButtonRight);
        uiButtonRight.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e, PointF position) {
                switch (e.event)
                {
                    case DOWN:
                        camera.translate(BUTTON_MOVEMENT, Camera.Direction.RIGHT);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        uiButtonUp = new GLButtonOld(context);
        uiButtonUp.setTexture(textureButtonUp);
        uiButtonUp.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e, PointF position) {
                switch (e.event)
                {
                    case DOWN:
                        camera.translate(BUTTON_MOVEMENT, Camera.Direction.UP);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        uiButtonDown = new GLButtonOld(context);
        uiButtonDown.setTexture(textureButtonDown);
        uiButtonDown.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e, PointF position) {
                switch (e.event)
                {
                    case DOWN:
                        camera.translate(-BUTTON_MOVEMENT, Camera.Direction.UP);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        uiButtonZoomIn = new GLButtonOld(context);
        uiButtonZoomIn.setTexture(textureButtonUp);
        uiButtonZoomIn.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e, PointF position) {
                switch (e.event)
                {
                    case DOWN:
                        camera.translate(BUTTON_MOVEMENT, Camera.Direction.FORWARD);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        uiButtonZoomOut = new GLButtonOld(context);
        uiButtonZoomOut.setTexture(textureButtonDown);
        uiButtonZoomOut.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e, PointF position) {
                switch (e.event)
                {
                    case DOWN:
                        camera.translate(-BUTTON_MOVEMENT, Camera.Direction.FORWARD);
                        break;
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        uiButtonLeft.setDimensions(BUTTON_PADDING, BUTTON_PADDING + BUTTON_SIZE,
                BUTTON_SIZE, BUTTON_SIZE, viewWidth, viewHeight);
        uiButtonRight.setDimensions(BUTTON_PADDING + BUTTON_SIZE + BUTTON_SIZE,
                BUTTON_PADDING + BUTTON_SIZE,
                BUTTON_SIZE, BUTTON_SIZE, viewWidth, viewHeight);
        uiButtonZoomIn.setDimensions(BUTTON_PADDING + BUTTON_SIZE,
                BUTTON_PADDING + BUTTON_SIZE + BUTTON_SIZE,
                BUTTON_SIZE, BUTTON_SIZE, viewWidth, viewHeight);
        uiButtonZoomOut.setDimensions(BUTTON_PADDING + BUTTON_SIZE,
                BUTTON_PADDING, BUTTON_SIZE, BUTTON_SIZE, viewWidth, viewHeight);
        uiButtonUp.setDimensions(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
                BUTTON_PADDING + BUTTON_PADDING + BUTTON_SIZE,
                BUTTON_SIZE, BUTTON_SIZE, viewWidth, viewHeight);
        uiButtonDown.setDimensions(viewWidth - BUTTON_PADDING - BUTTON_SIZE,
                BUTTON_PADDING, BUTTON_SIZE, BUTTON_SIZE, viewWidth, viewHeight);

       // playMusic(context, R.raw.epilogue);

      //  texture_ship = TextureHelper.loadTexture(context, R.drawable.sh3);
        texture_box = TextureHelper.loadTexture(context, R.drawable.box);
        texture_grass = TextureHelper.loadTexture(context, R.drawable.grass_tile);

        TEST_MODEL_1.setTexture(texture_box);
        TEST_MODEL_2.setTexture(texture_box);
        TEST_MODEL_3.setTexture(texture_box);
        TEST_MODEL_4.setTexture(texture_grass);
        TEST_MODEL_5.setTexture(texture_ship);
    }

    @Override
    public void surfaceChanged(int width, int height) {
        System.out.println("SURFACE CHANGE");

        viewWidth = width;
        viewHeight = height;

        camera.viewChanged(width, height);
        cameraUpdated = true;
        TEST_RENDERER.setCamera(camera);
        TEST_RENDERER.setShader(new PerFragMultiLightingShader(context));
    }

    boolean cameraUpdated = false;
    float angle = 0.0f;
    float[] modelViewMatrix = new float[16];

    float[] mLightModelMatrix = new float[16];
    float[] mLightPosInWorldSpace = new float[4];
    float[] mLightPosInEyeSpace = new float[4];
    float[] mLightPosInModelSpace = { 0.0f, 0.0f, 0.0f, 1.0f };

    float[] mTileModelMatrix = new float[16];
    float[] mMVPMatrix = new float[16];
    float[] mMVMatrix = new float[16];

    private VertexArray lightArray = new VertexArray(mLightPosInModelSpace);

    private void drawLight(float translateZ)
    {
        lineShader.useProgram();
        lightArray = new VertexArray(mLightPosInModelSpace);
        lightArray.setVertexAttribPointer(
                0,
                lineShader.getPositionAttributeLocation(),
                4,
                0
        );

        setIdentityM(mLightModelMatrix, 0);
        translateM(mLightModelMatrix, 0, 2.0f, 0.0f, 0.0f);
        rotateM(mLightModelMatrix, 0, angle, 0.0f, 1.0f, 0.0f);
        translateM(mLightModelMatrix, 0, 0.0f, 0.0f, translateZ);
        float[] lightInModelSpace = new float[4];
        multiplyMV(lightInModelSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        testLight.setPosition(new Geometry.Point(mLightPosInModelSpace[0], mLightPosInWorldSpace[1], mLightPosInWorldSpace[2]));
        multiplyMV(mLightPosInEyeSpace, 0, camera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        multiplyMM(mMVMatrix, 0, camera.getViewMatrix(), 0, mLightModelMatrix, 0);
        multiplyMM(mMVPMatrix, 0, camera.getProjectionMatrix(), 0, mMVMatrix, 0);

        lineShader.setUniforms(mMVPMatrix,
                1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 0, 1);
    }

    private void drawLight(float x, float y, float z)
    {
        lineShader.useProgram();
        lightArray = new VertexArray(mLightPosInModelSpace);
        lightArray.setVertexAttribPointer(
                0,
                lineShader.getPositionAttributeLocation(),
                4,
                0
        );

        setIdentityM(mLightModelMatrix, 0);
        translateM(mLightModelMatrix, 0, x, y, z);
        float[] lightInModelSpace = new float[4];
        multiplyMV(lightInModelSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        testLight.setPosition(new Geometry.Point(mLightPosInModelSpace[0], mLightPosInWorldSpace[1], mLightPosInWorldSpace[2]));
        multiplyMV(mLightPosInEyeSpace, 0, camera.getViewMatrix(), 0, mLightPosInWorldSpace, 0);
        multiplyMM(mMVMatrix, 0, camera.getViewMatrix(), 0, mLightModelMatrix, 0);
        multiplyMM(mMVPMatrix, 0, camera.getProjectionMatrix(), 0, mMVMatrix, 0);

        lineShader.setUniforms(mMVPMatrix,
                1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 0, 1);
    }

    @Override
    public void draw() {
        if(cameraUpdated)
        {
            cameraUpdated = false;

        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_ALPHA);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

       // lineShader.useProgram();
       // ground.bindData(lineShader);
      //  positionFloorInScene();
      //  lineShader.setUniforms(modelViewProjectionMatrix,
      //          53.0f / 255.0f, 73.0f / 255.0f, 10.0f / 255.0f, 1.0f);
      //  ground.draw();

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        cameraLight.setPosition(camera.getPosition());


        ////////////////////////////////////////////////////////////////////////////////////////////

        drawLight(2.0f);
        TEST_LIGHT.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
                mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));

        drawLight(-2.0f);
        TEST_LIGHT2.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
                mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));

        drawLight(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        TEST_LIGHT3.setPosition(new Geometry.Point(mLightPosInEyeSpace[0],
                mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]));
        angle += 0.5f;

      /*  TEST_SHADER.useProgram();
        tile.bindData(TEST_SHADER);

        for(int z = 0; z < 2; z++)
        {
            for(int x = 0; x < 2; x++)
            {
                setIdentityM(mTileModelMatrix, 0);
                translateM(mTileModelMatrix, 0, x * 2.0f, 0.0f, z * 2.0f);
                multiplyMM(mMVMatrix, 0, camera.getViewMatrix(), 0, mTileModelMatrix, 0);
                multiplyMM(mMVPMatrix, 0, camera.getProjectionMatrix(), 0, mMVMatrix, 0);
                TEST_SHADER.setUniforms(
                        mMVMatrix,
                        mMVPMatrix,
                        1.0f, 0.0f, 0.0f
                );

                tile.draw();
            }
        }*/

        TEST_RENDERER.render();

        ////////////////////////////////////////////////////////////////////////////////////////////

/*
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0.0f, 0.0f, 0.0f);
        multiplyMV(lightPosInSpace, 0, modelMatrix, 0, lightPos, 0);
        multiplyMV(lightPosInEyeSpace, 0, camera.getViewMatrix(), 0, lightPosInSpace, 0);

        colourLightingShader2.useProgram();
        tile.bindData(colourLightingShader2);
        for(int z = 0; z < 2; z++)
        {
            for(int x = 0; x < 2; x++)
            {
                setIdentityM(modelMatrix, 0);
                translateM(modelMatrix, 0, x * 2.0f, 0.0f, z * 2.0f);
     //           rotateM(modelMatrix, 0, angle, 1.0f, 1.0f, 1.0f);

                multiplyMM(modelViewMatrix, 0, camera.getViewMatrix(),
                        0, modelMatrix, 0);
                multiplyMM(modelViewProjectionMatrix, 0, camera.getProjectionMatrix(),
                        0, modelViewMatrix, 0);
                colourLightingShader2.setUniforms(modelViewMatrix, modelViewProjectionMatrix,
                    //    lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2],
                        0.0f, 0.0f, 0.0f,
                        1.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 0.0f);
                tile.draw();
            }
        }
     //   angle += 0.1f;*/

/*
        pfltShader.useProgram();
        tile.bindData(pfltShader);
        for(int z = 0; z < 10; z++)
        {
            for(int x = 0; x < 10; x++)
            {
                setIdentityM(modelMatrix, 0);
                translateM(modelMatrix, 0, x * 2.0f, 0.0f, z * 2.0f);
                multiplyMM(modelViewProjectionMatrix, 0, camera.getViewProjectionMatrix(),
                        0, modelMatrix, 0);
                pfltShader.setUniforms(modelMatrix, modelViewProjectionMatrix,
                        stone.getTextureId(), 1.0f, 1.0f, 1.0f);
                tile.draw();
            }
        }
*/

   /*     box.setScale(0.5f);
        box.setLightPosition(camera.getPosition());
        box.setLightColour(new Colour(1.0f, 0.0f, 0.0f));
        box.setAmbientDistance(30);
        box.setPosition(new Geometry.Point(2.0f, 0.5f, -2.0f));
        box.setMinAmbience(ShaderConstants.DEFAULT_MIN_AMBIENT);
        box.render(camera);
        box.setLightColour(new Colour(0.0f, 1.0f, 0.0f));
        box.setPosition(new Geometry.Point(4.0f, 0.5f, -2.0f));
        box.setAmbientDistance(2f);
        box.setMinAmbience(0.0f);
        box.render(camera);

        ship.setLightPosition(camera.getPosition());
        ship.setPosition(0.0f, 0.0f, 0.0f);
        ship.rotate(-90.0f, 0.0f, 1.0f, 0.33f);
        ship.setPosition(0.0f, 3.0f, -5.0f);
        ship.render(camera);
        ship.setPosition(4.0f, 3.0f, -5.0f);
        ship.render(camera);
        ship.setPosition(8.0f, 3.0f, -5.0f);
        ship.render(camera);
        ship.setPosition(12.0f, 3.0f, -5.0f);
        ship.render(camera);*/

        // Render UI
        glDisable(GL_DEPTH_TEST);
      //  renderLines(); // DEBUG
        uiButtonLeft.render();
        uiButtonRight.render();
        uiButtonUp.render();
        uiButtonDown.render();
        uiButtonZoomIn.render();
        uiButtonZoomOut.render();
      //  version.render();

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        frames++;
        if (System.currentTimeMillis() - startTime >= 1000) {
            fpsText.setText("FPS: " + frames);
            frames = 0;
            startTime = System.currentTimeMillis();
        }
      //  fpsText.render();
    }

    float hangle = 3.14f;
    @Override
    public void update(float deltaTime) {
        //camera.setLookAt(0.0f, 0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
       // angle += 0.1f;
       // camera.rotateViewProjection(angle, 0.0f, 1.0f, 0.0f);
       // camera.setPosition(new Geometry.Point(-translateX, -translateY, -translateZ));
       // camera.translate(translateX, translateY, translateZ);
      //  camera.translate(0.001f, 0.001f, 0.0f);

        //hangle -= 0.01f;
        //camera.setAngles(hangle, 0.0f);
        //camera.setAngles(0.0f, 45.0f);
        uiButtonLeft.update(deltaTime);
        uiButtonRight.update(deltaTime);
        uiButtonUp.update(deltaTime);
        uiButtonDown.update(deltaTime);
        uiButtonZoomIn.update(deltaTime);
        uiButtonZoomOut.update(deltaTime);
    }

    @Override
    protected void pause() {

    }

    @Override
    protected void resume() {
      //  if(TEST_RENDERER != null)
      //  {
      //      TEST_RENDERER.onActivityResume();
      //  }
  //      camera = null;
    }

    @Override
    protected void restart() {

    }

    @Override
    protected void destroy() {

    }

    float xCam = 0.0f;
    float yCam = 0.0f;
    float tempX = 0.0f;
    float tempY = 0.0f;

    float previousX = 0.0f;
    float previousY = 0.0f;
    boolean doneYet = false;

    private int mActivePointerId;
    @Override
    public void motionEvent(View view, MotionEvent event)
    {
        int action = MotionEventCompat.getActionMasked(event);
        int index = MotionEventCompat.getActionIndex(event);
        int pointerId = event.getPointerId(index);

        int xPos = (int)MotionEventCompat.getX(event, index);
        int yPos = (int)MotionEventCompat.getY(event, index);
        float normalizedX = (xPos / (float) view.getWidth()) * 2 - 1;
        float normalizedY = -((yPos / (float) view.getHeight()) * 2 - 1);

        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                handleTouchPress(normalizedX, normalizedY, pointerId);
                break;
            case MotionEvent.ACTION_MOVE:
                // Detect what pointer is moving
                int pointerCount = event.getPointerCount();
                for(int i = 0; i < pointerCount; ++i)
                {
                    int movePointer = event.getPointerId(i);
                    normalizedX = (event.getX(i) / (float) view.getWidth()) * 2 - 1;
                    normalizedY = -((event.getY(i) / (float) view.getHeight()) * 2 - 1);

                    // i is pointerIndex
                    handleTouchDrag(normalizedX, normalizedY, movePointer);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                handleTouchRelease(normalizedX, normalizedY, pointerId);

                if(event.getPointerCount() == 1)
                {
                    uiButtonLeft.forceRelease();
                    uiButtonRight.forceRelease();
                    uiButtonUp.forceRelease();
                    uiButtonDown.forceRelease();
                    uiButtonZoomIn.forceRelease();
                    uiButtonZoomOut.forceRelease();
                    draggingScreen = false;
                    screenDragPointer = -1;
                }
                break;
        }
    }

    private boolean draggingScreen = false;
    private int screenDragPointer = -1;
    private void handleTouchPress(float x, float y, int pointer) {
        boolean buttonClicked = false;
        if(uiButtonLeft.sendClick(x, y, pointer)) { buttonClicked = true; }
        if(uiButtonRight.sendClick(x, y, pointer)) { buttonClicked = true; }
        if(uiButtonUp.sendClick(x, y, pointer)) { buttonClicked = true; }
        if(uiButtonDown.sendClick(x, y, pointer)) { buttonClicked = true; }
        if(uiButtonZoomIn.sendClick(x, y, pointer)) { buttonClicked = true; }
        if(uiButtonZoomOut.sendClick(x, y, pointer)) { buttonClicked = true; }

        if(buttonClicked == false && draggingScreen == false)
        {
            screenDragPointer = pointer;
            draggingScreen = true;
        }
    }

    private void handleTouchDrag(float x, float y, int pointer) {
        if(pointer == screenDragPointer)
        {
            if(doneYet == false)
            {
                previousX = x;
                previousY = y;
                doneYet = true;
            }

            tempX -= previousX - x;
            tempY -= previousY - y;
            previousX = x;
            previousY = y;
            camera.setAngles(3.14159265f - tempX, tempY);
        }

        // Don't send drag to buttons because I dont care if the user drags off the button
    }

    private void handleTouchRelease(float x, float y, int pointer) {
        uiButtonLeft.sendRelease(x, y, pointer);
        uiButtonRight.sendRelease(x, y, pointer);
        uiButtonUp.sendRelease(x, y, pointer);
        uiButtonDown.sendRelease(x, y, pointer);
        uiButtonZoomIn.sendRelease(x, y, pointer);
        uiButtonZoomOut.sendRelease(x, y, pointer);

        doneYet = false;
        if(draggingScreen && pointer == screenDragPointer)
        {
            draggingScreen = false;
            screenDragPointer = -1;
        }
    }

    private void renderLines()
    {
        lineShader.useProgram();
        bindXLine(lineShader);
        for(int z = 0; z < 10; z++)
        {
            for(int x = 0; x < 10; x++)
            {
                if((x % 2) == 0)
                {
                    lineShader.setUniforms(modelViewProjectionMatrix, 1.0f, 0.0f, 0.0f, 1.0f);
                }
                else
                {
                    lineShader.setUniforms(modelViewProjectionMatrix, 0.0f, 1.0f, 0.0f, 1.0f);
                }

                positionLine(1.0f * x, 0.0f, 1.0f * -z);
                drawLine();
            }
        }


        bindYLine(lineShader);
        for(int z = 0; z < 10; z++)
        {
            for(int y = 0; y < 10; y++)
            {
                if((y % 2) == 0)
                {
                    lineShader.setUniforms(modelViewProjectionMatrix, 1.0f, 0.0f, 0.0f, 1.0f);
                }
                else
                {
                    lineShader.setUniforms(modelViewProjectionMatrix, 0.0f, 1.0f, 0.0f, 1.0f);
                }

                positionLine(0.0f, 1.0f * y, 1.0f * -z);
                drawLine();
            }
        }

        bindZLine(lineShader);
        for(int x = 0; x < 10; x++)
        {
            for(int z = 0; z < 10; z++)
            {
                if((z % 2) == 0)
                {
                    lineShader.setUniforms(modelViewProjectionMatrix, 1.0f, 0.0f, 0.0f, 1.0f);
                }
                else
                {
                    lineShader.setUniforms(modelViewProjectionMatrix, 0.0f, 1.0f, 0.0f, 1.0f);
                }

                positionLine(1.0f * x, 0.0f, 1.0f * -z);
                drawLine();
            }
        }
    }

    // a line the width of one normalised device co-ordinate
    private float[] lineData = {
            // two points for a line
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
    };
    private VertexArray lineXVertexArray;
    private VertexArray lineYVertexArray;
    private VertexArray lineZVertexArray;
    private void bindXLine(ColourShaderProgram colourShaderProgram)
    {
        lineXVertexArray.setVertexAttribPointer(
                0,
                colourShaderProgram.getPositionAttributeLocation(),
                3,
                0);
    }

    private void bindYLine(ColourShaderProgram colourShaderProgram) {
        lineYVertexArray.setVertexAttribPointer(
                0,
                colourShaderProgram.getPositionAttributeLocation(),
                3,
                0);
    }

    private void bindZLine(ColourShaderProgram colourShaderProgram) {
        lineZVertexArray.setVertexAttribPointer(
                0,
                colourShaderProgram.getPositionAttributeLocation(),
                3,
                0);
    }

    private void drawLine()
    {
        glDrawArrays(GL_LINES, 0, 2);
    }

    // arrays of 16 floats
    private float[] modelMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];
    private void positionLine(float nX, float nY, float nZ)
    {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, nX, nY, nZ);
        multiplyMM(modelViewProjectionMatrix, 0, camera.getViewProjectionMatrix(),
                0, modelMatrix, 0);
    }
}
