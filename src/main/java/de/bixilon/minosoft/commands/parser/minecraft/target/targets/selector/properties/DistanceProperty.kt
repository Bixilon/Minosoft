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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties

import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.entities.entities.Entity

class DistanceProperty(
    val min: Double = 0.0,
    val max: Double = Double.MAX_VALUE,
) : TargetProperty {

    init {
        check(min >= 0.0) { "Minimum distance can not be below 0" }
        check(max >= min) { "Maximum distance can not be smaller than minimum distance" }
    }

    override fun passes(selected: List<Entity>, entity: Entity): Boolean {
        TODO()
    }

    companion object : TargetPropertyFactory<DistanceProperty> {
        override val name: String = "distance"

        override fun read(reader: CommandReader): DistanceProperty {
            TODO("Not yet implemented")
        }
    }
}
