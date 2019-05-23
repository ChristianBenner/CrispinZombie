package com.crispin.crispinandroid2;

import android.opengl.Matrix;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.util.Geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

public class ParticleEngine {
    // Interface acts as a lambda to provide programmer ability to add their own functionality for
// adding and updating particles. Adding particles could involve setting direction, speed or
// other attributes. Update function is responsible for what happens to the particle through its
// lifetime
    public interface ParticleAddFunctionality
    {
        Particle add();
    }

    public interface ParticleUpdateFunctionality
    {
        void update(Particle particle, float deltaTime);
    }

    static final float SQUARE_MODEL_VERTICES[] =
            {
                    -1.0f,-1.0f,-1.0f,
                    -1.0f,-1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f,-1.0f,
                    -1.0f,-1.0f,-1.0f,
                    -1.0f, 1.0f,-1.0f,

                    1.0f,-1.0f, 1.0f,
                    -1.0f,-1.0f,-1.0f,
                    1.0f,-1.0f,-1.0f,
                    1.0f, 1.0f,-1.0f,
                    1.0f,-1.0f,-1.0f,
                    -1.0f,-1.0f,-1.0f,

                    -1.0f,-1.0f,-1.0f,
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, 1.0f,-1.0f,
                    1.0f,-1.0f, 1.0f,
                    -1.0f,-1.0f, 1.0f,
                    -1.0f,-1.0f,-1.0f,

                    -1.0f, 1.0f, 1.0f,
                    -1.0f,-1.0f, 1.0f,
                    1.0f,-1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f,-1.0f,-1.0f,
                    1.0f, 1.0f,-1.0f,

                    1.0f,-1.0f,-1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f,-1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f,-1.0f,
                    -1.0f, 1.0f,-1.0f,
                    
                    1.0f, 1.0f, 1.0f,
                    -1.0f, 1.0f,-1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f,-1.0f, 1.0f
            };

    private ArrayList<Particle> particles;
    private ParticleAddFunctionality particleAddFunctionality;
    private ParticleUpdateFunctionality particleUpdateFunctionality;
    private Texture texture = null;

    // Float buffer that holds all the triangle co-ordinate data
    private FloatBuffer VERTEX_BUFFER;

    public ParticleEngine(ParticleAddFunctionality particleAddFunctionality,
                            ParticleUpdateFunctionality particleUpdateFunctionality)
    {
        this.particles = new ArrayList<>();
        this.particleAddFunctionality = particleAddFunctionality;
        this.particleUpdateFunctionality = particleUpdateFunctionality;

        // Initialise a vertex byte buffer for the shape float array
        final ByteBuffer VERTICES_BYTE_BUFFER = ByteBuffer.allocateDirect(
                SQUARE_MODEL_VERTICES.length * BYTES_PER_FLOAT);

        // Use the devices hardware's native byte order
        VERTICES_BYTE_BUFFER.order(ByteOrder.nativeOrder());

        // Create a Float buffer from the ByteBuffer
        VERTEX_BUFFER = VERTICES_BYTE_BUFFER.asFloatBuffer();

        // Add the array of floats to the buffer
        VERTEX_BUFFER.put(SQUARE_MODEL_VERTICES);

        // Set buffer to read the first co-ordinate
        VERTEX_BUFFER.position(0);
    }

    void setTexture(Texture texture)
    {
        this.texture = texture;
    }

    public void add()
    {
        particles.add(particleAddFunctionality.add());
    }

    public void update(float deltaTime)
    {
        for(int i = 0; i < particles.size(); i++)
        {
            particleUpdateFunctionality.update(particles.get(i), deltaTime);
            particles.get(i).currentLifeTime -= deltaTime;
            if(particles.get(i).currentLifeTime <= 0.0f)
            {
                particles.remove(particles.get(i));
                i--;
            }
        }
    }

    static final int ELEMENTS_PER_VERTEX = 3;
    static final int BYTES_PER_FLOAT = 4;
    static final int VERTEX_STRIDE = ELEMENTS_PER_VERTEX * BYTES_PER_FLOAT;
    static final int VERTEX_COUNT = SQUARE_MODEL_VERTICES.length / ELEMENTS_PER_VERTEX;
    public void draw(float[] viewMatrix, float[] fustrumMatrix, int matrixUniformHandle, int colourUniformHandle, int positionAttributeHandle)
    {
        for(Particle particle : particles)
        {
            // Check if has texture, if not just draw coloured square
            float[] modelMatrix = new float[16];
            Matrix.setIdentityM(modelMatrix, 0);

            final float size = particle.size;
            Matrix.scaleM(modelMatrix, 0, size, size, size);
            Matrix.translateM(modelMatrix, 0, particle.position.x, particle.position.y, particle.position.z);
            Matrix.rotateM(modelMatrix, 0, particle.angle, 0.0f, 1.0f, 0.0f);

            float[] modelViewMatrix = new float[16];
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);

            float[] modelViewProjectionMatrix = new float[16];
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, fustrumMatrix, 0, modelViewMatrix, 0);

            glUniformMatrix4fv(matrixUniformHandle, 1, false, modelViewProjectionMatrix, 0);

            glUniform4fv(colourUniformHandle, 1, new float[]{particle.colour.r, particle.colour.g, particle.colour.b, particle.colour.a}, 0);

            glEnableVertexAttribArray(positionAttributeHandle);
            glVertexAttribPointer(positionAttributeHandle,
                    ELEMENTS_PER_VERTEX,
                    GL_FLOAT,
                    false,
                    VERTEX_STRIDE,
                    VERTEX_BUFFER);

            glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);

            glDisableVertexAttribArray(positionAttributeHandle);
        }
    }

    // Ability to add own function for 'addParticle'
    // Ability to add own function for 'updateParticle'
}