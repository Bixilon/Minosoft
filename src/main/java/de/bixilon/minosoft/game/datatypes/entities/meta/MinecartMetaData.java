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

import de.bixilon.minosoft.game.datatypes.blocks.Block;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class MinecartMetaData extends EntityMetaData {

    public MinecartMetaData(InByteBuffer buffer, ProtocolVersion v) {
        super(buffer, v);
    }

    public int getShakingPower() {
        switch (version) {
            case VERSION_1_7_10:
                return (int) sets.get(17).getData();
        }
        return 0;
    }

    public int getShakingDirection() {
        switch (version) {
            case VERSION_1_7_10:
                return (int) sets.get(18).getData();
        }
        return 0;
    }

    public float getDamageTaken() {
        switch (version) {
            case VERSION_1_7_10:
                return (float) sets.get(19).getData();
        }
        return 0;
    }

    public Block getBlock() {
        switch (version) {
            case VERSION_1_7_10:
                return Block.byLegacy((int) sets.get(20).getData() & 0xFF, (int) sets.get(20).getData() & 0xFF00);
        }
        return Block.AIR;
    }

}
