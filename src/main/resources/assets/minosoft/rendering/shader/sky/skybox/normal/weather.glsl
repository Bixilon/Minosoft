#define THUNDER_SKY vec3(0.12f, 0.1f, 0.16f)
#define THUNDER_NIGHT vec3(0.005f, 0.004f, 0.008f)
#define THUNDER_HORIZON vec3(0.08f, 0.06f, 0.1f)
#define THUNDER_DARK vec3(0.15f, 0.1f, 0.2f)

#define RAIN_SKY vec3(0.52f, 0.54f, 0.58f)
#define RAIN_NIGHT vec3(0.03f, 0.035f, 0.045f)
#define RAIN_HORIZON vec3(0.48f, 0.5f, 0.55f)
#define RAIN_DARK vec3(0.9f, 0.93f, 1.08f)


#define LIGHTNING_ILLUMINATION vec3(0.3f, 0.3f, 0.4f)
#define LIGHTNING_FLASH vec3(0.8f, 0.82f, 0.95f)

vec3 calculate_thunder(const vec3 color, const float day, const float horizon_gradient) {
    // if (uThunder <= 0.0) return color; // not needed

    vec3 zenith = mix(THUNDER_NIGHT, THUNDER_SKY, day);
    vec3 horizon = mix(THUNDER_NIGHT * 0.8f, THUNDER_HORIZON, day);
    vec3 thunder_color = mix(zenith, horizon, horizon_gradient);

    vec3 darkened = color * mix(vec3(1.0f), THUNDER_DARK, uThunder);


    float luminance = dot(color, GREYSCALE);
    vec3 purpled = vec3(luminance * 0.95f, luminance * 0.9f, luminance * 1.1f);

    return mix(mix(darkened, thunder_color, uThunder * 0.75f), purpled, uThunder * 0.5f);
}

vec3 calculate_rain(const vec3 color, const float day_factor, const float horizon_gradient) {
    // if (uRain <= 0.0) return color; // not needed

    vec3 zenith = mix(RAIN_NIGHT, RAIN_SKY, day_factor);
    vec3 horizon = mix(RAIN_NIGHT * 1.2f, RAIN_HORIZON, day_factor);
    vec3 rain_color = mix(zenith, horizon, horizon_gradient * 0.5f);

    float luminance = dot(color, GREYSCALE);
    vec3 darkened = mix(color, vec3(luminance), uRain * 0.6f);

    vec3 final = mix(darkened, rain_color, uRain * 0.6f);
    final *= mix(vec3(1.0f), RAIN_DARK, uRain * 0.5f);

    return final;
}

vec3 calculate_lightning(const vec3 color, const float horizon_gradient) {
    // if (uLightning <= 0.0) return color; // not needed

    vec3 illumination = LIGHTNING_ILLUMINATION * uLightning * horizon_gradient;

    return mix(color, LIGHTNING_FLASH, uLightning * 0.75f) + illumination;
}
