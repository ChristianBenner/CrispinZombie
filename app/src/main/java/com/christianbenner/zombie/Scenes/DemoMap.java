package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.Matrix;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.shaders.PerFragLightingTextureShader;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.RendererGroup;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.ui.BaseController;
import com.christianbenner.crispinandroid.ui.Button;
import com.christianbenner.crispinandroid.ui.MoveController;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Map.Cell;
import com.christianbenner.zombie.Map.Map;
import com.christianbenner.zombie.R;

import java.util.HashMap;

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
import static android.opengl.Matrix.multiplyMM;
import static com.christianbenner.zombie.Constants.DEMO_MAP_ID;

public class DemoMap extends Scene {
    // The horizontal camera angle to look back (switch -Z to +Z)
    private static final float CAMERA_ANGLE_HORIZONTAL_DEGREES = 180.0f;
    private static final float CAMERA_ANGLE_VERTICAL_DEGREES = -90.0f;

    // Movement speed of the gameplay camera
    private static final float GAMEPLAY_CAMERA_MOVEMENT_SPEED = 0.04f;

    // How many cells to render around the center point (in each direction)
    private static final int RENDER_CHUNKS = 10;

    // The display center point in NDC
    private static final float[] NDC_CENTER = { 0.0f, 0.0f, 0.0f, 1.0f };

    // Game-play Rendering
    private Renderer gameplayRenderer;
    private PerFragLightingTextureShader gameplayShader;
    private Camera gameplayCamera;

    // UI Renderer
    private TextureShaderProgram uiShader;
    private UIRenderer uiRenderer;

    // The map
    private Map map;
    private final Cell[][] mapCells;

    // Movement Controller
    private BaseController baseMoveController;
    private MoveController moveController;

    // Aim Controller
    private BaseController baseAimController;
    private MoveController aimController;

    // Delta Time
    private float deltaTime = 1.0f;

    // Matrix Used in Map Rendering
    private float[] mvMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] modelMatrix = new float[16];

    // Map Rendering Optimisation Variables
    private float[] invertedVPMatrix = new float[16];
    private float[] worldspaceAtCenter = new float[4];

    public DemoMap(Context context)
    {
        super(context, DEMO_MAP_ID);

        // Create game-play camera
        gameplayCamera = new Camera();
        gameplayCamera.setAnglesDegrees(CAMERA_ANGLE_HORIZONTAL_DEGREES,
                CAMERA_ANGLE_VERTICAL_DEGREES);
        gameplayCamera.setPosition(new Geometry.Point(0.0f, 5.0f, 0.0f));

        // Create game-play shader
        gameplayShader = new PerFragLightingTextureShader(context);

        // Create game-play gameplayRenderer
        gameplayRenderer = new Renderer(gameplayShader, gameplayCamera);

        // Create the ui shader
        uiShader = new TextureShaderProgram(context);

        // Create the ui renderer
        uiRenderer = new UIRenderer();
        uiRenderer.setShader(uiShader);

        // Add the map to the gameplayRenderer
        map = new Map(context, R.raw.demo_map4);

        // Add the maps render groups to the renderer (this is the different groups of models)
        // There are multiple groups for efficiency reasons
        final HashMap<Integer, RendererGroup> MAP_RENDER_GROUPS = map.getRenderGroups();
        for(RendererGroup rendererGroup : MAP_RENDER_GROUPS.values())
        {
            gameplayRenderer.addGroup(rendererGroup);
        }

        // Grab the cells to render later
        mapCells = map.getCells();

        initUI();
    }

    @Override
    protected void surfaceCreated()
    {
        gameplayShader.onSurfaceCreated();
        uiShader.onSurfaceCreated();
    }

    @Override
    public void surfaceChanged(int width, int height)
    {
        // Update the gameplay camera (resize the viewport and matrix)
        gameplayCamera.viewChanged(width, height);

        // Update the ui renderer canvas with the new width and height
        uiRenderer.setCanvasSize(width, height);

        // Reposition the UI using the new width and heights
        positionUI(width, height);
    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);

        // The map is 2D and should be rendered under everything else
        glDisable(GL_DEPTH_TEST);
        drawMap();

        glEnable(GL_DEPTH_TEST);
        gameplayRenderer.render();

        uiRenderer.render();
    }

    // Draws the 2D map (just the floor tiles, not the models in the map)
    private void drawMap()
    {
        gameplayShader.useProgram();
        gameplayShader.setColourUniforms(new Colour(1.0f, 1.0f, 1.0f, 1.0f));
        gameplayShader.setTextureUniforms(TextureHelper.loadTexture(context,
                R.drawable.mapatlas, true).getTextureId());

        /*
        The following code is an optimisation for the map rendering piece. It limits the range in
        the for loop that cycles through the map data to only the cells surrounding the center
        of the screen. Frustrum culling was used at first to decrease GPU rendering times but then
        the bottleneck was the CPU so this limits looping in the arrays significantly.
         */
        Matrix.invertM(invertedVPMatrix, 0, gameplayCamera.getViewProjectionMatrix(), 0);
        Matrix.multiplyMV(worldspaceAtCenter, 0, invertedVPMatrix, 0, NDC_CENTER, 0);
        worldspaceAtCenter[0] /= worldspaceAtCenter[3];
        worldspaceAtCenter[2] /= worldspaceAtCenter[3];
        int lowX = (int)(worldspaceAtCenter[0] / map.TILE_SIZE) - RENDER_CHUNKS;
        int highX = (int)(worldspaceAtCenter[0] / map.TILE_SIZE) + RENDER_CHUNKS;
        int lowY = (int)(worldspaceAtCenter[2] / map.TILE_SIZE) - RENDER_CHUNKS;
        int highY = (int)(worldspaceAtCenter[2] / map.TILE_SIZE) + RENDER_CHUNKS;

        // Make sure the values are in the range
        lowX = lowX < 0 ? 0 : lowX;
        highX = highX > map.getMapWidth() ? map.getMapWidth() : highX;
        lowY = lowY < 0 ? 0 : lowY;
        highY = highY > map.getMapHeight() ? map.getMapHeight() : highY;

        /*
        // Frustrum culling that didn't really work because it didn't increase CPU efficiency enough
        // Multiply the MVP by the position of the cell
        float[] tilePosBottomLeft = new float[4];
        float[] tilePosTopRight = new float[4];
        Matrix.multiplyMV(tilePosBottomLeft, 0, gameplayCamera.getViewProjectionMatrix(), 0, new float[]{x * map.TILE_SIZE, 0.0f, y * map.TILE_SIZE, 1.0f}, 0);
        Matrix.multiplyMV(tilePosTopRight, 0, gameplayCamera.getViewProjectionMatrix(), 0, new float[]{(x * map.TILE_SIZE) + map.TILE_SIZE, 0.0f, (y * map.TILE_SIZE) + map.TILE_SIZE, 1.0f}, 0);

        if(tilePosBottomLeft[0] / tilePosBottomLeft[3] <= 1.1f &&
                tilePosBottomLeft[0] / tilePosBottomLeft[3] >= -1.1f &&
                tilePosBottomLeft[1] / tilePosBottomLeft[3] <= 1.1f &&
                tilePosBottomLeft[1] / tilePosBottomLeft[3] >= -1.1f ||
                tilePosTopRight[0] / tilePosTopRight[3] <= 1.1f &&
                        tilePosTopRight[0] / tilePosTopRight[3] >= -1.1f &&
                        tilePosTopRight[1] / tilePosTopRight[3] <= 1.1f &&
                        tilePosTopRight[1] / tilePosTopRight[3] >= -1.1f)
        {
            // Render
        }*/

        for(int y = lowY; y < highY; y++)
        {
            for(int x = lowX; x < highX; x++)
            {
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, x * map.TILE_SIZE, 0.0f, y * map.TILE_SIZE);
                multiplyMM(mvMatrix, 0, gameplayCamera.getViewMatrix(), 0,
                        modelMatrix, 0);
                multiplyMM(mvpMatrix, 0, gameplayCamera.getProjectionMatrix(), 0,
                        mvMatrix, 0);

                gameplayShader.setMVMatrixUniform(mvMatrix);
                gameplayShader.setMVPMatrixUniform(mvpMatrix);

                mapCells[y][x].bindData(gameplayShader);
                mapCells[y][x].draw();
            }
        }

        // Frustrum culling vb#



        gameplayShader.unbindProgram();
    }

    @Override
    public void update(float deltaTime) {
        this.deltaTime = deltaTime;

        moveController.update(deltaTime);
        aimController.update(deltaTime);
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

    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent) {
        switch (pointerMotionEvent)
        {
            case CLICK:
                if(handlePointerControl(moveController, pointer)) { return; }
                if(handlePointerControl(aimController, pointer)) { return; }
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

    public void initUI()
    {
        // Initialise the UI
        baseMoveController = new BaseController(context, 200.0f, R.drawable.joy_stick_outer);
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
                        final Geometry.Vector CAMERA_MOVEMENT_VECTOR = moveController.getDirection().scale(GAMEPLAY_CAMERA_MOVEMENT_SPEED * deltaTime);
                        gameplayCamera.translate(new Geometry.Vector(CAMERA_MOVEMENT_VECTOR.x, 0.0f, -CAMERA_MOVEMENT_VECTOR.y));
                        /*if(debugView)
                        {
                            Geometry.Vector velocity = moveController.getDirection().scale(MOVE_SPEED);
                            // debugCamera.translate(new Geometry.Vector(velocity.x, 0.0f, -velocity.y));
                        }
                        else
                        {
                            Geometry.Vector velocity = moveController.getDirection().scale(MOVE_SPEED);
                            player.setVelocity(new Geometry.Vector(velocity.x, 0.0f, -velocity.y));
                        }*/
                        break;
                    case RELEASE:
                        //player.setVelocity(new Geometry.Vector(0.0f, 0.0f, 0.0f));
                        break;
                }
            }
        });

        baseAimController = new BaseController(context,200.0f, R.drawable.joy_stick_outer);
        aimController = new MoveController(context, baseAimController, R.drawable.aim_joy_stick_inner);

        aimController.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        //  crosshair.setAlpha(1.0f);
                        break;
                    case DOWN:
                     /*   // Fetch the direction from the joystick, divide by magnitude to get the
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
*/
                        break;
                    case RELEASE:
                  /*      crosshair.setPosition(new Geometry.Point((viewWidth/2.0f) - 25,
                                (viewHeight/2.0f) - 25, 0.0f));
                        crosshair.setAlpha(0.0f);*/
                        break;
                }
            }
        });

        uiRenderer.addUI(baseMoveController);
        uiRenderer.addUI(moveController);
        uiRenderer.addUI(baseAimController);
        uiRenderer.addUI(aimController);
    }

    public void positionUI(int viewWidth, int viewHeight)
    {
        // Add the move joystick
        baseMoveController.setPosition(new Geometry.Point(80.0f, 70.0f, 0.0f), moveController);
        baseAimController.setPosition(new Geometry.Point(viewWidth - 80.0f - 400.0f, 70.0f, 0.0f), aimController);
    }
}
