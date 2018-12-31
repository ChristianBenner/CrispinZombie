package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.ui.Button;
import com.christianbenner.crispinandroid.ui.Font;
import com.christianbenner.crispinandroid.ui.Image;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.ui.Text;
import com.christianbenner.crispinandroid.ui.TouchEvent;
import com.christianbenner.crispinandroid.ui.TouchListener;
import com.christianbenner.crispinandroid.util.Audio;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.R;

import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES20.GL_ALPHA;
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
    final private int TEXT_PADDING = 10;

    private Audio audio;
    private static int audioPos = 0;

    private UIRenderer uiRenderer;
    private Image image;
    private TextureShaderProgram textureShaderProgram;

    private Texture mainMenuTitleTexture;
    private Image mainMenuTitle;
    private Button mainMenuPlayButton;
    private Button mainMenuEndlessButton;
    private Button mainMenuSettingsButton;
    private Text mainMenuPlayText;
    private Text mainMenuSettingsText;
    private Text mainMenuEndlessText;
    private Text mainMenuVersionText;

    private final int MUSIC_SELECTION;

    public SceneMenuRemake(final Context context)
    {
        super(context, Constants.MENU_ID);

        uiRenderer = new UIRenderer();

        textureShaderProgram = new TextureShaderProgram(context);
        uiRenderer.setShader(textureShaderProgram);

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

        mainMenuSettingsButton = new Button(TextureHelper.loadTexture(context, R.drawable.button_settings));
        mainMenuSettingsButton.addButtonListener(new TouchListener() {
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

        mainMenuEndlessButton = new Button(TextureHelper.loadTexture(context, R.drawable.button_endless));
        mainMenuEndlessButton.addButtonListener(new TouchListener() {
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
        mainMenuPlayText = new Text("PLAY", 2, font, true);
        mainMenuPlayText.setColour(new Colour(0.25f, 0.25f, 0.25f));

        mainMenuSettingsText = new Text("SETTINGS", 2, font, true);
        mainMenuSettingsText.setColour(new Colour(0.25f, 0.25f, 0.25f));

        mainMenuEndlessText = new Text("ENDLESS", 2, font, true);
        mainMenuEndlessText.setColour(new Colour(0.25f, 0.25f, 0.25f));

        mainMenuVersionText = new Text("Version: " + Constants.VERSION_STRING, 2, font, false);
        mainMenuVersionText.setColour(new Colour(0.25f, 0.25f, 0.25f));

        uiRenderer.addUI(mainMenuTitle);
        uiRenderer.addUI(mainMenuPlayButton);
        uiRenderer.addUI(mainMenuSettingsButton);
        uiRenderer.addUI(mainMenuEndlessButton);
        uiRenderer.addUI(mainMenuPlayText);
        uiRenderer.addUI(mainMenuSettingsText);
        uiRenderer.addUI(mainMenuEndlessText);
        uiRenderer.addUI(mainMenuVersionText);

         /*  mainMenuUIGroup.addUI(mainMenuTitle);
        mainMenuUIGroup.addUI(mainMenuPlayButton);
        mainMenuUIGroup.addUI(mainMenuSettingsButton);
        mainMenuUIGroup.addUI(mainMenuEndlessButton);
        mainMenuUIGroup.addUI(mainMenuPlayText);
        mainMenuUIGroup.addUI(mainMenuSettingsText);
        mainMenuUIGroup.addUI(mainMenuEndlessText);
        mainMenuUIGroup.addUI(mainMenuVersionText);
        uiRenderer.addRendererGroup(mainMenuUIGroup);


        final float LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT = viewHeight / 2.0f;

        // Add the level select menu elements
        levelMenuTitleText = new Text("LEVEL SELECT", 4, font, viewWidth, uiRenderer, true);
        levelMenuTitleText.setColour(new Colour(1.0f, 1.0f, 1.0f));
        levelMenuTitleText.setPosition(new Geometry.Point(0.0f, viewHeight - levelMenuTitleText.getHeight() - LEVEL_SELECT_MENU_TITLE_PADDING, 0.0f));

        selectLevelButton = new Button(
                new Dimension2D((viewWidth/2.0f) - (GO_BUTTON_WIDTH / 2.0f),
                        viewHeight / 3.0f - BUTTON_GO_OFFSET - GO_BUTTON_HEIGHT,
                        GO_BUTTON_WIDTH, GO_BUTTON_HEIGHT),
                TextureHelper.loadTexture(context, R.drawable.button_go));
        selectLevelButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        transitionToPage(SceneMenu.PAGE.GAMEPLAY);
                        break;
                }
            }
        });


        levelIcons = new ArrayList<>();
        levelTexts = new ArrayList<>();

        final float LEVEL_SELECT_LEVEL_ICONS_WIDTH = 960.0f;
        final float LEVEL_SELECT_LEVEL_ICONS_HEIGHT = 540.0f;
        final float WIDTH_HEIGHT_RATIO = LEVEL_SELECT_LEVEL_ICONS_WIDTH / LEVEL_SELECT_LEVEL_ICONS_HEIGHT;

        levelMenuLevelIconBannerOffset = (LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT - (LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f)) / 1.5f;
        iconWidths = LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f * WIDTH_HEIGHT_RATIO;
        iconHeights = LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f;

        for(SceneMenu.LevelIconData levelIconData : levelIconsData)
        {
            // Load in the image for the icon (height = LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT)
            levelIcons.add(new Image(new Dimension2D(0.0f, 0.0f, iconWidths,
                    LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f),
                    TextureHelper.loadTexture(context, levelIconData.getTextureResourceId())));

            Text tempText = new Text(levelIconData.getTitle(), 2, font, viewWidth, uiRenderer, true);
            tempText.setColour(new Colour(1f, 1f, 1f));
            tempText.setPosition(new Geometry.Point(0.0f, 0.0f, 0.0f));

            levelTexts.add(tempText);
        }

        for(Image levelIcon : levelIcons)
        {
            levelSelectMenuUIGroup.addUI(levelIcon);
        }

        for(Text levelText : levelTexts)
        {
            levelSelectMenuUIGroup.addUI(levelText);
        }

        levelSelectBackButton = new Button(
                new Dimension2D(BUTTON_PADDING, viewHeight - (BUTTON_SIZE / 2.1f) - BUTTON_PADDING,
                        BUTTON_SIZE, BUTTON_SIZE / 2.1f),
                TextureHelper.loadTexture(context, R.drawable.button_back_long));
        levelSelectBackButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    case CLICK:
                        audio.playSound(R.raw.button_click, 1);
                        transitionToPage(SceneMenu.PAGE.MAIN_MENU);
                        break;
                }
            }
        });

        levelSelectMenuUIGroup.addUI(levelMenuTitleText);
        levelSelectMenuUIGroup.addUI(selectLevelButton);
        levelSelectMenuUIGroup.addUI(levelSelectBackButton);

        // Add the level select menu background elements
        levelMenuBackgroundOverlay = new Image(new Dimension2D(0.0f, viewHeight / 3.0f, viewWidth, LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT), new Colour(0.3f, 0.3f, 0.3f, 0.7f));
        levelSelectBackgroundUIGroup.addUI(levelMenuBackgroundOverlay);

        // Add the transition overlay element
        transitionOverlay = new Image(new Dimension2D(0.0f, 0.0f, viewWidth, viewHeight), new Colour(0.0f, 0.0f, 0.0f, 0.0f));
        transitionOverlayUIGroup.addUI(transitionOverlay);
        uiRenderer.addRendererGroup(transitionOverlayUIGroup);*/
        //  titleDimensions = mainMenuTitle.getDimensions();


     /*   image = new Image(new Dimension2D(400, 400, 200, 200),
                new Colour(1.0f, 0.0f, 1.0f, 1.0f),
                TextureHelper.loadTexture(context, R.drawable.button_play));
        uiRenderer.addUI(image);*/

        final Random MUSIC_RANDOMIZER = new Random();
        MUSIC_SELECTION = MUSIC_RANDOMIZER.nextInt(2);
    }

    @Override
    protected void surfaceCreated() {
        textureShaderProgram.onSurfaceCreated();

        audio = Audio.getInstance();
        audio.initMusicChannel(context);

        switch(MUSIC_SELECTION)
        {
            case 0:
                audio.playMusic(R.raw.menu_echoes_of_time, audioPos);
                break;
            case 1:
                audio.playMusic(R.raw.menu_gloom_horizon, audioPos);
                break;
        }
    }

    @Override
    public void surfaceChanged(int viewWidth, int viewHeight) {
        uiRenderer.setCanvasSize(viewWidth, viewHeight);
        mainMenuPlayText.generateText(
                uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight(), viewWidth);
        mainMenuSettingsText.generateText(
                uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight(), viewWidth);
        mainMenuEndlessText.generateText(
                uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight(), viewWidth);
        mainMenuVersionText.generateText(
                uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight(), viewWidth);

        mainMenuPlayText.setPosition(
                new Geometry.Point(-BUTTON_PADDING - BUTTON_SIZE,
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) -
                                mainMenuPlayText.getHeight() - TEXT_PADDING, 0.0f));
        mainMenuSettingsText.setPosition(
                new Geometry.Point(0.0f, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) -
                        mainMenuPlayText.getHeight() - TEXT_PADDING, 0.0f));
        mainMenuEndlessText.setPosition(
                new Geometry.Point(BUTTON_PADDING + BUTTON_SIZE,
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) -
                                mainMenuPlayText.getHeight() - TEXT_PADDING, 0.0f));
        mainMenuVersionText.setPosition(
                new Geometry.Point(viewWidth - 350.0f, 5.0f, 0.0f));

        mainMenuTitle.setDimensions(new Dimension2D((viewWidth / 2.0f) -
                (mainMenuTitleTexture.getWidth()),
                viewHeight - (mainMenuTitleTexture.getHeight() * 2.0f) - 100,
                mainMenuTitleTexture.getWidth() * 2.0f,
                mainMenuTitleTexture.getHeight() * 2.0f));

        mainMenuPlayButton.setDimensions(new Dimension2D((viewWidth/2.0f) -
        (BUTTON_SIZE / 2.0f) - BUTTON_PADDING - BUTTON_SIZE, (viewHeight / 2.0f) -
                (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE));

        mainMenuSettingsButton.setDimensions(
                new Dimension2D((viewWidth/2.0f) - (BUTTON_SIZE / 2.0f),
                (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE));

        mainMenuEndlessButton.setDimensions(
                new Dimension2D((viewWidth/2.0f) + (BUTTON_SIZE / 2.0f) + BUTTON_PADDING,
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE));

/*        mainMenuPlayText.generateText(viewWidth);
        mainMenuSettingsText.generateText(viewWidth);
        mainMenuEndlessText.generateText(viewWidth);
        mainMenuVersionText.generateText(viewWidth);*/




    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

        uiRenderer.render();
    }

    @Override
    public void update(float deltaTime) {
        mainMenuPlayButton.update(deltaTime);
        mainMenuSettingsButton.update(deltaTime);
        mainMenuEndlessButton.update(deltaTime);
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
    protected void destroy() {

    }

    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent) {
        switch(pointerMotionEvent) {
            case CLICK:
                // Look through the UI and check if the pointer interacts with them
                if (handlePointerControl(mainMenuPlayButton, pointer) ||
                        handlePointerControl(mainMenuSettingsButton, pointer) ||
                        handlePointerControl(mainMenuEndlessButton, pointer)) {
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
