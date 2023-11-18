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

package de.bixilon.minosoft.gui.rendering.gui.gui

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.input.count.MouseClickCounter
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.gui.rendering.renderer.drawable.BaseDrawable
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.system.opengl.MemoryLeakException
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.collections.floats.FloatListUtil

open class GUIMeshElement<T : Element>(
    val element: T,
) : HUDElement, AsyncDrawable, Drawable {
    override val guiRenderer: GUIRenderer = element.guiRenderer
    override val context: RenderContext = guiRenderer.context
    private val clickCounter = MouseClickCounter()
    private var _mesh: GUIMesh? = null
    var mesh: GUIMesh = GUIMesh(context, guiRenderer.halfSize, FloatListUtil.direct(1000))
    override val skipDraw: Boolean
        get() = if (element is BaseDrawable) element.skipDraw else false
    protected var lastRevision = 0L
    protected var lastPosition: Vec2? = null
    protected var lastDragPosition: Vec2? = null
    protected var dragged = false
    override var enabled = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (!value && state != ElementStates.CLOSED) {
                onClose()
            }
        }
    var state: ElementStates = ElementStates.CLOSED
        private set

    override val activeWhenHidden: Boolean
        get() = element.activeWhenHidden
    override val canPop: Boolean
        get() = element.canPop

    init {
        element.cache.data = mesh.data
    }

    override fun tick() {
        element.tick()
    }

    override fun init() {
        if (element is Initializable) {
            element.init()
        }
    }

    override fun postInit() {
        if (element is Initializable) {
            element.postInit()
        }
    }

    protected fun createNewMesh() {
        if (this._mesh != null) throw MemoryLeakException("Mesh to unload is already set!")
        this._mesh = this.mesh
        this.mesh = GUIMesh(context, guiRenderer.halfSize, mesh.data)
        this.mesh.finish()
    }

    fun prepare() = Unit

    fun prepareAsync(offset: Vec2) {
        element.render(offset, mesh, null)
        val revision = element.cache.revision
        if (revision != lastRevision) {
            createNewMesh()
            this.lastRevision = revision
        }
    }

    open fun postPrepare() {
        val _mesh = this._mesh ?: return
        if (_mesh.state == Mesh.MeshStates.LOADED) {
            _mesh.unload()
        }
        this._mesh = null

        if (this.mesh.state == Mesh.MeshStates.FINISHED) {
            mesh.load()
        }
    }

    open fun prepareAsync() {
        prepareAsync(Vec2.EMPTY)
    }

    override fun draw() {
        if (element is Drawable) {
            element.draw()
        }
    }

    override fun drawAsync() {
        if (element is AsyncDrawable) {
            element.drawAsync()
        }
    }

    fun initMesh() {
        mesh.load()
    }

    override fun onCharPress(char: Int): Boolean {
        return element.onCharPress(char)
    }

    override fun onMouseMove(position: Vec2): Boolean {
        lastPosition = Vec2(position)
        return element.onMouseMove(position, position)
    }

    override fun onKey(code: KeyCodes, change: KeyChangeTypes): Boolean {
        val mouseButton = MouseButtons[code] ?: return element.onKey(code, change)
        val position = Vec2(lastPosition ?: return false)

        val mouseAction = MouseActions[change] ?: return false

        return element.onMouseAction(position, mouseButton, mouseAction, clickCounter.getClicks(mouseButton, mouseAction, position, millis()))
    }

    override fun onScroll(scrollOffset: Vec2): Boolean {
        val position = Vec2(lastPosition ?: return false)
        return element.onScroll(position, scrollOffset)
    }

    override fun onDragMove(position: Vec2, dragged: Dragged): Element? {
        lastDragPosition = Vec2(position)
        if (!this.dragged) {
            this.dragged = true
            return element.onDragEnter(position, position, dragged)
        }
        return element.onDragMove(position, position, dragged)
    }

    override fun onDragKey(type: KeyChangeTypes, key: KeyCodes, dragged: Dragged): Element? {
        val mouseButton = MouseButtons[key] ?: return element.onDragKey(key, type, dragged)
        val position = Vec2(lastDragPosition ?: return null)

        val mouseAction = MouseActions[type] ?: return null

        return element.onDragMouseAction(position, mouseButton, mouseAction, clickCounter.getClicks(mouseButton, mouseAction, position, millis()), dragged)
    }

    override fun onDragScroll(scrollOffset: Vec2, dragged: Dragged): Element? {
        return element.onDragScroll(Vec2(lastDragPosition ?: return null), scrollOffset, dragged)
    }

    override fun onDragChar(char: Int, dragged: Dragged): Element? {
        return element.onDragChar(char.toChar(), dragged)
    }


    override fun onClose() {
        check(state != ElementStates.CLOSED) { "Element not active!" }
        state = ElementStates.CLOSED
        element.onClose()
        element.onMouseLeave()
        lastPosition = null
    }

    override fun onOpen() {
        check(state != ElementStates.OPENED) { "Element already active!" }
        state = ElementStates.OPENED
        element.onOpen()
        onMouseMove(guiRenderer.currentMousePosition)
    }

    override fun onHide() {
        check(state == ElementStates.OPENED) { "Can not hide in $state" }
        state = ElementStates.HIDDEN
        element.onHide()
        element.onMouseLeave()
    }
}
