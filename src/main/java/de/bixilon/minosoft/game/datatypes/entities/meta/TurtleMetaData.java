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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;

public class TurtleMetaData extends AnimalMetaData {

    public TurtleMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public BlockPosition getHomePosition() {
        final BlockPosition defaultValue = new BlockPosition(0, 0, 0);
        if (protocolId < 401) { // ToDo
            return defaultValue;
        }
        return sets.getPosition(super.getLastDataIndex() + 1, defaultValue);
    }

    public boolean hasEgg() {
        final boolean defaultValue = false;
        if (protocolId < 401) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 2, defaultValue);
    }

    public boolean isLayingEgg() {
        final boolean defaultValue = false;
        if (protocolId < 401) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    public BlockPosition getTravelPosition() {
        final BlockPosition defaultValue = new BlockPosition(0, 0, 0);
        if (protocolId < 401) { // ToDo
            return defaultValue;
        }
        return sets.getPosition(super.getLastDataIndex() + 4, defaultValue);
    }

    public boolean isGoingHome() {
        final boolean defaultValue = false;
        if (protocolId < 401) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 5, defaultValue);
    }

    public boolean isTraveling() {
        final boolean defaultValue = false;
        if (protocolId < 401) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 6, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (protocolId < 401) { // ToDo
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 6;
    }
}
