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

package de.bixilon.minosoft.gui.rendering.world.outline

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.MeshSwapper
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OtherDrawable
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class BlockOutlineRenderer(
    val connection: PlayConnection,
    override val context: RenderContext,
) : AsyncRenderer, OtherDrawable, MeshSwapper {
    private val profile = connection.profiles.block.outline
    override val renderSystem: RenderSystem = context.renderSystem

    private var position: Vec3i? = null
    private var state: BlockState? = null

    override var mesh: LineMesh? = null
    override val skipOther: Boolean
        get() = mesh == null

    /**
     * Unloads the current mesh and creates a new one
     * Uses when the profile changed
     */
    private var reload = false

    override var nextMesh: LineMesh? = null
    override var unload: Boolean = false

    override fun init(latch: CountUpAndDownLatch) {
        this.profile::enabled.observe(this) { reload = true }
        this.profile::collisions.observe(this) { reload = true }
        this.profile::outlineColor.observe(this) { reload = true }
        this.profile::collisionColor.observe(this) { reload = true }
    }


    override fun drawOther() {
        val mesh = mesh ?: return
        mesh.draw()
    }

    override fun setupOther() {
        context.renderSystem.reset(
            polygonOffset = true,
            polygonOffsetFactor = -3.0f,
            polygonOffsetUnit = -3.0f,
        )
        if (profile.showThroughWalls) {
            context.renderSystem.depth = DepthFunctions.ALWAYS
        }
        context.shaderManager.genericColorShader.use()
    }

    override fun postPrepareDraw() {
        if (unload) {
            this.position = null
            this.state = null
        }
        super<MeshSwapper>.postPrepareDraw()
    }


    override fun prepareDrawAsync() {
        val target = context.connection.camera.target.target.nullCast<BlockTarget>()

        if (target == null || target.state.block !is OutlinedBlock || connection.world.border.isOutside(target.blockPosition)) {
            unload = true
            return
        }

        if (target.distance >= connection.player.reachDistance) {
            unload = true
            return
        }

        if (connection.player.gamemode == Gamemodes.ADVENTURE || connection.player.gamemode == Gamemodes.SPECTATOR) {
            if (target.state.block !is BlockWithEntity<*>) {
                unload = true
                return
            }
        }

        val offsetPosition = (target.blockPosition - context.camera.offset.offset)

        if (offsetPosition == position && target.state == state && !reload) { // TODO: also compare shapes, some blocks dynamically change it (e.g. scaffolding)
            return
        }

        if (!profile.enabled) {
            return
        }

        val mesh = LineMesh(context)

        val blockOffset = target.blockPosition.toVec3d
        if (target.state.block is RandomOffsetBlock) {
            target.state.block.randomOffset?.let { blockOffset += target.blockPosition.getWorldOffset(it) }
        }


        target.state.block.getOutlineShape(connection, target.state)?.let { mesh.drawVoxelShape(it, blockOffset, RenderConstants.DEFAULT_LINE_WIDTH, profile.outlineColor) }


        if (target.state.block is CollidableBlock && profile.collisions) { // TODO: block entity
            target.state.block.getCollisionShape(EntityCollisionContext(connection.player), target.blockPosition, target.state, null)?.let { mesh.drawVoxelShape(it, blockOffset, RenderConstants.DEFAULT_LINE_WIDTH, profile.collisionColor, 0.005f) }
        }

        this.nextMesh = mesh


        this.position = offsetPosition
        this.state = target.state
        this.reload = false
    }


    companion object : RendererBuilder<BlockOutlineRenderer> {
        override val identifier = minosoft("block_outline")

        override fun build(connection: PlayConnection, context: RenderContext): BlockOutlineRenderer {
            return BlockOutlineRenderer(connection, context)
        }
    }
}
