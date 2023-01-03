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
package de.bixilon.minosoft.data.entities.entities.animal.water

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

class TropicalFish(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractSchoolingFish(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val variant: TropicalFishVariants
        get() = TropicalFishVariants.VALUES.getOrNull(data.get(VARIANT_DATA, TropicalFishVariants.KOB.ordinal)) ?: TropicalFishVariants.KOB


    enum class TropicalFishVariants {
        KOB,
        SUN_STREAK,
        SNOOPER,
        DASHER,
        BRINELY,
        SPOTTY,
        FLOPPER,
        STRIPEY,
        GLITTER,
        BLOCKFISH,
        BETTY,
        CLAYFISH,
        ;

        companion object : ValuesEnum<TropicalFishVariants> {
            override val VALUES: Array<TropicalFishVariants> = values()
            override val NAME_MAP: Map<String, TropicalFishVariants> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<TropicalFish> {
        override val identifier: ResourceLocation = ResourceLocation("tropical_fish")
        private val VARIANT_DATA = EntityDataField("TROPICAL_FISH_VARIANT")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): TropicalFish {
            return TropicalFish(connection, entityType, data, position, rotation)
        }
    }
}
