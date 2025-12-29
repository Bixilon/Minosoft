#define NIGHT_COLOR vec3(0.02f, 0.03f, 0.05f)
#define HORIZON_BAND_COLOR vec3(0.2f, 0.1f, 0.05f)

vec3 calculate_horizon_sunset(const vec3 color, const float height, const float intensity) {
    float strength = exp(-pow(height / 0.15f, 2.0f));
    return color + (HORIZON_BAND_COLOR * strength * intensity);
}


vec3 calculate_horizon(const vec3 color, const float height, const float sun_height, const vec3 horizon_color){
    float night = smoothstep(-0.25f, 0.15f, sun_height);

    float fade = smoothstep(0.05f, -0.35f, height);
    vec3 ground = mix(horizon_color * 0.3f, NIGHT_COLOR, 1.0f - night);


    return mix(mix(NIGHT_COLOR, color, night), ground, fade * fade);
}
