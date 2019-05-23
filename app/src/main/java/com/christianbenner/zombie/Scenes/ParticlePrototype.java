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
import com.crispin.crispinandroid2.Particle;
import com.crispin.crispinandroid2.ParticleEngine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class ParticlePrototype extends Scene {
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

    public ParticlePrototype(Context context)
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
    ParticleEngine particleRenderer;

    @Override
    protected void surfaceCreated() {
        ParticleEngine.ParticleAddFunctionality addFunctionality = () -> {
            Random random = new Random();
            float velocityX = (random.nextFloat() * 0.4f) - 0.2f;
            float velocityY = (random.nextFloat() * 0.4f);
            float velocityZ = (random.nextFloat() * 0.4f) - 0.2f;
            float whitefilter = random.nextFloat() * 0.1f;
          return new Particle(new Geometry.Point(0.0f, 5f, 0.0f),
                  new Geometry.Vector(velocityX, velocityY,velocityZ),
                  0.05f,
                  50.0f,
                  new Colour(whitefilter, whitefilter, 0.8f + random.nextFloat() * 0.2f, 1.0f),
                  random.nextFloat() * 360.0f);
        };

        ParticleEngine.ParticleUpdateFunctionality updateFunctionality = (particle, deltaTime) -> {
            particle.position = particle.position.translate(particle.velocity.scale(deltaTime));
            particle.velocity = particle.velocity.translateY(-0.025f);
            particle.angle += 3.0f;

            particle.colour.a = particle.currentLifeTime / particle.maxLifeTime;
            if(particle.colour.a < 0.0f)
            {
                particle.colour.a = 0.0f;
            }

           // if(particle.position.y <= -10.0f)
           // {
           //     particle.velocity = particle.velocity.translateY(-2f * particle.velocity.y);
             //   particle.velocity = particle.velocity.scale(0.7f);
            //}
        };

        particleRenderer = new ParticleEngine(addFunctionality, updateFunctionality);

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

    float cameraAngle = 0.0f;
    @Override
    public void draw() {
        // Enable alpha blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glslShader.enableIt();

        Geometry.Point position = new Geometry.Point(0.0f, 0.0f, 1.0f);
        Geometry.Vector right = new Geometry.Vector(1.0f, 0.0f, 0.0f);
        Geometry.Vector direction = new Geometry.Vector(0.0f, 0.0f, -1.0f);
        Geometry.Vector up = right.crossProduct(direction);

        // rotate position around a center point
        Geometry.Point centerPoint = new Geometry.Point(0.0f, 0.0f, 0.0f);

        // distance from point
        float distance = 1.0f;

        cameraAngle += 0.005f;

        // calculate x and z values for a constant radius
        float x = distance * (float)Math.sin((double)cameraAngle);
        float z = distance * (float)Math.cos((double)cameraAngle);

        Matrix.setLookAtM(viewMatrix, 0,
                // x y z
                x, position.y, z, // eye position
                position.x + direction.x, position.y + direction.y, position.z + direction.z, // center position
                up.x, up.y, up.z); // up position?

        Matrix.perspectiveM(fustrumMatrix, 0, 90f, (float)width/height, 0.1f, 5.0f);

        particleRenderer.draw(viewMatrix, fustrumMatrix, MATRIX_UNIFORM_HANDLE, COLOUR_UNIFORM_HANDLE, POSITION_ATTRIBUTE_HANDLE);

        glslShader.disableIt();
    }

    int i = 0;
    @Override
    public void update(float deltaTime) {
        angle += 1.0f * deltaTime;
        utime += 0.1f * deltaTime;


        particleRenderer.update(deltaTime);

     //   if(i++ > 30)
        {
            Random random = new Random();
            particleRenderer.add();
            particleRenderer.add();
            particleRenderer.add();
            particleRenderer.add();
            particleRenderer.add();
            particleRenderer.add();
            particleRenderer.add();
            particleRenderer.add();
            particleRenderer.add();
      //      i = 0;
        }

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
