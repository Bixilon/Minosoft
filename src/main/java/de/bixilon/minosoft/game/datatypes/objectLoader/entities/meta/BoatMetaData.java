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
package de.bixilon.minosoft.game.datatypes.objectLoader.entities.meta;

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class BoatMetaData extends EntityMetaData {

    public BoatMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    public int getTimeSinceHit() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (int) sets.get(17).getData();
            case VERSION_1_9_4:
                return (int) sets.get(5).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (int) sets.get(6).getData();
            case VERSION_1_14_4:
                return (int) sets.get(7).getData();
        }
        return 0;
    }

    public int getForwardDirection() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (int) sets.get(18).getData();
            case VERSION_1_9_4:
                return (int) sets.get(6).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (int) sets.get(7).getData();
            case VERSION_1_14_4:
                return (int) sets.get(8).getData();
        }
        return 1;
    }

    public float getDamageTaken() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (float) sets.get(19).getData();
            case VERSION_1_9_4:
                return (float) sets.get(7).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (float) sets.get(8).getData();
            case VERSION_1_14_4:
                return (float) sets.get(9).getData();
        }
        return 0.0F;
    }

    public BoatMaterial getMaterial() {
        switch (version) {
            case VERSION_1_9_4:
                return BoatMaterial.byId((int) sets.get(8).getData());
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BoatMaterial.byId((int) sets.get(9).getData());
            case VERSION_1_14_4:
                return BoatMaterial.byId((int) sets.get(10).getData());
        }
        return BoatMaterial.OAK;
    }

    public boolean isRightPaddleTurning() {
        switch (version) {
            case VERSION_1_9_4:
                return (boolean) sets.get(9).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return (boolean) sets.get(10).getData();
            case VERSION_1_13_2:
                return (boolean) sets.get(11).getData();
            case VERSION_1_14_4:
                return (boolean) sets.get(12).getData();
        }
        return false;
    }

    public boolean isLeftPaddleTurning() {
        switch (version) {
            case VERSION_1_9_4:
            case VERSION_1_13_2:
                return (boolean) sets.get(10).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_14_4:
                return (boolean) sets.get(11).getData();
        }
        return false;
    }

    public int getSplashTimer() {
        switch (version) {
            case VERSION_1_13_2:
                return (int) sets.get(12).getData();
            case VERSION_1_14_4:
                return (int) sets.get(13).getData();
        }
        return 0;
    }

    public enum BoatMaterial {
        OAK(0),
        SPRUCE(1),
        BIRCH(2),
        JUNGLE(3),
        ACACIA(4),
        DARK_OAK(5);

        final int id;

        BoatMaterial(int id) {
            this.id = id;
        }

        public static BoatMaterial byId(int id) {
            for (BoatMaterial material : values()) {
                if (material.getId() == id) {
                    return material;
                }
            }
            return OAK;
        }

        public int getId() {
            return id;
        }
    }
}
