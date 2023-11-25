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

import de.bixilon.kutil.avg.FloatAverage
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class PowderSnowOverlay(context: RenderContext) : SimpleOverlay(context) {
    private val config = context.connection.profiles.rendering.overlay
    override val texture: Texture = context.textures.static.create(OVERLAY_TEXTURE)
    private val strength = FloatAverage(1L * 1000000000L, 0.0f)
    override var render: Boolean = false
        get() = config.powderSnow && field

    override fun update() {
        val ticksFrozen = context.connection.player.ticksFrozen
        val strength = (minOf(ticksFrozen, FREEZE_DAMAGE_TICKS) / FREEZE_DAMAGE_TICKS.toFloat())
        this.strength += strength

        val avg = this.strength.avg

        if (avg > 1.0f || avg == 0.0f) {
            this.render = false
            return
        }
        this.render = true
        tintColor = RGBColor(1.0f, 1.0f, 1.0f, avg)
    }


    companion object : OverlayFactory<PowderSnowOverlay> {
        private const val FREEZE_DAMAGE_TICKS = 140
        private val OVERLAY_TEXTURE = "misc/powder_snow_outline".toResourceLocation().texture()

        override fun build(context: RenderContext): PowderSnowOverlay? {
            if (!context.connection.assetsManager.contains(OVERLAY_TEXTURE)) {
                // overlay not yet available (< 1.17)
                return null
            }
            return PowderSnowOverlay(context)
        }
    }
}
