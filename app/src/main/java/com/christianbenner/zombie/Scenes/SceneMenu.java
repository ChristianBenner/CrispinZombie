package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.util.Audio;
import com.christianbenner.crispinandroid.util.Scene;

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

    Context context;
    public SceneMenu(Context context)
    {
        this.context = context;
    }

    @Override
    public void surfaceCreated() {
        // Create audio
        audio = new Audio();
        audio.initMusicChannel(context);
       // audio.playMusic(R.raw.no_words, audioPos);
    }

    @Override
    public void surfaceChanged(int width, int height) {

    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(red, green, blue, 1.0f);
    }

    float red = 0.0f;
    boolean redb = true;
    float green = 0.5f;
    boolean greenb = true;
    float blue = 1.0f;
    boolean blueb = false;

    @Override
    public void update(float deltaTime) {
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
    public void motionEvent(View view, MotionEvent event) {

    }
}
