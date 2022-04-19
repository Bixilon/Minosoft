/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Panda(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    @get:SynchronizedEntityData(name = "Unhappy timer")
    val unhappyTimer: Int
        get() = data.sets.getInt(EntityDataFields.PANDA_UNHAPPY_TIMER)

    @get:SynchronizedEntityData(name = "Sneeze timer")
    val sneezeTimer: Int
        get() = data.sets.getInt(EntityDataFields.PANDA_SNEEZE_TIMER)

    @get:SynchronizedEntityData(name = "Eat timer")
    val eatTimer: Int
        get() = data.sets.getInt(EntityDataFields.PANDA_EAT_TIMER)

    @get:SynchronizedEntityData(name = "Main gene")
    val mainGene: Genes
        get() = Genes.byId(data.sets.getInt(EntityDataFields.PANDA_MAIN_GENE))

    @get:SynchronizedEntityData(name = "Hidden gene")
    val hiddenGene: Genes
        get() = Genes.byId(data.sets.getInt(EntityDataFields.PANDA_HIDDEN_GAME))

    private fun getPandaFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.PANDA_FLAGS, bitMask)
    }

    @get:SynchronizedEntityData(name = "Is sneezing")
    val isSneezing: Boolean
        get() = getPandaFlag(0x02)

    @get:SynchronizedEntityData(name = "Is rolling")
    val isRolling: Boolean
        get() = getPandaFlag(0x04)

    @get:SynchronizedEntityData(name = "Is sitting")
    val isSitting: Boolean
        get() = getPandaFlag(0x08)

    @get:SynchronizedEntityData(name = "Is on back")
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

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Panda {
            return Panda(connection, entityType, position, rotation)
        }
    }
}
