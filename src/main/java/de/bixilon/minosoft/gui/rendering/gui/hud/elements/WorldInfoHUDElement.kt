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
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.MMath.round10
import glm_.vec2.Vec2i

class WorldInfoHUDElement(hudRenderer: HUDRenderer) : HUDElement<TextElement>(hudRenderer) {
    override val layout: TextElement = TextElement(hudRenderer, "")

    override val layoutOffset: Vec2i = Vec2i(2, 2)

    private var fps = -1.0

    override fun apply() {
        val fps = hudRenderer.renderWindow.renderStats.smoothAvgFPS.round10
        if (this.fps == fps) {
            return
        }
        layout.text = "FPS $fps"
        this.fps = fps
    }

    companion object : HUDBuilder<WorldInfoHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:world_info".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): WorldInfoHUDElement {
            return WorldInfoHUDElement(hudRenderer)
        }
    }
}
