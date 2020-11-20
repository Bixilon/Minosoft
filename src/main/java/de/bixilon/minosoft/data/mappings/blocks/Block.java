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

    public BlockRotations getRotation() {
        return rotation;
    }

    public HashSet<BlockProperties> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (rotation != BlockRotations.NONE) {
            out.append(" (");
            out.append("rotation=");
            out.append(getRotation());
        }
        if (properties.size() > 0) {
            if (out.length() > 0) {
                out.append(", ");
            } else {
                out.append(" (");
            }
            out.append("properties=");
            out.append(properties);
        }
        if (out.length() > 0) {
            out.append(")");
        }
        return String.format("%s:%s%s", getMod(), getIdentifier(), out);
    }

    @Override
    public int hashCode() {
        int ret = mod.hashCode() * identifier.hashCode() * rotation.hashCode();
        if (properties.size() > 0) {
            ret *= properties.hashCode();
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        Block their = (Block) obj;
        return getIdentifier().equals(their.getIdentifier()) && getRotation() == their.getRotation() && getProperties().equals(their.getProperties()) && getMod().equals(their.getMod());
    }
}
