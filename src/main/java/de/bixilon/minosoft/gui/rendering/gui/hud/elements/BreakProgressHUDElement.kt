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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class BreakProgressHUDElement(hudRenderer: HUDRenderer) : HUDElement<TextElement>(hudRenderer) {
    override val layout: TextElement = TextElement(hudRenderer, "")
    private val leftClickHandler = hudRenderer.renderWindow.inputHandler.leftClickHandler
    override val layoutOffset: Vec2i
        get() = Vec2i((hudRenderer.scaledSize.x / 2) + CrosshairHUDElement.CROSSHAIR_SIZE / 2 + 5, (hudRenderer.scaledSize.y - layout.size.y) / 2)

    override fun draw() {
        val breakProgress = leftClickHandler.breakProgress
        if (breakProgress <= 0 || breakProgress >= 1.0) {
            layout.text = ""
            return
        }
        val percent = (leftClickHandler.breakProgress * 100).toInt()
        val text = TextComponent("$percent%")
        text.color = when {
            percent <= 30 -> ChatColors.RED
            percent <= 70 -> ChatColors.YELLOW
            else -> ChatColors.GREEN
        }
        layout.text = text
    }

    companion object : HUDBuilder<BreakProgressHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:progress_indicator".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): BreakProgressHUDElement {
            return BreakProgressHUDElement(hudRenderer)
        }
    }
}
