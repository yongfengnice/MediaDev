attribute vec4 aPosition;
attribute vec4 aTextureCoordinate;
varying vec2 textureCoordinate;

uniform mat4 uTransformMatrix;

void main(){
    gl_Position = uTransformMatrix * aPosition;
    textureCoordinate = aTextureCoordinate.xy;
}