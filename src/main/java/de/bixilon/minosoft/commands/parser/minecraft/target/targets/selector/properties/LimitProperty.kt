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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties

import de.bixilon.minosoft.commands.parser.brigadier._int.IntParser.Companion.readRequiredInt
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.EntitySelectorProperties
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.entities.entities.Entity

data class LimitProperty(
    val limit: Int,
) : EntityTargetProperty {

    override fun passes(properties: EntitySelectorProperties, entity: Entity): Boolean {
        return properties.entities.size < limit
    }


    companion object : EntityTargetPropertyFactory<LimitProperty> {
        override val name: String = "limit"

        override fun read(reader: CommandReader): LimitProperty {
            return LimitProperty(reader.readRequiredInt())
        }
    }
}
