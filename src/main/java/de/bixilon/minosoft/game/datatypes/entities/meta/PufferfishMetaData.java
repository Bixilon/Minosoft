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

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PufferfishMetaData extends AbstractFishMetaData {

    public PufferfishMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }

    public PufferStates getPufferState() {
        final int defaultValue = PufferStates.UN_PUFFED.getId();
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return PufferStates.byId(defaultValue);
        }
        return PufferStates.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
    }


    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 1;
    }

    public enum PufferStates {
        UN_PUFFED(0),
        SEMI_PUFFED(1),
        FULLY_PUFFED(2);


        final int id;

        PufferStates(int id) {
            this.id = id;
        }

        public static PufferStates byId(int id) {
            for (PufferStates state : values()) {
                if (state.getId() == id) {
                    return state;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
