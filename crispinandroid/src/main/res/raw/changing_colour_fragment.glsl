precision mediump float;

uniform vec4 u_Colour;

uniform float u_Time;

void main() {
    vec4 colour = vec4(u_Colour.x * sin(u_Time), u_Colour.y * cos(u_Time),
                    u_Colour.z * cos(2.0 * u_Time), u_Colour.w);
    gl_FragColor = colour;
}
