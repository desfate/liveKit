
precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
void main() {
    vec2 texCoord = vTextureCoord;
    texCoord.y = 1.0 - texCoord.y;
    gl_FragColor = texture2D(sTexture, texCoord);
 	if(texCoord.t > 0.990740741){
		gl_FragColor = vec4(0.0,0.0,0.0,1.0);
	}
}