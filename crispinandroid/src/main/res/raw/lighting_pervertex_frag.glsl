precision mediump float;

uniform vec4 u_Colour;
varying vec4 v_Colour;

void main()
{
    gl_FragColor = u_Colour * v_Colour;
}