#version 330

// Interface
in vec2 v_texCoord0;
uniform sampler2D tex0;
out vec4 o_color;

// Parameters:
// Takes a stencil buffer where 0 is identity lookup
uniform usampler2D stencil;
// Takes a y scale parameter, assuming width to go from -1 to 1
uniform float yScl;
// Takes a flag whether to fade towards black
uniform bool fade;

// Helper functions
vec2 lerp(vec2 first, vec2 second, float perc) {
    return first * (1.0 - perc) + second * perc;
}

vec2 toMathCoords(vec2 uv) {
    return vec2(uv.x * 2.0 - 1.0, (uv.y * 2.0 - 1.0) * yScl);
}

vec2 toUvCoords(vec2 mathCoords) {
    return vec2((mathCoords.x + 1.0) / 2.0, (mathCoords.y / yScl + 1.0) / 2.0);
}

#define PI 3.14159265359

// Flame var functions (are offset by 128)
// Flame var 1
vec2 sinusoidal(vec2 uv) {
    return vec2(sin(uv.x), sin(uv.y));
}

// Flame var 2
vec2 spherical(vec2 uv) {
    float r = length(uv);
    return uv / (r * r);
}

// Flame var 3
vec2 swirl(vec2 uv) {
    float r = length(uv);
    return vec2(uv.x * sin(r) - uv.y * cos(r), uv.x * cos(r) + uv.y * sin(r));
}

// Flame var 4
vec2 horseshoe(vec2 uv) {
    return vec2((uv.x - uv.y) * (uv.x + uv.y), 2.0 * uv.x * uv.y);
}

// Flame var 5
vec2 polar(vec2 uv) {
    float r = length(uv);
    float theta = atan(uv.y, uv.x);
    return vec2(theta / PI, r - 1.0);
}

// Flame var 6
vec2 handkerchief(vec2 uv) {
    float r = length(uv);
    float theta = atan(uv.y, uv.x);
    return vec2(r * sin(theta + r), r * cos(theta - r));
}

// Flame var 7
vec2 heart(vec2 uv) {
    float r = length(uv);
    float theta = atan(uv.y, uv.x);
    return vec2(r * sin(theta * r), -r * cos(theta * r));
}

// Flame var 8
vec2 disc(vec2 uv) {
    float r = length(uv);
    return uv / r;
}

// Iterative lookup function
vec4 iterativeLookup(vec2 uv) {
    vec2 mathCoords = toMathCoords(uv);
    uint stencilValue = texture(stencil, toUvCoords(mathCoords)).x;

    int i = 100;
    while (stencilValue != 0u && i > 0) {
        // Mapping #1, flip x
        if (stencilValue == 1u) mathCoords.x = -mathCoords.x;
        // Mapping #2, flip y
        if (stencilValue == 2u) mathCoords.y = -mathCoords.y;

        // Flame vars
        if (stencilValue >= 128u) {
            // Reduce stencil value by 128 via bitwise AND
            stencilValue &= 127u;
            if (stencilValue == 0u) break;
            if (stencilValue == 1u) mathCoords = sinusoidal(mathCoords);
            if (stencilValue == 2u) mathCoords = spherical(mathCoords);
            if (stencilValue == 3u) mathCoords = swirl(mathCoords);
            if (stencilValue == 4u) mathCoords = horseshoe(mathCoords);
            if (stencilValue == 5u) mathCoords = polar(mathCoords);
            if (stencilValue == 6u) mathCoords = handkerchief(mathCoords);
            if (stencilValue == 7u) mathCoords = heart(mathCoords);
            if (stencilValue == 8u) mathCoords = disc(mathCoords);
        }

        stencilValue = texture(stencil, toUvCoords(mathCoords)).x;
        i--;
    }

    float fac = 1.0;
    if (fade) fac = sqrt(i / 100.0);
    return texture(tex0, toUvCoords(mathCoords)) * fac;
}

// Main function:
// For each pixel, perform the iterative lookup
void main() {
    vec2 uv = v_texCoord0;
    o_color = iterativeLookup(uv);
}