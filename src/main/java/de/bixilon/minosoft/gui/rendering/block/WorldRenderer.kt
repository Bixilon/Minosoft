/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.block

import de.bixilon.minosoft.data.assets.AssetsUtil
import de.bixilon.minosoft.data.assets.Resources
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.direction.FakeDirection
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

class WorldRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val world: World = connection.world
    private val sectionPreparer = SectionPreparer(renderWindow)
    private lateinit var mesh: ChunkSectionMesh


    override fun init() {
        val asset = Resources.getAssetVersionByVersion(connection.version)
        val zip = ZipInputStream(GZIPInputStream(FileInputStream(AssetsUtil.getAssetDiskPath(asset.clientJarHash!!, true))))
        val modelLoader = ModelLoader(zip)
        modelLoader.load()

        val dirt = connection.registries.blockRegistry["dirt"]?.defaultState
        val chunk = ChunkSection(Array(4096) { dirt })
        mesh = sectionPreparer.prepare(chunk)
        mesh.load()
    }

    override fun postInit() {
        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)
        renderWindow.textureManager.staticTextures.animator.use(shader)
    }

    override fun setupOpaque() {
        super.setupOpaque()
        shader.use()
    }

    override fun drawOpaque() {
        mesh.draw()
    }


    companion object : RendererBuilder<WorldRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:world_renderer")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): WorldRenderer {
            return WorldRenderer(connection, renderWindow)
        }

        val Vec2i.neighbourPositions: List<Vec2i>
            get() {
                return listOf(
                    this + Directions.NORTH,
                    this + Directions.SOUTH,
                    this + Directions.WEST,
                    this + Directions.EAST,
                    this + FakeDirection.NORTH_WEST,
                    this + FakeDirection.NORTH_EAST,
                    this + FakeDirection.SOUTH_WEST,
                    this + FakeDirection.SOUTH_EAST,
                )
            }
    }
}
