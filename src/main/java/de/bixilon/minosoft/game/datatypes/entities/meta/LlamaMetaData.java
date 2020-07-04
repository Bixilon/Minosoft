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
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class LlamaMetaData extends ChestedHorseMetaData {

    public LlamaMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    public int getStrength() {
        switch (version) {
            case VERSION_1_11_2:
                return (int) sets.get(16).getData();
        }
        return 0;
    }

    public Color getCarpetColor() {
        switch (version) {
            case VERSION_1_11_2:
                return Color.byId((int) sets.get(17).getData());
        }
        return null;
    }

    public LlamaVariants getVariant() {
        switch (version) {
            case VERSION_1_11_2:
                return LlamaVariants.byId((int) sets.get(17).getData());
        }
        return null;
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
