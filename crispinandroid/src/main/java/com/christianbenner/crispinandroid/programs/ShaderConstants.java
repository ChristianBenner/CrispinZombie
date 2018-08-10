package com.christianbenner.crispinandroid.programs;

/**
 * Created by chris on 10/09/2017.
 */

public class ShaderConstants {
    public static final int NO_ATTRIBUTE = -1;

    // Uniform constants
    public static final String U_MATRIX = "u_Matrix";
    public static final String U_TEXTURE_UNIT = "u_TextureUnit";
    public static final String U_COLOUR = "u_Colour";
    public static final String U_TIME = "u_Time";
    public static final String A_FIRST_TEXTURE_COORDINATES = "a_FirstTextureCoordinates";
    public static final String A_SECOND_TEXTURE_COORDINATES = "a_SecondTextureCoordinates";
    public static final String U_AMBIENT_DISTANCE = "u_AmbientDistance";
    public static final String U_MIN_AMBIENT = "u_MinAmbient";
    public static final String U_MAX_AMBIENT = "u_MaxAmbient";
    public static final String U_AMBIENT_LIGHT_COLOUR = "u_AmbientLightColour";
    public static final String U_LIGHTS = "u_Lights";
    public static final String U_MV_MATRIX = "u_MVMatrix";
    public static final String U_MVP_MATRIX = "u_MVPMatrix";
    public static final String U_LIGHT_COLOURS_ARRAY = "u_LightColours";
    public static final String U_LIGHT_POSITIONS_ARRAY = "u_LightPositions";
    public static final String U_LIGHT_AMBIENT_DATA_ARRAY = "u_LightAmbientData";
    public static final String U_LIGHT_COUNT = "u_LightCount";

    // Attribute constants
    public static final String A_POSITION = "a_Position";
    public static final String A_COLOUR = "a_Colour";
    public static final String A_NORMAL = "a_Normal";
    public static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    public static final String U_FIRST_TEXTURE_UNIT = "u_FirstTextureUnit";
    public static final String U_SECOND_TEXTURE_UNIT = "u_SecondTextureUnit";

    // FLOATS PER TYPE
    public static final int FLOATS_PER_NORMAL = 3;
    public static final int FLOATS_PER_TEXEL = 2;
    public static final int FLOATS_PER_VERTEX = 3;
    public static final int BYTES_PER_FLOAT = 4;

    // STRIDES
    public static final int VERTEX_STRIDE = FLOATS_PER_VERTEX * BYTES_PER_FLOAT;
    public static final int NORMAL_STRIDE = FLOATS_PER_VERTEX * BYTES_PER_FLOAT;
    public static final int TEXEL_STRIDE = FLOATS_PER_VERTEX * BYTES_PER_FLOAT;

    // Lighting Stuff
    public static final float DEFAULT_AMBIENT_DISTANCE = 0.1f;
    public static final float DEFAULT_ATTENUATION = 0.4f;
    public static final float DEFAULT_MAX_AMBIENT = 10.0f;
}
