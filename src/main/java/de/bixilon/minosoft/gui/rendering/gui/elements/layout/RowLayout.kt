/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.elements.layout

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import glm_.vec2.Vec2i

/**
 * A layout, that works from top to bottom, containing other elements, that get wrapped below each other
 */
class RowLayout : Layout() {
    // ToDo: Spacing between elements
    private val children: MutableList<Element> = synchronizedListOf()

    override fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        var childYOffset = 0
        var totalZ = 0
        for (child in children) {
            val childZ = child.render(Vec2i(offset.x, offset.y + childYOffset), z, consumer)
            if (totalZ < childZ) {
                totalZ = childZ
            }
            childYOffset += child.size.y
        }

        return totalZ
    }

    operator fun plusAssign(element: Element) {
        element.parent = this
        children += element

        // ToDo: Optimize
        childChange(element)
        parent?.childChange(this)
    }

    override fun childChange(child: Element) {
        super.childChange(child)

        val size = Vec2i(0, 0)

        for (element in children) {
            size.y += element.size.y

            if (element.size.x > size.x) {
                size.x = element.size.x
            }

            // ToDo: Check max size
        }

        this.size = size
    }
}
