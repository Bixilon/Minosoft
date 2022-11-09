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

package de.bixilon.minosoft.gui.rendering.sky.properties

import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.minecraft

object OverworldSkyProperties : SkyProperties {
    override val resourceLocation = minecraft("overworld")

    override val daylightCycle: Boolean get() = true
    override val skylight: Boolean get() = true

    override val sun: Boolean get() = true
    override val moon: Boolean get() = true
    override val stars: Boolean get() = true

    override val clouds: Boolean get() = true
    override fun getCloudHeight(connection: PlayConnection): IntRange {
        val height = connection.world.dimension?.dataHeight ?: DimensionProperties.DEFAULT_HEIGHT
        if (height > DimensionProperties.DEFAULT_HEIGHT) {
            return 192..196
        }
        return 128..132
    }

    override val brighten: Boolean get() = false

    override val fog: Boolean get() = true
}
