package com.christianbenner.crispinandroid.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Christian Benner on 19/11/2017.
 */

public abstract class Scene {
    private boolean init = false;
    private boolean finishedWithRenderer = false;
    private int nextSceneId = 0;
    private int sceneId = 0;

    protected Audio audio;
    private static int musicPosition = 0;
    private int maxAudioStreams = 10;

    // Game logic/GLES methods
    public abstract void surfaceCreated();
    public abstract void surfaceChanged(int width, int height);
    public abstract void draw();

    ///////////////////////////////////////////////////////////////////////////////////////////
    // name:    update
    // brief:   The update function for each scene
    // details: An overridden function that each scene will use to update logic
    // param:   float deltaTime - A value used for logic timing to make sure that objects or data
    //          dependent on the system time are positioned correctly.
    // return:  int - The next scene ID
    ///////////////////////////////////////////////////////////////////////////////////////////
    public abstract void update(float deltaTime);

    // Activity lifecycle methods
    protected abstract void pause();
    protected abstract void resume();
    protected abstract void restart();
    protected abstract void destroy();

    public abstract void motionEvent(View view, MotionEvent event);

    public void onPause()
    {
        init = false;
        pause();

        if(audio != null)
        {
            musicPosition = audio.getMusicPos();
            audio.pause();
        }
    }

    public void onResume(Context context)
    {
        //TextureHelper.updateAll(context);
    //    TextureHelper.clearTextureCache();
        resume();
    }

    public void onRestart()
    {
        init = false;
        restart();

        if(audio != null)
        {
            musicPosition = audio.getMusicPos();
            audio.pause();
        }
    }

    public void onDestroy()
    {
        init = false;
        destroy();

        if(audio != null)
        {
            audio.cleanMusic();
        }
    }

    protected void playMusic(Context context, int resourceID)
    {
        playMusic(context, resourceID, false);
    }

    protected void playMusic(Context context, int resourceID, boolean loop)
    {
        if(audio == null)
        {
            audio = new Audio();
        }
        audio.initMusicChannel(context);
        audio.playMusic(resourceID, musicPosition);
        audio.setLooping(loop);
    }

    protected void playSound(Context context, int resourceID, int priority)
    {
        if(audio == null)
        {
            audio = new Audio();
        }

        if(!audio.isSoundInnit())
        {
            audio.initSoundChannel(context, maxAudioStreams);
        }

        audio.playSound(resourceID, priority);
    }

    protected void setVolume(float volume)
    {
        audio.setVolume(volume);
    }

    public void setInit(boolean state)
    {
        this.init = state;
    }

    public boolean isInit()
    {
        return this.init;
    }

    public int getSceneId()
    {
        return sceneId;
    }

    public void gotoScene(int nextSceneId)
    {
        setNextSceneId(nextSceneId, true);
    }

    public void setNextSceneId(int nextSceneId, boolean changeNow)
    {
        this.nextSceneId = nextSceneId;
        this.finishedWithRenderer = changeNow;
    }

    public void endScene(boolean finish)
    {
        this.finishedWithRenderer = finish;
    }

    public boolean isSceneFinished()
    {
        return this.finishedWithRenderer;
    }

    public int getNextSceneId()
    {
        return nextSceneId;
    }
}
