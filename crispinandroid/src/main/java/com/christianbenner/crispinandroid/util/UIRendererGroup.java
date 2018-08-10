package com.christianbenner.crispinandroid.util;

import android.opengl.Matrix;

import com.christianbenner.crispinandroid.ui.GLText;
import com.christianbenner.crispinandroid.ui.UIBase;

import java.util.ArrayList;

/**
 * Created by Christian Benner on 06/04/2018.
 */

public class UIRendererGroup {
    private boolean render;
    private ArrayList<UIBase> uiElements;
    private UIRenderer renderer;
    private float[] orthoMatrix = new float[16];
    private ShaderProgram shader;

    public UIRendererGroup(UIRenderer renderer, boolean renderingEnabled)
    {
        this.renderer = renderer;
        render = renderingEnabled;
        uiElements = new ArrayList<>();
    }

    public UIRendererGroup(UIRenderer renderer) {
        this(renderer, true);
    }

    public void setOrthoMatrix(float[] ortho)
    {
        this.orthoMatrix = ortho;
    }

    public void setShader(ShaderProgram shader)
    {
        this.shader = shader;
    }

    public void enableRendering()
    {
        render = true;
    }

    public void disableRendering()
    {
        render = false;
    }

    public void addUI(UIBase ui)
    {
        uiElements.add(ui);
    }

    public void removeUI(UIBase ui)
    {
        uiElements.remove(ui);
    }

    public void clear()
    {
        uiElements.clear();
    }

    public void render()
    {
        if(render)
        {
            for(UIBase ui : uiElements)
            {
                ui.bindData(shader);

                float[] modelMatrix = new float[16];
                Matrix.setIdentityM(modelMatrix, 0);

                // UI x,y
                Matrix.translateM(modelMatrix, 0, ui.getPosition().x, ui.getPosition().y, 0.0f);

                // UI Width and height
                if(ui instanceof GLText)
                {
                    Matrix.scaleM(modelMatrix, 0, renderer.getCanvasWidth()/2.0f,
                            renderer.getCanvasHeight()/2.0f, 0.0f);
                }
                else
                {
                    Matrix.scaleM(modelMatrix, 0, ui.getDimensions().w, ui.getDimensions().h, 0.0f);
                }


                float[] transformation = new float[16];
                Matrix.multiplyMM(transformation, 0, orthoMatrix, 0, modelMatrix, 0);

                shader.setTextureUniforms(ui.getTexture().getTextureId());
                shader.setMatrix(transformation);
                shader.setColourUniforms(ui.getColour());
                ui.draw();
            }
        }
    }
}
