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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.simple

import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldBorderOverlay(renderWindow: RenderWindow) : SimpleOverlay(renderWindow) {
    private val config = renderWindow.connection.profiles.rendering.overlay
    override val texture: AbstractTexture = renderWindow.textureManager.staticTextures.createTexture(OVERLAY_TEXTURE)
    override val render: Boolean
        get() = config.worldBorder && renderWindow.connection.world.border.isOutside(renderWindow.connection.player.position)

    override fun update() {
        tintColor = RGBColor(1.0f, 0.0f, 0.0f, 0.5f) // ToDo: Correct
    }


    companion object : OverlayFactory<WorldBorderOverlay> {
        private val OVERLAY_TEXTURE = "misc/vignette".toResourceLocation().texture()

        override fun build(renderWindow: RenderWindow): WorldBorderOverlay {
            return WorldBorderOverlay(renderWindow)
        }
    }
}
