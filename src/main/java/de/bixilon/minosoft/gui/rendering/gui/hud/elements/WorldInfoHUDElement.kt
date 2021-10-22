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
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.MMath.round10
import glm_.vec2.Vec2i

class WorldInfoHUDElement(hudRenderer: HUDRenderer) : HUDElement<TextElement>(hudRenderer), Pollable {
    override val layout: TextElement = TextElement(hudRenderer, "")

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
        val fps = hudRenderer.renderWindow.renderStats.smoothAvgFPS.round10
        if (this.fps == fps) {
            return false
        }
        this.fps = fps
        return true
    }

    override fun apply() {
        layout.text = if (hide) {
            ""
        } else {
            "FPS $fps"
        }
    }

    override fun draw() {
        val debugHUDElement: DebugHUDElement? = hudRenderer[DebugHUDElement]

        val debugEnabled = debugHUDElement?.enabled == true
        if (this.hide != debugEnabled) {
            this.hide = debugEnabled
            poll()
            apply()
        }
    }

    companion object : HUDBuilder<WorldInfoHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:world_info".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): WorldInfoHUDElement {
            return WorldInfoHUDElement(hudRenderer)
        }
    }
}
