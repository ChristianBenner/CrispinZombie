precision mediump float;

uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;

uniform vec4 u_Colour;
varying vec3 v_Normal;
varying vec3 v_Position;
varying vec2 v_TextureCoordinates;

const int maxLights = 5;
uniform int u_LightCount;
uniform vec3 u_LightColours[maxLights];
uniform vec3 u_LightPositions[maxLights];

// max, attenuation, intensity
uniform vec3 u_LightAmbientData[maxLights];
uniform sampler2D u_TextureUnit;

void main()
{
    vec4 baseColour = u_Colour * texture2D(u_TextureUnit, v_TextureCoordinates);
    vec4 buildColour = baseColour;
    vec3 modelViewVertex = v_Position;
    vec3 modelViewNormal = v_Normal;

    for(int i = 0; i < u_LightCount; i++)
    {
        if(i < maxLights)
        {
          //  buildColour.g = u_LightPositions[i].x;
            float distance = length(u_LightPositions[i] - modelViewVertex);
            vec3 lightVector = normalize(u_LightPositions[i] - modelViewVertex);
            float diffuse = max(dot(modelViewNormal, lightVector), u_LightAmbientData[i].z);
            diffuse = diffuse * (1.0 / (1.0 + (u_LightAmbientData[i].y * distance * distance)));

            if(i == 0)
            {
                buildColour = vec4(buildColour.rgb * (min(diffuse * u_LightAmbientData[i].x, 1.0)) * u_LightColours[i], buildColour.a);
            }
            else
            {
                buildColour += vec4(baseColour.rgb * (min(diffuse * u_LightAmbientData[i].x, 1.0)) * u_LightColours[i], 0.0);
            }

        }
    }

    gl_FragColor = buildColour;
}