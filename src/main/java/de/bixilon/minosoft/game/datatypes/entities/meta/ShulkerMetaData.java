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
import de.bixilon.minosoft.game.datatypes.Direction;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class ShulkerMetaData extends MobMetaData {

    public ShulkerMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    public Direction getDirection() {
        switch (version) {
            case VERSION_1_9_4:
                return (Direction) sets.get(11).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
                return (Direction) sets.get(12).getData();
        }
        return Direction.DOWN;
    }

    public BlockPosition getAttachmentPosition() {
        switch (version) {
            case VERSION_1_9_4:
                return (BlockPosition) sets.get(12).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
                return (BlockPosition) sets.get(13).getData();
        }
        return null;
    }

    public byte getShieldHeight() {
        switch (version) {
            case VERSION_1_9_4:
                return (byte) sets.get(13).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
                return (byte) sets.get(14).getData();
        }
        return 0;
    }

    public Color getColor() {
        switch (version) {
            case VERSION_1_11_2:
                return Color.byId((byte) sets.get(15).getData());
        }
        return Color.PURPLE;
    }


}
