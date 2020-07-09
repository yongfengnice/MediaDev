attribute vec4 aPosition;
attribute vec4 aTextureCoordinate;
varying vec2 textureCoordinate;

uniform mat4 uMVPMatrix;

void main(){
    gl_Position = uMVPMatrix * aPosition;
    textureCoordinate = aTextureCoordinate.xy;
}