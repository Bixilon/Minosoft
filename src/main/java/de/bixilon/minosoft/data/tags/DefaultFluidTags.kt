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

package de.bixilon.minosoft.data.tags

import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.util.KUtil.asResourceLocation

object DefaultFluidTags {
    // ToDo: Improve this

    val WATER_TAG = "minecraft:water".asResourceLocation()
    val WATER = setOf(DefaultFluids.WATER, DefaultFluids.FLOWING_WATER)
    val LAVA_TAG = "minecraft:lava".asResourceLocation()
    val LAVA = setOf(DefaultFluids.LAVA, DefaultFluids.FLOWING_LAVA)


    val FLUID_TAGS = mapOf(
        WATER_TAG to WATER,
        LAVA_TAG to LAVA,
    )
}
