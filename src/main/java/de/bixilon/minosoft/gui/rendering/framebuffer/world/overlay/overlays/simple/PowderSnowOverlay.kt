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

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class PowderSnowOverlay(renderWindow: RenderWindow, z: Float) : SimpleOverlay(renderWindow, z) {
    override val texture: AbstractTexture = renderWindow.textureManager.staticTextures.createTexture("misc/powder_snow_outline".toResourceLocation().texture())
    private var ticksFrozen: Int = 0
    override val render: Boolean
        get() = ticksFrozen > 0

    override fun update() {
        ticksFrozen = renderWindow.connection.player.ticksFrozen
        tintColor = RGBColor(1.0f, 1.0f, 1.0f, minOf(ticksFrozen, FREEZE_DAMAGE_TICKS) / FREEZE_DAMAGE_TICKS.toFloat()) // ToDo: Fade out
    }


    companion object : OverlayFactory<PowderSnowOverlay> {
        private const val FREEZE_DAMAGE_TICKS = 140

        override fun build(renderWindow: RenderWindow, z: Float): PowderSnowOverlay {
            return PowderSnowOverlay(renderWindow, z)
        }
    }
}
