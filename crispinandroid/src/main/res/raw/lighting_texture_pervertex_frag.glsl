precision mediump float;

uniform sampler2D u_TextureUnit;
uniform vec4 u_Colour;
varying vec4 v_Colour;
varying vec2 v_TextureCoordinates;

void main()
{
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates) * u_Colour * v_Colour;
    //gl_FragColor = vec4(1.0f, 0.0f, 0.0f, 1.0f);
}