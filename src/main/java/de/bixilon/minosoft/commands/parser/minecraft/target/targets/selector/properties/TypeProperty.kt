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

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.ResourceLocation

class TypeProperty(
    val type: ResourceLocation,
    val negated: Boolean,
) : TargetProperty {

    override fun passes(selected: List<Entity>, entity: Entity): Boolean {
        if (negated) {
            return entity.type.resourceLocation != type
        }
        return entity.type.resourceLocation == type
    }


    companion object : TargetPropertyFactory<TypeProperty> {
        override val name: String = "type"

        override fun read(reader: CommandReader): TypeProperty {
            val (resourceLocation, negated) = reader.readNegateable { readResourceLocation() ?: throw ExpectedArgumentError(reader) } ?: throw ExpectedArgumentError(reader)
            return TypeProperty(resourceLocation, negated)
        }
    }
}
