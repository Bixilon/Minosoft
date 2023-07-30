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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.commands.parser.minecraft.target.TargetSelectors
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.CommandEntityTarget
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.EntityTargetProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.center.XCenterProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.center.YCenterProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.center.ZCenterProperty
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.sort.SortProperty
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.world.entities.WorldEntities
import java.util.*

class SelectorEntityTarget(
    val selector: TargetSelectors,
    val properties: Map<String, EntityTargetProperty>,
) : CommandEntityTarget {

    override fun getEntities(executor: Entity?, entities: WorldEntities): List<Entity> {
        val selected: MutableList<Entity> = mutableListOf()
        entities.lock.acquire()
        for (entity in entities) {
            selected += entity
        }
        entities.lock.release()

        val selectorProperties = EntitySelectorProperties(
            entities = selected,
            center = executor?.physics?.position ?: Vec3d(),
            executor = executor,
        )

        properties[XCenterProperty.name]?.updateProperties(selectorProperties)
        properties[YCenterProperty.name]?.updateProperties(selectorProperties)
        properties[ZCenterProperty.name]?.updateProperties(selectorProperties)
        properties[SortProperty.name]?.updateProperties(selectorProperties) ?: selector.sort(selectorProperties.center, selectorProperties.entities)


        val output: MutableList<Entity> = mutableListOf()
        entityLoop@ for (entity in selectorProperties.entities) {
            for (property in properties.values) {
                if (!property.passes(selectorProperties, entity)) {
                    continue@entityLoop
                }
            }
            output += entity
        }

        return output
    }

    override fun hashCode(): Int {
        return Objects.hash(selector, properties)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SelectorEntityTarget) return false
        return selector == other.selector && properties == other.properties
    }

    override fun toString(): String {
        return "@${selector.char}$properties" // TODO: square brackets
    }
}
