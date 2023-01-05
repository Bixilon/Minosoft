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
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class Axolotl(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val variant: AxolotlVariants
        get() = AxolotlVariants.VALUES.getOrNull(data.get(VARIANT_DATA, AxolotlVariants.LUCY.ordinal)) ?: AxolotlVariants.LUCY

    @get:SynchronizedEntityData
    val isPlayingDead: Boolean
        get() = data.getBoolean(PLAYING_DEAD_DATA, false)

    @get:SynchronizedEntityData
    val isFromBucket: Boolean
        get() = data.getBoolean(FROM_BUCKET_DATA, false)

    enum class AxolotlVariants {
        LUCY,
        WILD,
        GOLD,
        CYAN,
        BLUE,
        ;

        companion object : ValuesEnum<AxolotlVariants> {
            override val VALUES: Array<AxolotlVariants> = values()
            override val NAME_MAP: Map<String, AxolotlVariants> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Axolotl> {
        override val identifier: ResourceLocation = KUtil.minecraft("axolotl")
        private val VARIANT_DATA = EntityDataField("AXOLOTL_VARIANT")
        private val PLAYING_DEAD_DATA = EntityDataField("AXOLOTL_PLAYING_DEAD")
        private val FROM_BUCKET_DATA = EntityDataField("AXOLOTL_FROM_BUCKET")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Axolotl {
            return Axolotl(connection, entityType, data, position, rotation)
        }
    }
}
