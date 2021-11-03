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
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.bottom
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.horizontal
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.left
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.offset
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.spaceSize
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.top
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.vertical
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import glm_.vec2.Vec2i
import java.lang.Integer.max

/**
 * A layout, that works from top to bottom, containing other elements, that get wrapped below each other
 */
open class RowLayout(
    hudRenderer: HUDRenderer,
    override var childAlignment: HorizontalAlignments = HorizontalAlignments.LEFT,
    spacing: Int = 0,
) : Element(hudRenderer), ChildAlignable {
    private val children: MutableList<Element> = synchronizedListOf()

    override var cacheEnabled: Boolean = false // ToDo: Cache

    override var prefSize: Vec2i
        get() = _prefSize
        set(value) = Unit

    var spacing: Int = spacing
        set(value) {
            field = value
            if (children.size <= 1) {
                return
            }
            forceApply()
        }

    fun clear() {
        if (children.size == 0) {
            return
        }
        children.clear()
        forceApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        var childYOffset = 0
        var maxZ = 0

        fun exceedsY(y: Int): Boolean {
            return childYOffset + y > size.y
        }

        fun addY(y: Int): Boolean {
            if (exceedsY(y)) {
                return true
            }
            childYOffset += y
            return false
        }

        if (addY(margin.top)) {
            return maxZ
        }

        for (child in children.toSynchronizedList()) {
            val childSize = child.size
            if (exceedsY(childSize.y)) {
                break
            }
            val childZ = child.render(offset + Vec2i(margin.left + childAlignment.getOffset(size.x - margin.horizontal, childSize.x), childYOffset), z, consumer, options)
            if (maxZ < childZ) {
                maxZ = childZ
            }
            childYOffset += childSize.y

            if (addY(child.margin.vertical + spacing)) {
                break
            }
        }

        return maxZ
    }

    operator fun plusAssign(element: Element) = add(element)

    fun add(element: Element) {
        element.parent = this
        children += element

        forceApply() // ToDo: Optimize: Keep current layout, just add the element without redoing stuff
    }

    operator fun minusAssign(element: Element) = remove(element)

    fun remove(element: Element) {
        val index = children.indexOf(element)
        if (index < 0) {
            return
        }
        element.parent = null
        children.removeAt(index)

        forceApply() // ToDo: Optimize: Keep current layout, just add the element without redoing stuff
    }

    @Synchronized
    override fun forceSilentApply() {
        for (child in children) {
            child.silentApply()
        }

        val maxSize = maxSize
        val size = margin.offset
        val prefSize = margin.spaceSize
        val xMargin = margin.horizontal

        val children = children.toSynchronizedList()
        val lastIndex = children.size - 1

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

        for ((index, child) in children.withIndex()) {
            if (addY(child.margin.top)) {
                break
            }

            val childSize = child.size
            if (addY(childSize.y)) {
                break
            }

            val xSize = childSize.x
            if (xSize > maxSize.x) {
                break
            }
            if (xSize > size.x) {
                size.x = xSize
            }
            if (addY(child.margin.bottom)) {
                break
            }
            if (index != lastIndex && addY(spacing)) {
                break
            }
        }

        _size = size
    }

    override fun onChildChange(child: Element) {
        forceApply() // ToDo (Performance): Check if the size, prefSize or whatever changed
    }

    override fun tick() {
        super.tick()

        // ToDo: Just tick visible elements?
        for (child in children.toSynchronizedList()) {
            child.tick()
        }
    }
}
