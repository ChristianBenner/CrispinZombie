precision mediump float;

// 5 Lights
uniform float u_Lights[5 * 9];
uniform int u_LightCount;

uniform sampler2D u_TextureUnit;
uniform vec3 u_LightPos;
uniform vec4 u_Colour;

uniform float u_AmbientDistance;
uniform float u_MinAmbient;
uniform float u_MaxAmbient;
uniform vec3 u_AmbientLightColour;

varying vec3 v_Normal;
varying vec3 v_Position;
varying vec2 v_TextureCoordinates;

void main()
{
    // for each light
    vec4 fcolour = texture2D(u_TextureUnit, v_TextureCoordinates) * u_Colour;
    vec3 lightColour = vec3(1.0f, 1.0f, 1.0f);
    for(int i = 0; i < u_LightCount; i++)
    {
            int pos = i * 9;
            vec3 colour = vec3(u_Lights[pos + 0], u_Lights[pos + 1], u_Lights[pos + 2]);
            vec3 position = vec3(u_Lights[pos + 3], u_Lights[pos + 4], u_Lights[pos + 5]);
            float maxAmbient = u_Lights[pos + 6];
            float minAmbient = u_Lights[pos + 7];
            float intensity = u_Lights[pos + 8];

            float distance = length(position - v_Position);
            vec3 lightVector = normalize(position - v_Position);
            float diffuse = max(dot(v_Normal, lightVector) * u_MaxAmbient, 0.1);
            diffuse = diffuse * (1.0 / (1.0 + ((1.0/intensity) * distance * distance)));
            diffuse = diffuse * 10.0f;
            if(i == 0)
            {
                lightColour = colour * max(minAmbient, diffuse);
            }
            else
            {
                lightColour = lightColour + (colour * max(minAmbient, diffuse));
            }
    }

    fcolour = vec4(fcolour.rgb * lightColour.rgb, fcolour.a);
    /*for(int i = 0; i < 5; i++)
    {
        int pos = i * 9;
        vec3 colour = vec3(u_Lights[pos + 0], u_Lights[pos + 1], u_Lights[pos + 2]);
        vec3 position = vec3(u_Lights[pos + 3], u_Lights[pos + 4], u_Lights[pos + 5]);
        float maxAmbient = u_Lights[pos + 6];
        float minAmbient = u_Lights[pos + 7];
        float intensity = u_Lights[pos + 8];

        float distance = length(position - v_Position);
        vec3 lightVector = normalize(position - v_Position);
        float diffuse = max(dot(v_Normal, lightVector), 0.1);
        diffuse = diffuse * (maxAmbient / (maxAmbient + ((1.0/intensity) * distance * distance)));
        fcolour = vec4(fcolour.rgb * (colour * max(minAmbient, diffuse)).rgb, fcolour.a);
    }*/

   // float distance = length(u_LightPos - v_Position);
   // vec3 lightVector = normalize(u_LightPos - v_Position);
  //  float diffuse = max(dot(v_Normal, lightVector) * 1.5, 0.1);
  //  diffuse = diffuse * (u_MaxAmbient / (u_MaxAmbient + ((1.0/u_AmbientDistance) * distance * distance)));
  //  vec4 texC = texture2D(u_TextureUnit, v_TextureCoordinates) * u_Colour;
   // gl_FragColor = vec4(texC.rgb * (u_AmbientLightColour * max(u_MinAmbient, diffuse)).rgb, texC.a);
   gl_FragColor = fcolour;
}