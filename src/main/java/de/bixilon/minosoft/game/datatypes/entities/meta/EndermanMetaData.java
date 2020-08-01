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


public class EndermanMetaData extends LivingMetaData {

    public EndermanMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }


    public Block getCarriedBlock() {
        final Block defaultValue = Blocks.nullBlock;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return Blocks.getBlockByLegacy(sets.getInt(16, 0), sets.getInt(17, 0));
        }
        return sets.getBlock(super.getLastDataIndex() + 1, defaultValue);
    }

    public boolean isScreaming() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getBoolean(18, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 2, defaultValue);
    }

    public boolean isStarredAt() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
            return super.getLastDataIndex() + 2;
        }
        return super.getLastDataIndex() + 3;
    }
}
