package com.christianbenner.crispinandroid.render.util;

import android.opengl.Matrix;

import com.christianbenner.crispinandroid.ui.Text;
import com.christianbenner.crispinandroid.ui.UIBase;

import java.util.ArrayList;

/**
 * Created by Christian Benner on 06/04/2018.
 */

public class UIRendererGroup {
    // Should we render or not
    private boolean render;

    // List of the UI elements in the group
    private ArrayList<UIBase> uiElements;

    // Reference to the UI rednerer
    private UIRenderer renderer;

    // Reference to the shader
    private ShaderProgram shader;

    // Matrix's
    private float[] orthoMatrix = new float[16];
    private float[] textOrthoMatrix = new float[16];
    private float[] transformation = new float[16];
    private float[] modelMatrix = new float[16];

    public UIRendererGroup(UIRenderer renderer, boolean renderingEnabled)
    {
        this.renderer = renderer;
        render = renderingEnabled;
        uiElements = new ArrayList<>();
    }

    public UIRendererGroup(UIRenderer renderer) {
        this(renderer, true);
    }

    // When the UIRenderer creates a canvas it should update the ortho matrix's
    public void setOrthoMatrix(float[] ortho, float[] textOrtho)
    {
        this.orthoMatrix = ortho;
        this.textOrthoMatrix = textOrtho;
    }

    // See UIRenderer to see how this render function works (the same one)
    public void render()
    {
        if(render)
        {
            for(UIBase ui : uiElements)
            {
                ui.bindData(shader);
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, ui.getPosition().x, ui.getPosition().y, 0.0f);

                if(ui instanceof Text)
                {
                    Matrix.scaleM(modelMatrix, 0, this.renderer.getCanvasWidth()/2f,
                            this.renderer.getCanvasHeight()/2f, 1.0f);
                    Matrix.multiplyMM(transformation, 0, textOrthoMatrix, 0, modelMatrix, 0);
                }
                else
                {

                    Matrix.scaleM(modelMatrix, 0, ui.getDimensions().w, ui.getDimensions().h, 0.0f);
                    Matrix.multiplyMM(transformation, 0, orthoMatrix, 0, modelMatrix, 0);
                }

                shader.setTextureUniforms(ui.getTexture().getTextureId());
                shader.setMatrix(transformation);
                shader.setColourUniforms(ui.getColour());
                ui.draw();
            }
        }
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
}
