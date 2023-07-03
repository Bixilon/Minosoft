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

package de.bixilon.minosoft.gui.rendering.gui.elements

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.GuiDelegate
import de.bixilon.minosoft.gui.rendering.gui.abstractions.CachedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ParentedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.update.UpdatableElement
import de.bixilon.minosoft.gui.rendering.gui.input.DragTarget
import de.bixilon.minosoft.gui.rendering.gui.input.InputElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.MAX
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.isSmaller
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.EMPTY
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

abstract class Element(val guiRenderer: GUIRenderer, initialCacheSize: Int = 1000) : InputElement, DragTarget, CachedElement, ParentedElement, UpdatableElement, Tickable {
    override val context = guiRenderer.context

    open val ignoreScreenDimensions get() = false
    open val activeWhenHidden get() = false
    open val canPop: Boolean get() = true

    override var update = true

    override var parent: Element? = null
        set(value) {
            if (value === field) return
            if (value === this) throw IllegalArgumentException("Can not set self as parent!")
            if (value != null && value !is ChildedElement) {
                throw IllegalArgumentException("Can not set non childed element as parent!")
            }
            field = value
            value.unsafeCast<ChildedElement>().children += this
            invalidate()
        }

    override val cache = GUIMeshCache(this, guiRenderer.screen, context.system.primitiveMeshOrder, context, initialCacheSize)

    open val canFocus: Boolean get() = false


    var preferredSize: Vec2? by GuiDelegate(null) // how large do I want it to be, element must not exceed it
    open val wishedSize get() = size // if there was not space limit, how large could the element be
    open var maxSize: Vec2 = calculateMaxSize() // how much space the element has if it wants to exceed it. Determinant by the parent, if not available by the screen size or infinity
        protected set
    open var size = Vec2.EMPTY // current rendered size of element (including padding)
        protected set

    protected fun calculateMaxSize(): Vec2 {
        val parent = this.parent
        var size = parent?.maxSize ?: if (ignoreScreenDimensions) Vec2.MAX else guiRenderer.screen.scaled

        val preferred = preferredSize ?: return size
        size = Vec2(size)
        if (preferred.x >= 0 && preferred.x < size.x) {
            size.x = preferred.x
        }
        if (preferred.y >= 0 && preferred.y < size.y) {
            size.y = preferred.y
        }

        return size
    }

    fun updateMaxSize() {
        val maxSize = calculateMaxSize()
        if (maxSize isSmaller size) { // now it affects this element and maybe children
            invalidate()
        }
        this.maxSize = maxSize
    }

    var margin by GuiDelegate(Vec4.EMPTY)
    var padding by GuiDelegate(Vec4.EMPTY)

    open fun render(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Boolean {
        val offset = Vec2(offset)
        val direct = consumer is GUIMesh && consumer.data == cache.data
        if (RenderConstants.DISABLE_GUI_CACHE || direct || !cache.enabled()) {
            forceRender(offset, consumer, options)
            return true
        }
        val screen = guiRenderer.screen
        var update = false
        if (!cache.isValid(screen, offset, options)) {
            cache.start(screen, Vec2(offset), options)
            forceRender(offset, cache, options)
            cache.end()
            update = true
        }
        consumer += cache

        return update
    }

    abstract fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?)

    override fun tick() {
        if (this is ChildedElement) {
            for (child in children) {
                child.tick()
            }
        }

        if (this is Pollable && poll()) {
            invalidate()
        }
    }

    // TODO: interface
    open fun onOpen() = Unit
    open fun onHide() = Unit
    open fun onClose() = Unit


    protected inline fun <T> KProperty0<T>.delegate(): GuiDelegate<T> = this.apply { isAccessible = true }.getDelegate().unsafeCast()
    protected inline fun <T> KProperty0<T>.acknowledge(): T = delegate().acknowledge()
    protected inline fun <T> KProperty0<T>.rendering(): T = delegate().rendering()
}
