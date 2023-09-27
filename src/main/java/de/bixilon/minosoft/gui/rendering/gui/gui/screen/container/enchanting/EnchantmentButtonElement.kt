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
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.AbstractButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class EnchantmentButtonElement(
    guiRenderer: GUIRenderer,
    val container: EnchantingContainerScreen,
    val levelAtlas: AtlasElement?,
    val disabledLevelAtlas: AtlasElement?,
    val index: Int,
) : AbstractButtonElement(guiRenderer, "", true) {
    override val disabledAtlas = container.atlas["card_disabled"]
    override val normalAtlas = container.atlas["card_normal"]
    override val hoveredAtlas = container.atlas["card_hovered"]
    private val levelText = TextElement(guiRenderer, ChatComponent.EMPTY, background = null)

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val level = AtlasImageElement(guiRenderer, if (disabled) disabledLevelAtlas else levelAtlas)
        val size = size
        level.render(offset + Vec2i(5, VerticalAlignments.CENTER.getOffset(size.y, level.size.y)), consumer, options)

        if (!_disabled) {
            levelText.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, levelText.size.x) - 3, VerticalAlignments.BOTTOM.getOffset(size.y, levelText.size.y) - 2), consumer, options)
        }
    }

    init {
        _parent = container
        dynamicSized = false
        _size = normalAtlas?.size?.let { Vec2(it) } ?: Vec2(108, 19)
    }

    override fun submit() {
        container.container.selectEnchantment(index)
    }

    fun update(disabled: Boolean, cost: Int, enchantment: Enchantment?, level: Int) {
        _disabled = disabled || cost <= 0
        levelText.text = TextComponent(cost).color(RenderConstants.EXPERIENCE_BAR_LEVEL_COLOR)
        textElement._chatComponent = if (enchantment == null) ChatComponent.EMPTY else TextComponent(enchantment.identifier.toMinifiedString() + " $level").color(ChatColors.BLUE)
        textElement.forceSilentApply()

        forceSilentApply()
    }
}
