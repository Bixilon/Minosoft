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
package de.bixilon.minosoft.data.entities.entities.animal.horse

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class Llama(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractChestedHorse(connection, entityType, data, position, rotation) {

    override val primaryPassenger: LivingEntity? get() = null

    @get:SynchronizedEntityData
    val strength: Int
        get() = data.get(STRENGTH_DATA, 0)

    @get:SynchronizedEntityData
    val carpetColor: Int
        get() = data.get(CARPET_COLOR_DATA, -1)

    @get:SynchronizedEntityData
    val variant: LlamaVariant
        get() = LlamaVariant.VALUES.getOrNull(data.get(VARIANT_DATA, LlamaVariant.CREAMY.ordinal)) ?: LlamaVariant.CREAMY


    enum class LlamaVariant {
        CREAMY,
        WHITE,
        BROWN,
        GRAY,
        ;

        companion object : ValuesEnum<LlamaVariant> {
            override val VALUES: Array<LlamaVariant> = values()
            override val NAME_MAP: Map<String, LlamaVariant> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Llama> {
        override val identifier: ResourceLocation = minecraft("llama")
        private val STRENGTH_DATA = EntityDataField("LLAMA_STRENGTH")
        private val CARPET_COLOR_DATA = EntityDataField("LLAMA_CARPET_COLOR")
        private val VARIANT_DATA = EntityDataField("LLAMA_VARIANT")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Llama {
            return Llama(connection, entityType, data, position, rotation)
        }
    }
}
