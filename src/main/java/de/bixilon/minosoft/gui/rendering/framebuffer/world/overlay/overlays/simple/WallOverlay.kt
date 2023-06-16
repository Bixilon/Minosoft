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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.positionHash
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import java.util.*

class WallOverlay(context: RenderContext) : SimpleOverlay(context) {
    private val player = context.connection.player
    override var texture: Texture = unsafeNull()
    private var blockState: BlockState? = null
    private var position: Vec3i = Vec3i.EMPTY
    override val render: Boolean
        get() {
            if (player.gamemode == Gamemodes.SPECTATOR) {
                return false
            }
            val blockState = blockState ?: return false
            if (blockState.block is FluidBlock || blockState.block !is CollidableBlock) {
                return false
            }
            val camera = context.connection.camera.entity
            val shape = blockState.block.getCollisionShape(EntityCollisionContext(camera), position, blockState, null) ?: return false // TODO: block entity
            if (!shape.intersect(player.physics.aabb)) {
                return false
            }
            return true
        }
    override var uvEnd: Vec2
        get() = Vec2(0.3f, context.window.sizef.x / context.window.sizef.y / 3.0f) // To make pixels squares and make it look more like minecraft
        set(value) {}
    private val random = Random()

    override fun update() {
        position = player.renderInfo.eyePosition.blockPosition
        blockState = context.connection.world[position]
    }

    override fun draw() {
        random.setSeed(position.positionHash)
        texture = blockState?.blockModel?.getParticleTexture(random, position) ?: return

        tintColor = RGBColor(0.1f, 0.1f, 0.1f, 1.0f)

        super.draw()
    }


    companion object : OverlayFactory<WallOverlay> {

        override fun build(context: RenderContext): WallOverlay {
            return WallOverlay(context)
        }
    }
}
