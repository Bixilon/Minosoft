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
package de.bixilon.minosoft.data.entities.meta;

public class SpellcasterMetaData extends LivingMetaData {

    public SpellcasterMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public SpellTypes getSpell() {
        final int defaultValue = SpellTypes.NONE.ordinal();
        if (protocolId < 315) { // ToDo
            return SpellTypes.byId(defaultValue);
        }
        return SpellTypes.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 1;
    }

    public enum SpellTypes {
        NONE,
        SUMMON_VEX,
        ATTACK,
        WOLOLO,
        DISAPPEAR,
        BLINDNESS;

        public static SpellTypes byId(int id) {
            return values()[id];
        }
    }
}
