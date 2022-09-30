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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.block.BlockItem
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil

class RawItemElement(
    guiRenderer: GUIRenderer,
    size: Vec2i = DEFAULT_SIZE,
    stack: ItemStack?,
    parent: Element?,
) : Element(guiRenderer) {
    private val countText = TextElement(guiRenderer, "", background = false, noBorder = true)

    var _stack: ItemStack? = null
        set(value) {
            if (field === value) {
                return
            }
            if (value != null) {
                value::revision.observe(this) { if (value === field) forceSilentApply() } // ToDo: check if watcher is still up-to-date
            }
            field = value
            forceSilentApply()
        }
    var stack: ItemStack?
        get() = _stack
        set(value) {
            if (_stack === value) {
                return
            }
            _stack = value
            parent?.onChildChange(this)
        }

    init {
        this._parent = parent
        _size = size
        this._stack = stack
        forceApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
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
            model.render2d(guiRenderer, offset, consumer, options, textureSize, stack)
        }

        val countSize = countText.size
        countText.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, countSize.x), VerticalAlignments.BOTTOM.getOffset(size.y, countSize.y)), consumer, options)
    }

    override fun forceSilentApply() {
        val count = _stack?.item?.count
        countText.text = when {
            count == null || count == 1 -> ChatComponent.EMPTY
            count < -99 -> NEGATIVE_INFINITE_TEXT
            count < 0 -> TextComponent(count, color = ChatColors.RED) // No clue why I do this...
            count == 0 -> ZERO_TEXT
            count > 99 -> INFINITE_TEXT
            count > ProtocolDefinition.ITEM_STACK_MAX_SIZE -> TextComponent(count, color = ChatColors.RED)
            else -> TextComponent(count)
        }

        cacheUpToDate = false
    }

    override fun toString(): String {
        return stack.toString()
    }

    companion object {
        private val NEGATIVE_INFINITE_TEXT = TextComponent("-∞").color(ChatColors.RED)
        private val INFINITE_TEXT = TextComponent("∞").color(ChatColors.RED)
        private val ZERO_TEXT = TextComponent("0").color(ChatColors.YELLOW)

        val DEFAULT_SIZE = Vec2i(17, 17) // 16x16 for the item and 1px for the count offset
    }
}
