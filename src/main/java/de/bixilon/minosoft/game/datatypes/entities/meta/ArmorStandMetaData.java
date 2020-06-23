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

import de.bixilon.minosoft.game.datatypes.EntityRotation;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

public class ArmorStandMetaData extends MobMetaData {

    public ArmorStandMetaData(InByteBuffer buffer, ProtocolVersion v) {
        super(buffer, v);
    }


    public boolean isSmall() {
        switch (version) {
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(10).getData(), 0);
        }
        return false;
    }

    public boolean hasGravity() {
        switch (version) {
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(10).getData(), 1);
        }
        return false;
    }

    public boolean hasArms() {
        switch (version) {
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(10).getData(), 2);
        }
        return false;
    }

    public boolean removeBasePlate() {
        switch (version) {
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(10).getData(), 3);
        }
        return false;
    }

    public boolean hasMarker() {
        switch (version) {
            case VERSION_1_8:
                return BitByte.isBitSet((byte) sets.get(10).getData(), 4);
        }
        return false;
    }

    public EntityRotation getHeadPosition() {
        switch (version) {
            case VERSION_1_8:
                return (EntityRotation) sets.get(11).getData();
        }
        return null;
    }

    public EntityRotation getBodyPosition() {
        switch (version) {
            case VERSION_1_8:
                return (EntityRotation) sets.get(12).getData();
        }
        return null;
    }

    public EntityRotation getLeftArmPosition() {
        switch (version) {
            case VERSION_1_8:
                return (EntityRotation) sets.get(13).getData();
        }
        return null;
    }

    public EntityRotation getRightArmPosition() {
        switch (version) {
            case VERSION_1_8:
                return (EntityRotation) sets.get(14).getData();
        }
        return null;
    }

    public EntityRotation getLeftLegPosition() {
        switch (version) {
            case VERSION_1_8:
                return (EntityRotation) sets.get(15).getData();
        }
        return null;
    }

    public EntityRotation getRightLegPosition() {
        switch (version) {
            case VERSION_1_8:
                return (EntityRotation) sets.get(16).getData();
        }
        return null;
    }


}
