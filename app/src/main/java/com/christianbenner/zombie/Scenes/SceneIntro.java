package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.objects.FlexibleSquare;
import com.christianbenner.crispinandroid.programs.TextureShaderProgram;
import com.christianbenner.crispinandroid.ui.GLFont;
import com.christianbenner.crispinandroid.ui.GLText;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.R;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

/**
 * Created by Christian Benner on 19/11/2017.
 */

public class SceneIntro extends Scene {

    // Objects
    private FlexibleSquare backgroundObject;

    // Textures
    private Texture textureCrispinWhite;
    private Texture textureCrispinRed;

    // Version Text
    private GLText version;

    // Logic Variables
    private float alphaWhite = 1.0f;
    private float alphaRed = 0.0f;
    private float backgroundRGB = 0.0f;
    static private float timePassed = 0.0f;
    private float textureScaleRatio = 1.0f;
    private int viewportWidth;
    private int viewportHeight;

    // Shaders
    private TextureShaderProgram textureShader;

    private float aspectRatio;

    public SceneIntro(Context context)
    {
        super(context);
    }

    @Override
    protected void surfaceCreated()
    {
        // Setup shader
        textureShader = new TextureShaderProgram(context);

        // Set up objects
        backgroundObject = new FlexibleSquare(-1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f);

        // Setup textures
        textureCrispinWhite = TextureHelper.loadTexture(context, R.drawable.crispinwhite);
        textureCrispinRed = TextureHelper.loadTexture(context, R.drawable.crispinred);

        // Setup text
        version = new GLText(context, "Version " + Constants.VERSION_STRING, 1, new GLFont(context, R.drawable.arial_font, R.raw.arial_font_fnt), 1920f, 1080f, 1.0f);
        version.setPosition(-1.0f, -1.0f);
        version.setColour(new Colour(1.0f, 0.0f, 0.0f));

        // Play a tune
        playSound(context, R.raw.crispingintropiano, 0);
    }

    // MATRIX
    private final float[] defaultMatrix =
    {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private final float[] scaleMatrix = new float[16];

    @Override
    public void surfaceChanged(int width, int height)
    {
        // Set the OpenGL viewport to fill the entire surface
        viewportWidth = width;
        viewportHeight = height;
        glViewport(0, 0, viewportWidth, viewportHeight);
        aspectRatio = (float)viewportWidth / (float)viewportHeight;

        // Enable alpha blending for transparency
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void draw()
    {
        // Clear previous frame and set background colour
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(backgroundRGB, backgroundRGB, backgroundRGB, 1.0f);

        // Use the texture shader
        textureShader.useProgram();
        backgroundObject.bindData(textureShader);

        // Scale it based on the difference in aspect ratio
        textureScaleRatio = aspectRatio / textureCrispinWhite.getAspectRatio();

        // Scale the object so that it appears the same on any device
        if(textureScaleRatio <= 1.0f)
        {
            Matrix.scaleM(scaleMatrix, 0, defaultMatrix, 0, 1.0f, textureScaleRatio, 1.0f);
        }else
        {
            Matrix.scaleM(scaleMatrix, 0, defaultMatrix, 0, 1.0f / textureScaleRatio, 1.0f, 1.0f);
        }

        // Draw the white logo
        textureShader.setUniforms(scaleMatrix, textureCrispinWhite.getTextureId(), alphaWhite);
        backgroundObject.draw();

        // Draw the red logo
        textureShader.setUniforms(scaleMatrix, textureCrispinRed.getTextureId(), alphaRed);
        backgroundObject.draw();

        // Render the version text
       // version.render();
    }

    @Override
    public void update(float deltaTime)
    {
        // Calculate logic using the timing method
        alphaWhite -= 0.01f * deltaTime;
        alphaRed += 0.01f * deltaTime;
        backgroundRGB += 0.01f * deltaTime;

        // 60 in one second
        timePassed += 1.0f * deltaTime;
        if(timePassed >= 240.0f) {
            alphaRed -= 0.05f * deltaTime;
            backgroundRGB -= 0.05f * deltaTime;

            if (alphaRed < 0.0f) {
                setVolume(0.0f);
            } else if (alphaRed > 1.0f) {
                setVolume(1.0f);
            } else {
                setVolume(alphaRed);
                version.setAlpha(alphaRed);
            }

            // Scene is over, switch to the next one
            if(timePassed >= 300.0f)
            {
                timePassed = 0.0f;
                gotoScene(RendererIDConstants.GAME_ID);
            }
        }
    }

    // Activity lifecycle methods
    @Override
    protected void pause()
    {

    }

    @Override
    protected void resume()
    {

    }

    @Override
    protected void restart()
    {

    }

    @Override
    protected void destroy()
    {

    }

    @Override
    public void motionEvent(View view, MotionEvent event) {
    }

    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent) {

    }
}
