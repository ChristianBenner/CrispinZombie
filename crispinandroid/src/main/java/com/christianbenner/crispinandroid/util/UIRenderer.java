package com.christianbenner.crispinandroid.util;

import android.content.Context;
import android.opengl.Matrix;

import com.christianbenner.crispinandroid.ui.GLText;
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
 *
 * UIRenderer is a class that allows the user to efficiently and easily group UI rendering objects
 * with some extra functionality. UIRenderer will also position and scale your UI elements for you.
 */
public class UIRenderer {
    // The shader program that will be used to render the UI
    private ShaderProgram shader;

    // Canvas properties
    private float canvasWidth;
    private float canvasHeight;
    private boolean uiCanvasCreated = false;

    // Render objects and groups
    private ArrayList<UIBase> uiElements;
    private ArrayList<UIRendererGroup> groups;

    // Matrix's
    private float[] orthoMatrix = new float[16];
    private float[] textOrthoMatrix = new float[16];
    private float[] transformation = new float[16];
    private float[] modelMatrix = new float[16];

    // Constructor for Rendering UI only
    public UIRenderer(Context context, int textureAtlas, int textureDetails)
    {
        shader = null;

        uiElements = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public void createUICanvas(int width, int height)
    {
        // On creation of a new UI canvas we should remove the old UI elements
        removeAll();

        // Set the width and height of the canvas
        this.canvasWidth = (float)width;
        this.canvasHeight = (float)height;

        // Set the OpenGL viewport to fill the entire surface
        glViewport(0, 0, width, height);

        // Initialise the ortho matrix
        // Create the ortho matrix with the width and height
        Matrix.orthoM(orthoMatrix, 0, 0, width,
                0.0f, height, 1f, -1f);

        // The text ortho is slightly different because of the way text verticies are generated
        Matrix.orthoM(textOrthoMatrix, 0, 0.0f, width, 0.0f, height, 0f, 1f);

        // Set the created state as true
        uiCanvasCreated = true;
    }

    // Add a new UI renderer group to the UI Renderer
    public void addRendererGroup(UIRendererGroup group)
    {
        // Make sure that the group has the ortho matrix and shader
        group.setOrthoMatrix(orthoMatrix, textOrthoMatrix);
        group.setShader(shader);
        this.groups.add(group);
    }

    // Remove a UI renderer group from the renderer
    public void removeRendererGroup(UIRendererGroup group)
    {
        this.groups.remove(group);
    }

    // Set the shader of the UI renderer
    public void setShader(ShaderProgram shader)
    {
        this.shader = shader;

        // Need to set the shader for each group
        for(UIRendererGroup group : groups)
        {
            group.setShader(shader);
        }
    }

    public void render()
    {
        // Used to determine if OpenGL is currently using GL_DEPTH_TEST
        boolean[] depthTest = new boolean[1];
        glGetBooleanv(GL_CULL_FACE, depthTest, 0);
        glDisable(GL_DEPTH_TEST);

        // Enable the shader
        shader.useProgram();

        // Render single UI elements
        for(UIBase ui : uiElements)
        {
            ui.bindData(shader);
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, ui.getPosition().x, ui.getPosition().y, 0.0f);

            /* Text has to be treated slightly differently due to the way it's built...
            see the GLText class comment for more information.
             */
            if(ui instanceof GLText)
            {
                Matrix.scaleM(modelMatrix, 0, canvasWidth/2f, canvasHeight/2f, 1.0f);
                Matrix.multiplyMM(transformation, 0, textOrthoMatrix, 0, modelMatrix, 0);
            }
            else
            {

                Matrix.scaleM(modelMatrix, 0, ui.getDimensions().w, ui.getDimensions().h, 0.0f);
                Matrix.multiplyMM(transformation, 0, orthoMatrix, 0, modelMatrix, 0);
            }

            // Pass the uniforms for each UI to the shader
            shader.setTextureUniforms(ui.getTexture().getTextureId());
            shader.setMatrix(transformation);
            shader.setColourUniforms(ui.getColour());

            // Run the GPU draw call
            ui.draw();
        }

        // Render each group
        for(UIRendererGroup group : groups)
        {
            group.render();
        }

        // If depth was on then re-enable it
        if(depthTest[0] == true)
        {
            glEnable(GL_DEPTH_TEST);
        }

        // Stop using the shader program
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
        return canvasWidth;
    }

    public float getCanvasHeight()
    {
        return canvasHeight;
    }

    public void removeAll()
    {
        uiElements.clear();
        groups.clear();
    }

    public boolean hasUICanvas()
    {
        return uiCanvasCreated;
    }
}
