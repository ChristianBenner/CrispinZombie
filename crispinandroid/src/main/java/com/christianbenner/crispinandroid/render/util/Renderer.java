package com.christianbenner.crispinandroid.render.util;

import android.opengl.GLES20;

import com.christianbenner.crispinandroid.render.data.Light;
import com.christianbenner.crispinandroid.render.model.RendererModel;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_INVALID_VALUE;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.Matrix.multiplyMM;

/**
 * Created by Christian Benner on 22/03/2018.
 */

public class Renderer {
    private ShaderProgram shader;
    private ArrayList<RendererModel> models;
    private ArrayList<RendererGroup> groups;
    private ArrayList<Light> lights;
    private Camera camera;

    private float[] mvMatrix;
    private float[] mvpMatrix;
    public Renderer(ShaderProgram shader, Camera camera)
    {
        this.lights = new ArrayList<>();
        this.groups = new ArrayList<>();
        setShader(shader);
        this.camera = camera;
        this.models = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.mvMatrix = new float[16];
        this.mvpMatrix = new float[16];
    }

    public void render()
    {
        // Render the models with the shader
        shader.useProgram();
        shader.setLightUniforms();

        for(RendererGroup group : groups)
        {
            group.render();
        }

        for(RendererModel model : models)
        {
            drawModel(model);
        }

        shader.unbindProgram();
    }

    private void drawModel(RendererModel model)
    {
      //  GLES20.glBindBuffer(GL_ARRAY_BUFFER, model.getVBO());
        model.bindData(shader);

        if(model.isTexelsLoaded())
        {
            shader.setTextureUniforms(model.getTextureId());
        }

        multiplyMM(mvMatrix, 0, camera.getViewMatrix(), 0,
                model.getModelMatrix(), 0);
        multiplyMM(mvpMatrix, 0, camera.getProjectionMatrix(), 0,
                mvMatrix, 0);

        shader.setMVMatrixUniform(mvMatrix);
        shader.setMVPMatrixUniform(mvpMatrix);
        shader.setColourUniforms(model.getColour());

        glDrawArrays(GL_TRIANGLES, 0, model.getVertexCount());
    }

    public void setShader(ShaderProgram shader)
    {
        this.shader = shader;
        this.shader.setLights(lights);
        for(RendererGroup group : groups)
        {
            group.setShader(this.shader);
        }
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera;
        for(RendererGroup group : groups)
        {
            group.setCamera(camera);
        }
    }

    public void addLight(Light light)
    {
        this.lights.add(light);
    }

    public void removeLight(Light light)
    {
        this.lights.remove(light);
    }

    public void addModel(RendererModel model)
    {
        this.models.add(model);
    }

    public void removeModel(RendererModel model)
    {
        this.models.remove(model);
    }

    public void addGroup(RendererGroup group)
    {
        this.groups.add(group);
        group.setShader(this.shader);
        group.setCamera(this.camera);
    }

    public void removeGroup(RendererGroup group)
    {
        this.groups.remove(group);
    }
}
