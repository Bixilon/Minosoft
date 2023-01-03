/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class Panda(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val askForBambooTimer: Int
        get() = data.get(BAMBOO_ASK_TIMER_DATA, 0)

    @get:SynchronizedEntityData
    val sneezeTimer: Int
        get() = data.get(SNEEZE_TIMER_DATA, 0)

    @get:SynchronizedEntityData
    val eatTimer: Int
        get() = data.get(EATING_TICKS_DATA, 0)

    @get:SynchronizedEntityData
    val mainGene: Genes
        get() = Genes.VALUES.getOrNull(data.get(MAIN_GENE_DATA, Genes.NORMAL.ordinal)) ?: Genes.NORMAL

    @get:SynchronizedEntityData
    val hiddenGene: Genes
        get() = Genes.VALUES.getOrNull(data.get(HIDDEN_GENE_DATA, Genes.NORMAL.ordinal)) ?: Genes.NORMAL

    private fun getPandaFlag(bitMask: Int): Boolean {
        return data.getBitMask(FLAGS_DATA, bitMask, 0x00)
    }

    @get:SynchronizedEntityData
    val isSneezing: Boolean
        get() = getPandaFlag(0x02)

    @get:SynchronizedEntityData
    val isRolling: Boolean
        get() = getPandaFlag(0x04)

    @get:SynchronizedEntityData
    val isSitting: Boolean
        get() = getPandaFlag(0x08)

    @get:SynchronizedEntityData
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

        companion object : ValuesEnum<Genes> {
            override val VALUES: Array<Genes> = values()
            override val NAME_MAP: Map<String, Genes> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Panda> {
        override val identifier: ResourceLocation = KUtil.minecraft("panda")
        private val BAMBOO_ASK_TIMER_DATA = EntityDataField("PANDA_UNHAPPY_TIMER")
        private val SNEEZE_TIMER_DATA = EntityDataField("PANDA_SNEEZE_TIMER")
        private val EATING_TICKS_DATA = EntityDataField("PANDA_EAT_TIMER")
        private val MAIN_GENE_DATA = EntityDataField("PANDA_MAIN_GENE")
        private val HIDDEN_GENE_DATA = EntityDataField("PANDA_HIDDEN_GENE", "PANDA_HIDDEN_GAME")
        private val FLAGS_DATA = EntityDataField("PANDA_FLAGS")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Panda {
            return Panda(connection, entityType, data, position, rotation)
        }
    }
}
