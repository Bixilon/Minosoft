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
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;

public class Panda extends Animal {

    public Panda(Connection connection, Vec3 position, EntityRotation rotation) {
        super(connection, position, rotation);
    }

    @EntityMetaDataFunction(name = "Unhappy timer")
    public int getUnhappyTimer() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.PANDA_UNHAPPY_TIMER);
    }

    @EntityMetaDataFunction(name = "Sneeze timer")
    public int getSneezeTimer() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.PANDA_SNEEZE_TIMER);
    }

    @EntityMetaDataFunction(name = "Eat timer")
    public int getEatTimer() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.PANDA_EAT_TIMER);
    }

    @EntityMetaDataFunction(name = "Main gene")
    public Genes getMainGene() {
        return Genes.byId(getEntityMetaData().getSets().getInt(EntityMetaDataFields.PANDA_MAIN_GENE));
    }

    @EntityMetaDataFunction(name = "Hidden gene")
    public Genes getHiddenGene() {
        return Genes.byId(getEntityMetaData().getSets().getInt(EntityMetaDataFields.PANDA_HIDDEN_GAME));
    }

    private boolean getPandaFlag(int bitMask) {
        return getEntityMetaData().getSets().getBitMask(EntityMetaDataFields.PANDA_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(name = "Is sneezing")
    public boolean isSneezing() {
        return getPandaFlag(0x02);
    }

    @EntityMetaDataFunction(name = "Is rolling")
    public boolean isRolling() {
        return getPandaFlag(0x04);
    }

    @EntityMetaDataFunction(name = "Is sitting")
    public boolean isSitting() {
        return getPandaFlag(0x08);
    }

    @EntityMetaDataFunction(name = "Is on back")
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
