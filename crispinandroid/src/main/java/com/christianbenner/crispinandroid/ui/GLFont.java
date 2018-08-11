package com.christianbenner.crispinandroid.ui;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.crispinandroid.util.TextMeshCreator;
import com.christianbenner.crispinandroid.data.TextMesh;

/**
 * Created by Christian Benner on 13/12/2017.
 */

public class GLFont {
    private Texture textureAtlas;
    private TextMeshCreator meshCreator;

    public GLFont(Context context, int textureAtlas, int fontDetails)
    {
        // Load the font texture
        this.textureAtlas = TextureHelper.loadTexture(context, textureAtlas);
        this.meshCreator = new TextMeshCreator(context, fontDetails);
    }

    public Texture getTextureAtlas()
    {
        return textureAtlas;
    }

    public TextMesh loadText(GLText text)
    {
        return meshCreator.createTextMesh(text);
    }

    public TextMesh loadText(GLText2 text)
    {
        return meshCreator.createTextMesh(text);
    }
}
