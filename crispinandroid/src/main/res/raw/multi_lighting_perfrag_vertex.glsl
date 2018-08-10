precision mediump float;

uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_TextureCoordinates;

varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_TextureCoordinates;

void main()
{
    v_Position = vec3(u_MVMatrix * a_Position);
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
    v_TextureCoordinates = a_TextureCoordinates;
    gl_Position = u_MVPMatrix * a_Position;
}
