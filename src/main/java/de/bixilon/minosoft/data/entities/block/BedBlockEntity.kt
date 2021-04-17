/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.protocol.network.connection.PlayConnection

class BedBlockEntity(connection: PlayConnection) : BlockEntity(connection) {
    var color = ChatColors.RED
        private set


    override fun updateNBT(nbt: Map<String, Any>) {
        color = nbt["color"]?.let {
            when (it) {
                is String -> {
                    RGBColor(it)
                }
                is Number -> {
                    when (it.toInt()) {
                        0 -> RGBColor(255, 255, 255) // white
                        1 -> RGBColor(234, 103, 3) // orange
                        2 -> RGBColor(199, 78, 189) // magenta
                        3 -> RGBColor(47, 162, 212) // light blue
                        4 -> RGBColor(251, 194, 32) // yellow
                        5 -> RGBColor(101, 178, 24) // lime
                        6 -> RGBColor(236, 126, 161) // pink
                        7 -> RGBColor(76, 76, 76) // gray
                        8 -> RGBColor(130, 130, 120) // light gray
                        9 -> RGBColor(22, 128, 142) // cyan
                        10 -> RGBColor(99, 30, 154) // purple
                        11 -> RGBColor(44, 46, 143) // blue
                        12 -> RGBColor(105, 65, 35) // brown
                        13 -> RGBColor(77, 97, 34) // green
                        14 -> RGBColor(139, 30, 31) // red
                        15 -> RGBColor(15, 16, 19) // black
                        else -> TODO("Can not find color!")
                    }
                }
                else -> {
                    TODO()
                }
            }
        } ?: ChatColors.RED
    }

    companion object : BlockEntityFactory<BedBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:bed")

        override fun build(connection: PlayConnection): BedBlockEntity {
            return BedBlockEntity(connection)
        }
    }
}
