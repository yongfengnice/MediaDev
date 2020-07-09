precision mediump float;

varying vec2 textureCoordinate;
uniform sampler2D vTexture;

uniform float vColorRatio;

void main(){
    float colorRatio = 0.3f + vColorRatio;
    vec4 textureColor = texture2D(vTexture, textureCoordinate);
    gl_FragColor = vec4(textureColor.rgb * colorRatio, textureColor.a);
}