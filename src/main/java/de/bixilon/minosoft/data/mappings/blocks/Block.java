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

package de.bixilon.minosoft.data.mappings.blocks;

import de.bixilon.minosoft.data.mappings.ModIdentifier;

import java.util.HashSet;

public class Block extends ModIdentifier {
    final BlockRotations rotation;
    final HashSet<BlockProperties> properties;

    public Block(String mod, String identifier, HashSet<BlockProperties> properties, BlockRotations rotation) {
        super(mod, identifier);
        this.properties = properties;
        this.rotation = rotation;
    }

    public Block(String mod, String identifier, HashSet<BlockProperties> properties) {
        super(mod, identifier);
        this.properties = properties;
        this.rotation = BlockRotations.NONE;
    }

    public Block(String mod, String identifier, BlockRotations rotation) {
        super(mod, identifier);
        this.properties = new HashSet<>();
        this.rotation = rotation;
    }

    public Block(String mod, String identifier) {
        super(mod, identifier);
        this.properties = new HashSet<>();
        this.rotation = BlockRotations.NONE;
    }

    public Block(String fullIdentifier) {
        super(fullIdentifier);
        this.properties = new HashSet<>();
        this.rotation = BlockRotations.NONE;
    }

    public BlockRotations getRotation() {
        return this.rotation;
    }

    public HashSet<BlockProperties> getProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (this.rotation != BlockRotations.NONE) {
            out.append(" (");
            out.append("rotation=");
            out.append(getRotation());
        }
        if (!this.properties.isEmpty()) {
            if (!out.isEmpty()) {
                out.append(", ");
            } else {
                out.append(" (");
            }
            out.append("properties=");
            out.append(this.properties);
        }
        if (!out.isEmpty()) {
            out.append(")");
        }
        return String.format("%s:%s%s", getMod(), getIdentifier(), out);
    }

    @Override
    public int hashCode() {
        int ret = this.mod.hashCode() * this.identifier.hashCode() * this.rotation.hashCode();
        if (!this.properties.isEmpty()) {
            ret *= this.properties.hashCode();
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        if (!(obj instanceof Block their)) {
            return false;
        }
        return getIdentifier().equals(their.getIdentifier()) && getRotation() == their.getRotation() && getProperties().equals(their.getProperties()) && getMod().equals(their.getMod());
    }
}
