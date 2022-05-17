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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.enchanting

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.AbstractButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class EnchantmentButtonElement(
    guiRenderer: GUIRenderer,
    val container: EnchantingContainerScreen,
    val levelAtlas: AtlasElement?,
    val disabledLevelAtlas: AtlasElement?,
) : AbstractButtonElement(guiRenderer, "< text >", true) {
    override val disabledAtlas = guiRenderer.atlasManager["enchanting_table_card_disabled"]
    override val normalAtlas = guiRenderer.atlasManager["enchanting_table_card_normal"]
    override val hoveredAtlas = guiRenderer.atlasManager["enchanting_table_card_hovered"]

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val level = AtlasImageElement(guiRenderer, if (disabled) disabledLevelAtlas else levelAtlas)
        val size = size
        level.render(offset + Vec2i(5, HorizontalAlignments.CENTER.getOffset(size.y, level.size.y)), consumer, options)
    }

    init {
        _parent = container
        dynamicSized = false
        _size = normalAtlas?.size ?: Vec2i(108, 19)
    }

    override fun submit() {
        TODO("Not yet implemented")
    }
}
