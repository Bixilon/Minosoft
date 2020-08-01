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
package de.bixilon.minosoft.game.datatypes.entities.meta;

import de.bixilon.minosoft.game.datatypes.Color;

import javax.annotation.Nullable;

public class LlamaMetaData extends ChestedHorseMetaData {

    public LlamaMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public int getStrength() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_11_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 1, defaultValue);
    }

    @Nullable
    public Color getCarpetColor() {
        final int defaultValue = -1;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_11_2.getVersionNumber()) {
            return Color.byId(defaultValue);
        }
        return Color.byId(sets.getInt(super.getLastDataIndex() + 2, defaultValue));
    }

    public LlamaVariants getVariant() {
        final int defaultValue = LlamaVariants.CREAMY.getId();
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_11_2.getVersionNumber()) {
            return LlamaVariants.byId(defaultValue);
        }
        return LlamaVariants.byId(sets.getInt(super.getLastDataIndex() + 3, defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_11_2.getVersionNumber()) {
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 3;
    }

    public enum LlamaVariants {
        CREAMY(0),
        WHITE(1),
        BROWN(2),
        GRAY(3);

        final int id;

        LlamaVariants(int id) {
            this.id = id;
        }

        public static LlamaVariants byId(int id) {
            for (LlamaVariants variant : values()) {
                if (variant.getId() == id) {
                    return variant;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
