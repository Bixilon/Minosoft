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

#define PI 3.1415926535
#define SUN_COLOR vec3(1.0, 0.6, 0.05)
#define ATMOSPHERE_COLOR vec3(1.0, 1.0, 1.0)
#define SCATTER_COLOR vec3(0.95, 0.7, 0.15)

// scatter: vec3(1.0, 0.3, 0.0)

// inspired by https://github.com/Technici4n/voxel-rs/blob/master/assets/shaders/skybox.frag

float dist_sphere(vec3 a, vec3 b) {
    float angle = acos(dot(a, b));
    return min(angle, 2.0 * PI - angle);
}


vec3 calculate_sky(vec3 position, vec3 sun_position) {
    float y_limit = clamp(position.y, 0.0, 1.0) - 5 * clamp(position.y, -0.2, 0.0);
    float atmosphere = pow(1.0 - y_limit, 1.4);

    float scatter = pow(1.0 - dist_sphere(position, sun_position) / PI, 1.0 / 20.0);
    scatter = 1.0 - clamp(scatter, 0.7, 1.0);

    vec3 scatterColor = mix(ATMOSPHERE_COLOR, SCATTER_COLOR * 2.5, scatter);

    return mix(uSkyColor.xyz, vec3(scatterColor), atmosphere / 1.3);
}

vec3 calculate_sun(vec3 position, vec3 sun_position) {
    float glow = 1.0 - dist_sphere(position, sun_position);
    glow = clamp(glow, 0.0, 1.0);

    float y_limit = clamp(position.y, 0.0, 1.0) - 5 * clamp(position.y, -0.2, 0.0);

    glow = pow(glow, 6.0) * 1.0;
    glow = pow(glow, y_limit);
    glow = clamp(glow, 0.0, 1.0);

    glow *= pow(y_limit * y_limit, 1.0 / 2.0);


    return SUN_COLOR * glow;
}

void main() {
    vec3 position_normalized = normalize(finPosition);
    vec3 position_sun = normalize(uSunPosition);

    vec3 sky = calculate_sky(position_normalized, position_sun);
    vec3 sun = calculate_sun(position_normalized, position_sun);

    float brightness = max(uSkyColor.r, max(uSkyColor.g, uSkyColor.b));

    foutColor = vec4(brightness * (sky + sun), 1.0);
}
