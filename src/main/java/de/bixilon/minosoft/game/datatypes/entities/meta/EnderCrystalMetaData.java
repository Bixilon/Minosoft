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
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class EnderCrystalMetaData extends EntityMetaData {

    public EnderCrystalMetaData(InByteBuffer buffer) {
        super(buffer);
    }

    public int getHealth() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (int) sets.get(8).getData();
        }
        return 0;
    }

    public BlockPosition getBeamTarget() {
        switch (version) {
            case VERSION_1_9_4:
                return (BlockPosition) sets.get(5).getData();
        }
        return null;
    }

    public boolean showBottom() {
        switch (version) {
            case VERSION_1_9_4:
                return (boolean) sets.get(6).getData();
        }
        return true;
    }
}
