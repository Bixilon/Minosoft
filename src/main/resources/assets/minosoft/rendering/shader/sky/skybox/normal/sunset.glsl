#define SUNSET_ORANGE vec3(1.0f, 0.45f, 0.0f)
#define SUNSET_YELLOW vec3(1.0f, 0.75f, 0.1f)


float calculate_sunset_intensity(const float sun_height) {
    float horizon = smoothstep(0.5f, 0.0, sun_height);
    float above = smoothstep(-0.2f, 0.1f, sun_height);

    return horizon * above;
}

vec3 calculate_sunset(const vec3 color, const float height, const float intensity, const float angle) {
    float proximity = pow(1.0f - acos(angle) / PI, 1.5f);

    float fade = smoothstep(-0.15f, 0.1f, height);

    float blend = 1.0f - pow(smoothstep(-0.1f, 0.5f, height), 0.4f);

    vec3 gradient = mix(SUNSET_ORANGE, SUNSET_YELLOW, pow(proximity * 0.3f, 3.0f));

    float diffuse = smoothstep(-0.5f, 0.8f, angle) * 0.15f;
    return mix(mix(color, gradient, intensity * blend * 0.7f), SUNSET_YELLOW, intensity * diffuse * fade);
}
