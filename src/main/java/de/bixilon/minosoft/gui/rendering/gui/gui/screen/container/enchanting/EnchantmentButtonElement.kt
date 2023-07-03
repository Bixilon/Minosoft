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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.enchanting

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.collection.SetChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonStyle
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.properties.ButtonProperties
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class EnchantmentButtonElement(
    guiRenderer: GUIRenderer,
    val container: EnchantingContainerScreen,
    val levelAtlas: AtlasElement?,
    val disabledLevelAtlas: AtlasElement?,
    val index: Int,
) : Element(guiRenderer), AbstractLayout<Element> {
    override val children = SetChildrenManager(this)
    private val button = ButtonElement(guiRenderer, EmptyComponent, properties = ButtonProperties(false), style = ButtonStyle(
        guiRenderer.atlasManager["enchanting_table_card_disabled"],
        guiRenderer.atlasManager["enchanting_table_card_normal"],
        guiRenderer.atlasManager["enchanting_table_card_hovered"],
    )) { container.container.selectEnchantment(index) }
    private val level = TextElement(guiRenderer, ChatComponent.EMPTY, background = null)

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        button.forceRender(offset, consumer, options)
        val properties = button::properties.rendering()

        val level = AtlasImageElement(guiRenderer, if (properties.disabled) disabledLevelAtlas else levelAtlas)
        val size = size
        level.render(offset + Vec2i(5, VerticalAlignments.CENTER.getOffset(size.y, level.size.y)), consumer, options)

        if (!properties.disabled) {
            this.level.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, this.level.size.x) - 3, VerticalAlignments.BOTTOM.getOffset(size.y, this.level.size.y) - 2), consumer, options)
        }
    }

    init {
        button.parent = this
        parent = container
        _size = button.style.normal?.size?.let { Vec2(it) } ?: Vec2(108, 19)
    }

    fun update(disabled: Boolean, cost: Int, enchantment: Enchantment?, level: Int) {
        button.properties = button.properties.copy(disabled = disabled || cost <= 0)
        this.level.text = TextComponent(cost).color(RenderConstants.EXPERIENCE_BAR_LEVEL_COLOR)
        button.text = if (enchantment == null) ChatComponent.EMPTY else TextComponent(enchantment.identifier.toMinifiedString() + " $level").color(ChatColors.BLUE)
        invalidate()
    }

    override var activeElement: Element? = null
    override var activeDragElement: Element? = null
    override fun getAt(position: Vec2) = Pair(button, position)
}
