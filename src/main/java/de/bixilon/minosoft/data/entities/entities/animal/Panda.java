/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.entities.animal;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class Panda extends Animal {

    public Panda(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    @EntityMetaDataFunction(identifier = "unhappyTimer")
    public int getUnhappyTimer() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.PANDA_UNHAPPY_TIMER);
    }

    @EntityMetaDataFunction(identifier = "sneezeTimer")
    public int getSneezeTimer() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.PANDA_SNEEZE_TIMER);
    }

    @EntityMetaDataFunction(identifier = "eatTimer")
    public int getEatTimer() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.PANDA_EAT_TIMER);
    }

    @EntityMetaDataFunction(identifier = "mainGene")
    public Genes getMainGene() {
        return Genes.byId(this.metaData.getSets().getInt(EntityMetaDataFields.PANDA_MAIN_GENE));
    }

    @EntityMetaDataFunction(identifier = "hiddenGene")
    public Genes getHiddenGene() {
        return Genes.byId(this.metaData.getSets().getInt(EntityMetaDataFields.PANDA_HIDDEN_GAME));
    }

    private boolean getPandaFlag(int bitMask) {
        return this.metaData.getSets().getBitMask(EntityMetaDataFields.PANDA_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(identifier = "isSneezing")
    public boolean isSneezing() {
        return getPandaFlag(0x02);
    }

    @EntityMetaDataFunction(identifier = "isRolling")
    public boolean isRolling() {
        return getPandaFlag(0x04);
    }

    @EntityMetaDataFunction(identifier = "isSitting")
    public boolean isSitting() {
        return getPandaFlag(0x08);
    }

    @EntityMetaDataFunction(identifier = "isOnBack")
    public boolean isOnBack() {
        return getPandaFlag(0x10);
    }

    public enum Genes {
        NORMAL,
        LAZY,
        WORRIED,
        PLAYFUL,
        BROWN,
        WEAK,
        AGGRESSIVE;
        private static final Genes[] GENES = values();

        public static Genes byId(int id) {
            return GENES[id];
        }
    }
}
