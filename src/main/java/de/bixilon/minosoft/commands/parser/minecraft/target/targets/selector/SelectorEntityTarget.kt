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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.commands.parser.minecraft.target.TargetSelectors
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.EntityTarget
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.TargetProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.sort.SortProperty
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.world.WorldEntities

class SelectorEntityTarget(
    val selector: TargetSelectors,
    val properties: Map<String, TargetProperty>,
) : EntityTarget {

    override fun getEntities(entities: WorldEntities): List<Entity> {
        val selected: MutableList<Entity> = mutableListOf()
        entities.lock.acquire()
        for (entity in entities) {
            selected += entity
        }
        entities.lock.release()

        properties[SortProperty.name]?.nullCast<SortProperty>()?.sort(selected) ?: selector.sort(selected)

        val output: MutableList<Entity> = mutableListOf()
        entityLoop@ for (entity in selected) {
            for (property in properties.values) {
                if (!property.passes(output, entity)) {
                    continue@entityLoop
                }
            }
            output += entity
        }

        return output
    }
}
