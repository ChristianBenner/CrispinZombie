package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.Matrix;
import android.view.View;

import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.crispin.crispinandroid2.GLSLShader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

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
                    "uniform float uTime;" +
            "void main() {" +
                    "vec4 colour = uColour;" +
                    "colour.r = colour.r * sin(uTime);" +
                    "colour.g = colour.g * cos(uTime);" +
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

    @Override
    protected void surfaceCreated() {
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
            TIME_UNIFORM_HANDLE = glslShader.getUniform("uTime");
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
    }

    float utime = 0.0f;
    float angle = 0.0f;
    float[] viewMatrix = new float[16];
    float[] fustrumMatrix = new float[16];

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

       // glUseProgram(SHADER_PROGRAM_ID);

        glslShader.enableIt();

        float[] orthoMatrix = {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };

        Geometry.Point position = new Geometry.Point(0.0f, 0.0f, 1.0f);
        Geometry.Vector right = new Geometry.Vector(1.0f, 0.0f, 0.0f);
        Geometry.Vector direction = new Geometry.Vector(0.0f, 0.0f, -1.0f);
        Geometry.Vector up = right.crossProduct(direction);

        Matrix.setLookAtM(viewMatrix, 0,
                // x y z
                position.x, position.y, position.z, // eye position
                position.x + direction.x, position.y + direction.y, position.z + direction.z, // center position
                up.x, up.y, up.z); // up position?

        Matrix.perspectiveM(fustrumMatrix, 0, 90f, width/height, 0.0f, 5.0f);

        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        angle += 1.0f;
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

        utime += 0.1f;
        glUniform1f(TIME_UNIFORM_HANDLE, utime);

        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);

        glDisableVertexAttribArray(POSITION_ATTRIBUTE_HANDLE);

        glslShader.disableIt();
    }

    @Override
    public void update(float deltaTime) {

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
