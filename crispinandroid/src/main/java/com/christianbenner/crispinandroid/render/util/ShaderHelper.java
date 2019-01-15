package com.christianbenner.crispinandroid.render.util;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_INVALID_OPERATION;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * Created by Christian Benner on 03/08/2017.
 */

public class ShaderHelper {
    public static int compileShader(int type, String shaderCode){
        final int shaderObjectId = glCreateShader(type);

        // Check if the object could be created
        if(shaderObjectId == 0){
            System.out.println("[ShaderHelper] ERROR: Could not create new shader");
            return 0;
        }

        // Point the object to the code
        glShaderSource(shaderObjectId, shaderCode);

        // Compile the code
        glCompileShader(shaderObjectId);

        // Check if the shader compiled
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        if(compileStatus[0] == 0)
        {
            System.out.println("[ShaderHelper] ERROR: shader compilation failed: \n" + shaderCode +
                    "\n: " + glGetShaderInfoLog(shaderObjectId));

            // Shader failed
            glDeleteShader(shaderObjectId);
        }

        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId){
        final int programObjectId = glCreateProgram();

        // Check if the program object was created
        if(programObjectId == 0){
            System.out.println("[ShaderHelper] ERROR: Failed to create program object");
            return 0;
        }

        // Attach and link the shaders
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);
        glLinkProgram(programObjectId);

        // Check if the shaders linked without failing
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        if(linkStatus[0] == 0)
        {
            System.out.println("[ShaderHelper] ERROR: Failed to link shaders:\n" +
                    glGetProgramInfoLog(programObjectId));

            return 0;
        }

        return programObjectId;
    }

    private static void checkValidation(int program)
    {
        // Validate the program to check if it built
        glValidateProgram(program);

        final int[] validateStatus = new int[1];
        glGetProgramiv(program, GL_VALIDATE_STATUS, validateStatus, 0);

        if(validateStatus[0] == 0)
        {
            System.out.println("[ShaderHelper] ERROR: Failed to validate shader program (" +
                    validateStatus[0] + ")");
            System.out.println("Shader Info Log: " + glGetProgramInfoLog(program));
        }
    }

    public static void buildProgram(String vertexShaderCode,
                                   String fragmentShaderCode,
                                   ShaderProgram program)
    {
        System.out.println("BUILDING A SHADER");
        System.out.println("VERTEX CODE: " + vertexShaderCode);
        System.out.println("FRAGMENT CODE: " + fragmentShaderCode);
        program.vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderCode);
        program.fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderCode);
        program.program = ShaderHelper.linkProgram(program.vertexShader, program.fragmentShader);

        // Check if the shader program is valid
        checkValidation(program.program);
    }
}
