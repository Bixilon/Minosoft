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

package de.bixilon.minosoft.data.mappings;

public class Dimension extends ModIdentifier {
    final boolean hasSkyLight;

    public Dimension(String mod, String identifier, boolean hasSkyLight) {
        super(mod, identifier);
        this.hasSkyLight = hasSkyLight;
    }

    public boolean hasSkyLight() {
        return this.hasSkyLight;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        Dimension their = (Dimension) obj;
        return super.equals(obj) && hasSkyLight() == their.hasSkyLight();
    }
}
