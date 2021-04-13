/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.primitive

import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i

open class Layout(
    start: Vec2i,
    val z: Int,
) : Element(start) {
    override var size: Vec2i
        get() = Vec2i(fakeX ?: super.size.x, fakeY ?: super.size.y)
        set(value) {
            super.size = value
        }
    private val children: MutableList<Element> = mutableListOf()
    var fakeX: Int? = null
    var fakeY: Int? = null


    fun clear() {
        synchronized(children) {
            children.clear()
        }
        clearCache()
        recalculateSize()
    }

    fun finishBatchAdd() {
        cache.clear()
        recalculateSize()
    }

    fun batchAdd(child: Element) {
        child.parent = this
        synchronized(children) {
            children.add(child)
        }
    }

    fun addChild(child: Element) {
        batchAdd(child)
        finishBatchAdd()
    }

    override fun recalculateSize() {
        if (children.isEmpty()) {
            size = Vec2i(0, 0)
        } else {
            synchronized(children) {
                for (child in children) {
                    checkSize(child.start + child.size)
                }
            }
        }
        parent?.recalculateSize()
    }

    override fun clearCache() {
        cache.clear()
        parent?.clearCache()
    }

    private fun checkSize(vec2: Vec2i) {
        var changed = false
        if (vec2.x > size.x) {
            size.x = vec2.x
            changed = true
        }
        if (vec2.y > size.y) {
            size.y = vec2.y
            changed = true
        }
        if (changed) {
            clearCache()
        }
    }

    fun pushChildrenToRight(offset: Int = 0) {
        for (child in children) {
            child.start = Vec2i((size.x - child.size.x) - offset, child.start.y)
            child.clearCache()
        }
    }

    private fun addToStart(start: Vec2i, elementPosition: Vec2i): Vec2i {
        return Vec2i(start.x + elementPosition.x, start.y - elementPosition.y)
    }

    override fun prepareCache(start: Vec2i, scaleFactor: Float, matrix: Mat4, z: Int) {
        val normalStart = addToStart(start, this.start * scaleFactor)


        synchronized(children) {
            for (child in children) {
                child.checkCache(normalStart, scaleFactor, matrix, this.z + z)
                cache.addCache(child.cache)
            }
        }
    }

    fun clearChildrenCache() {
        synchronized(children) {
            for (child in children) {
                if (child is Layout) {
                    child.clearChildrenCache()
                }
                child.clearCache()
            }
        }
        clearCache()
    }
}
