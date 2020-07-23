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

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import java.util.HashMap;

public class PandaMetaData extends AnimalMetaData {

    public PandaMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    public int getBreedTimer() {
        switch (version) {
            case VERSION_1_14_4:
                return (int) sets.get(15).getData();
        }
        return 0;
    }

    public int getSneezeTimer() {
        switch (version) {
            case VERSION_1_14_4:
                return (int) sets.get(16).getData();
        }
        return 0;
    }

    public int getEatTimer() {
        switch (version) {
            case VERSION_1_14_4:
                return (int) sets.get(17).getData();
        }
        return 0;
    }

    public byte getMainGene() {
        switch (version) {
            case VERSION_1_14_4:
                return (byte) sets.get(18).getData();
        }
        return 0;
    }

    public byte getHiddenGene() {
        switch (version) {
            case VERSION_1_14_4:
                return (byte) sets.get(19).getData();
        }
        return 0;
    }

    public boolean isSneezing() {
        switch (version) {
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(20).getData(), 0x02);
        }
        return false;
    }

    @Override
    public boolean isEating() {
        switch (version) {
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(20).getData(), 0x04);
        }
        return false;
    }

}
