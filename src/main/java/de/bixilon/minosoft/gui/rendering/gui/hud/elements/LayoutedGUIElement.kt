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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isOutside
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i

class LayoutedGUIElement<T : LayoutedElement>(
    val layout: T,
) : HUDElement, Drawable {
    val elementLayout = layout.unsafeCast<Element>()
    override val guiRenderer: GUIRenderer = elementLayout.guiRenderer
    override val renderWindow: RenderWindow = guiRenderer.renderWindow
    var mesh: GUIMesh = GUIMesh(renderWindow, guiRenderer.matrix, DirectArrayFloatList(1000))
    private var lastRevision = 0L
    override val skipDraw: Boolean
        get() = if (layout is Drawable) layout.skipDraw else false
    private var lastPosition: Vec2i = Vec2i(-1, -1)
    override var enabled = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (!value) {
                elementLayout.onClose()
            }
        }
    override val activeWhenHidden: Boolean
        get() = elementLayout.activeWhenHidden

    init {
        elementLayout.cache.data = mesh.data
    }

    override fun tick() {
        elementLayout.tick()
    }

    override fun init() {
        if (layout is Initializable) {
            layout.init()
        }
    }

    override fun postInit() {
        if (layout is Initializable) {
            layout.postInit()
        }
    }

    private fun createNewMesh() {
        val mesh = this.mesh
        if (mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
        this.mesh = GUIMesh(renderWindow, guiRenderer.matrix, mesh.data)
    }

    fun prepare() {
        val layoutOffset = layout.layoutOffset
        elementLayout.render(layoutOffset, mesh, null)

        val revision = elementLayout.cache.revision
        if (revision != lastRevision) {
            createNewMesh()
            this.mesh.load()
            this.lastRevision = revision
        }
    }

    override fun draw() {
        if (layout is Drawable) {
            layout.draw()
        }
    }


    fun initMesh() {
        mesh.load()
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        val offset = layout.layoutOffset
        val size = elementLayout.size
        val lastPosition = lastPosition

        if (position.isOutside(offset, offset + size)) {
            if (lastPosition == INVALID_MOUSE_POSITION) {
                return false
            }
            // move out
            this.lastPosition = INVALID_MOUSE_POSITION
            elementLayout.onMouseLeave()
            return true
        }
        val delta = position - offset
        this.lastPosition = delta

        if (lastPosition.isOutside(offset, size)) {
            elementLayout.onMouseEnter(delta)
            return true
        }

        elementLayout.onMouseMove(delta)
        return true
    }

    override fun onCharPress(char: Int): Boolean {
        elementLayout.onCharPress(char)
        return true
    }

    override fun onKeyPress(type: KeyChangeTypes, key: KeyCodes): Boolean {
        val mouseButton = MouseButtons[key]
        if (mouseButton == null) {
            elementLayout.onKey(key, type)
            return true
        }

        val position = lastPosition
        if (position == INVALID_MOUSE_POSITION) {
            return false
        }

        val mouseAction = MouseActions[type] ?: return false
        elementLayout.onMouseAction(position, mouseButton, mouseAction)
        return true
    }

    override fun onScroll(scrollOffset: Vec2d): Boolean {
        val position = lastPosition
        if (lastPosition == INVALID_MOUSE_POSITION) {
            return false
        }
        elementLayout.onScroll(position, scrollOffset)
        return true
    }

    override fun onClose() {
        elementLayout.onClose()
    }

    override fun onOpen() {
        elementLayout.onOpen()
        onMouseMove(guiRenderer.currentCursorPosition)
    }

    override fun onHide() {
        elementLayout.onHide()
    }

    companion object {
        private val INVALID_MOUSE_POSITION = Vec2i(-1, -1)
    }
}
