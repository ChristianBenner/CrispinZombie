package com.christianbenner.crispinandroid.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import static com.christianbenner.crispinandroid.util.Logger.errorf;

/**
 * Created by Christian Benner on 12/11/2017.
 */

public class Audio {
    Context context;

    // Store the current playing music ID
    private int currentMusicID = -1;
    static private int currentPosition = 0;
    private MediaPlayer mediaPlayer;

    // Store the loaded sound effects
    // Key = Resource Id, Value = Loaded
    private Map<Integer, Sound> soundList;
    private SoundPool soundPool;
    private AudioManager audioManager;

    private boolean musicInnit = false;
    private boolean soundInnit = false;

    private boolean trackFinished = true;

    public void changeContext(Context context){
        this.context = context;
    }

    public void initSoundChannel(Context context, int maxStreams){
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        soundList = new HashMap<Integer, Sound>();
        audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);

        soundInnit = true;
    }

    public void playSound(final int resourceId, int priority)
    {
        boolean exists = soundList.containsKey(resourceId);
        boolean isLoaded = false;

        if(exists)
        {
             isLoaded = soundList.get(resourceId).isLoaded();
        }

        if(!exists)
        {
            int soundId = soundPool.load(context, resourceId, priority);
            soundList.put(resourceId, new Sound(soundId, false));
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
            {
                @Override
                public void onLoadComplete(SoundPool soundPool, int soundId, int status)
                {
                    soundList.get(resourceId).setLoaded(true);
                    soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            });
        }
        else
        {
            if(!isLoaded)
            {
                errorf("Cannot play sound resource because it is being loaded.");
            }
            else
            {
                soundPool.play(soundList.get(resourceId).getSound(), 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }

    public SoundPool getSoundPool()
    {
        return soundPool;
    }

    public final void cleanSound() {
        soundList.clear();
        soundList = null;
        soundPool.release();
        soundPool = null;
    }

    public final void cleanMusic(){
        if(mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = null;
    }

    public final void cleanAll(){
        soundList.clear();
        soundList = null;
        soundPool.release();
        soundPool = null;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void initMusicChannel(Context context)
    {
        this.musicInnit = true;
        this.context = context;
    }

    public boolean isMusicInnit()
    {
        return this.musicInnit;
    }

    public boolean isSoundInnit()
    {
        return this.soundInnit;
    }

    public void playMusic(int resourceID){
        currentMusicID = resourceID;
        mediaPlayer = MediaPlayer.create(this.context, resourceID);
        mediaPlayer.start();
    }

    public void setLooping(boolean looping)
    {
        mediaPlayer.setLooping(looping);
    }

    public void pause(){
        System.out.println("Paused audio.");
        if(currentMusicID != -1 && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            currentPosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resume(){
        System.out.println("Playing audio from position " + currentPosition + ".");
        if(currentMusicID != -1 && !mediaPlayer.isPlaying()){
            mediaPlayer = MediaPlayer.create(this.context, currentMusicID);
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
        }
    }

    public void setOnMusicComlete(MediaPlayer.OnCompletionListener listener)
    {
        mediaPlayer.setOnCompletionListener(listener);
    }

    public void playMusic(int resourceID, int position){
        currentMusicID = resourceID;
        mediaPlayer = MediaPlayer.create(this.context, resourceID);
        mediaPlayer.seekTo(position);
        mediaPlayer.start();
    }

    public int getMusicPos(){
        if(currentMusicID != -1){
            return mediaPlayer.getCurrentPosition();
        }else{
            return 0;
        }
    }

    public void setVolume(float volume)
    {
        if(currentMusicID != -1 && mediaPlayer != null)
        {
            mediaPlayer.setVolume(volume, volume);
        }
    }
}
