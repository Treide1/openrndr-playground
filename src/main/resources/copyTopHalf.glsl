#version 330

in vec2 v_texCoord0;
uniform sampler2D tex0;
out vec4 o_color;

// The texture of the bottom half is the texture of the top half
void main()
{
    vec2 uv = v_texCoord0;
    uv.y = mod(uv.y, 0.5); // bottom y becomes top y
    o_color = texture(tex0, uv);
}