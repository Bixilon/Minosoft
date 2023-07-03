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
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.block.legacy.PixLyzerBlockItem
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.font.renderer.element.CharSpacing
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.SingleChildrenManager
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
import de.bixilon.minosoft.util.KUtil

class RawItemElement(
    guiRenderer: GUIRenderer,
    size: Vec2 = DEFAULT_SIZE,
    stack: ItemStack?,
    parent: Element?,
) : Element(guiRenderer), ChildedElement {
    override val children = SingleChildrenManager()
    private val countText = TextElement(guiRenderer, "", background = null, parent = this, properties = TextRenderProperties(charSpacing = CharSpacing.VERTICAL))

    var _stack: ItemStack? = null
        set(value) {
            if (field === value) {
                return
            }
            if (value != null) {
                value::revision.observe(this) { if (value === field) invalidate() } // ToDo: check if watcher is still up-to-date
            }
            field = value
            invalidate()
        }
    var stack: ItemStack?
        get() = _stack
        set(value) {
            if (_stack === value) {
                return
            }
            _stack = value
            invalidate()
        }

    init {
        this.parent = parent
        _size = size
        this._stack = stack
        update()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val stack = stack ?: return
        if (!stack._valid) return
        val size = size
        val textureSize = size - 1

        val item = stack.item.item
        val model = item.model
        if (model == null) {
            var element: Element? = null

            val color = ChatColors.WHITE
            if (item is PixLyzerBlockItem) {
                val defaultState = item.block.states.default
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

    override fun update() {
        val item = _stack?.item
        val count = item?.count
        countText.text = when {
            count == null || count == 1 -> ChatComponent.EMPTY
            count < -99 -> NEGATIVE_INFINITE_TEXT
            count < 0 -> TextComponent(count, color = ChatColors.RED) // No clue why I do this...
            count == 0 -> ZERO_TEXT
            count > 99 -> INFINITE_TEXT
            count > if (item.item is StackableItem) item.item.maxStackSize else 1 -> TextComponent(count, color = ChatColors.RED)
            else -> TextComponent(count)
        }
    }

    override fun toString(): String {
        return stack.toString()
    }

    companion object {
        private val NEGATIVE_INFINITE_TEXT = TextComponent("-∞").color(ChatColors.RED)
        private val INFINITE_TEXT = TextComponent("∞").color(ChatColors.RED)
        private val ZERO_TEXT = TextComponent("0").color(ChatColors.YELLOW)

        val DEFAULT_SIZE = Vec2(17, 17) // 16x16 for the item and 1px for the count offset
    }
}
