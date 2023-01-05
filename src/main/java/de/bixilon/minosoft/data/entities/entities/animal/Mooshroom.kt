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
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class Mooshroom(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Cow(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val variant: MooshroomVariants
        get() = MooshroomVariants.NAME_MAP[data.get(VARIANT_DATA, MooshroomVariants.RED.name).lowercase()] ?: MooshroomVariants.RED


    enum class MooshroomVariants {
        RED,
        BROWN,
        ;

        companion object : ValuesEnum<MooshroomVariants> {
            override val VALUES: Array<MooshroomVariants> = values()
            override val NAME_MAP: Map<String, MooshroomVariants> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Mooshroom> {
        override val identifier: ResourceLocation = minecraft("mooshroom")
        private val VARIANT_DATA = EntityDataField("MOOSHROOM_VARIANT")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Mooshroom {
            return Mooshroom(connection, entityType, data, position, rotation)
        }
    }
}
