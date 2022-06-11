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

package de.bixilon.minosoft.gui.rendering.gui.gui

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.input.count.MouseClickCounter
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList

open class GUIMeshElement<T : Element>(
    val element: T,
) : HUDElement, Drawable {
    override val guiRenderer: GUIRenderer = element.guiRenderer
    override val renderWindow: RenderWindow = guiRenderer.renderWindow
    private val clickCounter = MouseClickCounter()
    var mesh: GUIMesh = GUIMesh(renderWindow, guiRenderer.matrix, DirectArrayFloatList(1000))
    override val skipDraw: Boolean
        get() = if (element is Drawable) element.skipDraw else false
    protected var lastRevision = 0L
    protected var lastPosition: Vec2i? = null
    protected var lastDragPosition: Vec2i? = null
    protected var dragged = false
    override var enabled = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (!value) {
                element.onClose()
            }
        }
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
        val mesh = this.mesh
        if (mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
        this.mesh = GUIMesh(renderWindow, guiRenderer.matrix, mesh.data)
    }

    fun prepare() = Unit

    fun prepareAsync(offset: Vec2i) {
        element.render(offset, mesh, null)
    }

    open fun postPrepare() {
        val revision = element.cache.revision
        if (revision != lastRevision) {
            createNewMesh()
            this.mesh.load()
            this.lastRevision = revision
        }
    }

    open fun prepareAsync() {
        prepareAsync(Vec2i.EMPTY)
    }

    override fun draw() {
        if (element is Drawable) {
            element.draw()
        }
    }

    fun initMesh() {
        mesh.load()
    }

    override fun onCharPress(char: Int): Boolean {
        return element.onCharPress(char)
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        lastPosition = Vec2i(position)
        return element.onMouseMove(position, position)
    }

    override fun onKey(type: KeyChangeTypes, key: KeyCodes): Boolean {
        val mouseButton = MouseButtons[key] ?: return element.onKey(key, type)
        val position = Vec2i(lastPosition ?: return false)

        val mouseAction = MouseActions[type] ?: return false

        return element.onMouseAction(position, mouseButton, mouseAction, clickCounter.getClicks(mouseButton, mouseAction, position, TimeUtil.millis))
    }

    override fun onScroll(scrollOffset: Vec2d): Boolean {
        val position = Vec2i(lastPosition ?: return false)
        return element.onScroll(position, scrollOffset)
    }

    override fun onDragMove(position: Vec2i, dragged: Dragged): Element? {
        lastDragPosition = Vec2i(position)
        if (!this.dragged) {
            this.dragged = true
            return element.onDragEnter(position, position, dragged)
        }
        return element.onDragMove(position, position, dragged)
    }

    override fun onDragKey(type: KeyChangeTypes, key: KeyCodes, dragged: Dragged): Element? {
        val mouseButton = MouseButtons[key] ?: return element.onDragKey(key, type, dragged)
        val position = Vec2i(lastDragPosition ?: return null)

        val mouseAction = MouseActions[type] ?: return null

        return element.onDragMouseAction(position, mouseButton, mouseAction, clickCounter.getClicks(mouseButton, mouseAction, position, TimeUtil.millis), dragged)
    }

    override fun onDragScroll(scrollOffset: Vec2d, dragged: Dragged): Element? {
        return element.onDragScroll(Vec2i(lastDragPosition ?: return null), scrollOffset, dragged)
    }

    override fun onDragChar(char: Int, dragged: Dragged): Element? {
        return element.onDragChar(char.toChar(), dragged)
    }


    override fun onClose() {
        element.onClose()
        element.onMouseLeave()
        lastPosition = null
    }

    override fun onOpen() {
        element.onOpen()
        onMouseMove(guiRenderer.currentMousePosition)
    }

    override fun onHide() {
        element.onHide()
        element.onMouseLeave()
    }
}
