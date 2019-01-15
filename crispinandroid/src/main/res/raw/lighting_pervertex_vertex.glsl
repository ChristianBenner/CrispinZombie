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

varying vec4 v_Colour; // passed to the frag

void main()
{
    vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
    vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
    float distance = length(u_LightPos - modelViewVertex);
    vec3 lightVector = normalize(u_LightPos - modelViewVertex);
    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
    diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
    vec3 colour = u_AmbientLightColour * (diffuse * 10.0);
    v_Colour = vec4(vec3(1.0, 1.0, 1.0) * colour, 1.0);
    gl_Position = u_MVPMatrix * a_Position;

/*    vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
    vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
    float distance = length(u_LightPos - modelViewVertex);
    vec3 lightVector = normalize(u_LightPos - modelViewVertex);
    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
    diffuse = diffuse * (u_MaxAmbient / (u_MaxAmbient + ((1.0/u_AmbientDistance) * distance * distance)));
    v_Colour = vec4(vec3(1.0f, 1.0f, 1.0f) * (u_AmbientLightColour * max(u_MinAmbient, diffuse)).rgb, 1.0f);
    gl_Position = u_MVPMatrix * a_Position;*/
}
