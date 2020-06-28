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

import de.bixilon.minosoft.game.datatypes.entities.StatusEffects;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class MobMetaData extends EntityMetaData {

    public MobMetaData(InByteBuffer buffer) {
        super(buffer);
    }


    public float getHealth() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (float) sets.get(6).getData();
        }
        return 0.0F;
    }

    public StatusEffects getPotionEffectColor() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return StatusEffects.byId((int) sets.get(7).getData());
        }
        return null;
    }


    public byte getPotionEffectAmbient() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (byte) sets.get(8).getData();
        }
        return 0;
    }

    public byte getNumberOfArrowsInEntity() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (byte) sets.get(9).getData();
        }
        return 0;
    }

    public String getNameTag() {
        switch (version) {
            case VERSION_1_7_10:
                return (String) sets.get(10).getData();
            case VERSION_1_8:
                return (String) sets.get(2).getData();
        }
        return null;
    }

    public byte getAlwaysShowNameTag() {
        switch (version) {
            case VERSION_1_7_10:
                return (byte) sets.get(11).getData();
            case VERSION_1_8:
                return (byte) sets.get(3).getData();
        }
        return 0;
    }

    public boolean hasAI() {
        switch (version) {
            case VERSION_1_8:
                return (byte) sets.get(15).getData() == 0x01;
        }
        return false;
    }


}
