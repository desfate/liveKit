precision mediump float;
varying highp vec2 vTextureCoord;
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

//varying vec4 vPosition;

//uniform float perOffset;

void main()
{
	vec2 coord2 = vTextureCoord;
	coord2.s = coord2.s * 0.5 ;
	vec4 coll = texture2D(Sampler0,coord2);//+vec4(1.0,-1.0,-1.0,0);
    //vec4 coll = texture2D(Sampler0,coord2)+vec4(-1.0,-1.0,-1.0,0);
	
	coord2.s = vTextureCoord.s * 0.5 + 0.5 ;
	vec4 colr = texture2D(Sampler0,coord2);//+vec4(-1.0,-1.0,1.0,0);
    //vec4 colr = texture2D(Sampler0,coord2)+vec4(1.0,1.0,1.0,0);
	
	float dis_test = texture2D(Sampler1,vTextureCoord).r;
	vec4 rgb = (1.0-dis_test)*colr + dis_test*coll;
    //vec4 rgb = (1.0-dis_test)*col + dis_test*cor;


	bool sup = fract(dis_test) != 0.0;
	if(sup){	
		rgb = rgb*(abs(dis_test-0.5)+0.5);
	}

	gl_FragColor = rgb;
}




