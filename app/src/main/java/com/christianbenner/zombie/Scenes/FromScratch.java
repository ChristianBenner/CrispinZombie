package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.Matrix;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.data.Texture;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.crispin.crispinandroid2.GLSLShader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

class ParticleRenderer {
    static final float SQUARE_MODEL_VERTICES[] =
    {
            // Front face
            -1f, -1f, 0.0f,
            -1f, 1f, 0.0f,
            1f, 1f, 0.0f,
            -1f, -1f, 0.0f,
            1f, 1f, 0.0f,
            1f, -1f, 0.0f,
    };

    private ArrayList<Particle> particles;
    private ParticleAddFunctionality particleAddFunctionality;
    private ParticleUpdateFunctionality particleUpdateFunctionality;
    private Texture texture = null;

    // Float buffer that holds all the triangle co-ordinate data
    private FloatBuffer VERTEX_BUFFER;

    public ParticleRenderer(ParticleAddFunctionality particleAddFunctionality,
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

    void add()
    {
        particles.add(particleAddFunctionality.add());
    }

    void update(float deltaTime)
    {
        for(Particle particle : particles)
        {
            particleUpdateFunctionality.update(particle, deltaTime);
        }
    }

    static final int ELEMENTS_PER_VERTEX = 3;
    static final int BYTES_PER_FLOAT = 4;
    static final int VERTEX_STRIDE = ELEMENTS_PER_VERTEX * BYTES_PER_FLOAT;
    static final int VERTEX_COUNT = SQUARE_MODEL_VERTICES.length / ELEMENTS_PER_VERTEX;
    void draw(float[] viewMatrix, float[] fustrumMatrix, int matrixUniformHandle, int colourUniformHandle, int positionAttributeHandle)
    {
        for(Particle particle : particles)
        {
            // Check if has texture, if not just draw coloured square
            float[] modelMatrix = new float[16];
            Matrix.setIdentityM(modelMatrix, 0);

            final float size = particle.size;
            Matrix.scaleM(modelMatrix, 0, size, size, size);
            Matrix.translateM(modelMatrix, 0, particle.position.x, particle.position.y, particle.position.z);

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
};

// This should be a data only class that contains values such as direction, speed
class Particle {
    public float maxLifeTime;
    public float currentLifeTime;
    public Geometry.Point position;
    public float size; // size of the side of the square
    public Geometry.Vector velocity;
    public Colour colour;
    // Particle should have a circle texture (square for now though)

    // Square

    public Particle(Geometry.Point startPosition, Geometry.Vector startVelocity, float size, float life, Colour colour)
    {
        this.position = startPosition;
        this.velocity = startVelocity;
        this.size = size;
        this.maxLifeTime = life;
        this.currentLifeTime = 0.0f;
        this.colour = colour;
    }
};

// Interface acts as a lambda to provide programmer ability to add their own functionality for
// adding and updating particles. Adding particles could involve setting direction, speed or
// other attributes. Update function is responsible for what happens to the particle through its
// lifetime
interface ParticleAddFunctionality
{
    Particle add();
}

interface ParticleUpdateFunctionality
{
    void update(Particle particle, float deltaTime);
}


public class FromScratch extends Scene {

    private static final int FROM_SCRATCH_SCENE_ID = 192521;

    static final int ELEMENTS_PER_VERTEX = 3;
    static final int BYTES_PER_FLOAT = 4;
    static final int VERTEX_STRIDE = ELEMENTS_PER_VERTEX * BYTES_PER_FLOAT;

    static final float TRIANGLE_COORDS[] =
    {
            // Front face
            -0.5f, -0.5f, 0.0f,
            -0.5f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
    };

    static final int VERTEX_COUNT = TRIANGLE_COORDS.length / ELEMENTS_PER_VERTEX;

    static final float COLOUR[] = { 0.64f, 0.77f, 0.22f, 1.0f };

    // Float buffer that holds all the triangle co-ordinate data
    private FloatBuffer VERTEX_BUFFER;

    // The vertex shader program
    private static final String VERTEX_SHADER_CODE =
            "attribute vec4 vPosition;" +
            "uniform mat4 uMatrix;" +
            "void main() {" +
                    "gl_Position = uMatrix * vPosition;" +
            "}";

    // The fragment shader code
    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "uniform vec4 uColour;" +
               //     "uniform float uTime;" +
            "void main() {" +
                    "vec4 colour = uColour;" +
                 //   "colour.r = colour.r * sin(uTime);" +
                 //   "colour.g = colour.g * cos(uTime);" +
                    "gl_FragColor = colour;" +
            "}";

    // ID of the shader program
    private int SHADER_PROGRAM_ID;

    // Position attribute handle in the shader
    private int POSITION_ATTRIBUTE_HANDLE;

    // Colour attribute handle in the shader
    private int COLOUR_UNIFORM_HANDLE;

    // Matrix attribute handle in the shader
    private int MATRIX_UNIFORM_HANDLE;

    // Time uniform handle
    private int TIME_UNIFORM_HANDLE;

    public FromScratch(Context context)
    {
        super(context, FROM_SCRATCH_SCENE_ID);
    }

    public static int loadShader(int type, String shaderCode)
    {
        // Create a shader of the specified type and get the ID
        final int SHADER = glCreateShader(type);

        // Upload the source to the shader
        glShaderSource(SHADER, shaderCode);

        // Compile that shader object
        glCompileShader(SHADER);

        return SHADER;
    }

    GLSLShader glslShader;
    ParticleRenderer particleRenderer;

    @Override
    protected void surfaceCreated() {
        ParticleAddFunctionality addFunctionality = () -> {
            Random random = new Random();
            float velocityX = (random.nextFloat() * 0.4f) - 0.2f;
            float velocityY = (random.nextFloat() * 0.4f) - 0.1f;
          return new Particle(new Geometry.Point(0.0f, 0.0f, 0.0f),
                  new Geometry.Vector(velocityX, velocityY,0.0f),
                  0.05f,
                  10.0f,
                  new Colour(0.5f + random.nextFloat() * (1.0f - 0.5f), 0.0f, 0.0f, 1.0f));
        };

        ParticleUpdateFunctionality updateFunctionality = (particle, deltaTime) -> {
            particle.position = particle.position.translate(particle.velocity.scale(deltaTime));
            particle.velocity = particle.velocity.translateY(-0.05f);
        };

        particleRenderer = new ParticleRenderer(addFunctionality, updateFunctionality);

        // Initialise a vertex byte buffer for the shape float array
        final ByteBuffer VERTICES_BYTE_BUFFER = ByteBuffer.allocateDirect(
                TRIANGLE_COORDS.length * BYTES_PER_FLOAT);

        // Use the devices hardware's native byte order
        VERTICES_BYTE_BUFFER.order(ByteOrder.nativeOrder());

        // Create a Float buffer from the ByteBuffer
        VERTEX_BUFFER = VERTICES_BYTE_BUFFER.asFloatBuffer();

        // Add the array of floats to the buffer
        VERTEX_BUFFER.put(TRIANGLE_COORDS);

        // Set buffer to read the first co-ordinate
        VERTEX_BUFFER.position(0);

        try {
            glslShader = new GLSLShader(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
            POSITION_ATTRIBUTE_HANDLE = glslShader.getAttribute("vPosition");
            COLOUR_UNIFORM_HANDLE = glslShader.getUniform("uColour");
            MATRIX_UNIFORM_HANDLE = glslShader.getUniform("uMatrix");
            //TIME_UNIFORM_HANDLE = glslShader.getUniform("uTime");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        // Vertex shader ID
//        final int VERTEX_SHADER_ID =
//                loadShader(GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
//
//        // Fragment shader ID
//        final int FRAGMENT_SHADER_ID =
//                loadShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);
//
//        // Create the shader program
//        SHADER_PROGRAM_ID = glCreateProgram();
//
//        // Attach the vertex and fragment shaders to the program
//        glAttachShader(SHADER_PROGRAM_ID, VERTEX_SHADER_ID);
//        glAttachShader(SHADER_PROGRAM_ID, FRAGMENT_SHADER_ID);
//
//        // Link the shaders
//        glLinkProgram(SHADER_PROGRAM_ID);
//
//        glUseProgram(SHADER_PROGRAM_ID);
//        POSITION_ATTRIBUTE_HANDLE = glGetAttribLocation(SHADER_PROGRAM_ID, "vPosition");
//        COLOUR_UNIFORM_HANDLE = glGetUniformLocation(SHADER_PROGRAM_ID, "uColor");
    }

    int width = 0;
    int height = 0;
    @Override
    public void surfaceChanged(int width, int height) {
        this.width = width;
        this.height = height;

        // Set the OpenGL viewport to fill the entire surface
        glViewport(0, 0, width, height);
    }

    float utime = 0.0f;
    float angle = 0.0f;
    float[] viewMatrix = new float[16];
    float[] fustrumMatrix = new float[16];

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

       // glUseProgram(SHADER_PROGRAM_ID);

        glslShader.enableIt();

        Geometry.Point position = new Geometry.Point(0.0f, 0.0f, 1.0f);
        Geometry.Vector right = new Geometry.Vector(1.0f, 0.0f, 0.0f);
        Geometry.Vector direction = new Geometry.Vector(0.0f, 0.0f, -1.0f);
        Geometry.Vector up = right.crossProduct(direction);

        Matrix.setLookAtM(viewMatrix, 0,
                // x y z
                position.x, position.y, position.z, // eye position
                position.x + direction.x, position.y + direction.y, position.z + direction.z, // center position
                up.x, up.y, up.z); // up position?

        Matrix.perspectiveM(fustrumMatrix, 0, 90f, (float)width/height, 0.0f, 5.0f);

        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f);

        float[] modelViewMatrix = new float[16];
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        float[] modelViewProjectionMatrix = new float[16];
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, fustrumMatrix, 0, modelViewMatrix, 0);

        glUniformMatrix4fv(MATRIX_UNIFORM_HANDLE, 1, false, modelViewProjectionMatrix, 0);

        glEnableVertexAttribArray(POSITION_ATTRIBUTE_HANDLE);
        glVertexAttribPointer(POSITION_ATTRIBUTE_HANDLE,
                ELEMENTS_PER_VERTEX,
                GL_FLOAT,
                false,
                VERTEX_STRIDE,
                VERTEX_BUFFER);

        glUniform4fv(COLOUR_UNIFORM_HANDLE, 1, COLOUR, 0);

       // glUniform1f(TIME_UNIFORM_HANDLE, utime);

        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);

        glDisableVertexAttribArray(POSITION_ATTRIBUTE_HANDLE);

        particleRenderer.draw(viewMatrix, fustrumMatrix, MATRIX_UNIFORM_HANDLE, COLOUR_UNIFORM_HANDLE, POSITION_ATTRIBUTE_HANDLE);

        glslShader.disableIt();
    }

    @Override
    public void update(float deltaTime) {
        angle += 1.0f * deltaTime;
        utime += 0.1f * deltaTime;

        particleRenderer.update(deltaTime);

        particleRenderer.add();
    }

    @Override
    protected void pause() {

    }

    @Override
    protected void resume() {

    }

    @Override
    protected void restart() {

    }

    @Override
    protected void destroy() {

    }

    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent) {

    }
}
