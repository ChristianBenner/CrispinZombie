package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.ui.Image;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.R;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

public class TestScene extends Scene {
    private UIRenderer renderer;
    private Image image;
    private TextureShaderProgram textureShaderProgram;

    public TestScene(Context context) {
        super(context, Constants.TEST_ID);

        renderer = new UIRenderer();
        image = new Image(new Dimension2D(400, 400, 200, 200),
                new Colour(1.0f, 1.0f, 1.0f, 1.0f),
                TextureHelper.loadTexture(context, R.drawable.button_play));
        renderer.addUI(image);

        textureShaderProgram = new TextureShaderProgram(context);
        renderer.setShader(textureShaderProgram);
    }

    @Override
    protected void surfaceCreated() {
        textureShaderProgram.onSurfaceCreated();
    }

    @Override
    public void surfaceChanged(int width, int height) {

       // this.colourShaderProgram = new ColourShaderProgram(context);
        renderer.setCanvasSize(width, height);
    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

        renderer.render();
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
