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
import de.bixilon.minosoft.gui.rendering.chunk.models.BlockModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class Block extends ModIdentifier {
    private final BlockRotations rotation;
    private final HashSet<BlockProperties> properties;
    private BlockModel blockModel;

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

    public Block(String fullIdentifier, BlockProperties... properties) {
        super(fullIdentifier);
        this.properties = new HashSet<>(Arrays.asList(properties));
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

    public BlockModel getBlockModel() {
        return this.blockModel;
    }

    public void setBlockModel(BlockModel blockModel) {
        this.blockModel = blockModel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.mod, this.identifier, this.properties, this.rotation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        if (obj instanceof Block their) {
            return getIdentifier().equals(their.getIdentifier()) && getRotation() == their.getRotation() && getProperties().equals(their.getProperties()) && getMod().equals(their.getMod());
        }
        if (obj instanceof ModIdentifier identifier) {
            return super.equals(identifier);
        }
        return false;
    }

    public boolean bareEquals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Block their) {
            if (!getMod().equals(their.getMod()) || !getIdentifier().equals(their.getIdentifier())) {
                return false;
            }
            if (their.getRotation() != BlockRotations.NONE) {
                if (their.getRotation() != getRotation()) {
                    return false;
                }
            }
            for (BlockProperties property : their.getProperties()) {
                if (!getProperties().contains(property)) {
                    return false;
                }
            }
            return true;
        }
        if (obj instanceof ModIdentifier identifier) {
            return super.equals(identifier);
        }
        return false;
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
        return String.format("%s%s", getFullIdentifier(), out);
    }
}
