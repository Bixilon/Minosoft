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

package de.bixilon.minosoft.gui.rendering.hud.elements.other

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderBuilder
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.nodes.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.nodes.layout.AbsoluteLayout
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.LabelNode
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class BreakProgressHUDElement(
    hudRenderer: HUDRenderer,
) : HUDElement(hudRenderer) {
    override val layout = AbsoluteLayout(hudRenderer.renderWindow)

    private val text: LabelNode = LabelNode(hudRenderer.renderWindow)

    override fun init() {
        layout.addChild(Vec2i(0, 0), text)
    }

    override fun draw() {
        val currentProgress = hudRenderer.renderWindow.inputHandler.leftClickHandler.breakProgress
        if (currentProgress in 0.0..1.0) {
            val textComponent = TextComponent("${(currentProgress * 100).toInt()}%")
            textComponent.color(when {
                currentProgress < 0.3 -> ChatColors.RED
                currentProgress < 0.7 -> ChatColors.YELLOW
                else -> ChatColors.GREEN
            })
            text.text = textComponent
        } else {
            // Toggle visibility?
            text.sText = ""
        }
    }

    companion object : HUDRenderBuilder<BreakProgressHUDElement> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:break_progress")
        override val DEFAULT_PROPERTIES = HUDElementProperties(
            position = Vec2(0.08f, 0.0f),
            xBinding = HUDElementProperties.PositionBindings.CENTER,
            yBinding = HUDElementProperties.PositionBindings.CENTER,
        )

        override fun build(hudRenderer: HUDRenderer): BreakProgressHUDElement {
            return BreakProgressHUDElement(hudRenderer)
        }
    }
}
