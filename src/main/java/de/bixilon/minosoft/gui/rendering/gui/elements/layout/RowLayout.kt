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
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.bottom
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.horizontal
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.left
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.spaceSize
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.top
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import glm_.vec2.Vec2i
import java.lang.Integer.max

/**
 * A layout, that works from top to bottom, containing other elements, that get wrapped below each other
 */
open class RowLayout(
    hudRenderer: HUDRenderer,
    override var childAlignment: ElementAlignments = ElementAlignments.LEFT,
    spacing: Int = 0,
) : Layout(hudRenderer), ChildAlignable {
    private var _prefSize = Vec2i.EMPTY

    // ToDo: Spacing between elements
    private val children: MutableList<Element> = synchronizedListOf()

    override var prefSize: Vec2i
        get() = _prefSize
        set(value) {}

    var spacing: Int = spacing
        set(value) {
            field = value
            apply()
        }

    fun clear() {
        children.clear()
    }

    override fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        // ToDo: Check max size

        var childYOffset = margin.top
        var maxZ = 0
        for (child in children) {
            val childZ = child.render(Vec2i(offset.x + margin.left + childAlignment.getOffset(size.x - margin.horizontal, child.size.x), offset.y + childYOffset), z, consumer)
            if (maxZ < childZ) {
                maxZ = childZ
            }
            childYOffset += child.margin.top
            childYOffset += child.size.y
            childYOffset += child.margin.bottom
            childYOffset += spacing
        }

        return maxZ
    }

    operator fun plusAssign(element: Element) = add(element)

    fun add(element: Element) {
        element.parent = this
        children += element


        element.onParentChange()
        apply() // ToDo: Optimize
        parent?.onChildChange(this)
    }

    override fun apply() {
        silentApply()
        parent?.onChildChange(this)
    }

    override fun silentApply() {
        val maxSize = maxSize
        val size = Vec2i(0, 0)
        val prefSize = margin.spaceSize
        val xMargin = margin.horizontal

        for (child in children) {
            prefSize.x = max(prefSize.x, xMargin + child.prefSize.x)
            prefSize.y += child.prefSize.y
        }

        _prefSize = prefSize

        fun addY(y: Int): Boolean {
            val available = maxSize.y - size.y

            if (y > available) {
                return true
            }
            size.y += y
            return false
        }

        for (child in children) {
            if (addY(child.margin.top)) {
                break
            }
            if (addY(child.size.y)) {
                break
            }

            val xSize = child.size.x
            if (xSize > maxSize.x) {
                break
            }
            if (xSize > size.x) {
                size.x = xSize
            }
            if (addY(child.margin.bottom)) {
                break
            }
            if (addY(spacing)) { // ToDo: Only add of not the last element
                break
            }
        }

        this.size = size
    }

    override fun onParentChange() {
        silentApply()

        for (child in children) {
            child.onParentChange()
        }
    }

    override fun tick() {
        super.tick()

        for (child in children.toSynchronizedList()) {
            child.tick()
        }
    }
}
