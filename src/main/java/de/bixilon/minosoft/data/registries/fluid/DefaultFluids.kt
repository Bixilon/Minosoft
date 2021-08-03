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

package de.bixilon.minosoft.data.registries.fluid

import de.bixilon.minosoft.util.KUtil.toResourceLocation

object DefaultFluids {
    val EMPTY = "minecraft:empty".toResourceLocation()
    val FLOWING_WATER = "minecraft:flowing_water".toResourceLocation()
    val WATER = "minecraft:water".toResourceLocation()
    val FLOWING_LAVA = "minecraft:flowing_lava".toResourceLocation()
    val LAVA = "minecraft:lava".toResourceLocation()
}
