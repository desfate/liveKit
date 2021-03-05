precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform float  screenHeight;
uniform float  is2dto3d;
void main() {
    vec2 texCoord = vTextureCoord;
    texCoord.y = 1.0 - texCoord.y;
	if(is2dto3d > 1.0){
		float p = texCoord.y * screenHeight;
		vec2  tmp = texCoord;
		tmp.x += 0.004 - p*0.0000122;
		gl_FragColor = texture2D(sTexture, tmp);
	}else {
		gl_FragColor = texture2D(sTexture, texCoord);
	}
	
	if(texCoord.t > 0.990740741){
		gl_FragColor = vec4(0.0,0.0,0.0,1.0);
	}
	
	
}