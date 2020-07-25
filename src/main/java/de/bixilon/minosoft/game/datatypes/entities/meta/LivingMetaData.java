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

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.player.Hand;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import java.util.HashMap;

public class LivingMetaData extends EntityMetaData {

    public LivingMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }


    public float getHealth() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
                return (float) sets.get(6).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (float) sets.get(7).getData();
            case VERSION_1_14_4:
                return (float) sets.get(8).getData();
        }
        return 1.0F;
    }

    public int getPotionEffectColor() {
        // ToDo: color?
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
                return (int) sets.get(7).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (int) sets.get(8).getData();
            case VERSION_1_14_4:
                return (int) sets.get(9).getData();
        }
        return 0;
    }


    public boolean isPotionEffectAmbient() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (byte) sets.get(8).getData() == 0x01;
            case VERSION_1_9_4:
                return (boolean) sets.get(8).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (boolean) sets.get(9).getData();
            case VERSION_1_14_4:
                return (boolean) sets.get(10).getData();
        }
        return false;
    }

    public int getNumberOfArrowsInEntity() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (byte) sets.get(9).getData();
            case VERSION_1_9_4:
                return (int) sets.get(9).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (int) sets.get(10).getData();
            case VERSION_1_14_4:
                return (int) sets.get(11).getData();
        }
        return 0;
    }

    @Override
    public TextComponent getNameTag() {
        switch (version) {
            case VERSION_1_7_10:
                return new TextComponent((String) sets.get(10).getData());
            case VERSION_1_8:
                return new TextComponent((String) sets.get(2).getData());
            default:
                return super.getNameTag();
        }
    }

    @Override
    public boolean isCustomNameVisible() {
        switch (version) {
            case VERSION_1_7_10:
                return (byte) sets.get(11).getData() == 0x01;
            case VERSION_1_8:
                return (byte) sets.get(3).getData() == 0x01;
            default:
                return super.isCustomNameVisible();
        }
    }

    public boolean hasAI() {
        switch (version) {
            case VERSION_1_8:
                return (byte) sets.get(15).getData() == 0x01;
        }
        return false;
    }

    public boolean isHandActive() {
        switch (version) {
            case VERSION_1_9_4:
                return BitByte.isBitMask((byte) sets.get(5).getData(), 0x01);
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((byte) sets.get(6).getData(), 0x01);
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(7).getData(), 0x01);
        }
        return false;
    }

    public Hand getActiveHand() {
        // ToDo main, offhand
        switch (version) {
            case VERSION_1_9_4:
                return BitByte.isBitMask((byte) sets.get(5).getData(), 0x02) ? Hand.LEFT : Hand.RIGHT;
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((byte) sets.get(6).getData(), 0x02) ? Hand.LEFT : Hand.RIGHT;
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(7).getData(), 0x02) ? Hand.LEFT : Hand.RIGHT;
        }
        return Hand.RIGHT;
    }

    public boolean isRiptideSpinAttack() {
        switch (version) {
            case VERSION_1_13_2:
                return BitByte.isBitMask((byte) sets.get(6).getData(), 0x04);
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(7).getData(), 0x04);
        }
        return false;
    }
}
