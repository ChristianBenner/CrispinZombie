package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.shaders.ColourShaderProgram;
import com.christianbenner.crispinandroid.render.shaders.PerFragMultiLightingShader;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.render.util.UIRendererGroup;
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
import com.christianbenner.zombie.Entities.Player;
import com.christianbenner.zombie.Objects.MenuLevelSelectSlider;
import com.christianbenner.zombie.R;

import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

public class SceneMenuRemake extends Scene {
    // The different pages
    private enum PAGE
    {
        NONE,
        MAIN_MENU,
        LEVEL_SELECT,
        SETTINGS,
        GAMEPLAY,
        ENDLESS
    }

    private class LevelIconData
    {
        private final int TEXTURE_RESOURCE_ID;
        private final int MAP_RESOURCE;
        private final String TITLE;

        public LevelIconData(int textureResourceId, int resourceFile, String title)
        {
            this.TEXTURE_RESOURCE_ID = textureResourceId;
            this.MAP_RESOURCE = resourceFile;
            this.TITLE = title;
        }

        public final String getTitle()
        {
            return TITLE;
        }

        public final int getMapResource()
        {
            return MAP_RESOURCE;
        }

        private final int getTextureResourceId()
        {
            return TEXTURE_RESOURCE_ID;
        }
    }

    // UI Positioning Vars
    final private int BUTTON_SIZE = 300;
    final private int BUTTON_PADDING = 30;
    final private int TEXT_PADDING = 10;
    final private int LEVEL_SELECT_MENU_TITLE_PADDING = 20;
    final private int LEVEL_ICON_TEXT_OFFSET = 5;
    final private int LEVEL_ICON_OFFSET = 20;
    final private int BUTTON_GO_OFFSET = 20;
    final private int GO_BUTTON_WIDTH = 400;
    final private int GO_BUTTON_HEIGHT = 200;

    // How much the title should increase when animated
    private final float MAX_SIZE_INC = 100.0f;

    // How fast the title should animate
    private final float ANIMATE_SPEED = 0.03f;

    // How long it takes for pages to transition
    private final float TRANSITION_TIME_GOAL = 40.0f;

    // Which music track to play
    private final int MUSIC_SELECTION;

    // The page to start on
    private final PAGE START_PAGE = PAGE.MAIN_MENU;

    // Audio player
    private Audio audio;

    // Current position of the music (used to play from the correct position when scene is resumed)
    private static int audioPos = 0;

    // Rendering
    private Renderer modelRenderer;
    private UIRenderer uiRenderer;
    private UIRendererGroup mainMenuRendererGroup;
    private UIRendererGroup levelSelectMenuUIGroup;
    private UIRendererGroup levelSelectBackgroundUIGroup;
    private UIRendererGroup transitionOverlayUIGroup;

    // Texture Shader Program for UI that uses textures
    private TextureShaderProgram textureShaderProgram;

    // Colour Shader Program for UI that only uses colour
    private ColourShaderProgram colourShaderProgram;

    // Fragment Shader for Models
    private PerFragMultiLightingShader modelShader;

    // Camera used for viewing the player model
    private Camera modelViewCamera;

    private Font font;

    // Main Menu UI Objects
    private Player playerModel;
    private Texture mainMenuTitleTexture;
    private Image mainMenuTitle;
    private Button mainMenuPlayButton;
    private Button mainMenuEndlessButton;
    private Button mainMenuSettingsButton;
    private Text mainMenuPlayText;
    private Text mainMenuSettingsText;
    private Text mainMenuEndlessText;
    private Text mainMenuVersionText;
    private Dimension2D titleDimensions;
    // The amount to increase the title size in radians
    private float sizeIncreaseRad = 0.0f;

    // Level Select UI Objects
    private Image levelMenuBackgroundOverlay;
    private Text levelMenuTitleText;
    private MenuLevelSelectSlider levelSelectSlider;
    private Button selectLevelButton;
    private Button levelSelectBackButton;
    private float levelMenuLevelIconBannerOffset;
    private ArrayList<LevelIconData> levelIconsData;
    private ArrayList<Image> levelIcons;
    private ArrayList<Text> levelTexts;
    private boolean draggingCompleted = false;
    private float previousX = 0.0f;
    private float dragXAmount = 0.0f;
    private float iconWidths = 0.0f;
    private float iconHeights = 0.0f;

    // Transition Overlay UI Objects
    private Image transitionOverlay;
    private boolean startSceneFadeIn = true;
    private float transitionOverlayAlpha = startSceneFadeIn ? 1.0f : 0.0f;
    private float transitionTimer = startSceneFadeIn ? TRANSITION_TIME_GOAL : 0.0f;
    private boolean transitionFadeOut = startSceneFadeIn ? false : true;

    // Current page
    private PAGE currentPage = START_PAGE;

    // The page to switch to
    private PAGE switchingToPage = PAGE.NONE;

    public SceneMenuRemake(final Context context)
    {
        super(context, Constants.MENU_ID);


        modelShader = new PerFragMultiLightingShader(context);

        modelViewCamera = new Camera();
        modelViewCamera.setPosition(new Geometry.Point(5.0f, 1.0f, 8.9f));
        modelViewCamera.setAngles(-3.314f, -0.38f);
        modelRenderer = new Renderer(modelShader, modelViewCamera);

        // Create the UI Renderer
        uiRenderer = new UIRenderer();

        // Create the renderer groups
        mainMenuRendererGroup = new UIRendererGroup(uiRenderer);
        levelSelectMenuUIGroup = new UIRendererGroup(uiRenderer);
        levelSelectBackgroundUIGroup = new UIRendererGroup(uiRenderer);
        transitionOverlayUIGroup = new UIRendererGroup(uiRenderer);

        // Add the used renderer groups to the renderer
        uiRenderer.addRendererGroup(mainMenuRendererGroup);
        uiRenderer.addRendererGroup(transitionOverlayUIGroup);

        textureShaderProgram = new TextureShaderProgram(context);
        uiRenderer.setShader(textureShaderProgram);
        levelSelectMenuUIGroup.setShader(textureShaderProgram);

        colourShaderProgram = new ColourShaderProgram(context);
        levelSelectBackgroundUIGroup.setShader(colourShaderProgram);
        transitionOverlayUIGroup.setShader(colourShaderProgram);

        font = new Font(context, R.drawable.arial_font, R.raw.arial_font_fnt);

        initMenuPage();

        // Add the levels
        levelIconsData = new ArrayList<>();
        levelIconsData.add(new LevelIconData(R.drawable.demo_map_icon, R.raw.demo_map4, "demo_map"));
        levelIconsData.add(new LevelIconData(R.drawable.unavailable_map_icon, R.raw.demo_map4, "unavailable1"));
        levelIconsData.add(new LevelIconData(R.drawable.unavailable_map_icon, R.raw.demo_map4, "unavailable2"));

        initLevelSelectPage();

        // Add the transition overlay element
        transitionOverlay = new Image(new Colour(0.0f, 0.0f, 0.0f, 1.0f));
        transitionOverlayUIGroup.addUI(transitionOverlay);

        // Add the transition overlay element
/*        transitionOverlay = new Image(new Dimension2D(0.0f, 0.0f, viewWidth, viewHeight), new Colour(0.0f, 0.0f, 0.0f, 0.0f));
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
        colourShaderProgram.onSurfaceCreated();

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

    private void transitionPage()
    {
        // Handle the render groups for each page
        switch(currentPage)
        {
            case MAIN_MENU:
                switch(switchingToPage)
                {
                    case LEVEL_SELECT:
                        // Remove the current page render groups
                        uiRenderer.removeRendererGroup(mainMenuRendererGroup);
                      //  player.removeFromRenderer(renderer);

                        // Add the next page render groups (level select page)
                        uiRenderer.removeRendererGroup(transitionOverlayUIGroup);
                        uiRenderer.addRendererGroup(levelSelectBackgroundUIGroup);
                        uiRenderer.addRendererGroup(levelSelectMenuUIGroup);
                        uiRenderer.addRendererGroup(transitionOverlayUIGroup);
                        break;
                    case SETTINGS:
                        // todo
                        break;
                    case ENDLESS:
                        // todo
                        break;
                }
                break;
            case LEVEL_SELECT:
                switch(switchingToPage)
                {
                    case MAIN_MENU:
                        // Remove the current page render groups
                        uiRenderer.removeRendererGroup(levelSelectBackgroundUIGroup);
                        uiRenderer.removeRendererGroup(levelSelectMenuUIGroup);
                        uiRenderer.removeRendererGroup(transitionOverlayUIGroup);

                        // Add the next page render groups (main menu page)
                        uiRenderer.addRendererGroup(mainMenuRendererGroup);
                        uiRenderer.addRendererGroup(transitionOverlayUIGroup);
                       // player.addToRenderer(renderer);
                        break;
                    case GAMEPLAY:
                        gotoScene(Constants.GAME_ID);
                        break;
                }
                break;
        }

        currentPage = switchingToPage;
        transitionFadeOut = false;
    }

    @Override
    public void surfaceChanged(int viewWidth, int viewHeight) {
        glViewport(0, 0, viewWidth, viewHeight);

        uiRenderer.setCanvasSize(viewWidth, viewHeight);

        positionMenuPageElements(viewWidth, viewHeight);
        positionLevelSelectPageElements(viewWidth, viewHeight);

        transitionOverlay.setDimensions(new Dimension2D(0.0f, 0.0f, viewWidth, viewHeight));
    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        uiRenderer.render();
        modelRenderer.render();
    }

    @Override
    public void update(float deltaTime) {
        if(switchingToPage != PAGE.NONE || startSceneFadeIn)
        {
            if(transitionFadeOut)
            {
                // Increase the alpha of the transition overlay
                transitionTimer += deltaTime;

                // Calculate the alpha value of the overlay
                transitionOverlayAlpha = transitionTimer / TRANSITION_TIME_GOAL;
                transitionOverlayAlpha = transitionOverlayAlpha >= 1.0f ? 1.0f :
                        transitionOverlayAlpha;
                transitionOverlay.setAlpha(transitionOverlayAlpha);

                // Determine if the scene has faded out
                if(transitionOverlayAlpha == 1.0f)
                {
                    // Switch the scene
                    transitionPage();
                }
            }
            else
            {
                // Decrease the alpha of the transition overlay
                transitionTimer -= deltaTime;

                // Calculate the alpha value of the overlay
                transitionOverlayAlpha = transitionTimer / TRANSITION_TIME_GOAL;
                transitionOverlayAlpha = transitionOverlayAlpha <= 0.0f ? 0.0f :
                        transitionOverlayAlpha;
                transitionOverlay.setAlpha(transitionOverlayAlpha);

                // Determine whether the transitioning has completed
                if (transitionOverlayAlpha == 0.0f)
                {
                    switchingToPage = PAGE.NONE;
                    transitionFadeOut = true;
                    startSceneFadeIn = false;
                }
            }
        }

        switch (currentPage)
        {
            case MAIN_MENU:
                playerModel.update(deltaTime);
                mainMenuPlayButton.update(deltaTime);
                mainMenuSettingsButton.update(deltaTime);
                mainMenuEndlessButton.update(deltaTime);
                animateTitle(deltaTime);
                break;
            case LEVEL_SELECT:
                levelSelectBackButton.update(deltaTime);
                selectLevelButton.update(deltaTime);

                // Decrease to the closest center point
                System.out.println("Drag X Converted: " + (-dragXAmount * uiRenderer.getCanvasWidth()));
                int selected = (int)(((-dragXAmount * uiRenderer.getCanvasWidth()) + (iconWidths / 2.0f)) / (iconWidths + LEVEL_ICON_OFFSET));

                if(selected < 0)
                {
                    selected = 0;
                }
                else if(selected >= levelIconsData.size())
                {
                    selected = levelIconsData.size() - 1;
                }

                final float SELECTED_POSITION = (selected * (iconWidths + LEVEL_ICON_OFFSET));

                //System.out.println("Selected: " + SELECTED + ", POS: " + SELECTED_POSITION);

                // Go to the selected position
                if(-dragXAmount * uiRenderer.getCanvasWidth() < SELECTED_POSITION && levelSelectSlider.hasTouchFocus() == false)
                {
                    dragXAmount -= 0.01f * deltaTime;
                    if(dragXAmount < (-SELECTED_POSITION/uiRenderer.getCanvasWidth()))
                    {
                        dragXAmount = (-SELECTED_POSITION/uiRenderer.getCanvasWidth());
                    }
                }

                if(-dragXAmount * uiRenderer.getCanvasWidth() > SELECTED_POSITION && levelSelectSlider.hasTouchFocus() == false)
                {
                    dragXAmount += 0.01f * deltaTime;
                    if(dragXAmount > (-SELECTED_POSITION/uiRenderer.getCanvasWidth()))
                    {
                        dragXAmount = (-SELECTED_POSITION/uiRenderer.getCanvasWidth());
                    }
                }

                float currentXAndCounting = 0.0f;
                for(int i = 0; i < levelIcons.size(); i++)
                {
                    // levelIcons.get(i).setPosition(new Geometry.Point(currentXAndCounting + (viewWidth / 2.0f) - (levelIcons.get(i).getWidth() / 2.0f) + (dragXAmount * viewWidth), (viewHeight / 3.0f) + levelMenuLevelIconBannerOffset, 0.0f));
                    levelTexts.get(i).setPosition(new Geometry.Point(currentXAndCounting + (dragXAmount * uiRenderer.getCanvasWidth()), (uiRenderer.getCanvasHeight() / 3.0f) + levelMenuLevelIconBannerOffset - levelTexts.get(i).getHeight() - LEVEL_ICON_TEXT_OFFSET, 0.0f));

                    if(i != selected)
                    {
                        levelIcons.get(i).setDimensions(new Dimension2D(currentXAndCounting + (uiRenderer.getCanvasWidth() / 2.0f) - (levelIcons.get(i).getWidth() / 2.0f) + (dragXAmount * uiRenderer.getCanvasWidth()), (uiRenderer.getCanvasHeight() / 3.0f) + levelMenuLevelIconBannerOffset, iconWidths / 1.5f,
                                iconHeights / 1.5f));
                    }
                    else
                    {
                        levelIcons.get(i).setDimensions(new Dimension2D(currentXAndCounting + (uiRenderer.getCanvasWidth() / 2.0f) - (levelIcons.get(i).getWidth() / 2.0f) + (dragXAmount * uiRenderer.getCanvasWidth()), (uiRenderer.getCanvasHeight() / 3.0f) + levelMenuLevelIconBannerOffset, iconWidths,
                                iconHeights));
                    }
                    currentXAndCounting += LEVEL_ICON_OFFSET + iconWidths;
                }
                break;
        }
    }

    private void transitionToPage(PAGE page)
    {
        switchingToPage = page;
        transitionFadeOut = true;
    }

    private void animateTitle(float deltaTime)
    {
        sizeIncreaseRad += deltaTime * ANIMATE_SPEED;
        if(sizeIncreaseRad >= Math.PI)
        {
            sizeIncreaseRad -= Math.PI;
        }

        final float SIZE_INC_X = MAX_SIZE_INC * (float)Math.sin(sizeIncreaseRad);
        final float SIZE_INC_Y = SIZE_INC_X * (titleDimensions.h / titleDimensions.w);

        mainMenuTitle.setDimensions(new Dimension2D(titleDimensions.x - (SIZE_INC_X / 2.0f), titleDimensions.y + (SIZE_INC_Y / 2.0f), titleDimensions.w + SIZE_INC_X, titleDimensions.h + SIZE_INC_Y));
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
        switch(currentPage)
        {
            case MAIN_MENU:
                switch(pointerMotionEvent) {
                    case CLICK:
                        // Look through the UI and check if the pointer interacts with them
                        if (handlePointerControl(mainMenuPlayButton, pointer) ||
                                handlePointerControl(mainMenuSettingsButton, pointer) ||
                                handlePointerControl(mainMenuEndlessButton, pointer)) {
                            return;
                        }
                }
                break;
            case LEVEL_SELECT:
                switch(pointerMotionEvent) {
                    case CLICK:
                        if (handlePointerControl(selectLevelButton, pointer) ||
                                handlePointerControl(levelSelectBackButton, pointer))
                        {
                            return;
                        }

                        if(!levelSelectSlider.hasTouchFocus())
                        {
                            pointer.setControlOver(levelSelectSlider);
                        }
                        break;
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

    private void initMenuPage()
    {
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
                        transitionToPage(PAGE.LEVEL_SELECT);
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

        mainMenuPlayText = new Text("PLAY", 2, font, true);
        mainMenuPlayText.setColour(new Colour(0.85f, 0.85f, 0.85f));

        mainMenuSettingsText = new Text("SETTINGS", 2, font, true);
        mainMenuSettingsText.setColour(new Colour(0.85f, 0.85f, 0.85f));

        mainMenuEndlessText = new Text("ENDLESS", 2, font, true);
        mainMenuEndlessText.setColour(new Colour(0.85f, 0.85f, 0.85f));

        mainMenuVersionText = new Text("Version: " + Constants.VERSION_STRING, 2, font, false);
        mainMenuVersionText.setColour(new Colour(0.85f, 0.85f, 0.85f));

        mainMenuRendererGroup.addUI(mainMenuTitle);
        mainMenuRendererGroup.addUI(mainMenuPlayButton);
        mainMenuRendererGroup.addUI(mainMenuSettingsButton);
        mainMenuRendererGroup.addUI(mainMenuEndlessButton);
        mainMenuRendererGroup.addUI(mainMenuPlayText);
        mainMenuRendererGroup.addUI(mainMenuSettingsText);
        mainMenuRendererGroup.addUI(mainMenuEndlessText);
        mainMenuRendererGroup.addUI(mainMenuVersionText);

        playerModel = new Player(context, TextureHelper.loadTexture(context, R.drawable.player,
                true),
                0.0f, null, null, null);
        playerModel.setWaving(true);
        playerModel.setPosition(new Geometry.Point(4.6f, 0.0f, 7.76f));
        playerModel.addToRenderer(modelRenderer);
    }

    private void positionMenuPageElements(int viewWidth, int viewHeight)
    {
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
        titleDimensions = mainMenuTitle.getDimensions();

        mainMenuPlayButton.setDimensions(new Dimension2D((viewWidth/2.0f) -
                (BUTTON_SIZE / 2.0f) - BUTTON_PADDING - BUTTON_SIZE, (viewHeight / 2.0f) -
                (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE));

        mainMenuSettingsButton.setDimensions(
                new Dimension2D((viewWidth/2.0f) - (BUTTON_SIZE / 2.0f),
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE));

        mainMenuEndlessButton.setDimensions(
                new Dimension2D((viewWidth/2.0f) + (BUTTON_SIZE / 2.0f) + BUTTON_PADDING,
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE));
    }

    private void initLevelSelectPage()
    {
        // Add the level select menu elements
        levelMenuTitleText = new Text("LEVEL SELECT", 4, font, true, new Colour(1.0f, 1.0f, 1.0f));

        selectLevelButton = new Button(TextureHelper.loadTexture(context, R.drawable.button_go));
        selectLevelButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        transitionToPage(PAGE.GAMEPLAY);
                        break;
                }
            }
        });

        levelIcons = new ArrayList<>();
        levelTexts = new ArrayList<>();

        for(LevelIconData levelIconData : levelIconsData)
        {
            // Load in the image for the icon (height = LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT)
            levelIcons.add(new Image(
                    TextureHelper.loadTexture(context, levelIconData.getTextureResourceId())));
            levelTexts.add(new Text(levelIconData.getTitle(), 2, font, true));
        }

        for(Image levelIcon : levelIcons)
        {
            levelSelectMenuUIGroup.addUI(levelIcon);
        }

        for(Text levelText : levelTexts)
        {
            levelSelectMenuUIGroup.addUI(levelText);
        }

        levelSelectBackButton = new Button(TextureHelper.loadTexture(context,
                R.drawable.button_back_long));
        levelSelectBackButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    case CLICK:
                        audio.playSound(R.raw.button_click, 1);
                        transitionToPage(PAGE.MAIN_MENU);
                        break;
                }
            }
        });

        levelSelectSlider = new MenuLevelSelectSlider();
        levelSelectSlider.addTouchListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    // Allows for the user to look around by dragging there finger
                    case DOWN:
                        float normalizedX = (e.getPosition().x / uiRenderer.getCanvasWidth()) * 2 - 1;

                        if(draggingCompleted == false)
                        {
                            previousX = normalizedX;
                            draggingCompleted = true;
                        }

                        dragXAmount -= previousX - normalizedX;
                        previousX = normalizedX;

                        break;
                    case RELEASE:
                        draggingCompleted = false;

                        break;
                }
            }
        });

        levelSelectMenuUIGroup.addUI(levelMenuTitleText);
        levelSelectMenuUIGroup.addUI(selectLevelButton);
        levelSelectMenuUIGroup.addUI(levelSelectBackButton);

        // Add the level select menu background elements
        levelMenuBackgroundOverlay = new Image(new Colour(0.3f, 0.3f, 0.3f, 0.7f));
        levelSelectBackgroundUIGroup.addUI(levelMenuBackgroundOverlay);
    }

    private void positionLevelSelectPageElements(int viewWidth, int viewHeight)
    {
        levelMenuTitleText.generateText(uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight(), viewWidth);

        levelMenuTitleText.setPosition(new Geometry.Point(
                0.0f,
                viewHeight - levelMenuTitleText.getHeight() - LEVEL_SELECT_MENU_TITLE_PADDING,
                0.0f));

        selectLevelButton.setDimensions(new Dimension2D((viewWidth/2.0f) - (GO_BUTTON_WIDTH / 2.0f),
                viewHeight / 3.0f - BUTTON_GO_OFFSET - GO_BUTTON_HEIGHT,
                GO_BUTTON_WIDTH, GO_BUTTON_HEIGHT));

        // Figure out where to position the icons
        final float LEVEL_SELECT_LEVEL_ICONS_WIDTH = 960.0f;
        final float LEVEL_SELECT_LEVEL_ICONS_HEIGHT = 540.0f;
        final float WIDTH_HEIGHT_RATIO = LEVEL_SELECT_LEVEL_ICONS_WIDTH / LEVEL_SELECT_LEVEL_ICONS_HEIGHT;
        final float LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT = viewHeight / 2.0f;

        levelMenuLevelIconBannerOffset = (LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT - (LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f)) / 1.5f;
        iconWidths = LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f * WIDTH_HEIGHT_RATIO;
        iconHeights = LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f;

        for(Image levelIcon : levelIcons)
        {
            levelIcon.setDimensions(new Dimension2D(0.0f, 0.0f, iconWidths,
                    LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT * 0.7f));
        }

        for(Text levelText : levelTexts)
        {
            levelText.generateText(uiRenderer.getCanvasWidth(), uiRenderer.getCanvasHeight(),
                    viewWidth);
        }

        levelSelectBackButton.setDimensions(new Dimension2D(BUTTON_PADDING,
                viewHeight - (BUTTON_SIZE / 2.1f) - BUTTON_PADDING,
                BUTTON_SIZE, BUTTON_SIZE / 2.1f));

        levelMenuBackgroundOverlay.setDimensions(new Dimension2D(0.0f, viewHeight / 3.0f,
                viewWidth, LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT));
    }
}
