#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES vTexture;

uniform float vColorRatio;

void main(){
    vec4 textureColor = texture2D(vTexture, textureCoordinate);
    gl_FragColor = vec4(textureColor.r, textureColor.g * (1.0f + vColorRatio), textureColor.b, textureColor.a);
}