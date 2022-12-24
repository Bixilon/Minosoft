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

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.fluid.fluids.flowable.water.WaterFluid
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WaterOverlay(context: RenderContext) : SimpleOverlay(context) {
    private val player = context.connection.player
    override val texture: AbstractTexture = context.textureManager.staticTextures.createTexture("minecraft:misc/underwater".toResourceLocation().texture())
    override val render: Boolean
        get() = player.gamemode != Gamemodes.SPECTATOR && player.submergedFluid is WaterFluid

    override fun draw() {
        // TODO: make brightness depend on ambient light (e.g. rain gradient, thunder gradient, time, ...)
        val brightness = context.connection.world.getBrightness(context.connection.player.positionInfo.blockPosition) * 0.5f
        tintColor = RGBColor(brightness, brightness, brightness, 0.1f)

        // ToDo: Minecraft sets the uv coordinates according to the yaw and pitch (see InGameOverlayRenderer::renderUnderwaterOverlay)

        super.draw()
    }


    companion object : OverlayFactory<WaterOverlay> {

        override fun build(context: RenderContext): WaterOverlay {
            return WaterOverlay(context)
        }
    }
}
