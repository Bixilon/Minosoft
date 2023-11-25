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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.simple

import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldBorderOverlay(context: RenderContext) : SimpleOverlay(context) {
    private val config = context.connection.profiles.rendering.overlay
    override val texture: Texture = context.textures.static.create(OVERLAY_TEXTURE)
    override val render: Boolean
        get() = config.worldBorder && context.connection.world.border.isOutside(context.connection.player.physics.position)

    override fun update() {
        tintColor = RGBColor(1.0f, 0.0f, 0.0f, 0.5f) // ToDo: Correct
    }


    companion object : OverlayFactory<WorldBorderOverlay> {
        private val OVERLAY_TEXTURE = "misc/vignette".toResourceLocation().texture()

        override fun build(context: RenderContext): WorldBorderOverlay {
            return WorldBorderOverlay(context)
        }
    }
}
