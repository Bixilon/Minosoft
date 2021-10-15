/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.lerp
import glm_.vec2.Vec2i

class HotbarExperienceBarElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {

    /**
     * [experience|horse_jump][full|empty]
     */
    private val atlasElements = arrayOf(
        arrayOf(
            hudRenderer.atlasManager["minecraft:empty_experience_bar"]!!,
            hudRenderer.atlasManager["minecraft:full_experience_bar"]!!,
        ),
        arrayOf(
            hudRenderer.atlasManager["minecraft:empty_horse_jump_bar"]!!,
            hudRenderer.atlasManager["minecraft:full_horse_jump_bar"]!!,
        ),
    )

    init {
        size = SIZE
        cacheUpToDate = false
    }

    private var jumping = false
    private var barIndex = 0
    private var progress = 0.0f
    private var level = 0

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        val bars = atlasElements[barIndex]

        // background
        ImageElement(hudRenderer, bars[0]).render(offset, z, consumer)

        val progressAtlasElement = bars[1]

        // foreground
        val progress = ImageElement(hudRenderer, progressAtlasElement.texture, uvStart = progressAtlasElement.uvStart, uvEnd = lerp(progress, progressAtlasElement.uvStart, progressAtlasElement.uvEnd), size = Vec2i((progressAtlasElement.size.x * progress).toInt(), SIZE.y))

        progress.render(offset, z + 1, consumer)

        // level
        val text = TextElement(hudRenderer, TextComponent(level).apply { color = RenderConstants.EXPERIENCE_BAR_LEVEL_COLOR }, fontAlignment = HorizontalAlignments.CENTER, true)

        text.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, text.size.x), -5), z + 2, consumer)

        return 2 + TextElement.LAYERS // background + foreground + text(level)
    }

    override fun forceSilentApply() {
        silentApply()
    }

    override fun silentApply(): Boolean {
        val jumping = false // ToDo

        if (!jumping) {
            val experienceCondition = hudRenderer.connection.player.experienceCondition
            if (this.jumping != jumping || progress != experienceCondition.experienceBarProgress) {
                this.progress = experienceCondition.experienceBarProgress
                this.jumping = jumping
                this.level = experienceCondition.level
                cacheUpToDate = false
                return true
            }
        }
        return false
    }

    companion object {
        private val SIZE = Vec2i(182, 5)
    }
}
