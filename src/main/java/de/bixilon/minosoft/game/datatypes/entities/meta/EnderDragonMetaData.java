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


public class EnderDragonMetaData extends InsentientMetaData {

    public EnderDragonMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }


    public DragonPhases getDragonPhase() {
        final int defaultValue = DragonPhases.HOVERING.getId();
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return DragonPhases.byId(defaultValue);
        }
        return DragonPhases.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 1;
    }

    public enum DragonPhases {
        CIRCLING(0),
        STRAFING(1),
        FLYING_TO_PORTAL_TO_LAND(2),
        LANDING_ON_PORTAL(3),
        TAKING_OFF_THE_PORTAL(4),
        LANDED_BREATH_ATTACK(5),
        LANDED_ROAR(6),
        CHARGING_PLAYER(7),
        FLYING_TO_DIE(8),
        HOVERING(10);

        final int id;

        DragonPhases(int id) {
            this.id = id;
        }

        public static DragonPhases byId(int id) {
            for (DragonPhases phase : values()) {
                if (phase.getId() == id) {
                    return phase;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
