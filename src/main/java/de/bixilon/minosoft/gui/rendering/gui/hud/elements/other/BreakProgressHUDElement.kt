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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.other

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class BreakProgressHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<TextElement>(hudRenderer), Drawable {
    override val layout: TextElement = TextElement(hudRenderer, "")
    private val breakInteractionHandler = hudRenderer.renderWindow.inputHandler.interactionManager.`break`

    override val layoutOffset: Vec2i
        get() = Vec2i((guiRenderer.scaledSize.x / 2) + CrosshairHUDElement.CROSSHAIR_SIZE / 2 + 5, (guiRenderer.scaledSize.y - layout.size.y) / 2)

    private var percent = -1

    override fun draw() {
        val breakProgress = breakInteractionHandler.breakProgress
        if (breakProgress <= 0 || breakProgress >= 1.0) {
            layout.text = ""
            this.percent = -1
            return
        }
        val percent = (breakInteractionHandler.breakProgress * 100).toInt()
        if (percent == this.percent) {
            return
        }
        layout.text = TextComponent("$percent%").apply {
            color = when {
                percent <= 30 -> ChatColors.RED
                percent <= 70 -> ChatColors.YELLOW
                else -> ChatColors.GREEN
            }
        }
        this.percent = percent
    }

    companion object : HUDBuilder<BreakProgressHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:progress_indicator".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): BreakProgressHUDElement {
            return BreakProgressHUDElement(hudRenderer)
        }
    }
}
