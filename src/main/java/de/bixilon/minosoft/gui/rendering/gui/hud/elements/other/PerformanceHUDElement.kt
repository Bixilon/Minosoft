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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.other

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.GuiDelegate
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.SingleChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.ScreenPositionedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.debug.DebugHUDElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class PerformanceHUDElement(guiRenderer: GUIRenderer) : Element(guiRenderer), ScreenPositionedElement, ChildedElement {
    override val children = SingleChildrenManager()
    private val text = TextElement(guiRenderer, "", parent = this)
    override val screenOffset: Vec2 = Vec2(2, 2)
    private var fps by GuiDelegate(0.0)
    private var hide by GuiDelegate(false)


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (hide) {
            return
        }
        text.render(offset, consumer, options)
    }

    override fun tick() {
        val debugHUDElement = guiRenderer.hud[DebugHUDElement]
        val hide = debugHUDElement?.enabled == true
        val fps = guiRenderer.context.renderStats.smoothAvgFPS.rounded10
        this.hide = hide
        this.fps = fps
    }

    override fun update() {
        text.chatComponent = if (hide) ChatComponent.EMPTY else ChatComponent.of("§aFPS $fps")
    }

    companion object : HUDBuilder<LayoutedGUIElement<PerformanceHUDElement>> {
        override val identifier: ResourceLocation = "minosoft:performance".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<PerformanceHUDElement> {
            return LayoutedGUIElement(PerformanceHUDElement(guiRenderer))
        }
    }
}
