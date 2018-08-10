package com.christianbenner.crispinandroid.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.christianbenner.crispinandroid.data.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;
import static com.christianbenner.crispinandroid.util.Logger.errorf;

/**
 * Created by Christian Benner on 10/08/2017.
 */

public class TextureHelper {
    private static final String TAG = "TextureHelper";
    private static Map<Integer, Texture> cache = new HashMap<>();

    public static void updateAll(Context context)
    {
        ArrayList<Integer> resourceIds = new ArrayList<>();
        ArrayList<Boolean> lowQuality = new ArrayList<>();
        for(Map.Entry<Integer, Texture> entry : cache.entrySet())
        {
            resourceIds.add(entry.getKey());
            lowQuality.add(entry.getValue().isLowQuality());

            // Delete the old texture
            int[] tex = new int[1];
            tex[0] = entry.getValue().getTextureId();
            glDeleteTextures(1, tex, 0);
        }

        for(int i = 0; i < resourceIds.size(); i++)
        {
            Texture temp = loadNewTexture(context, resourceIds.get(i), lowQuality.get(i));
            if(temp != null)
            {
                cache.get(resourceIds.get(i)).setTextureId(temp.getTextureId());
                cache.get(resourceIds.get(i)).setDimensions(temp.getWidth(), temp.getHeight());
            }
        }
    }

    private static Texture loadNewTexture(Context context, int resourceId, boolean isLowQuality)
    {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);
        if(textureObjectIds[0] == 0){
            if(LoggerConfig.ON){
                Log.w(TAG, "Could not generate a new OpenGL Texture Object");
            }
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

        if(bitmap == null){
            if(LoggerConfig.ON){
                Log.w(TAG, "Resource ID " + resourceId + " could not be decoded");
            }

            glDeleteTextures(1, textureObjectIds, 0);
            errorf("%s - Failed to load resource: %d\n", TAG, resourceId);
            return null;
        }

        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);
        if(isLowQuality)
        {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }
        else
        {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        bitmap.recycle();

        glGenerateMipmap(GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);

        Texture texture = new Texture(textureObjectIds[0], bitmapWidth, bitmapHeight, isLowQuality);
        return texture;
    }

    public static Texture loadTexture(Context context, int resourceId, boolean isLowQuality){
        // look for the texture in the map to see if it exists
        if(cache.containsKey(resourceId))
        {
            return cache.get(resourceId);
        }
        else
        {
            Texture texture = loadNewTexture(context, resourceId, true);
            cache.put(resourceId, texture);
            return cache.get(resourceId);
        }
    }

    public static Texture loadTexture(Context context, int resourceId){
        // look for the texture in the map to see if it exists
        if(cache.containsKey(resourceId))
        {
            return cache.get(resourceId);
        }
        else
        {
            Texture texture = loadNewTexture(context, resourceId, false);
            cache.put(resourceId, texture);
            return cache.get(resourceId);
        }
    }

    // Needs to be done on activity resume
    public static void clearTextureCache()
    {
        cache.clear();
    }
}
