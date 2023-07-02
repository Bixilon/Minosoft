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

package de.bixilon.minosoft.gui.rendering.gui.elements.items

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.actions.types.*
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.elements.item.FloatingItem
import de.bixilon.minosoft.gui.rendering.gui.gui.popper.item.ItemInfoPopper
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions.Companion.copy
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

class ItemElement(
    guiRenderer: GUIRenderer,
    size: Vec2 = RawItemElement.DEFAULT_SIZE,
    item: ItemStack?,
    val slotId: Int = 0,
    val itemsElement: ContainerItemsElement,
) : Element(guiRenderer) {
    private val raw = RawItemElement(guiRenderer, size, item, this)
    private var popper: ItemInfoPopper? = null
    private var hovered = false

    var _stack: ItemStack? by raw::_stack
    var stack: ItemStack? by raw::stack
    // override var cacheUpToDate: Boolean by raw::cacheUpToDate

    init {
        this._parent = itemsElement
        _size = size
        forceApply()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (raw.stack == null) {
            if (hovered) {
                ImageElement(guiRenderer, context.textures.whiteTexture.texture, size = this.size, tint = HOVERED_COLOR).forceRender(offset, consumer, options)
            }
        }
        var options = options
        if (hovered) {
            options = options.copy(alpha = 0.7f)
        }
        raw.render(offset, consumer, options)
    }

    override fun forceSilentApply() {
        raw.silentApply()
    }

    override fun onMouseEnter(position: Vec2, absolute: Vec2): Boolean {
        context.window.cursorShape = CursorShapes.HAND
        val stack = this.stack
        if (stack != null) {
            popper = ItemInfoPopper(guiRenderer, absolute, stack).apply { show() }
        }
        hovered = true
        cache.invalidate()
        return true
    }

    override fun onMouseLeave(): Boolean {
        context.window.resetCursor()
        popper?.hide()
        popper = null
        hovered = false
        cache.invalidate()
        return true
    }

    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (action != MouseActions.PRESS) {
            return true
        }
        if (button == MouseButtons.LEFT && count == 2 && itemsElement.container[slotId] == null) {
            itemsElement.container.actions.invoke(PickAllContainerAction(slotId))
            return true
        }

        val shiftDown = guiRenderer.isKeyDown(ModifierKeys.SHIFT)
        if (button == MouseButtons.MIDDLE) {
            if (guiRenderer.connection.player.gamemode != Gamemodes.CREATIVE) {
                return true
            }
            itemsElement.container.actions.invoke(CloneContainerAction(slotId))
            return true
        }
        if (button == MouseButtons.LEFT || button == MouseButtons.RIGHT) {
            itemsElement.container.actions.invoke(if (shiftDown) {
                FastMoveContainerAction(slotId)
            } else {
                SimpleContainerAction(slotId, if (button == MouseButtons.LEFT) SimpleContainerAction.ContainerCounts.ALL else SimpleContainerAction.ContainerCounts.PART)
            })
            return true
        }
        return true
    }

    override fun onDragMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int, draggable: Dragged): Element {
        if (action != MouseActions.PRESS) {
            return this
        }
        if (button == MouseButtons.LEFT && count == 2) {
            itemsElement.container.actions.invoke(PickAllContainerAction(slotId))
            return this
        }
        if (draggable !is FloatingItem) {
            return this
        }
        val shiftDown = guiRenderer.isKeyDown(ModifierKeys.SHIFT)
        if (shiftDown && button == MouseButtons.LEFT) {
            itemsElement.container.actions.invoke(FastMoveContainerAction(slotId))
        } else if (button == MouseButtons.LEFT || button == MouseButtons.RIGHT) {
            itemsElement.container.actions.invoke(SimpleContainerAction(slotId, if (button == MouseButtons.LEFT) SimpleContainerAction.ContainerCounts.ALL else SimpleContainerAction.ContainerCounts.PART))
            return this
        }
        return this
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (type != KeyChangeTypes.PRESS) {
            return true
        }
        val container = itemsElement.container
        when (key) {
            // ToDo: Make this configurable
            KeyCodes.KEY_Q -> container.actions.invoke(DropContainerAction(slotId, guiRenderer.isKeyDown(ModifierKeys.CONTROL)))

            KeyCodes.KEY_1 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_1))
            KeyCodes.KEY_2 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_2))
            KeyCodes.KEY_3 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_3))
            KeyCodes.KEY_4 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_4))
            KeyCodes.KEY_5 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_5))
            KeyCodes.KEY_6 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_6))
            KeyCodes.KEY_7 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_7))
            KeyCodes.KEY_8 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_8))
            KeyCodes.KEY_9 -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.HOTBAR_9))
            KeyCodes.KEY_F -> container.actions.invoke(SlotSwapContainerAction(slotId, SlotSwapContainerAction.SwapTargets.OFFHAND))
            else -> Unit
        }

        return true
    }

    override fun onDragEnter(position: Vec2, absolute: Vec2, draggable: Dragged): Element {
        if (draggable !is FloatingItem) {
            return this
        }
        hovered = true
        cache.invalidate()

        return this
    }

    override fun onDragLeave(draggable: Dragged): Element {
        if (draggable !is FloatingItem) {
            return this
        }
        hovered = false
        cache.invalidate()

        return this
    }

    override fun toString(): String {
        return stack.toString()
    }

    companion object {
        private val HOVERED_COLOR = RGBColor(0xFF_FF_FF_80.toInt())
    }
}
