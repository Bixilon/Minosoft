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

import javax.annotation.Nullable;

public class LivingMetaData extends EntityMetaData {

    public LivingMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }


    public boolean isHandActive() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBitMask(5, 0x01, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBitMask(6, 0x01, defaultValue);
        }
        return sets.getBitMask(7, 0x01, defaultValue);
    }

    public Hand getActiveHand() {
        final int defaultValue = Hand.LEFT.getId();
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return Hand.byId(defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return Hand.byBoolean(sets.getBitMask(5, 0x02, defaultValue == 0x01));
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return Hand.byBoolean(sets.getBitMask(6, 0x02, defaultValue == 0x01));
        }
        return Hand.byBoolean(sets.getBitMask(7, 0x02, defaultValue == 0x01));
    }

    public boolean isRiptideSpinAttack() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x04, defaultValue);
    }


    public float getHealth() {
        final float defaultValue = 1.0F;
        return sets.getFloat(super.getLastDataIndex() + 2, defaultValue);
    }

    public int getPotionEffectColor() {
        // ToDo: color?
        final int defaultValue = 0;
        return sets.getInt(super.getLastDataIndex() + 3, defaultValue);
    }


    public boolean isPotionEffectAmbient() {
        final boolean defaultValue = false;
        return sets.getBoolean(super.getLastDataIndex() + 4, defaultValue);
    }

    public int getNumberOfArrowsInEntity() {
        final int defaultValue = 0;
        return sets.getInt(super.getLastDataIndex() + 5, defaultValue);
    }

    @Nullable
    @Override
    public TextComponent getNameTag() {
        switch (version) {
            case VERSION_1_7_10:
                return new TextComponent(sets.getString(10, null));
            case VERSION_1_8:
                return new TextComponent(sets.getString(2, null));
            default:
                return super.getNameTag();
        }
    }

    @Override
    public boolean isCustomNameVisible() {
        switch (version) {
            case VERSION_1_7_10:
                return sets.getBoolean(11, super.isCustomNameVisible());
            case VERSION_1_8:
                return sets.getBoolean(3, super.isCustomNameVisible());
            default:
                return super.isCustomNameVisible();
        }
    }

    public boolean hasAI() {
        if (version == ProtocolVersion.VERSION_1_8) {
            return sets.getBoolean(15, false);
        }
        return false;
    }

    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return super.getLastDataIndex() + 5;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return super.getLastDataIndex() + 6;
        }
        return super.getLastDataIndex() + 7; // 5 + absorption hearts + unknown
    }

}
