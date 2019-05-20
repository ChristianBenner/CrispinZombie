package com.crispin.crispinandroid2;

import android.opengl.GLException;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_CURRENT_PROGRAM;
import static android.opengl.GLES20.GL_FALSE;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_INVALID_VALUE;
import static android.opengl.GLES20.GL_TRUE;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetIntegerv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUseProgram;

public class GLSLShader {
    private final int PROGRAM_ID;

    public GLSLShader(String vertexShaderCode, String fragmentShaderCode) throws Exception {
        PROGRAM_ID = createProgram(vertexShaderCode, fragmentShaderCode);
    }

    public int getAttribute(String attributeName)
    {
        return glGetAttribLocation(PROGRAM_ID, attributeName);
    }

    public int getUniform(String uniformName)
    {
        return glGetUniformLocation(PROGRAM_ID, uniformName);
    }

    public void enableIt()
    {
        glUseProgram(PROGRAM_ID);
    }

    public void disableIt()
    {
        glUseProgram(0);
    }

    private static int createProgram(String vertexShaderCode, String fragmentShaderCode) throws Exception
    {
        // Create a vertex shader
        final int VERTEX_SHADER_ID = loadShader(GL_VERTEX_SHADER, vertexShaderCode);

        // Create a fragment shader
        final int FRAGMENT_SHADER_ID = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Create the shader program
        final int PROGRAM_ID = glCreateProgram();

        // Throw exception on failure
        if (PROGRAM_ID == 0) {
            throw new Exception("OpenGLES failed to generate a program object");
        }

        // Attach the vertex and fragment shaders to the program
        glAttachShader(PROGRAM_ID, VERTEX_SHADER_ID);
        glAttachShader(PROGRAM_ID, FRAGMENT_SHADER_ID);

        // Link the shaders
        glLinkProgram(PROGRAM_ID);

        return PROGRAM_ID;
    }

    // Resolve the name of a type of shader
    private static String typeToString(int type)
    {
        switch (type)
        {
            case GL_VERTEX_SHADER:
                return "GL_VERTEX_SHADER";
            case GL_FRAGMENT_SHADER:
                return "GL_FRAGMENT_SHADER";
            default:
                return "UNDEFINED";
        }
    }

    private static int loadShader(int type, String shaderCode) throws Exception
    {
        // Create a shader of the specified type and get the ID
        final int SHADER = glCreateShader(type);

        // Check if the shader ID is invalid
        if (SHADER == 0) {
            // Failed to generate shader object
            throw new Exception("OpenGLES failed to generate " +
                    typeToString(type) +
                    " shader object");
        }

        // Upload the source to the shader
        glShaderSource(SHADER, shaderCode);

        // Compile that shader object
        glCompileShader(SHADER);

        // Check compilation
        if(!isCompiled(SHADER))
        {
            throw new Exception("OpenGLES failed to compile " +
                    typeToString(type) +
                    "shader program");
        }

        return SHADER;
    }

    // Returns true if shader object is valid and compiled successfully
    private static boolean isCompiled(int shaderObjectID) {
        // Place to store the vertex shader compilation status
        final int[] SHADER_COMPILATION_STATUS = new int[1];

        // Get the vertex shader compilation status
        glGetShaderiv(shaderObjectID,
                GL_COMPILE_STATUS,
                SHADER_COMPILATION_STATUS,
                0);

        // Return true if shader compilation successful
        return SHADER_COMPILATION_STATUS[0] == GL_TRUE;
    }
}
