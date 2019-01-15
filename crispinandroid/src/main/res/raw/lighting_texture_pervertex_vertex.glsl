precision mediump float;

uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;
uniform vec3 u_LightPos;

uniform float u_AmbientDistance;
uniform float u_MinAmbient;
uniform float u_MaxAmbient;
uniform vec3 u_AmbientLightColour;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_TextureCoordinates;

varying vec2 v_TextureCoordinates;
varying vec4 v_Colour; // passed to the frag

void main()
{
    vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
    vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
    float distance = length(u_LightPos - modelViewVertex);
    vec3 lightVector = normalize(u_LightPos - modelViewVertex);
    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
    diffuse = diffuse * (u_MaxAmbient / (u_MaxAmbient + ((1.0/u_AmbientDistance) * distance * distance)));
    v_Colour = vec4((u_AmbientLightColour * max(u_MinAmbient, diffuse)).rgb, 1.0);

    v_TextureCoordinates = a_TextureCoordinates;
    gl_Position = u_MVPMatrix * a_Position;
}
