#version 450

out vec4 fragColor;
in vec3 vertColor;
in vec2 texCoord;

uniform sampler2D theTexture;

void main()
{
    fragColor = texture(theTexture, texCoord) * vec4(vertColor, 1.0);
    if (fragColor.a < 0.1)
        discard;
}