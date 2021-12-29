/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OtherDrawable
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3i

class BlockOutlineRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OtherDrawable {
    private val profile = connection.profiles.block.outline
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private var currentOutlinePosition: Vec3i? = null
    private var currentOutlineBlockState: BlockState? = null

    private var currentMesh: LineMesh? = null
    override val skipOther: Boolean
        get() = currentMesh == null

    /**
     * Unloads the current mesh and creates a new one
     * Uses when the profile changed
     */
    private var reload = false

    override fun init() {
        val profile = connection.profiles.block
        this.profile::enabled.profileWatch(this, profile = profile) { reload = true }
        this.profile::showCollisionBoxes.profileWatch(this, profile = profile) { reload = true }
        this.profile::outlineColor.profileWatch(this, profile = profile) { reload = true }
        this.profile::collisionColor.profileWatch(this, profile = profile) { reload = true }
    }


    override fun drawOther() {
        val currentMesh = currentMesh ?: return
        currentMesh.draw()
    }

    override fun setupOther() {
        renderWindow.renderSystem.reset(faceCulling = false)
        if (profile.showThroughWalls) {
            renderWindow.renderSystem.depth = DepthFunctions.ALWAYS
        }
        renderWindow.shaderManager.genericColorShader.use()
    }

    private fun unload() {
        currentMesh ?: return
        currentMesh?.unload()
        this.currentMesh = null
        this.currentOutlinePosition = null
        this.currentOutlineBlockState = null
    }

    override fun prepareDraw() {
        val target = renderWindow.camera.targetHandler.target.nullCast<BlockTarget>()

        var currentMesh = currentMesh

        if (target == null) {
            unload()
            return
        }

        if (target.distance >= connection.player.reachDistance) {
            unload()
            return
        }

        if (connection.player.gamemode == Gamemodes.ADVENTURE || connection.player.gamemode == Gamemodes.SPECTATOR) {
            if (target.blockState.block.blockEntityType == null) {
                unload()
                return
            }
        }

        if (target.blockPosition == currentOutlinePosition && target.blockState == currentOutlineBlockState && !reload) {
            return
        }

        if (!profile.enabled) {
            return
        }

        currentMesh?.unload()
        currentMesh = LineMesh(renderWindow)

        val blockOffset = target.blockPosition.toVec3d + target.blockPosition.getWorldOffset(target.blockState.block)

        currentMesh.drawVoxelShape(target.blockState.outlineShape, blockOffset, RenderConstants.DEFAULT_LINE_WIDTH, profile.outlineColor)


        if (profile.showCollisionBoxes) {
            currentMesh.drawVoxelShape(target.blockState.collisionShape, blockOffset, RenderConstants.DEFAULT_LINE_WIDTH, profile.collisionColor, 0.005f)
        }

        currentMesh.load()


        this.currentOutlinePosition = target.blockPosition
        this.currentOutlineBlockState = target.blockState
        this.currentMesh = currentMesh
        this.reload = false
    }


    companion object : RendererBuilder<BlockOutlineRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:block_outline")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): BlockOutlineRenderer {
            return BlockOutlineRenderer(connection, renderWindow)
        }
    }
}
