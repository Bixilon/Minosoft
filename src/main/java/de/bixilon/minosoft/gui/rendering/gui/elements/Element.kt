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

package de.bixilon.minosoft.gui.rendering.gui.elements

import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.max
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.min
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.spaceSize
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i

abstract class Element(val hudRenderer: HUDRenderer) {
    val renderWindow = hudRenderer.renderWindow
    open var parent: Element? = null
        set(value) {
            field = value
            onParentChange()
        }
    protected var cache = GUIMeshCache(hudRenderer.matrix, 0)
    open var cacheEnabled: Boolean = true
    open var initialCacheSize: Int = 100
    open var cacheUpToDate: Boolean = false

    /**
     * If maxSize was infinity, what size would the element have?
     */
    open var prefSize: Vec2i = Vec2i.EMPTY
        set(value) {
            field = value
            apply()
        }

    open var prefMaxSize: Vec2i = Vec2i(-1, -1)
        set(value) {
            field = value
            apply()
        }

    open val maxSize: Vec2i
        get() {
            var maxSize = Vec2i(prefMaxSize)

            if (maxSize.x < 0) {
                maxSize.x = hudRenderer.scaledSize.x
            }
            if (maxSize.y < 0) {
                maxSize.y = hudRenderer.scaledSize.y
            }


            (parent?.maxSize ?: hudRenderer.scaledSize).let {
                maxSize = maxSize.min(it)
            }

            return Vec2i.EMPTY.max(maxSize - margin.spaceSize)
        }

    open var size: Vec2i = Vec2i.EMPTY
        get() {
            return field.min(maxSize)
        }

    /**
     * Margin for the element
     *
     * The max size already includes the margin, the size not. To get the actual size of an element, add the margin to the element.
     * For rendering: Every element adds its padding itself
     */
    open var margin: Vec4i = Vec4i.EMPTY
        set(value) {
            field = value
            apply()
        }

    /**
     * Renders the element (eventually from a cache) to a vertex consumer
     *
     * @return The number of z layers used
     */
    fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        if (!cacheEnabled) {
            return forceRender(offset, z, consumer)
        }
        if (!cacheUpToDate || cache.offset != offset || hudRenderer.matrixChange || cache.matrix != hudRenderer.matrix || z != cache.z) {
            val cache = GUIMeshCache(hudRenderer.matrix)
            cache.offset = offset
            cache.z = z
            val maxZ = forceRender(offset, z, cache)
            cache.maxZ = maxZ
            cacheUpToDate = true
        }

        consumer.addCache(cache)
        return cache.maxZ
    }

    /**
     * Force renders the element to the cache/vertex consumer
     *
     * @return The number of z layers used
     */
    abstract fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int

    /**
     * Applied all changes made to any property, but does not notify the parent about the change
     */
    abstract fun silentApply()

    /**
     * Applied all changes made to any property and calls `parent?.onChildChange(this)`
     */
    open fun apply() {
        silentApply()
        parent?.onChildChange(this)
    }

    /**
     * Calls when any relevant property of the parent changes (e.g. maxSize)
     */
    open fun onParentChange() {}

    /**
     * Called by the child of an element (probably a layout), because the child changed a relevant property (probably size)
     */
    open fun onChildChange(child: Element?) {
        parent?.onChildChange(this)
    }

    /**
     * Called every tick to execute time based actions
     */
    open fun tick() {}
}
