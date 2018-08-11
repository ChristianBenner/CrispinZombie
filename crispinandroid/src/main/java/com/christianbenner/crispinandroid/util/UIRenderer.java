package com.christianbenner.crispinandroid.util;

import android.content.Context;
import android.opengl.Matrix;

import com.christianbenner.crispinandroid.ui.GLText;
import com.christianbenner.crispinandroid.ui.GLText2;
import com.christianbenner.crispinandroid.ui.UIBase;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGetBooleanv;
import static android.opengl.GLES20.glViewport;

/**
 * Created by Christian Benner on 24/03/2018.
 */

public class UIRenderer {
    private ShaderProgram shader;
    private float width;
    private float height;
    private float[] orthoMatrix = null;
    private float[] textOrthoMatrix = null;
    private ArrayList<UIBase> uiElements;
    private ArrayList<UIRendererGroup> groups;

    // Constructor for Rendering UI only
    public UIRenderer(Context context, int textureAtlas, int textureDetails)
    {
        shader = null;
        orthoMatrix = new float[16];
        this.uiElements = new ArrayList<>();
        this.groups = new ArrayList<>();
    }

    public void createUICanvas(int width, int height)
    {
        removeAll();
        this.width = (float)width;
        this.height = (float)height;

        // Set the OpenGL viewport to fill the entire surface
        glViewport(0, 0, width, height);

        // Initialise the ortho matrix
        if(orthoMatrix == null) {
            orthoMatrix = new float[16];
        }

        if(textOrthoMatrix == null)
        {
            textOrthoMatrix = new float[16];
        }

        // Create the ortho matrix with the width and height
        Matrix.orthoM(orthoMatrix, 0, 0, width,
                0.0f, height, 1f, -1f);

        // The text ortho is slightly different because of the way text verticies are generated
        Matrix.orthoM(textOrthoMatrix, 0, 0.0f, width, 0.0f, height, 0f, 1f);

        // Pass in ortho dimension data to text (to
        for(UIBase uiElement : uiElements)
        {
            if(uiElement instanceof GLText)
            {
                ((GLText) uiElement).setOrthoDimensions(width, height);
            }
        }

        // for all groups set ortho matrix
        for(UIRendererGroup group : groups)
        {
            group.setOrthoMatrix(orthoMatrix);
            group.setShader(shader);
        }
    }

    // for all groups added set ortho matrix
    public void addRendererGroup(UIRendererGroup group)
    {
        group.setOrthoMatrix(orthoMatrix);
        group.setShader(shader);
        this.groups.add(group);
    }

    public void removeRendererGroup(UIRendererGroup group)
    {
        this.groups.remove(group);
    }

    public void setShader(ShaderProgram shader)
    {
        this.shader = shader;

        for(UIRendererGroup group : groups)
        {
            group.setShader(shader);
        }
    }

    private boolean[] depthTest = new boolean[1];
    private float[] transformation = new float[16];
    private float[] modelMatrix = new float[16];
    public void render()
    {
        glGetBooleanv(GL_CULL_FACE, depthTest, 0);
        glDisable(GL_DEPTH_TEST);

        // Draw ui elements
        shader.useProgram();
        for(UIBase ui : uiElements)
        {
            ui.bindData(shader);
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, ui.getPosition().x, ui.getPosition().y, 0.0f);

            if(ui instanceof GLText2)
            {
                Matrix.scaleM(modelMatrix, 0, width/2f, height/2f, 1.0f);
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

        for(UIRendererGroup group : groups)
        {
            group.render();
        }

        if(depthTest[0] == true)
        {
            glEnable(GL_DEPTH_TEST);
        }

        shader.unbindProgram();
    }

    public void addUI(UIBase ui)
    {
        uiElements.add(ui);
    }

    public void removeUI(UIBase ui)
    {
        uiElements.remove(ui);
    }

    public float getCanvasWidth()
    {
        return width;
    }

    public float getCanvasHeight()
    {
        return height;
    }

    public void removeAll()
    {
        uiElements.clear();
        groups.clear();
    }
}
