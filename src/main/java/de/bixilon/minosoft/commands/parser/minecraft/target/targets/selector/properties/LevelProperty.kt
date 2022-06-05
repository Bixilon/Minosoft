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

import de.bixilon.minosoft.commands.parser.minecraft.range._int.IntRangeParser
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.EntitySelectorProperties
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.entities.entities.Entity

class LevelProperty(
    val range: IntRange,
) : EntityTargetProperty {

    override fun passes(properties: EntitySelectorProperties, entity: Entity): Boolean {
        TODO()
    }

    companion object : EntityTargetPropertyFactory<LevelProperty> {
        override val name: String = "level"
        private val parser = IntRangeParser()

        override fun read(reader: CommandReader): LevelProperty {
            return LevelProperty(parser.parse(reader))
        }
    }
}
