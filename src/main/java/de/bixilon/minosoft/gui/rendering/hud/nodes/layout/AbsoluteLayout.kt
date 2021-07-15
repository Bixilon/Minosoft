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

package de.bixilon.minosoft.gui.rendering.hud.nodes.layout

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.Node
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.NodeAlignment
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.NodeSizing
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.MMath
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i

open class AbsoluteLayout(
    renderWindow: RenderWindow,
    sizing: NodeSizing = NodeSizing(),
    initialCacheSize: Int = DEFAULT_INITIAL_CACHE_SIZE,
) : Layout(renderWindow, sizing, initialCacheSize) {
    private val children: MutableMap<Node, Vec2i> = synchronizedMapOf()

    override fun clearChildren() {
        children.clear()
    }

    fun addChild(start: Vec2i, child: Node) {
        child.parent = this
        child.apply()
        if (children.isEmpty()) {
            start.x += child.sizing.margin.left + sizing.padding.left
            start.y += child.sizing.margin.top + sizing.padding.top
        }
        children[child] = start
        apply()
    }

    override fun recursiveApply() {
        for ((child, _) in children.toSynchronizedMap()) {
            if (child is AbsoluteLayout) {
                child.recursiveApply()
            } else {
                child.apply()
            }
        }
    }

    override fun checkAlignment() {
        when (sizing.forceAlign) {
            NodeAlignment.RIGHT -> {
                for ((child, start) in children.toSynchronizedMap()) {
                    start.x = sizing.currentSize.x - (child.sizing.margin.right + sizing.padding.right + child.sizing.currentSize.x)
                }
            }
            NodeAlignment.LEFT -> {
                for ((child, start) in children.toSynchronizedMap()) {
                    start.x = child.sizing.margin.left + sizing.padding.left
                }
            }
            NodeAlignment.CENTER -> {
                for ((child, start) in children.toSynchronizedMap()) {
                    start.x = (sizing.currentSize.x - child.sizing.currentSize.x) / 2
                }
            }
        }
    }

    override fun apply() {
        sizing.validate()
        clearCache()
        recalculateSize()
        checkAlignment()
        parent?.apply()
    }

    override fun recalculateSize() {
        sizing.currentSize = Vec2i(sizing.minSize)
        for ((childNode, start) in children.toSynchronizedMap()) {
            checkSize(childNode, start)
        }
    }

    override fun clearCache() {
        cache.clear()
        parent?.clearCache()
    }

    private fun checkSize(child: Node, start: Vec2i) {
        var changed = false
        val end = start + child.sizing.currentSize
        if (end.x > sizing.currentSize.x) {
            sizing.currentSize.x = MMath.clamp(end.x, sizing.minSize.x, sizing.maxSize.x)
            changed = true
        }
        if (end.y > sizing.currentSize.y) {
            sizing.currentSize.y = MMath.clamp(end.y, sizing.minSize.y, sizing.maxSize.y)
            changed = true
        }
        if (changed) {
            clearCache()
        }
    }

    private fun addToStart(start: Vec2i, elementPosition: Vec2i): Vec2i {
        return Vec2i(start.x + elementPosition.x, start.y - elementPosition.y)
    }

    override fun prepareCache(start: Vec2i, scaleFactor: Float, matrix: Mat4, z: Int) {
        for ((child, childStart) in children.toSynchronizedMap()) {
            child.checkCache(addToStart(start, childStart * scaleFactor), scaleFactor, matrix, z)
            cache.addCache(child.cache)
        }
    }

    override fun clearChildrenCache() {
        for ((child, _) in children.toSynchronizedMap()) {
            if (child is AbsoluteLayout) {
                child.clearChildrenCache()
            } else {
                child.clearCache()
            }
        }
        clearCache()
    }

    override fun removeChild(node: Node) {
        children.remove(node)
    }
}
