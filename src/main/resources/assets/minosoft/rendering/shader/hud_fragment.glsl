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

out vec4 outColor;

in vec3 passTextureCoordinates;
in vec4 passTintColor;


uniform sampler2DArray textureArray;

void main() {
    vec4 texelColor = texture(textureArray, passTextureCoordinates);

    if (passTintColor.a == 1.0f && texelColor.a == 0) {
        discard;
    }
    if (passTintColor.a != 0.0f){
        texelColor *= passTintColor;
    }

    // ToDo
    if (passTintColor.r == 0.0f && passTintColor.g == 0.0f && passTintColor.b == 0.0f && passTintColor.a != 0.0f){
        outColor= vec4(passTintColor.rgb * (vec3(1.0f) / texelColor.rgb), passTintColor.a);
    } else {
        outColor = texelColor;
    }

    //
    //  outColor = vec4(1.0f, 0.0f, 0.5f, 1.0f);
    //
}
