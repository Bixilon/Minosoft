/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class PumpkinOverlay(context: RenderContext) : FirstPersonOverlay(context) {
    private val config = context.session.profiles.rendering.overlay
    override val texture: Texture = context.textures.static.create(OVERLAY_TEXTURE)
    override val render: Boolean
        get() {
            if (!config.pumpkin) {
                return false
            }
            if (!super.render) {
                return false
            }
            val head = player.equipment[EquipmentSlots.HEAD] ?: return false
            if (head.item.identifier != MinecraftBlocks.CARVED_PUMPKIN) {
                return false
            }
            return true
        }


    companion object : OverlayFactory<PumpkinOverlay> {
        private val OVERLAY_TEXTURE = "misc/pumpkinblur".toResourceLocation().texture()

        override fun build(context: RenderContext): PumpkinOverlay {
            return PumpkinOverlay(context)
        }
    }
}
