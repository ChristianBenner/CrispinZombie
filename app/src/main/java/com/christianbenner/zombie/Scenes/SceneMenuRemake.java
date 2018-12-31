package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.ui.Button;
import com.christianbenner.crispinandroid.ui.Image;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.R;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_ALPHA_BITS;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;

public class SceneMenuRemake extends Scene {
    final private int BUTTON_SIZE = 300;
    final private int BUTTON_PADDING = 30;

    private UIRenderer renderer;
    private Image image;
    private TextureShaderProgram textureShaderProgram;

    private Texture mainMenuTitleTexture;
    private Image mainMenuTitle;
    private Button mainMenuPlayButton;

    public SceneMenuRemake(final Context context)
    {
        super(context, Constants.MENU_ID);

        renderer = new UIRenderer();

        mainMenuTitleTexture = TextureHelper.loadTexture(context, R.drawable.title);
        mainMenuTitle = new Image(mainMenuTitleTexture);

        mainMenuPlayButton = new Button(
                TextureHelper.loadTexture(context, R.drawable.button_play));
        mainMenuPlayButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        //transitionToPage(SceneMenu.PAGE.LEVEL_SELECT);
                        break;
                }
            }
        });

      //  titleDimensions = mainMenuTitle.getDimensions();


     /*   image = new Image(new Dimension2D(400, 400, 200, 200),
                new Colour(1.0f, 0.0f, 1.0f, 1.0f),
                TextureHelper.loadTexture(context, R.drawable.button_play));
        renderer.addUI(image);*/

        renderer.addUI(mainMenuTitle);
        renderer.addUI(mainMenuPlayButton);

        textureShaderProgram = new TextureShaderProgram(context);
        renderer.setShader(textureShaderProgram);
    }

    @Override
    protected void surfaceCreated() {
        textureShaderProgram.onSurfaceCreated();
    }

    @Override
    public void surfaceChanged(int width, int height) {
        renderer.setCanvasSize(width, height);
        mainMenuTitle.setDimensions(new Dimension2D((width / 2.0f) - (mainMenuTitleTexture.getWidth()),
                height - (mainMenuTitleTexture.getHeight() * 2.0f) - 100,
                mainMenuTitleTexture.getWidth() * 2.0f,
                mainMenuTitleTexture.getHeight() * 2.0f));

        mainMenuPlayButton.setDimensions(new Dimension2D((width/2.0f) -
        (BUTTON_SIZE / 2.0f) - BUTTON_PADDING - BUTTON_SIZE, (height / 2.0f) -
                (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE));
    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

        renderer.render();
    }

    @Override
    public void update(float deltaTime) {
        mainMenuPlayButton.update(deltaTime);
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
        switch(pointerMotionEvent) {
            case CLICK:
                // Look through the UI and check if the pointer interacts with them
                if (handlePointerControl(mainMenuPlayButton, pointer)) {
                    return;
                }
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
}
