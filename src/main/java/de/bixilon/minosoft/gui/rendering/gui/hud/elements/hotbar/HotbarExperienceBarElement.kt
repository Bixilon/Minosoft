/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.util.ProgressElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class HotbarExperienceBarElement(guiRenderer: GUIRenderer) : Element(guiRenderer), Pollable {
    private val atlas = guiRenderer.atlas[ATLAS]

    /**
     * [experience|horse_jump][full|empty]
     */
    private val atlasElements = arrayOf(
        arrayOf(
            atlas["empty"],
            atlas["full"],
        ),
        arrayOf(
            atlas["horse_empty"],
            atlas["horse_full"],
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

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val bars = atlasElements[barIndex]

        val progress = ProgressElement(guiRenderer, bars, progress)
        progress.prefMaxSize = SIZE
        progress.render(offset, consumer, options)

        if (level > 0) {
            // level
            val text = TextElement(guiRenderer, TextComponent(level).apply { color = LEVEL_COLOR }, background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, shadow = false))

            text.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, text.size.x), -TEXT_PROPERTIES.lineHeight + 1), consumer, options)
        }
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }

    override fun poll(): Boolean {
        val jumping = false // ToDo

        if (!jumping) {
            val experienceCondition = guiRenderer.context.session.player.experienceCondition
            if (this.jumping != jumping || progress != experienceCondition.bar || this.level != experienceCondition.level) {
                this.progress = experienceCondition.bar
                this.jumping = jumping
                this.level = experienceCondition.level
                return true
            }
        }
        return false
    }

    companion object {
        val ATLAS = minecraft("hud/hotbar/experience")
        private val TEXT_PROPERTIES = TextRenderProperties(HorizontalAlignments.CENTER, shadow = false)
        private val SIZE = Vec2(182, 5)
        val LEVEL_COLOR = "#80ff20".asColor()
    }
}
