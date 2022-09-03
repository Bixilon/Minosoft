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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BreakProgressHUDElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, AsyncDrawable {
    private val textElement = TextElement(guiRenderer, "").apply { parent = this@BreakProgressHUDElement }
    private val breakInteractionHandler = guiRenderer.renderWindow.inputHandler.interactionManager.`break`
    private var previousProgress = -1.0

    override val layoutOffset: Vec2i
        get() = Vec2i((guiRenderer.scaledSize.x / 2) + CrosshairHUDElement.CROSSHAIR_SIZE / 2 + 5, (guiRenderer.scaledSize.y - textElement.size.y) / 2)


    private var percent = -1

    override fun drawAsync() {
        val breakProgress = breakInteractionHandler.breakProgress
        if (this.previousProgress == breakProgress) {
            return
        }
        this.previousProgress = breakProgress
        if (breakProgress <= 0 || breakProgress >= 1.0) {
            textElement.text = ChatComponent.EMPTY
            this.percent = -1
            return
        }
        val percent = (breakInteractionHandler.breakProgress * 100).toInt()
        if (percent == this.percent) {
            return
        }
        textElement.text = TextComponent("$percent%").apply {
            color = when {
                percent <= 30 -> ChatColors.RED
                percent <= 70 -> ChatColors.YELLOW
                else -> ChatColors.GREEN
            }
        }
        this.percent = percent
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        textElement.forceRender(offset, consumer, options)
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
        super.onChildChange(this)
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }

    companion object : HUDBuilder<LayoutedGUIElement<BreakProgressHUDElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:progress_indicator".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<BreakProgressHUDElement> {
            return LayoutedGUIElement(BreakProgressHUDElement(guiRenderer))
        }
    }
}
