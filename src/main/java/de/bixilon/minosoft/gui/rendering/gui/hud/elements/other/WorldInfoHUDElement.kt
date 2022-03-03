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

import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class WorldInfoHUDElement(guiRenderer: GUIRenderer) : TextElement(guiRenderer, ""), LayoutedElement, Pollable {
    override val layoutOffset: Vec2i = Vec2i(2, 2)
    private var fps = -1.0
    private var hide: Boolean = false

    override fun tick() {
        if (hide) {
            return
        }
        if (!poll()) {
            return
        }
        apply()
    }

    override fun poll(): Boolean {
        val debugHUDElement = guiRenderer.hud[DebugHUDElement]
        val hide = debugHUDElement?.enabled == true
        val fps = guiRenderer.renderWindow.renderStats.smoothAvgFPS.rounded10
        if (this.hide == hide && this.fps == fps) {
            return false
        }
        this.hide = hide
        this.fps = fps
        return true
    }

    override fun apply() {
        text = if (hide) {
            ""
        } else {
            "§aFPS $fps"
        }
    }

    companion object : HUDBuilder<LayoutedGUIElement<WorldInfoHUDElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:world_info".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<WorldInfoHUDElement> {
            return LayoutedGUIElement(WorldInfoHUDElement(guiRenderer))
        }
    }
}
