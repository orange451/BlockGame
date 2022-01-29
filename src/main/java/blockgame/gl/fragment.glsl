#version 330

uniform sampler2D texture_diffuse;

in vec2 passTexCoord;
in vec4 passColor;

out vec4 outColor;

void main(void) {
	vec4 color = texture(texture_diffuse, passTexCoord)*passColor;
	if ( color.a < 0.001 )
		discard;
		
	outColor = color;
}
