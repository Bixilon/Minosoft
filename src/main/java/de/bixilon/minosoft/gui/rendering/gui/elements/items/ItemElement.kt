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

package de.bixilon.minosoft.gui.rendering.gui.elements.items

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.inventory.ContainerClickActions
import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.gui.popper.item.ItemInfoPopper
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import glm_.vec2.Vec2i

class ItemElement(
    guiRenderer: GUIRenderer,
    size: Vec2i = RawItemElement.DEFAULT_SIZE,
    item: ItemStack?,
    val slotId: Int = 0,
    val itemsElement: ContainerItemsElement,
) : Element(guiRenderer), Pollable {
    private val raw = RawItemElement(guiRenderer, size, item, this)
    private var popper: ItemInfoPopper? = null
    private var hovered = false

    var stack: ItemStack? = item
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
            cacheUpToDate = false
        }

    init {
        this._parent = itemsElement
        _size = size
        forceApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        var options = options
        if (hovered) {
            options = (options?.copy(alpha = options.alpha * 0.7f)) ?: GUIVertexOptions(null, 0.7f)
        }
        raw.render(offset, consumer, options)
    }

    override fun poll(): Boolean {
        return raw.poll()
    }

    override fun forceSilentApply() {
        raw.silentApply()
    }

    override fun onMouseEnter(position: Vec2i, absolute: Vec2i): Boolean {
        renderWindow.window.cursorShape = CursorShapes.HAND
        val stack = stack ?: return true
        popper = ItemInfoPopper(guiRenderer, absolute, stack).apply { show() }
        hovered = true
        cacheUpToDate = false
        return true
    }

    override fun onMouseMove(position: Vec2i, absolute: Vec2i): Boolean {
        popper?.position = absolute
        return true
    }

    override fun onMouseLeave(): Boolean {
        renderWindow.window.resetCursor()
        popper?.hide()
        popper = null
        hovered = false
        cacheUpToDate = false
        return true
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (type != KeyChangeTypes.PRESS) {
            return true
        }
        val container = itemsElement.container
        val containerId = renderWindow.connection.player.containers.getKey(container) ?: return false
        val controlDown = guiRenderer.isKeyDown(ModifierKeys.CONTROL)
        val shiftDown = guiRenderer.isKeyDown(ModifierKeys.SHIFT)
        // ToDo
        when (key) {
            KeyCodes.KEY_Q -> {
                val action: ContainerClickActions
                if (controlDown) {
                    stack?.item?.count = 0
                    action = ContainerClickActions.DROP_STACK
                } else {
                    stack?.item?.decreaseCount()
                    action = ContainerClickActions.DROP_ITEM
                }
                renderWindow.connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slotId, action, container.createAction(), mapOf(slotId to stack), stack))
            }
        }
        return true
    }

    override fun toString(): String {
        return stack.toString()
    }
}
