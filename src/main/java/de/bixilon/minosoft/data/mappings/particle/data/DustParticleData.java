/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.particle.data;

import de.bixilon.minosoft.data.mappings.particle.ParticleType;

public class DustParticleData extends ParticleData {
    private final float red;
    private final float green;
    private final float blue;
    private final float scale;

    public DustParticleData(float red, float green, float blue, float scale, ParticleType type) {
        super(type);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.scale = scale;
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }

    public float getScale() {
        return this.scale;
    }

    @Override
    public String toString() {
        return String.format("{red=%s, green=%s, blue=%s, scale=%s)", this.red, this.green, this.blue, this.scale);
    }
}
