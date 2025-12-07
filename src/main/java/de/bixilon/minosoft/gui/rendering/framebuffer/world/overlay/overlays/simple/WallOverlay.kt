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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import java.util.*

class WallOverlay(context: RenderContext) : SimpleOverlay(context) {
    private val player = context.session.player
    override var texture: Texture = unsafeNull()
    private var blockState: BlockState? = null
    private var position: BlockPosition = BlockPosition.EMPTY
    override val render: Boolean
        get() {
            if (player.gamemode == Gamemodes.SPECTATOR) {
                return false
            }
            val blockState = blockState ?: return false
            if (blockState.block is FluidBlock || blockState.block !is CollidableBlock) {
                return false
            }
            val camera = context.session.camera.entity
            val context = EntityCollisionContext.of(camera) ?: return false
            val shape = blockState.block.getCollisionShape(this.context.session, context, position, blockState) ?: return false // TODO: block entity
            if (!shape.intersects(player.physics.aabb ?: return false)) {
                return false
            }
            return true
        }
    override var uvEnd: Vec2f
        get() = Vec2f(0.3f, context.window.size.x.toFloat() / context.window.size.y.toFloat() / 3.0f) // To make pixels squares and make it look more like minecraft
        set(value) {}
    private val random = Random()

    override fun update() {
        position = player.renderInfo.eyePosition.blockPosition
        blockState = context.session.world[position]
    }


    override fun draw() {
        random.setSeed(position.hash)
        texture = blockState?.model?.getParticleTexture(random, position) ?: return

        color = RGBAColor(0.1f, 0.1f, 0.1f, 1.0f)

        super.draw()
    }


    companion object : OverlayFactory<WallOverlay> {

        override fun build(context: RenderContext): WallOverlay {
            return WallOverlay(context)
        }
    }
}
