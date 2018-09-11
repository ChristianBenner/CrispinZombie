package com.christianbenner.crispinandroid.ui;

import android.content.Context;

import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.crispinandroid.render.text.TextMeshCreator;
import com.christianbenner.crispinandroid.render.text.TextMesh;

/**
 * Created by Christian Benner on 13/12/2017.
 */

public class Font {
    private Texture textureAtlas;
    private TextMeshCreator meshCreator;

    public Font(Context context, int textureAtlas, int fontDetails)
    {
        // Load the font texture
        this.textureAtlas = TextureHelper.loadTexture(context, textureAtlas);
        this.meshCreator = new TextMeshCreator(context, fontDetails);
    }

    public Texture getTextureAtlas()
    {
        return textureAtlas;
    }

    public TextMesh loadText(Text text)
    {
        return meshCreator.createTextMesh(text);
    }
}
