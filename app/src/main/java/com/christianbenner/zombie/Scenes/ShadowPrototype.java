package com.christianbenner.zombie.Scenes;

import android.content.Context;
import android.opengl.Matrix;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Scene;
import com.crispin.crispinandroid2.GLSLShader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class ShadowPrototype extends Scene {
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
    static final float COLOUR_FLOOR[] = { 0.8f, 0.2f, 0.22f, 1.0f };

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
                    "void main() {" +
                    "vec4 colour = uColour;" +
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

    public ShadowPrototype(Context context)
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
            //TIME_UNIFORM_HANDLE = glslShader.getUniform("uTime");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        glEnable(GL_DEPTH_TEST);
        // Enable alpha blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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


        // Draw the floor
//        modelMatrix = new float[16];
//        Matrix.setIdentityM(modelMatrix, 0);
//        Matrix.rotateM(modelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
//        Matrix.translateM(modelMatrix, 0, 0.0f, -0.5f, 0.0f);
//
//        modelViewMatrix = new float[16];
//        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
//
//        modelViewProjectionMatrix = new float[16];
//        Matrix.multiplyMM(modelViewProjectionMatrix, 0, fustrumMatrix, 0, modelViewMatrix, 0);
//
//        glEnableVertexAttribArray(POSITION_ATTRIBUTE_HANDLE);
//        glVertexAttribPointer(POSITION_ATTRIBUTE_HANDLE,
//                ELEMENTS_PER_VERTEX,
//                GL_FLOAT,
//                false,
//                VERTEX_STRIDE,
//                VERTEX_BUFFER);
//
//        glUniform4fv(COLOUR_UNIFORM_HANDLE, 1, COLOUR_FLOOR, 0);
//
//        // glUniform1f(TIME_UNIFORM_HANDLE, utime);
//
//        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);




        // Check if has texture, if not just draw coloured square
        float[] modelMatrix2 = new float[16];
        Matrix.setIdentityM(modelMatrix2, 0);
        Matrix.translateM(modelMatrix2, 0, 0.0f, -0.5f, 0.0f);
        Matrix.rotateM(modelMatrix2, 0, 90.0f, 1.0f, 0.0f, 0.0f);

        float[] modelViewMatrix2 = new float[16];
        Matrix.multiplyMM(modelViewMatrix2, 0, viewMatrix, 0, modelMatrix2, 0);

        float[] modelViewProjectionMatrix2 = new float[16];
        Matrix.multiplyMM(modelViewProjectionMatrix2, 0, fustrumMatrix, 0, modelViewMatrix2, 0);

        glUniformMatrix4fv(MATRIX_UNIFORM_HANDLE, 1, false, modelViewProjectionMatrix2, 0);

        glUniform4fv(COLOUR_UNIFORM_HANDLE, 1, COLOUR_FLOOR, 0);

        glEnableVertexAttribArray(POSITION_ATTRIBUTE_HANDLE);
        glVertexAttribPointer(POSITION_ATTRIBUTE_HANDLE,
                ELEMENTS_PER_VERTEX,
                GL_FLOAT,
                false,
                VERTEX_STRIDE,
                VERTEX_BUFFER);

        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);

        glDisableVertexAttribArray(POSITION_ATTRIBUTE_HANDLE);

        glslShader.disableIt();
    }

    int i = 0;
    @Override
    public void update(float deltaTime) {
        angle += 1.0f * deltaTime;
        utime += 0.1f * deltaTime;

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
