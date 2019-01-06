package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.Matrix;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.shaders.PerFragLightingTextureShader;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.RendererGroup;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.ui.Pointer;
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

    // Game-play Rendering
    private Renderer gameplayRenderer;
    private PerFragLightingTextureShader gameplayShader;
    private Camera gameplayCamera;

    // The map
    private Map map;
    private final Cell[][] mapCells;

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
    }

    @Override
    protected void surfaceCreated() {
        gameplayShader.onSurfaceCreated();
    }

    @Override
    public void surfaceChanged(int width, int height) {
        gameplayCamera.viewChanged(width, height);
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
    }

    // Draws the 2D map (just the floor tiles, not the models in the map)
    private void drawMap()
    {
        gameplayShader.useProgram();
        float[] mvMatrix = new float[16];
        float[] mvpMatrix = new float[16];
        float[] modelMatrix = new float[16];

        gameplayShader.setColourUniforms(new Colour(1.0f, 1.0f, 1.0f, 1.0f));
        gameplayShader.setTextureUniforms(TextureHelper.loadTexture(context,
                R.drawable.mapatlas, true).getTextureId());

        for(int y = 0; y < map.getMapHeight(); y++)
        {
            for(int x = 0; x < map.getMapWidth(); x++)
            {
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, x * 0.5f, 0.0f, y * 0.5f);
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

        gameplayShader.unbindProgram();
    }

    @Override
    public void update(float deltaTime) {

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

    }
}
