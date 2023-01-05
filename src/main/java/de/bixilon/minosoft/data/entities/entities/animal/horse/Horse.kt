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
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil

class Horse(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractHorse(connection, entityType, data, position, rotation) {

    private val variant: Int
        get() = data.get(VARIANT_DATA, 0)

    @get:SynchronizedEntityData
    val color: HorseColors
        get() = HorseColors.VALUES.getOrNull(variant and 0xFF) ?: HorseColors.WHITE

    @get:SynchronizedEntityData
    val markings: HorseMarkings
        get() = HorseMarkings.VALUES.getOrNull((variant shr 8) and 0xFF) ?: HorseMarkings.NONE

    // ToDo
    @get:SynchronizedEntityData
    val armor: Item?
        get() {
            if (connection.version.versionId <= ProtocolVersions.V_1_8_9) { // ToDo
                return connection.registries.item[when (this.data.get(LEGACY_ARMOR_DATA, 0)) {
                    1 -> LEGACY_IRON_ARMOR
                    2 -> LEGACY_GOLD_ARMOR
                    3 -> LEGACY_DIAMOND_ARMOR
                    else -> null
                }]
            }
            return null
        }

    enum class HorseColors {
        WHITE,
        CREAMY,
        CHESTNUT,
        BROWN,
        BLACK,
        GRAY,
        DARK_BROWN,
        ;

        companion object : ValuesEnum<HorseColors> {
            override val VALUES: Array<HorseColors> = values()
            override val NAME_MAP: Map<String, HorseColors> = EnumUtil.getEnumValues(VALUES)
        }
    }

    enum class HorseMarkings {
        NONE,
        WHITE,
        WHITE_FIELD,
        WHITE_DOTS,
        BLACK_DOTS,
        ;

        companion object : ValuesEnum<HorseMarkings> {
            override val VALUES: Array<HorseMarkings> = values()
            override val NAME_MAP: Map<String, HorseMarkings> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Horse> {
        override val identifier: ResourceLocation = KUtil.minecraft("horse")
        private val VARIANT_DATA = EntityDataField("HORSE_VARIANT")
        private val LEGACY_ARMOR_DATA = EntityDataField("LEGACY_HORSE_ARMOR")

        private val LEGACY_IRON_ARMOR = KUtil.minecraft("iron_horse_armor")
        private val LEGACY_GOLD_ARMOR = KUtil.minecraft("golden_horse_armor")
        private val LEGACY_DIAMOND_ARMOR = KUtil.minecraft("diamond_horse_armor")

        private val LEGACY_SPECIAL_TYPE_DATA = EntityDataField("LEGACY_HORSE_SPECIAL_TYPE")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Horse {
            return Horse(connection, entityType, data, position, rotation)
        }

        override fun tweak(connection: PlayConnection, data: EntityData?, versionId: Int): ResourceLocation {
            if (data == null || versionId <= ProtocolVersions.V_1_8_9) {
                return identifier
            }
            val specialType = data.get(LEGACY_SPECIAL_TYPE_DATA, 0)
            return when (specialType) {
                1 -> Donkey
                2 -> Mule
                3 -> ZombieHorse
                4 -> SkeletonHorse
                else -> this
            }.identifier
        }
    }
}
