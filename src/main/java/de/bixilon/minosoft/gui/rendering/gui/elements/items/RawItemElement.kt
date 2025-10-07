/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.font.renderer.element.CharSpacing
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.item.ItemRenderUtil.getModel

class RawItemElement(
    guiRenderer: GUIRenderer,
    size: Vec2f = DEFAULT_SIZE,
    stack: ItemStack?,
    parent: Element?,
) : Element(guiRenderer) {
    private val countText = TextElement(guiRenderer, "", background = null, properties = TextRenderProperties(charSpacing = CharSpacing.VERTICAL))

    var _stack: ItemStack? = null
        set(value) {
            if (field === value) {
                return
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

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val stack = stack ?: return
        if (!stack.valid) return
        val size = size
        val textureSize = size - 1

        val item = stack.item
        val model = item.getModel(guiRenderer.session)
        if (model != null) {
            val tints = context.tints.getItemTint(stack)
            model.render(guiRenderer, offset, consumer, options, textureSize, stack, tints)
        } else {
            ColorElement(guiRenderer, textureSize, ChatColors.WHITE).render(offset, consumer, options)
        }

        val countSize = countText.size
        countText.render(offset + Vec2f(HorizontalAlignments.RIGHT.getOffset(size.x, countSize.x), VerticalAlignments.BOTTOM.getOffset(size.y, countSize.y)), consumer, options)
    }

    override fun forceSilentApply() {
        val item = _stack?.item
        val count = _stack?.count
        countText.text = when {
            count == null || count == 1 -> ChatComponent.EMPTY
            count > 99 -> INFINITE_TEXT
            count > if (item is StackableItem) item.maxStackSize else 1 -> TextComponent(count, color = ChatColors.RED)
            else -> TextComponent(count)
        }

        cacheUpToDate = false
    }

    override fun toString(): String {
        return stack.toString()
    }

    companion object {
        private val INFINITE_TEXT = TextComponent("∞").color(ChatColors.RED)

        val DEFAULT_SIZE = Vec2f(17, 17) // 16x16 for the item and 1px for the count offset
    }
}
