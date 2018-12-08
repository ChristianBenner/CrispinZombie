package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.shaders.PerFragMultiLightingShader;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.ui.Button;
import com.christianbenner.crispinandroid.ui.Font;
import com.christianbenner.crispinandroid.ui.Image;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.ui.Text;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;
import com.christianbenner.crispinandroid.ui.UIDimension;
import com.christianbenner.crispinandroid.util.Audio;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.Entities.Player;
import com.christianbenner.zombie.R;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

/**
 * Created by Christian Benner on 19/11/2017.
 */

public class SceneMenu extends Scene {
    private Audio audio;
    private static int audioPos = 0;
    private UIRenderer uiRenderer;
    private Button playButton;
    private Button endlessButton;
    private Button settingsButton;
    private TextureShaderProgram uiShader;
    private Image title;
    private Texture titleTexture;

    private Text playText;
    private Text settingsText;
    private Text endlessText;
    private Text versionText;

    final private int BUTTON_SIZE = 300;
    final private int BUTTON_PADDING = 30;
    final private int TEXT_PADDING = 10;

    private UIDimension titleDimensions;

    private Camera camera;
    private Renderer renderer;
    private PerFragMultiLightingShader shader;
    private Player player;

    public SceneMenu(Context context)
    {
        super(context);
        uiRenderer = new UIRenderer(context, R.drawable.arial_font, R.raw.arial_font_fnt);

        shader = new PerFragMultiLightingShader(context);

        camera = new Camera();
        camera.setPosition(new Geometry.Point(5.0f, 1.0f, 8.9f));
        camera.setAngles(-3.314f, -0.323f);
        renderer = new Renderer(shader, camera);

        player = new Player(context, TextureHelper.loadTexture(context, R.drawable.player, true),
                0.0f, null, null);

        player.setWaving(true);
        player.setPosition(new Geometry.Point(5.8f, 0.0f, 7.76f));
        player.addToRenderer(renderer);
    }

    @Override
    protected void surfaceCreated() {
        // Create audio
        audio = Audio.getInstance();
        audio.initMusicChannel(context);
        audio.playMusic(R.raw.hibymaeson, audioPos);
       // audio.playMusic(R.raw.no_words, audioPos);
    }

    @Override
    public void surfaceChanged(int width, int height) {
        uiRenderer.createUICanvas(width, height);
        uiShader = new TextureShaderProgram(context);
        uiRenderer.setShader(uiShader);

        shader = new PerFragMultiLightingShader(context);
        renderer.setShader(shader);
        camera.viewChanged(width, height);
        renderer.setCamera(camera);
        initUI(width, height);
    }

    private void initUI(int viewWidth, int viewHeight)
    {
        titleTexture = TextureHelper.loadTexture(context, R.drawable.title);

        title = new Image(new UIDimension((viewWidth / 2.0f) - (titleTexture.getWidth()),
                viewHeight - (titleTexture.getHeight() * 2.0f) - 100,
                titleTexture.getWidth() * 2.0f,
                titleTexture.getHeight() * 2.0f), titleTexture);
        titleDimensions = title.getDimensions();

        playButton = new Button(
                new UIDimension((viewWidth/2.0f) - (BUTTON_SIZE / 2.0f) - BUTTON_PADDING - BUTTON_SIZE, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_play));
        playButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        gotoScene(Constants.GAME_ID);
                        break;
                }
            }
        });

        settingsButton = new Button(
                new UIDimension((viewWidth/2.0f) - (BUTTON_SIZE / 2.0f),
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_settings));
        settingsButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        endlessButton = new Button(
                new UIDimension((viewWidth/2.0f) + (BUTTON_SIZE / 2.0f) + BUTTON_PADDING,
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_endless));
        endlessButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        break;
                }
            }
        });

        Font font = new Font(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        playText = new Text("PLAY", 2, font, viewWidth, uiRenderer, true);
        playText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        playText.setPosition(new Geometry.Point(-BUTTON_PADDING - BUTTON_SIZE, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) - playText.getHeight() - TEXT_PADDING, 0.0f));

        settingsText = new Text("SETTINGS", 2, font, viewWidth, uiRenderer, true);
        settingsText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        settingsText.setPosition(new Geometry.Point(0.0f, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) - playText.getHeight() - TEXT_PADDING, 0.0f));

        endlessText = new Text("ENDLESS", 2, font, viewWidth, uiRenderer, true);
        endlessText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        endlessText.setPosition(new Geometry.Point(BUTTON_PADDING + BUTTON_SIZE, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) - playText.getHeight() - TEXT_PADDING, 0.0f));

        versionText = new Text("Version: " + Constants.VERSION_STRING, 2, font, viewWidth, uiRenderer, false);
        versionText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        versionText.setPosition(new Geometry.Point(viewWidth - 350.0f, 5.0f, 0.0f));

     //   settingsText = new Text("PLAY", 2, font, width, uiRenderer, true);
      //  settingsText.setPosition(new Geometry.Point(0.0f, height - cameraText.getHeight(), 0.0f));

      //  endlessText = new Text("PLAY", 2, font, width, uiRenderer, true);
     //   endlessText.setPosition(new Geometry.Point(0.0f, height - cameraText.getHeight(), 0.0f));

        uiRenderer.addUI(title);
        uiRenderer.addUI(playButton);
        uiRenderer.addUI(settingsButton);
        uiRenderer.addUI(endlessButton);
        uiRenderer.addUI(playText);
        uiRenderer.addUI(settingsText);
        uiRenderer.addUI(endlessText);
        uiRenderer.addUI(versionText);
    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(red, green, blue, 1.0f);
        uiRenderer.render();
        renderer.render();
    }

    float red = 0.0f;
    boolean redb = true;
    float green = 0.5f;
    boolean greenb = true;
    float blue = 1.0f;
    boolean blueb = false;
    float timePassed = 0.0f;

    final float MAX_SIZE_INC = 100.0f;
    float x = 0.0f;

    float xPos = 0.0f;
    @Override
    public void update(float deltaTime) {
        playButton.update(deltaTime);
        settingsButton.update(deltaTime);
        endlessButton.update(deltaTime);

        xPos -= deltaTime * 1.0f;
        camera.setPosition(new Geometry.Point(xPos, 0.0f, 0.0f));
        player.update(deltaTime);

        x += deltaTime * 0.03f;
        if(x >= Math.PI)
        {
            x -= Math.PI;
        }

        final float SIZE_INC_X = MAX_SIZE_INC * (float)Math.sin(x);
        final float SIZE_INC_Y = SIZE_INC_X * (titleDimensions.h / titleDimensions.w);

        title.setDimensions(new UIDimension(titleDimensions.x - (SIZE_INC_X / 2.0f), titleDimensions.y + (SIZE_INC_Y / 2.0f), titleDimensions.w + SIZE_INC_X, titleDimensions.h + SIZE_INC_Y));

        if(red >= 1.0f)
        {
            redb = false;
        }else if(red <= 0.0f)
        {
            redb = true;
        }

        if(green >= 1.0f)
        {
            greenb = false;
        }else if(red <= 0.0f)
        {
            greenb = true;
        }

        if(blue >= 1.0f)
        {
            blueb = false;
        }else if(red <= 0.0f)
        {
            blueb = true;
        }

        if(redb)
        {
            red += 0.01f * deltaTime;
        }
        else
        {
            red -= 0.01f * deltaTime;
        }

        if(greenb)
        {
            green += 0.01f * deltaTime;
        }
        else
        {
            green -= 0.01f * deltaTime;
        }

        if(blueb)
        {
            blue += 0.01f * deltaTime;
        }
        else
        {
            blue -= 0.01f * deltaTime;
        }
    }

    @Override
    protected void pause()
    {
        audioPos = audio.getMusicPos();
        audio.pause();
    }

    @Override
    protected void resume()
    {

    }

    @Override
    protected void restart()
    {
        audioPos = audio.getMusicPos();
        audio.pause();
    }

    @Override
    protected void destroy()
    {
        audio.cleanMusic();
    }

    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent) {
        switch(pointerMotionEvent) {
            case CLICK:
                // Look through the UI and check if the pointer interacts with them
                if (handlePointerControl(playButton, pointer)) {
                    return;
                }
                if (handlePointerControl(settingsButton, pointer)) {
                    return;
                }
                if (handlePointerControl(endlessButton, pointer)) {
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
