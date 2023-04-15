#version 330

// Interface
in vec2 v_texCoord0;
uniform sampler2D tex0;
out vec4 o_color;

// Parameters:
// Takes a source rectangle as corner pair (x,y) to (z,w)
uniform vec4 srcRect;
// Takes a destination rectangle as corner pair (x,y) to (z,w)
uniform vec4 dstRect;

// Define a function that checks if a pixel is inside the circle
bool isInsideRect(vec2 uv) {
    return uv.x > srcRect.x && uv.x < srcRect.z && uv.y > srcRect.y && uv.y < srcRect.w ;
}

// Define a function that takes a uv coord from srcRect and maps it to dstRect
vec2 mapCoord(vec2 uv) {
    vec2 srcSize = srcRect.zw - srcRect.xy;
    vec2 dstSize = dstRect.zw - dstRect.xy;
    vec2 srcPos = uv - srcRect.xy;
    vec2 dstPos = srcPos / srcSize * dstSize;
    return dstPos + dstRect.xy;
}

// Define a function that takes the uv coord from srcRect and maps it to dstRect until it isnt in srcRect anymore
vec2 mapIteratively(vec2 uv) {
    vec2 srcSize = srcRect.zw - srcRect.xy;
    vec2 dstSize = dstRect.zw - dstRect.xy;
    vec2 srcPos = uv - srcRect.xy;
    vec2 dstPos = srcPos / srcSize * dstSize;
    vec2 mappedCoord = dstPos + dstRect.xy;

    int i = 0;
    while (isInsideRect(mappedCoord) && i < 100) {
        srcPos = mappedCoord - srcRect.xy;
        dstPos = srcPos / srcSize * dstSize;
        mappedCoord = dstPos + dstRect.xy;
        i++;
    }

    return mappedCoord;
}

vec4 mapIterativelyWithFac(vec2 uv) {
    vec2 mappedCoord = uv;

    int i = 100;
    while (isInsideRect(mappedCoord) && i > 0) {
        mappedCoord = mapCoord(mappedCoord);
        i--;
    }

    return texture(tex0, mappedCoord) * (i / 100.0);
}

// Main function:
// Checks if a pixel is inside the srcRect, and if so maps it to dstRect from other texture
void main() {
    vec2 uv = v_texCoord0;
    o_color = mapIterativelyWithFac(uv);
}