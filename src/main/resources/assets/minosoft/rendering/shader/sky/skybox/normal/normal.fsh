/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */


#version 330 core
in vec3 finPosition;
out lowp vec4 foutColor;

uniform vec3 uSunPosition;
uniform vec4 uSkyColor;

uniform float uLightning;
uniform float uThunder;
uniform float uRain;


#define PI 3.14159265359
#define GREYSCALE vec3(0.299f, 0.587f, 0.114f)


#define HORIZON_COLOR_DAY vec3(0.85f, 0.75f, 0.75f)
#define HORIZON_COLOR_NIGHT vec3(0.35f, 0.25f, 0.4f)



#include "./mie_scatter.glsl"
#include "./sunset.glsl"
#include "./horizon.glsl"
#include "./weather.glsl"


float calculate_horizon_gradient(const float height) {
    return 1.0f - pow(smoothstep(-0.3f, 1.0f, height), 0.35f);
}

void main() {
    vec3 position = normalize(finPosition);
    vec3 sun = normalize(uSunPosition);


    float angle = clamp(dot(position, sun), -1.0f, 1.0f);
    float horizon_gradient = calculate_horizon_gradient(position.y);
    float storm_factor = max(uThunder, uRain);

    float sunset_intensity = calculate_sunset_intensity(sun.y) * (1.0f - storm_factor * 0.9f);


    float night = smoothstep(0.15f, -0.2f, sun.y);
    vec3 horizon_color = mix(HORIZON_COLOR_DAY, HORIZON_COLOR_NIGHT, night);



    vec3 color = uSkyColor.rgb;

    color = mix(color, horizon_color, horizon_gradient);
    color = calculate_sunset(color, position.y, sunset_intensity, angle);

    color = calculate_mie_scattering(color, position.y, sun.y, angle, storm_factor);

    color = calculate_horizon_sunset(color, position.y, sunset_intensity);

    color = calculate_horizon(color, position.y, sun.y, horizon_color);

    float day = smoothstep(-0.2f, 0.15f, sun.y);

    color = calculate_thunder(color, day, horizon_gradient);
    color = calculate_rain(color, day, horizon_gradient);
    color = calculate_lightning(color, horizon_gradient);


    foutColor = vec4(color, 1.0f);
}
