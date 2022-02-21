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
import de.bixilon.minosoft.data.inventory.InventoryActions
import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.block.BlockItem
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.popper.item.ItemInfoPopper
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

class ItemElement(
    guiRenderer: GUIRenderer,
    size: Vec2i,
    item: ItemStack?,
    val slotId: Int = 0,
    val container: Container? = null,
    parent: Element?,
) : Element(guiRenderer), Pollable {
    private var count = -1
    private val countText = TextElement(guiRenderer, "", background = false, noBorder = true)
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
        this._parent = parent
        _size = size
        forceApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        var options = options
        if (hovered) {
            options = (options?.copy(alpha = options.alpha * 0.7f)) ?: GUIVertexOptions(null, 0.7f)
        }
        val stack = stack ?: return
        val size = size
        val textureSize = size - 1

        val item = stack.item.item
        val model = item.model
        if (model == null) {
            var element: Element? = null

            var color = ChatColors.WHITE
            if (item is BlockItem) {
                val defaultState = item.block.defaultState
                defaultState.material.color?.let { color = it }
                defaultState.blockModel?.getParticleTexture(KUtil.RANDOM, Vec3i.EMPTY)?.let {
                    element = ImageElement(guiRenderer, it, size = textureSize)
                }
            }

            (element ?: ColorElement(guiRenderer, textureSize, color)).render(offset, consumer, options)
        } else {
            model.render2d(offset, consumer, options, textureSize, stack)
        }

        val countSize = countText.size
        countText.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, countSize.x), VerticalAlignments.BOTTOM.getOffset(size.y, countSize.y)), consumer, options)
    }

    override fun poll(): Boolean {
        val stack = stack ?: return false
        val count = stack.item.count
        if (this.count != count) {
            this.count = count
            return true
        }

        return false
    }

    override fun forceSilentApply() {
        countText.text = when {
            count < -99 -> NEGATIVE_INFINITE_TEXT
            count < 0 -> TextComponent(count, color = ChatColors.RED) // No clue why I do this...
            count == 0 -> ZERO_TEXT
            count == 1 -> ChatComponent.EMPTY
            count > 99 -> INFINITE_TEXT
            count > ProtocolDefinition.ITEM_STACK_MAX_SIZE -> TextComponent(count, color = ChatColors.RED)
            else -> TextComponent(count)
        }

        cacheUpToDate = false
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
        val container = container ?: return false
        val containerId = renderWindow.connection.player.containers.getKey(container) ?: return false
        val controlDown = guiRenderer.isKeyDown(ModifierKeys.CONTROL)
        val shiftDown = guiRenderer.isKeyDown(ModifierKeys.SHIFT)
        // ToDo
        when (key) {
            KeyCodes.KEY_Q -> {
                val action: InventoryActions
                if (controlDown) {
                    stack?.item?.count = 0
                    action = InventoryActions.DROP_STACK
                } else {
                    stack?.item?.decreaseCount()
                    action = InventoryActions.DROP_ITEM
                }
                renderWindow.connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slotId, action, container.createAction(), mapOf(slotId to stack), stack))
            }
        }
        return true
    }

    override fun toString(): String {
        return stack.toString()
    }

    private companion object {
        private val NEGATIVE_INFINITE_TEXT = TextComponent("-∞").color(ChatColors.RED)
        private val INFINITE_TEXT = TextComponent("∞").color(ChatColors.RED)
        private val ZERO_TEXT = TextComponent("0").color(ChatColors.YELLOW)
    }
}
