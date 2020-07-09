attribute vec4 aPosition;
attribute vec4 aTextureCoordinate;
varying vec2 textureCoordinate;

uniform mat4 uMVPMatrix;
uniform mat4 uTransformMatrix;

void main(){
    gl_Position = uTransformMatrix * uMVPMatrix * aPosition;
    textureCoordinate = aTextureCoordinate.xy;
}