package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.RendererGroupType;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.shaders.ColourShaderProgram;
import com.christianbenner.crispinandroid.render.shaders.PerFragMultiLightingShader;
import com.christianbenner.crispinandroid.render.shaders.TextureShaderProgram;
import com.christianbenner.crispinandroid.render.util.Camera;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.RendererGroup;
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
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Audio;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.zombie.Constants;
import com.christianbenner.zombie.Entities.Bullet;
import com.christianbenner.zombie.Entities.Player;
import com.christianbenner.zombie.Objects.MenuLevelSelectSlider;
import com.christianbenner.zombie.R;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;

/**
 * Created by Christian Benner on 19/11/2017.
 */

public class SceneMenu extends Scene {
    private Audio audio;
    private static int audioPos = 0;
    private UIRenderer uiRenderer;
    private UIRendererGroup mainMenuUIGroup;
    private UIRendererGroup levelSelectMenuUIGroup;
    private UIRendererGroup levelSelectBackgroundUIGroup;
    private UIRendererGroup transitionOverlayUIGroup;
    private TextureShaderProgram uiShader;

    private Texture mainMenuTitleTexture;
    private Image mainMenuTitle;
    private Button mainMenuPlayButton;
    private Button mainMenuEndlessButton;
    private Button mainMenuSettingsButton;
    private Text mainMenuPlayText;
    private Text mainMenuSettingsText;
    private Text mainMenuEndlessText;
    private Text mainMenuVersionText;

    private Image levelMenuBackgroundOverlay;
    private Text levelMenuTitleText;
    private MenuLevelSelectSlider levelSelectSlider;
    private Button selectLevelButton;

    private float levelMenuLevelIconBannerOffset;

    final private int BUTTON_SIZE = 300;
    final private int BUTTON_PADDING = 30;
    final private int TEXT_PADDING = 10;
    final private int LEVEL_SELECT_MENU_TITLE_PADDING = 20;
    final private int LEVEL_ICON_TEXT_OFFSET = 5;
    final private int LEVEL_ICON_OFFSET = 20;
    final private int BUTTON_GO_OFFSET = 20;
    final private int GO_BUTTON_WIDTH = 400;
    final private int GO_BUTTON_HEIGHT = 200;

    private Dimension2D titleDimensions;

    private Camera camera;
    private Renderer renderer;
    private PerFragMultiLightingShader shader;
    private ColourShaderProgram colourShader;
    private Player player;
    private ArrayList<Bullet> bullets;
    private RendererGroup bulletsGroup;

    private float transitionTimer = 0.0f;
    private final float TRANSITION_TIME_GOAL = 40.0f;
    private TRANSITION_TYPE transitionType = TRANSITION_TYPE.NONE;
    private PAGE page = PAGE.MAIN_MENU;
    private Image transitionOverlay;
    private float transitionOverlayAlpha = 0.0f;
    private boolean transitionBackRequired = false;

    private float viewWidth;
    private float viewHeight;

    enum PAGE
    {
        NONE,
        MAIN_MENU,
        LEVEL_SELECT,
        SETTINGS
    }

    enum TRANSITION_TYPE
    {
        NONE,
        PLAY,
        SETTINGS,
        ENDLESS,
        SELECTED_MAP
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

    private ArrayList<LevelIconData> levelIconsData;
    private ArrayList<Image> levelIcons;
    private ArrayList<Text> levelTexts;
    private boolean draggingCompleted = false;
    private float previousX = 0.0f;
    private float dragXAmount = 0.0f;
    private float iconWidths = 0.0f;
    private float iconHeights = 0.0f;

    public SceneMenu(Context context)
    {
        super(context);

        levelIconsData = new ArrayList<>();
        levelIconsData.add(new LevelIconData(R.drawable.demo_map_icon, R.raw.demo_map4, "demo_map"));
        levelIconsData.add(new LevelIconData(R.drawable.unavailable_map_icon, R.raw.demo_map4, "unavailable1"));
        levelIconsData.add(new LevelIconData(R.drawable.unavailable_map_icon, R.raw.demo_map4, "unavailable2"));

        levelSelectSlider = new MenuLevelSelectSlider();
        levelSelectSlider.addTouchListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch (e.getEvent())
                {
                    // Allows for the user to look around by dragging there finger
                    case DOWN:
                        float normalizedX = (e.getPosition().x / (float) viewWidth) * 2 - 1;

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

        uiRenderer = new UIRenderer(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        mainMenuUIGroup = new UIRendererGroup(uiRenderer);
        levelSelectMenuUIGroup = new UIRendererGroup(uiRenderer);
        levelSelectBackgroundUIGroup = new UIRendererGroup(uiRenderer);
        transitionOverlayUIGroup = new UIRendererGroup(uiRenderer);

        shader = new PerFragMultiLightingShader(context);
        colourShader = new ColourShaderProgram(context);

        camera = new Camera();
        camera.setPosition(new Geometry.Point(5.0f, 1.0f, 8.9f));
        camera.setAngles(-3.314f, -0.38f);
        renderer = new Renderer(shader, camera);

        bullets = new ArrayList<>();
        bulletsGroup = new RendererGroup(RendererGroupType.SAME_BIND_SAME_TEX);

        player = new Player(context, TextureHelper.loadTexture(context, R.drawable.player, true),
                0.0f, bullets, bulletsGroup);
        player.setWaving(true);
        player.setPosition(new Geometry.Point(4.6f, 0.0f, 7.76f));
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
        this.viewWidth = width;
        this.viewHeight = height;

        uiRenderer.createUICanvas(width, height);

        uiShader = new TextureShaderProgram(context);
        colourShader = new ColourShaderProgram(context);

        uiRenderer.setShader(uiShader);
        mainMenuUIGroup.setShader(uiShader);
        levelSelectMenuUIGroup.setShader(uiShader);
        levelSelectBackgroundUIGroup.setShader(colourShader);
        transitionOverlayUIGroup.setShader(colourShader);

        shader = new PerFragMultiLightingShader(context);
        renderer.setShader(shader);
        camera.viewChanged(width, height);
        renderer.setCamera(camera);

        initUI(width, height);
    }

    private void initUI(int viewWidth, int viewHeight)
    {
        // Add the main menu elements
        mainMenuTitleTexture = TextureHelper.loadTexture(context, R.drawable.title);

        mainMenuTitle = new Image(new Dimension2D((viewWidth / 2.0f) - (mainMenuTitleTexture.getWidth()),
                viewHeight - (mainMenuTitleTexture.getHeight() * 2.0f) - 100,
                mainMenuTitleTexture.getWidth() * 2.0f,
                mainMenuTitleTexture.getHeight() * 2.0f), mainMenuTitleTexture);
        titleDimensions = mainMenuTitle.getDimensions();

        mainMenuPlayButton = new Button(
                new Dimension2D((viewWidth/2.0f) - (BUTTON_SIZE / 2.0f) - BUTTON_PADDING - BUTTON_SIZE, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_play));
        mainMenuPlayButton.addButtonListener(new TouchListener() {
            @Override
            public void touchEvent(TouchEvent e) {
                switch(e.getEvent())
                {
                    case CLICK:
                        playSound(context, R.raw.button_click, 1);
                        transitionType = TRANSITION_TYPE.PLAY;
                        break;
                }
            }
        });

        mainMenuSettingsButton = new Button(
                new Dimension2D((viewWidth/2.0f) - (BUTTON_SIZE / 2.0f),
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_settings));
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

        mainMenuEndlessButton = new Button(
                new Dimension2D((viewWidth/2.0f) + (BUTTON_SIZE / 2.0f) + BUTTON_PADDING,
                        (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f), BUTTON_SIZE, BUTTON_SIZE),
                TextureHelper.loadTexture(context, R.drawable.button_endless));
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
        mainMenuPlayText = new Text("PLAY", 2, font, viewWidth, uiRenderer, true);
        mainMenuPlayText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        mainMenuPlayText.setPosition(new Geometry.Point(-BUTTON_PADDING - BUTTON_SIZE, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) - mainMenuPlayText.getHeight() - TEXT_PADDING, 0.0f));

        mainMenuSettingsText = new Text("SETTINGS", 2, font, viewWidth, uiRenderer, true);
        mainMenuSettingsText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        mainMenuSettingsText.setPosition(new Geometry.Point(0.0f, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) - mainMenuPlayText.getHeight() - TEXT_PADDING, 0.0f));

        mainMenuEndlessText = new Text("ENDLESS", 2, font, viewWidth, uiRenderer, true);
        mainMenuEndlessText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        mainMenuEndlessText.setPosition(new Geometry.Point(BUTTON_PADDING + BUTTON_SIZE, (viewHeight / 2.0f) - (BUTTON_SIZE / 2.0f) - mainMenuPlayText.getHeight() - TEXT_PADDING, 0.0f));

        mainMenuVersionText = new Text("Version: " + Constants.VERSION_STRING, 2, font, viewWidth, uiRenderer, false);
        mainMenuVersionText.setColour(new Colour(0.25f, 0.25f, 0.25f));
        mainMenuVersionText.setPosition(new Geometry.Point(viewWidth - 350.0f, 5.0f, 0.0f));

        mainMenuUIGroup.addUI(mainMenuTitle);
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
                        transitionType = TRANSITION_TYPE.SELECTED_MAP;
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

        for(LevelIconData levelIconData : levelIconsData)
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

        levelSelectMenuUIGroup.addUI(levelMenuTitleText);
        levelSelectMenuUIGroup.addUI(selectLevelButton);

        // Add the level select menu background elements
        levelMenuBackgroundOverlay = new Image(new Dimension2D(0.0f, viewHeight / 3.0f, viewWidth, LEVEL_SELECT_BACKGROUND_BANNER_HEIGHT), new Colour(0.3f, 0.3f, 0.3f, 0.7f));
        levelSelectBackgroundUIGroup.addUI(levelMenuBackgroundOverlay);

        // Add the transition overlay element
        transitionOverlay = new Image(new Dimension2D(0.0f, 0.0f, viewWidth, viewHeight), new Colour(0.0f, 0.0f, 0.0f, 0.0f));
        transitionOverlayUIGroup.addUI(transitionOverlay);
        uiRenderer.addRendererGroup(transitionOverlayUIGroup);
    }

    @Override
    public void draw() {
/*        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        */

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glClearColor(red, green, blue, 1.0f);
        renderer.render();
        uiRenderer.render();

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
    float dragVelocity = 0.0f;

    @Override
    public void update(float deltaTime) {
        switch (page)
        {
            case MAIN_MENU:
                mainMenuPlayButton.update(deltaTime);
                mainMenuSettingsButton.update(deltaTime);
                mainMenuEndlessButton.update(deltaTime);

                player.update(deltaTime);

                // Handle the transition to the next scene
                if(transitionType != TRANSITION_TYPE.NONE)
                {
                    transitionTimer += deltaTime;
                    transitionOverlayAlpha = transitionTimer / TRANSITION_TIME_GOAL;
                    if(transitionOverlayAlpha >= 1.0f)
                    {
                        transitionOverlayAlpha = 1.0f;
                    }
                    transitionOverlay.setAlpha(transitionOverlayAlpha);

                    if(transitionTimer >= TRANSITION_TIME_GOAL)
                    {
                        // Finished
                        switch (transitionType)
                        {
                            case PLAY:
                                uiRenderer.removeRendererGroup(mainMenuUIGroup);
                                player.removeFromRenderer(renderer);

                                // Add the level select render group (move transition group
                                // so that it gets render over the top)
                                uiRenderer.removeRendererGroup(transitionOverlayUIGroup);
                                uiRenderer.addRendererGroup(levelSelectBackgroundUIGroup);
                                uiRenderer.addRendererGroup(levelSelectMenuUIGroup);
                                uiRenderer.addRendererGroup(transitionOverlayUIGroup);

                                page = PAGE.LEVEL_SELECT;
                                transitionBackRequired = true;
                                transitionType = TRANSITION_TYPE.NONE;
                                break;
                            case SETTINGS:

                                break;
                            case ENDLESS:

                                break;
                        }
                    }
                }

                x += deltaTime * 0.03f;
                if(x >= Math.PI)
                {
                    x -= Math.PI;
                }

                final float SIZE_INC_X = MAX_SIZE_INC * (float)Math.sin(x);
                final float SIZE_INC_Y = SIZE_INC_X * (titleDimensions.h / titleDimensions.w);

                mainMenuTitle.setDimensions(new Dimension2D(titleDimensions.x - (SIZE_INC_X / 2.0f), titleDimensions.y + (SIZE_INC_Y / 2.0f), titleDimensions.w + SIZE_INC_X, titleDimensions.h + SIZE_INC_Y));
                break;
            case LEVEL_SELECT:
                if(transitionBackRequired)
                {
                    transitionTimer -= deltaTime;
                    transitionOverlayAlpha = transitionTimer / TRANSITION_TIME_GOAL;
                    if(transitionOverlayAlpha <= 0.0f)
                    {
                        transitionOverlayAlpha = 0.0f;
                        transitionBackRequired = false;
                    }

                    transitionOverlay.setAlpha(transitionOverlayAlpha);
                }

                switch(transitionType)
                {
                    case SELECTED_MAP:
                        transitionTimer += deltaTime;
                        transitionOverlayAlpha = transitionTimer / TRANSITION_TIME_GOAL;
                        if(transitionOverlayAlpha >= 1.0f)
                        {
                            transitionOverlayAlpha = 1.0f;
                        }
                        transitionOverlay.setAlpha(transitionOverlayAlpha);

                        if(transitionTimer >= TRANSITION_TIME_GOAL)
                        {
                            gotoScene(Constants.GAME_ID);
                        }
                        break;
                }

                // Decrease to the closest center point
                System.out.println("Drag X Converted: " + (-dragXAmount * viewWidth));
                int selected = (int)(((-dragXAmount * viewWidth) + (iconWidths / 2.0f)) / (iconWidths + LEVEL_ICON_OFFSET));

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
                if(-dragXAmount * viewWidth < SELECTED_POSITION && levelSelectSlider.hasTouchFocus() == false)
                {
                    dragXAmount -= 0.01f * deltaTime;
                    if(dragXAmount < (-SELECTED_POSITION/viewWidth))
                    {
                        dragXAmount = (-SELECTED_POSITION/viewWidth);
                    }
                }

                if(-dragXAmount * viewWidth > SELECTED_POSITION && levelSelectSlider.hasTouchFocus() == false)
                {
                    dragXAmount += 0.01f * deltaTime;
                    if(dragXAmount > (-SELECTED_POSITION/viewWidth))
                    {
                        dragXAmount = (-SELECTED_POSITION/viewWidth);
                    }
                }

                float currentXAndCounting = 0.0f;
                for(int i = 0; i < levelIcons.size(); i++)
                {
                   // levelIcons.get(i).setPosition(new Geometry.Point(currentXAndCounting + (viewWidth / 2.0f) - (levelIcons.get(i).getWidth() / 2.0f) + (dragXAmount * viewWidth), (viewHeight / 3.0f) + levelMenuLevelIconBannerOffset, 0.0f));
                    levelTexts.get(i).setPosition(new Geometry.Point(currentXAndCounting + (dragXAmount * viewWidth), (viewHeight / 3.0f) + levelMenuLevelIconBannerOffset - levelTexts.get(i).getHeight() - LEVEL_ICON_TEXT_OFFSET, 0.0f));

                    if(i != selected)
                    {
                        levelIcons.get(i).setDimensions(new Dimension2D(currentXAndCounting + (viewWidth / 2.0f) - (levelIcons.get(i).getWidth() / 2.0f) + (dragXAmount * viewWidth), (viewHeight / 3.0f) + levelMenuLevelIconBannerOffset, iconWidths / 1.5f,
                                iconHeights / 1.5f));
                    }
                    else
                    {
                        levelIcons.get(i).setDimensions(new Dimension2D(currentXAndCounting + (viewWidth / 2.0f) - (levelIcons.get(i).getWidth() / 2.0f) + (dragXAmount * viewWidth), (viewHeight / 3.0f) + levelMenuLevelIconBannerOffset, iconWidths,
                                iconHeights));
                    }
                    currentXAndCounting += LEVEL_ICON_OFFSET + iconWidths;
                }

             //   gotoScene(Constants.GAME_ID);
                break;
            case SETTINGS:

                break;
        }

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
                switch (page)
                {
                    case MAIN_MENU:
                        // Look through the UI and check if the pointer interacts with them
                        if (handlePointerControl(mainMenuPlayButton, pointer)) {
                            return;
                        }
                        if (handlePointerControl(mainMenuSettingsButton, pointer)) {
                            return;
                        }
                        if (handlePointerControl(mainMenuEndlessButton, pointer)) {
                            return;
                        }

                        break;
                    case SETTINGS:

                        break;
                    case LEVEL_SELECT:
                        if (handlePointerControl(selectLevelButton, pointer)) {
                            return;
                        }

                        if(!levelSelectSlider.hasTouchFocus())
                        {
                            pointer.setControlOver(levelSelectSlider);
                        }
                        break;
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
