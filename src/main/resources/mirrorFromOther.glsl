#version 330

// Interface
in vec2 v_texCoord0;
uniform sampler2D tex0;
out vec4 o_color;

// Parameters:
// Takes a unifrom from another texture
uniform sampler2D tex1;
// Takes a source rectangle as corner pair (x,y) to (z,w)
uniform vec4 srcRect;
// Takes a destination rectangle as corner pair (x,y) to (z,w)
uniform vec4 dstRect;
// Inner margin
uniform float margin;

// Define a function that checks if a pixel is inside the circle
bool isInsideRect(vec2 uv) {
    return uv.x > srcRect.x + margin && uv.x < srcRect.z - margin && uv.y > srcRect.y + margin && uv.y < srcRect.w - margin;
}

// Define a function that takes a uv coord from srcRect and maps it to dstRect
vec2 mapCoord(vec2 uv) {
    vec2 srcSize = srcRect.zw - srcRect.xy;
    vec2 dstSize = dstRect.zw - dstRect.xy;
    vec2 srcPos = uv - srcRect.xy;
    vec2 dstPos = srcPos / srcSize * dstSize;
    return dstPos + dstRect.xy;
}

// Main function:
// Checks if a pixel is inside the srcRect, and if so maps it to dstRect from other texture
void main() {
    vec2 uv = v_texCoord0;
    if (isInsideRect(uv)) {
        vec2 mappedCoord = mapCoord(uv);
        o_color = texture(tex1, mappedCoord);
    } else {
        o_color = texture(tex0, uv);
    }
}