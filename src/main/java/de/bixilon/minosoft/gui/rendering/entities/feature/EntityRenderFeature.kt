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

package de.bixilon.minosoft.gui.rendering.entities.feature

import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.visibility.EntityLayer
import de.bixilon.minosoft.gui.rendering.entities.visibility.VisibilityManager

abstract class EntityRenderFeature(val renderer: EntityRenderer<*>) : Comparable<EntityRenderFeature> {
    protected var _enabled = true
    var visible = true
    open val priority: Int get() = 0
    val sort = this::class.java.hashCode()

    open var enabled
        get() = _enabled
        set(value) {
            _enabled = value
        }

    open val layer: EntityLayer get() = EntityLayer.Opaque

    open fun updateVisibility(occluded: Boolean) {
        this.visible = !occluded
    }

    open fun reset() = Unit
    open fun prepare() = Unit
    open fun update(millis: Long, delta: Float) = Unit
    open fun unload() = Unit

    abstract fun draw()

    open fun compareByDistance(other: EntityRenderFeature): Int {
        val compared = renderer.distance.compareTo(other.renderer.distance)
        if (layer === EntityLayer.Translucent) return -compared // translucent is sorted the exact other way
        return compared
    }

    override fun compareTo(other: EntityRenderFeature): Int {
        var compare = priority.compareTo(other.priority)
        if (compare != 0) return compare
        compare = sort.compareTo(other.sort) // dirty sort by type (that makes using of shaders, etc way "faster")
        if (compare != 0) return compare

        return compareByDistance(other)
    }

    open fun collect(visibility: VisibilityManager) {
        visibility[layer] += this
    }
}
