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

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.items.block.BlockItem
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
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

class ItemElement(
    guiRenderer: GUIRenderer,
    size: Vec2i,
    item: ItemStack?,
) : Element(guiRenderer), Pollable {
    private var count = -1
    private val countText = TextElement(guiRenderer, "", background = false, noBorder = true)

    var item: ItemStack? = item
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
            cacheUpToDate = false
        }

    init {
        _size = size
        forceApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val item = item ?: return
        val size = size
        val textureSize = size - 1

        val model = item.item.model
        if (model == null) {
            var element: Element? = null

            var color = ChatColors.WHITE
            if (item.item is BlockItem) {
                val defaultState = item.item.block.defaultState
                defaultState.material.color?.let { color = it }
                defaultState.blockModel?.getParticleTexture(KUtil.RANDOM, Vec3i.EMPTY)?.let {
                    element = ImageElement(guiRenderer, it, size = textureSize)
                }
            }

            (element ?: ColorElement(guiRenderer, textureSize, color)).render(offset, consumer, options)
        } else {
            model.render2d(offset, consumer, options, textureSize, item)
        }

        val countSize = countText.size
        countText.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, countSize.x), VerticalAlignments.BOTTOM.getOffset(size.y, countSize.y)), consumer, options)
    }

    override fun poll(): Boolean {
        val item = item ?: return false
        val count = item.count
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

    override fun toString(): String {
        return item.toString()
    }

    private companion object {
        private val NEGATIVE_INFINITE_TEXT = TextComponent("-∞").color(ChatColors.RED)
        private val INFINITE_TEXT = TextComponent("∞").color(ChatColors.RED)
        private val ZERO_TEXT = TextComponent("0").color(ChatColors.YELLOW)
    }
}
