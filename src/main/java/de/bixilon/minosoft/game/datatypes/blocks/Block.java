/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.blocks;

public class Block {
    final String mod;
    final String identifier;
    final BlockRotation rotation;
    final BlockProperties[] properties;

    public Block(String mod, String identifier, BlockProperties[] properties, BlockRotation rotation) {
        this.mod = mod;
        this.identifier = identifier;
        this.properties = properties;
        this.rotation = rotation;
    }

    public Block(String mod, String identifier, BlockProperties[] properties) {
        this.mod = mod;
        this.identifier = identifier;
        this.properties = properties;
        this.rotation = BlockRotation.NONE;
    }

    public Block(String mod, String identifier, BlockRotation rotation) {
        this.mod = mod;
        this.identifier = identifier;
        this.properties = new BlockProperties[0];
        this.rotation = rotation;
    }

    public Block(String mod, String identifier) {
        this.mod = mod;
        this.identifier = identifier;
        this.properties = new BlockProperties[0];
        this.rotation = BlockRotation.NONE;
    }

    public String getMod() {
        return mod;
    }

    public String getIdentifier() {
        return identifier;
    }

    public BlockRotation getRotation() {
        return rotation;
    }

    public BlockProperties[] getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (rotation != BlockRotation.NONE) {
            out.append(" (");
            out.append("rotation=");
            out.append(getRotation().name());
        }
        if (properties.length > 0) {
            if (out.length() > 0) {
                out.append(" ,");
            } else {
                out.append(" (");
            }
            out.append("properties={");
            for (BlockProperties property : properties) {
                out.append(property.name());
                out.append(",");
            }
            // remove last ,
            out.setLength(out.length() - 1);
            out.append("}");
        }
        if (out.length() > 0) {
            out.append(")");
        }
        return String.format("%s:%s%s", getMod(), getIdentifier(), out.toString());
    }
}
