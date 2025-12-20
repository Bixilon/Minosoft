/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.entities.visibility

import de.bixilon.minosoft.gui.rendering.system.base.layer.OpaqueLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.TranslucentLayer

interface EntityLayer : RenderLayer {
    val sort get() = EntitySortOrders.NEAREST_FIRST

    object Opaque : EntityLayer {
        override val settings = OpaqueLayer.settings.copy(faceCulling = false)
        override val priority: Int get() = OpaqueLayer.priority + 1 // entities are mostly more expensive to render, let the gpu clip all fragments first
    }

    object Translucent : EntityLayer {
        override val settings = TranslucentLayer.settings.copy(faceCulling = false)
        override val priority: Int get() = TranslucentLayer.priority - 1 // otherwise not visible through water, etc
        override val sort get() = EntitySortOrders.FURTHEST_FIRST
    }

    enum class EntitySortOrders(val sign: Int) {
        NEAREST_FIRST(+1),
        FURTHEST_FIRST(-1),
    }

    companion object {
        val LAYERS by lazy { arrayOf(Opaque, Translucent) } // This is screwed, the companion instance is null during initialization since kotlin 2.3
    }
}
