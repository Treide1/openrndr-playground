#version 330

// Interface
in vec2 v_texCoord0;
uniform sampler2D tex0;
out vec4 o_color;

// Parameters:
// Takes a stencil buffer where 0 is identity lookup
// and larger values are respective mirrors
uniform usampler2D stencil;
// Center of lerping
uniform vec2 center;

// Helper functions
vec2 lerp(vec2 first, vec2 second, float perc) {
    return first * (1.0 - perc) + second * perc;
}



// Iterative lookup function
vec4 iterativeLookup(vec2 uv) {
    uint stencilValue = texture(stencil, uv).x;

    int i = 100;
    while (stencilValue > 0u && i > 0) {
        if (stencilValue == 1u) {
            // Mapping #1, lerp towards center
            uv = lerp(uv, center, 0.05);
        }
        if (stencilValue == 2u) {
            // Mapping #2, flip x
            uv.x = 1.0 - uv.x;
        }
        stencilValue = texture(stencil, uv).x;
        i--;
    }

    return texture(tex0, uv); // * (i / 100.0);
}


// Main function:
// For each pixel, perform the iterative lookup
void main() {
    vec2 uv = v_texCoord0;
    o_color = iterativeLookup(uv);
}