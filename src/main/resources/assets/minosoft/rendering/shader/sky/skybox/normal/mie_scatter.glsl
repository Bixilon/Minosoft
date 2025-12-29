#define MIE_SCATTER_DIRECTION 0.65f
#define MIE_SCATTER_SQUARE (MIE_SCATTER_DIRECTION * MIE_SCATTER_DIRECTION)

#define MIE_COLOR_LOW vec3(1.0f, 0.85f, 0.7f)
#define MIE_COLOR_HIGH vec3(1.0f, 0.95f, 0.9f)

// Henyey-Greenstein approximation
float calculate_mie_phase(const float angle) {
    float denominator = 1.0f + MIE_SCATTER_SQUARE - 2.0f * MIE_SCATTER_DIRECTION * angle;
    return (1.0f - MIE_SCATTER_SQUARE) / (4.0f * PI * pow(denominator, 1.5f));
}

vec3 calculate_mie_color(const float sun_height) {
    float factor = pow(max(sun_height, 0.0f), 0.5f);
    return mix(MIE_COLOR_LOW, MIE_COLOR_HIGH, factor);
}

vec3 calculate_mie_scattering(const vec3 color, const float height, const float sun_height, const float angle, const float reduction) {
    float phase = calculate_mie_phase(angle);
    vec3 mie_color = calculate_mie_color(sun_height);

    float strength = phase * 0.015f * smoothstep(-0.1f, 0.3f, sun_height) * (1.0f - reduction * 0.9f);
    float fade = smoothstep(-0.2f, 0.2f, height);

    return color + (mie_color * strength * fade);
}
