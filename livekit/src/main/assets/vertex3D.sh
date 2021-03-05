        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;
        varying vec2 vTextureCoord;
  //      varying vec4 vPosition;
        void main() {
          gl_Position = aPosition;
    //      vPosition = aPosition;
          vTextureCoord = aTextureCoord.xy;
        }