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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.position.center

import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.EntitySelectorProperties
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.EntityTargetProperty
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.set
import java.util.*

abstract class CenterProperty(
    val axis: Axes,
    val value: Double,
) : EntityTargetProperty {

    override fun passes(properties: EntitySelectorProperties, entity: Entity): Boolean {
        return true
    }

    override fun updateProperties(properties: EntitySelectorProperties) {
        properties.center[axis] = value
    }

    override fun hashCode(): Int {
        return Objects.hash(axis, value)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CenterProperty) return false
        return axis == other.axis && value == other.value
    }
}
