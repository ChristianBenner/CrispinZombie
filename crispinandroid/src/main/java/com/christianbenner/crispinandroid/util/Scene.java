package com.christianbenner.crispinandroid.util;

import android.content.Context;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.ui.Pointer;

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

    /////// Game logic/GLES methods //////
    protected abstract void surfaceCreated();
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

    protected Context context;
    public Scene(Context context)
    {
        this.context = context;
    }

    // This is required because every time the surface destroyed OpenGL wipes the textures
    public void surfaceCreatedCall()
    {
        TextureHelper.updateAll(context);
        surfaceCreated();
    }

    private SparseArray<Pointer> mActivePointers = new SparseArray<>();
    public void motionEvent(View view, MotionEvent event)
    {
        // The index of the pointer in the event object
        final int POINTER_INDEX = event.getActionIndex();

        // The identifier of the pointer
        final int POINTER_ID = event.getPointerId(POINTER_INDEX);

        // The action of the motion event (masked means it can pick up pointers)
        final int ACTION_MASKED = event.getActionMasked();

        // See what the action is
        switch(ACTION_MASKED)
        {
            // The first finger down is not registered the same as the others, this gets all
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // Create a pointer object that keeps track of what UI is being interacted with
                final Pointer POINTER = new Pointer(POINTER_ID,
                        new PointF(event.getX(POINTER_INDEX),
                                view.getHeight() - event.getY(POINTER_INDEX)));

                // Check if the object doesn't exist in the pointer array
                if (mActivePointers.get(POINTER_ID) == null) {
                    // Add the pointer to the array
                    mActivePointers.put(POINTER_ID, POINTER);

                    // Handle UI touch press
                    motion(view, POINTER, PointerMotionEvent.CLICK);
                }
                break;
            case MotionEvent.ACTION_MOVE: {
                // A pointer moved, so update all the pointer positions
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (mActivePointers.get(event.getPointerId(i)) != null) {
                        mActivePointers.get(event.getPointerId(i)).
                                setPosition(new PointF(event.getX(i),
                                        view.getHeight()-event.getY(i)));
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                // Check if the pointer still exists in the array
                if(mActivePointers.get(POINTER_ID) != null)
                {
                    motion(view, mActivePointers.get(POINTER_ID), PointerMotionEvent.RELEASE);

                    // Tell the UI that the pointer has been released
                    mActivePointers.get(POINTER_ID).releaseControl();

                    // Remove the Pointer from the array as it is no longer in use
                    mActivePointers.remove(POINTER_ID);
                }

                break;
            }
        }

        // Check if there was drag in this event
        if(ACTION_MASKED == MotionEvent.ACTION_MOVE)
        {
            // Iterate through the pointers
            for(int i = 0; i < mActivePointers.size(); i++)
            {
                // Tell the pointer to handle drag
                if(mActivePointers.valueAt(i) != null)
                {
                    motion(view, mActivePointers.valueAt(i), PointerMotionEvent.DRAG);
                    mActivePointers.valueAt(i).handleDrag();
                }
            }
        }

        view.invalidate();
    }

    public enum PointerMotionEvent
    {
        CLICK,
        RELEASE,
        DRAG
    }

    public abstract void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent);

    public void onPause()
    {
        System.out.println("ACTION PAUSE");
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
        System.out.println("ACTION RESUME");
        //TextureHelper.updateAll(context);
    //    TextureHelper.clearTextureCache();
        resume();
    }

    public void onRestart()
    {
        System.out.println("ACTION RESTART");
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
        System.out.println("ACTION DESTROY");
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
            audio = Audio.getInstance();
        }
        audio.initMusicChannel(context);
        audio.playMusic(resourceID, musicPosition);
        audio.setLooping(loop);
    }

    protected void playSound(Context context, int resourceID, int priority)
    {
        if(audio == null)
        {
            audio = Audio.getInstance();
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
