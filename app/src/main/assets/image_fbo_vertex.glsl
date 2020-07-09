attribute vec4 aPosition;
uniform mat4 uMVPMatrix;

attribute vec4 aTextureCoordinate;
varying vec2 vTextureCoordinate;

void main(){
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoordinate = aTextureCoordinate.xy;
}