precision mediump float;
varying vec2 vTextureCoordinate;
uniform sampler2D vTexture;

uniform float vColorRatio;

void main(){
    vec4 textureColor = texture2D(vTexture, vTextureCoordinate);
    gl_FragColor = vec4(textureColor.r, textureColor.g * vColorRatio, textureColor.b, textureColor.a);
}