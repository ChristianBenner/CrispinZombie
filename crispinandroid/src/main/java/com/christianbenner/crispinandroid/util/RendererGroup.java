package com.christianbenner.crispinandroid.util;

import com.christianbenner.crispinandroid.data.objects.RendererModel;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.Matrix.multiplyMM;

/**
 * Created by Christian Benner on 22/03/2018.
 */

public class RendererGroup {
    private RendererGroupType type;
    private ArrayList<RendererModel> models;
    private ShaderProgram shader;
    private Camera camera;

    private float[] mvMatrix;
    private float[] mvpMatrix;
    public RendererGroup(RendererGroupType rendererGroupType)
    {
        this.type = rendererGroupType;
        this.models = new ArrayList<>();
        this.mvMatrix = new float[16];
        this.mvpMatrix = new float[16];
    }

    public void setShader(ShaderProgram shader)
    {
        this.shader = shader;
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera;
    }

    public RendererGroupType getType()
    {
        return this.type;
    }

    public void addModel(RendererModel model)
    {
        this.models.add(model);
    }

    public void removeModel(RendererModel model)
    {
        this.models.remove(model);
    }

    public void render() {
        if(type == RendererGroupType.SAME_BIND) {
            boolean boundData = false;
            for(RendererModel model : models) {
                // Bind the model data once
                if(boundData == false) {
                    model.bindData(this.shader);
                    boundData = true;
                }

                // Calculate the mv and mvp matrix for the model
                multiplyMM(mvMatrix, 0, camera.getViewMatrix(), 0,
                        model.getModelMatrix(), 0);
                multiplyMM(mvpMatrix, 0, camera.getProjectionMatrix(), 0,
                        mvMatrix, 0);

                // Pass Data to shader
                shader.setMVMatrixUniform(mvMatrix);
                shader.setMVPMatrixUniform(mvpMatrix);
                shader.setColourUniforms(model.getColour());
                if(model.isTexelsLoaded())
                {
                    shader.setTextureUniforms(model.getTextureId());
                }

                // Draw Model
                glDrawArrays(GL_TRIANGLES, 0, model.getVertexCount());
            }
        }
        else if(type == RendererGroupType.SAME_TEX)
        {
            boolean boundTexture = false;
            for(RendererModel model : models)
            {
                model.bindData(shader);

                // Calculate the mv and mvp matrix for the model
                multiplyMM(mvMatrix, 0, camera.getViewMatrix(), 0,
                        model.getModelMatrix(), 0);
                multiplyMM(mvpMatrix, 0, camera.getProjectionMatrix(), 0,
                        mvMatrix, 0);

                // Pass Data to shader
                shader.setMVMatrixUniform(mvMatrix);
                shader.setMVPMatrixUniform(mvpMatrix);
                shader.setColourUniforms(model.getColour());
                if(model.isTexelsLoaded() && boundTexture == false)
                {
                    shader.setTextureUniforms(model.getTextureId());
                    boundTexture = true;
                }

                // Draw Model
                glDrawArrays(GL_TRIANGLES, 0, model.getVertexCount());
            }
        }
        else if(type == RendererGroupType.SAME_BIND_SAME_TEX)
        {
            boolean boundData = false;
            boolean boundTexture = false;
            for(RendererModel model : models)
            {
                if(boundData == false)
                {
                    model.bindData(shader);
                    boundData = true;
                }

                // Calculate the mv and mvp matrix for the model
                multiplyMM(mvMatrix, 0, camera.getViewMatrix(), 0,
                        model.getModelMatrix(), 0);
                multiplyMM(mvpMatrix, 0, camera.getProjectionMatrix(), 0,
                        mvMatrix, 0);

                // Pass Data to shader
                shader.setMVMatrixUniform(mvMatrix);
                shader.setMVPMatrixUniform(mvpMatrix);
                shader.setColourUniforms(model.getColour());
                if(model.isTexelsLoaded() && boundTexture == false)
                {
                    shader.setTextureUniforms(model.getTextureId());
                    boundTexture = true;
                }

                // Draw Model
                glDrawArrays(GL_TRIANGLES, 0, model.getVertexCount());
            }
        }
    }
}
