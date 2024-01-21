#version 330 core

in vec3 N;
in vec3 ecPosition;
in vec2 texCoord;

out vec4 fColor;

uniform vec4 LightPosition;
uniform vec4 AmbientProduct, DiffuseProduct, SpecularProduct;
uniform float Shininess;

// Textures
uniform sampler2D imgTexture;
uniform sampler2D normalMap;

// Fixes the lighting so that the objects are in tangent space
mat3 tbn()
{
    vec3 norm = N;

    vec3 tangent;
    vec3 c1 = cross(norm, vec3(0, 0, 1));
    vec3 c2 = cross(norm, vec3(0, 1, 0));

    if (length(c1) > length(c2)) {
        tangent = normalize(c1);
    }
    else{
        tangent = normalize(c2);
    }
    vec3 bitangent = normalize(cross(norm, tangent));

    return transpose(mat3(tangent, bitangent, norm));
}

void main()
{
    // Light position in eye coordinates
    vec3 L = normalize(LightPosition.xyz - ecPosition);
    vec3 E = normalize(-ecPosition);
    vec3 H = normalize(L + E);

    // Get the normal from the normal map & convert it to -1,1 range
    vec3 normal = texture(normalMap, texCoord).rgb;
    normal = normalize(normal * 2.0 - 1.0);

    // Calculating TBN (Tangent, Bitangent, Normal) matrix
    normal *= tbn();

    // Ambient lighting
    vec4 ambient = AmbientProduct;
    float Kd = max(dot(L, normal), 0.0);
    //float Kd = max(dot(L, N), 0.0);
    vec4  diffuse = Kd*DiffuseProduct;

    // Specular lighting
    float Ks = pow( max(dot(normal, H), 0.0), Shininess );
    vec4  specular = Ks * SpecularProduct;

    if( dot(L, normal) < 0.0 ) {
        specular = vec4(0.0, 0.0, 0.0, 1.0);
    }

    fColor = ambient + diffuse + specular;
    fColor = fColor * texture(imgTexture, texCoord);
}