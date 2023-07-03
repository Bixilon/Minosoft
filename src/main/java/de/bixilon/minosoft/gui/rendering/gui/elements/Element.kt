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
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.spaceSize
import org.jetbrains.annotations.Contract
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

abstract class Element(val guiRenderer: GUIRenderer, initialCacheSize: Int = 1000) : InputElement, DragTarget, CachedElement, ParentedElement, UpdatableElement, Tickable {
    override val context = guiRenderer.context

    open val ignoreDisplaySize get() = false
    open val activeWhenHidden get() = false
    open val canPop: Boolean get() = true

    override var update = true

    protected var _parent: Element? = null
        @Contract
        set(value) {
            if (value === this) throw IllegalArgumentException("Can not set self as parent!")
            if (value != null && value !is ChildedElement) {
                throw IllegalArgumentException("Can not set non childed element as parent!")
            }
            field = value
        }
    override var parent: Element?
        get() = _parent
        set(value) {
            if (_parent == value) return
            _parent = value
            value.unsafeCast<ChildedElement>().children += this
            invalidate()
        }

    override val cache = GUIMeshCache(this, guiRenderer.screen, context.system.primitiveMeshOrder, context, initialCacheSize)

    @Deprecated("queued update")
    private var previousMaxSize = Vec2.EMPTY

    protected open var _prefSize: Vec2 = Vec2.EMPTY

    open val canFocus: Boolean get() = false

    /**
     * If maxSize was infinity, what size would the element have? (Excluded margin!)
     */
    open var prefSize: Vec2
        get() = _prefSize
        set(value) {
            _prefSize = value
            invalidate()
        }

    protected open var _prefMaxSize: Vec2 = Vec2(-1, -1)
    open var prefMaxSize: Vec2
        get() = _prefMaxSize
        set(value) {
            _prefMaxSize = value
            invalidate()
        }

    open val maxSize: Vec2
        get() {
            var maxSize = Vec2(prefMaxSize)

            var parentMaxSize = _parent?.maxSize
            if (parentMaxSize == null && !ignoreDisplaySize) {
                parentMaxSize = guiRenderer.screen.scaled
            }

            if (maxSize.x < 0) {
                maxSize.x = parentMaxSize?.x ?: guiRenderer.screen.scaled.x
            }
            if (maxSize.y < 0) {
                maxSize.y = parentMaxSize?.y ?: guiRenderer.screen.scaled.y
            }

            parentMaxSize?.let {
                maxSize = maxSize.min(it)
            }

            return Vec2.EMPTY.max(maxSize - margin.spaceSize)
        }

    protected open var _size: Vec2 = Vec2.EMPTY
    open var size: Vec2
        get() {
            return _size.min(maxSize)
        }
        set(value) {
            _size = value
            invalidate()
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
