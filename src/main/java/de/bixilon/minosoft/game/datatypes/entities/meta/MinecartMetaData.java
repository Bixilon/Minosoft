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

import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Block;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Blocks;


public class MinecartMetaData extends EntityMetaData {

    public MinecartMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public int getShakingPower() {
        final int defaultValue = 0;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getInt(17, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getShakingDirection() {
        final int defaultValue = 0;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getInt(18, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    public float getMultiplier() {
        final float defaultValue = 0.0F;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getFloat(19, defaultValue);
        }
        return sets.getFloat(super.getLastDataIndex() + 3, defaultValue);
    }

    public Block getBlock() {
        final int defaultValue = 0;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return Blocks.getBlockByLegacy(sets.getInt(20, defaultValue));
        }
        return Blocks.getBlock(sets.getInt(super.getLastDataIndex() + 4, defaultValue), version);
    }

    public int getBlockYPosition() {
        final int defaultValue = 6; //ToDo
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getInt(21, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 5, defaultValue);
    }

    public boolean isShowingBlock() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getBoolean(22, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 6, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 6;
    }
}
