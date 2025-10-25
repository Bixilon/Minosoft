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

package de.bixilon.minosoft.gui.rendering.chunk.outline

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.OffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.MeshSwapper
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.integrated.LineMeshBuilder
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class BlockOutlineRenderer(
    val session: PlaySession,
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer, MeshSwapper<Mesh> {
    override val layers = LayerSettings()
    private val profile = session.profiles.block.outline

    private var position: BlockPosition? = null
    private var state: BlockState? = null

    override var mesh: Mesh? = null

    /**
     * Unloads the current mesh and creates a new one
     * Uses when the profile changed
     */
    private var reload = false

    override var nextMesh: Mesh? = null
    override var unload: Boolean = false

    override fun registerLayers() {
        layers.register(BlockOutlineLayer, context.shaders.genericColorShader, this::draw) { this.mesh == null }
    }

    override fun init(latch: AbstractLatch) {
        this.profile::enabled.observe(this) { reload = true }
        this.profile::collisions.observe(this) { reload = true }
        this.profile::outlineColor.observe(this) { reload = true }
        this.profile::collisionColor.observe(this) { reload = true }

        session.events.listen<WorldUpdateEvent> {
            if (session.version.flattened) return@listen
            // neighbour blocks might change other properties
            reload = true
        }
    }

    private fun draw() {
        val mesh = mesh ?: return
        if (profile.showThroughWalls) {
            context.system.depth = DepthFunctions.ALWAYS
        }
        mesh.draw()
    }

    override fun postPrepareDraw() {
        if (unload) {
            this.position = null
            this.state = null
        }
        super<MeshSwapper>.postPrepareDraw()
    }


    override fun prepareDrawAsync() {
        val target = context.session.camera.target.target.nullCast<BlockTarget>()

        if (target == null || target.state.block !is OutlinedBlock || session.world.border.isOutside(target.blockPosition)) {
            unload = true
            return
        }

        if (target.distance >= session.player.reachDistance) {
            unload = true
            return
        }

        if (session.player.gamemode == Gamemodes.ADVENTURE || session.player.gamemode == Gamemodes.SPECTATOR) {
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

        val mesh = LineMeshBuilder(context)

        val blockOffset = MVec3d(target.blockPosition)
        if (target.state.block is OffsetBlock) {
            blockOffset += target.state.block.offsetShape(target.blockPosition)
        }


        target.state.block.getOutlineShape(session, target.blockPosition, target.state)?.let { mesh.drawVoxelShape(it, blockOffset.unsafe, RenderConstants.DEFAULT_LINE_WIDTH, profile.outlineColor.rgba()) }


        if (target.state.block is CollidableBlock && profile.collisions) { // TODO: block entity
            target.state.block.getCollisionShape(session, EntityCollisionContext(session.player), target.blockPosition, target.state, null)?.let { mesh.drawVoxelShape(it, blockOffset.unsafe, RenderConstants.DEFAULT_LINE_WIDTH, profile.collisionColor.rgba(), 0.005f) }
        }

        this.nextMesh = mesh.bake()


        this.position = offsetPosition
        this.state = target.state
        this.reload = false
    }

    private object BlockOutlineLayer : RenderLayer {
        override val settings = RenderSettings(
            polygonOffset = true,
            polygonOffsetFactor = -3.0f,
            polygonOffsetUnit = -3.0f,
        )
        override val priority get() = 1500
    }

    companion object : RendererBuilder<BlockOutlineRenderer> {

        override fun build(session: PlaySession, context: RenderContext): BlockOutlineRenderer {
            return BlockOutlineRenderer(session, context)
        }
    }
}
