package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.crispinandroid.ui.GLFont;
import com.christianbenner.crispinandroid.ui.GLText;
import com.christianbenner.zombie.R;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Christian Benner on 13/12/2017.
 */

public class SceneStarWarsTest extends Scene {
    private Context context;

    // Data
    private GLText coolText;
    private GLText fpsText;

    private GLFont font;

    // MATRIX
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    public SceneStarWarsTest(Context context)
    {
        this.context = context;
    }

    @Override
    public void surfaceCreated() {
        font = new GLFont(context, R.drawable.arial_font, R.raw.arial_font_fnt);

        // Create some text
        coolText = new GLText(context, "What the fuck did you just fucking say about me, " +
                "you little bitch? I'll have you know I graduated top of my class in the Navy" +
                " Seals, and I've been involved in numerous secret raids on Al-Quaeda, and I" +
                " have over 300 confirmed kills. I am trained in gorilla warfare and I'm the" +
                " top sniper in the entire US armed forces. You are nothing to me but just " +
                "another target. I will wipe you the fuck out with precision the likes of " +
                "which has never been seen before on this Earth, mark my fucking words. You " +
                "think you can get away with saying that shit to me over the Internet? Think " +
                "again, fucker. As we speak I am contacting my secret network of spies across" +
                " the USA and your IP is being traced right now so you better prepare for the " +
                "storm, maggot. The storm that wipes out the pathetic little thing you call your" +
                " life. You're fucking dead, kid. I can be anywhere, anytime, and I can kill you" +
                " in over seven hundred ways, and that's just with my bare hands. Not only am I" +
                " extensively trained in unarmed combat, but I have access to the entire arsenal" +
                " of the United States Marine Corps and I will use it to its full extent to wipe" +
                " your miserable ass off the face of the continent, you little shit. If only you" +
                " could have known what unholy retribution your little \"clever\" comment was " +
                "about to bring down upon you, maybe you would have held your fucking tongue. " +
                "But you couldn't, you didn't, and now you're paying the price, you goddamn " +
                "idiot. I will shit fury all over you and you will drown in it. You're fucking" +
                " dead, kiddo.", 4, font, 1.0f, 1920f, 1080f, true);
        coolText.setColour(new Colour(1.0f, 1.0f, 0.0f));

        fpsText = new GLText(context, "fps: x", 4, font, 1.0f, 1920f ,1080f, false);
        fpsText.setColour(new Colour(1.0f, 1.0f, 1.0f));

        // Play a tune
        //playMusic(context, R.raw.starwars);
    }

    @Override
    public void surfaceChanged(int width, int height) {
        // Set the OpenGL viewport to fill the entire surface
        glViewport(0, 0, width, height);

        /* Use OpenGL or MatrixHelper class
        PerspectiveM creates a frustrum matrix that allows objects within
        a specific and realistic viewpoint to be rendered - the further
        things are, the smaller
          */
        Matrix.perspectiveM(projectionMatrix, 0, 45, (float) width
                / (float) height, 0.1f, 10f);
        Matrix.setLookAtM(viewMatrix, 0,
                // x y z
                0f, 0f, 3f, // eye position
                0f, 0f, 0f, // center position
                0f, 1.0f, 0f); // up position?
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    float zVal = 4.0f;

    int frames = 0;
    long startTime = 0;
    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_ALPHA);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, -0.5f, zVal);
        rotateM(modelMatrix, 0, 75.0f, -1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
        zVal -= 0.005f;

        //coolText.render(modelViewProjectionMatrix);

        if(startTime == 0)
        {
            startTime = System.currentTimeMillis();
        }
        frames++;
        if(System.currentTimeMillis() - startTime >= 1000)
        {
            fpsText.setText("fps: " + frames);
            frames = 0;
            startTime = System.currentTimeMillis();
        }

      //  fpsText.render(-1.0f, -1.0f);
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
    public void motionEvent(View view, MotionEvent event)
    {

    }
}
