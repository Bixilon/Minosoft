/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.Connection
import glm_.vec3.Vec3

class Panda(connection: Connection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Unhappy timer")
    val unhappyTimer: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.PANDA_UNHAPPY_TIMER)

    @get:EntityMetaDataFunction(name = "Sneeze timer")
    val sneezeTimer: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.PANDA_SNEEZE_TIMER)

    @get:EntityMetaDataFunction(name = "Eat timer")
    val eatTimer: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.PANDA_EAT_TIMER)

    @get:EntityMetaDataFunction(name = "Main gene")
    val mainGene: Genes
        get() = Genes.byId(entityMetaData.sets.getInt(EntityMetaDataFields.PANDA_MAIN_GENE))

    @get:EntityMetaDataFunction(name = "Hidden gene")
    val hiddenGene: Genes
        get() = Genes.byId(entityMetaData.sets.getInt(EntityMetaDataFields.PANDA_HIDDEN_GAME))

    private fun getPandaFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.PANDA_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is sneezing")
    val isSneezing: Boolean
        get() = getPandaFlag(0x02)

    @get:EntityMetaDataFunction(name = "Is rolling")
    val isRolling: Boolean
        get() = getPandaFlag(0x04)

    @get:EntityMetaDataFunction(name = "Is sitting")
    val isSitting: Boolean
        get() = getPandaFlag(0x08)

    @get:EntityMetaDataFunction(name = "Is on back")
    val isOnBack: Boolean
        get() = getPandaFlag(0x10)

    enum class Genes {
        NORMAL,
        LAZY,
        WORRIED,
        PLAYFUL,
        BROWN,
        WEAK,
        AGGRESSIVE,
        ;

        companion object {
            private val GENES = values()
            fun byId(id: Int): Genes {
                return GENES[id]
            }
        }
    }

    companion object : EntityFactory<Panda> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("panda")

        override fun build(connection: Connection, entityType: EntityType, position: Vec3, rotation: EntityRotation): Panda {
            return Panda(connection, entityType, position, rotation)
        }
    }
}
