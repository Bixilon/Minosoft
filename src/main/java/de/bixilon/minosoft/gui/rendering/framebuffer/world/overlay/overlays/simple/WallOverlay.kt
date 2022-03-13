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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import glm_.vec2.Vec2
import glm_.vec3.Vec3i
import java.util.*

class WallOverlay(renderWindow: RenderWindow, z: Float) : SimpleOverlay(renderWindow, z) {
    private val player = renderWindow.connection.player
    override var texture: AbstractTexture = unsafeNull()
    private var blockState: BlockState? = null
    private var position: Vec3i = Vec3i.EMPTY
    override val render: Boolean
        get() {
            if (player.gamemode == Gamemodes.SPECTATOR) {
                return false
            }
            val blockState = blockState ?: return false
            if (blockState.block is FluidBlock) {
                return false
            }
            if (!blockState.collisionShape.intersect(player.aabb)) {
                return false
            }
            return true
        }
    override var uvEnd: Vec2
        get() = Vec2(0.3f, renderWindow.window.sizef.x / renderWindow.window.sizef.y / 3.0f) // To make pixels squares and make it look more like minecraft
        set(value) {}
    private val random = Random()

    override fun update() {
        position = player.renderInfo.eyePosition.blockPosition
        blockState = renderWindow.connection.world[position]
    }

    override fun draw() {
        random.setSeed(VecUtil.generatePositionHash(position.x, position.y, position.z))
        texture = blockState?.blockModel?.getParticleTexture(random, position) ?: return

        tintColor = RGBColor(0.1f, 0.1f, 0.1f, 1.0f)

        super.draw()
    }


    companion object : OverlayFactory<WallOverlay> {

        override fun build(renderWindow: RenderWindow, z: Float): WallOverlay {
            return WallOverlay(renderWindow, z)
        }
    }
}
