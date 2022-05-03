/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec4.Vec4i
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.input.DragTarget
import de.bixilon.minosoft.gui.rendering.gui.input.InputElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.max
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.min
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.spaceSize
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList

abstract class Element(val guiRenderer: GUIRenderer, var initialCacheSize: Int = 1000) : InputElement, DragTarget {
    var ignoreDisplaySize = false
    val renderWindow = guiRenderer.renderWindow
    open val activeWhenHidden = false
    open var canPop: Boolean = true

    protected open var _parent: Element? = null
    open var parent: Element?
        get() = _parent
        set(value) {
            check(value !== this) { "Can not self as parent!" }
            _parent = value
            silentApply()
        }

    open var cacheEnabled: Boolean = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            parent?.cacheEnabled = value
        }
    open var cacheUpToDate: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (!value) {
                parent?.cacheUpToDate = false
            }
        }

    @Deprecated("Warning: Should not be directly accessed!")
    val cache = GUIMeshCache(guiRenderer.matrix, renderWindow.renderSystem.primitiveMeshOrder, initialCacheSize)

    private var previousMaxSize = Vec2i.EMPTY

    protected open var _prefSize: Vec2i = Vec2i.EMPTY

    open val canFocus: Boolean get() = false

    /**
     * If maxSize was infinity, what size would the element have? (Excluded margin!)
     */
    open var prefSize: Vec2i
        get() = _prefSize
        set(value) {
            _prefSize = value
            apply()
        }

    protected open var _prefMaxSize: Vec2i = Vec2i(-1, -1)
    open var prefMaxSize: Vec2i
        get() = _prefMaxSize
        set(value) {
            _prefMaxSize = value
            apply()
        }

    open val maxSize: Vec2i
        get() {
            var maxSize = Vec2i(prefMaxSize)

            var parentMaxSize = parent?.maxSize
            if (parentMaxSize == null && !ignoreDisplaySize) {
                parentMaxSize = guiRenderer.scaledSize
            }

            if (maxSize.x < 0) {
                maxSize.x = parentMaxSize?.x ?: guiRenderer.scaledSize.x
            }
            if (maxSize.y < 0) {
                maxSize.y = parentMaxSize?.y ?: guiRenderer.scaledSize.y
            }

            parentMaxSize?.let {
                maxSize = maxSize.min(it)
            }

            return Vec2i.EMPTY.max(maxSize - margin.spaceSize)
        }

    protected open var _size: Vec2i = Vec2i.EMPTY
    open var size: Vec2i
        get() {
            return _size.min(maxSize)
        }
        set(value) {
            _size = value
            apply()
        }

    protected open var _margin: Vec4i = Vec4i.EMPTY

    /**
     * Margin for the element
     *
     * The max size already includes the margin, the size not. To get the actual size of an element, add the margin to the element.
     * For rendering: Every element adds its padding itself
     */
    open var margin: Vec4i
        get() = _margin
        set(value) {
            _margin = value
            apply()
        }

    /**
     * Renders the element (eventually from a cache) to a vertex consumer
     *
     * @return The number of z layers used
     */
    fun render(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val offset = Vec2i(offset)
        var directRendering = false
        if (consumer is GUIMesh && consumer.data == cache.data) {
            directRendering = true
        }
        if (RenderConstants.DISABLE_GUI_CACHE || !cacheEnabled) {
            if (directRendering) {
                cache.clear()
            }
            forceRender(offset, consumer, options)
            if (directRendering) {
                cache.revision++
            }
            return
        }
        if (!cacheUpToDate || cache.offset != offset || guiRenderer.matrixChange || cache.options != options || cache.matrix !== guiRenderer.matrix) {
            this.cache.clear()
            cache.matrix = guiRenderer.matrix
            cache.offset = Vec2i(offset)
            cache.options = options
            forceRender(offset, cache, options)
            if (cache.data !is DirectArrayFloatList) {
                // not raw mesh data
                cache.data.finish()
            }
            cacheUpToDate = true
        }
        if (!directRendering) {
            consumer.addCache(cache)
        }
    }

    /**
     * Force renders the element to the cache/vertex consumer
     *
     * @return The number of z layers used
     */
    abstract fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?)

    /**
     * Force applies all changes made to any property, but does not notify the parent about the change
     */
    @Deprecated("Generally a bad idea to call")
    protected fun forceSilentApply(poll: Boolean) {
        if (poll && this is Pollable) {
            poll()
        }
        forceSilentApply()
    }

    /**
     * Force applies all changes made to any property, but does not notify the parent about the change
     */
    abstract fun forceSilentApply()

    /**
     * Force applied all changes made to any property and calls `parent?.onChildChange(this)`
     */
    open fun forceApply() {
        if (this is Pollable) {
            poll()
        }
        forceSilentApply()
        parent?.onChildChange(this)
    }

    /**
     * Applied all changes made to any property and calls `parent?.onChildChange(this)`
     */
    open fun apply() {
        if (!silentApply()) {
            return
        }
        parent?.onChildChange(this)
    }

    /**
     * Calls when the client needs to check if it needs to apply (maybe the parent changed?) changes
     * Otherwise, force silent applies.
     * Can be used to improve the performance
     *
     * @return true, if force applied
     */
    @Suppress("DEPRECATION")
    open fun silentApply(): Boolean {
        val maxSize = maxSize
        if (this is Pollable && this.poll() || (previousMaxSize != maxSize && (maxSize isSmaller _size || maxSize isSmaller _prefMaxSize || (maxSize isGreater previousMaxSize && _size isSmaller _prefSize)))) {
            forceSilentApply(false)
            previousMaxSize = maxSize
            return true
        }
        previousMaxSize = maxSize
        return false
    }

    /**
     * Called by the child of an element (probably a layout), because the child changed a relevant property (probably size)
     */
    open fun onChildChange(child: Element) {
        parent?.onChildChange(this)
    }

    /**
     * Called every tick to execute time based actions
     */
    open fun tick() {
        if (this is Pollable && poll()) {
            forceSilentApply()
            parent?.onChildChange(this)
        }
    }

    open fun onOpen() = Unit
    open fun onHide() = Unit
    open fun onClose() = Unit
}
