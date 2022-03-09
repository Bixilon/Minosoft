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
package de.bixilon.minosoft.data.entities.entities.animal.horse

import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

class Horse(connection: PlayConnection, entityType: EntityType) : AbstractHorse(connection, entityType) {

    private fun getAbstractHorseFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.ABSTRACT_HORSE_FLAGS, bitMask)
    }

    private val variant: Int
        get() = data.sets.getInt(EntityDataFields.HORSE_VARIANT)

    @get:EntityMetaDataFunction(name = "Color")
    val color: HorseColors
        get() = HorseColors.byId(variant and 0xFF)

    @get:EntityMetaDataFunction(name = "Dots")
    val dots: HorseDots
        get() = HorseDots.byId(variant shr 8)

    // ToDo
    @get:EntityMetaDataFunction(name = "Armor")
    val armor: Item?
        get() {
            if (versionId <= ProtocolVersions.V_1_8_9) { // ToDo
                return null
            }
            return connection.registries.itemRegistry[when (this.data.sets.getInt(EntityDataFields.LEGACY_HORSE_ARMOR)) {
                1 -> LEGACY_IRON_ARMOR
                2 -> LEGACY_GOLD_ARMOR
                3 -> LEGACY_DIAMOND_ARMOR
                else -> null
            }]
        }

    enum class HorseColors {
        WHITE,
        CREAMY,
        CHESTNUT,
        BROWN,
        BLACK,
        GRAY,
        DARK_BROWN;

        companion object {
            private val HORSE_COLORS = values()
            fun byId(id: Int): HorseColors {
                return HORSE_COLORS[id]
            }
        }
    }

    enum class HorseDots {
        NONE,
        WHITE,
        WHITEFIELD,
        WHITE_DOTS,
        BLACK_DOTS;

        companion object {
            private val HORSE_DOTS = values()
            fun byId(id: Int): HorseDots {
                return HORSE_DOTS[id]
            }
        }
    }

    companion object : EntityFactory<Horse> {
        private val LEGACY_IRON_ARMOR = ResourceLocation("iron_horse_armor")
        private val LEGACY_GOLD_ARMOR = ResourceLocation("golden_horse_armor")
        private val LEGACY_DIAMOND_ARMOR = ResourceLocation("diamond_horse_armor")
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("horse")

        override fun build(connection: PlayConnection, entityType: EntityType): Horse {
            return Horse(connection, entityType)
        }
    }
}
