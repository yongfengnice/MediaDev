attribute vec4 aPosition;
attribute vec4 aTextureCoordinate;
varying vec2 textureCoordinate;

void main(){
    gl_Position = aPosition;
    textureCoordinate = aTextureCoordinate.xy;
}