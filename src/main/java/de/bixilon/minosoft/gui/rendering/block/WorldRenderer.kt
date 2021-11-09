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
import de.bixilon.minosoft.gui.rendering.block.preparer.AbstractSectionPreparer
import de.bixilon.minosoft.gui.rendering.block.preparer.CullSectionPreparer
import de.bixilon.minosoft.gui.rendering.block.preparer.GenericSectionPreparer
import de.bixilon.minosoft.gui.rendering.block.preparer.GreedySectionPreparer
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
import kotlin.random.Random

class WorldRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val world: World = connection.world
    private val sectionPreparer: AbstractSectionPreparer = GenericSectionPreparer(renderWindow)
    private val lightMap = LightMap(connection)
    private lateinit var mesh: ChunkSectionMesh

    private val culledPreparer = GenericSectionPreparer(renderWindow, CullSectionPreparer(renderWindow))
    private val greedyPreparer = GenericSectionPreparer(renderWindow, GreedySectionPreparer(renderWindow))


    override fun init() {
        val asset = Resources.getAssetVersionByVersion(connection.version)
        val zip = ZipInputStream(GZIPInputStream(FileInputStream(AssetsUtil.getAssetDiskPath(asset.clientJarHash!!, true))))
        val modelLoader = ModelLoader(zip, renderWindow)
        modelLoader.load()
    }

    override fun postInit() {
        lightMap.init()

        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)
        renderWindow.textureManager.staticTextures.animator.use(shader)
        lightMap.use(shader)


        val random = Random(0L)
        val blockState1 = connection.registries.blockRegistry["end_portal_frame"]?.defaultState
        val blockState2 = connection.registries.blockRegistry["carved_pumpkin"]?.defaultState
        val section = ChunkSection(Array(4096) {
            when (random.nextInt(3)) {
                1 -> blockState2
                2 -> blockState2
                else -> blockState2
            }
        })
        //val section = ChunkSection(Array(4096) { if (it < 1) blockState else null })

        mesh = sectionPreparer.prepare(section)
        /*
        for (i in 0 until 1000)
            mesh = sectionPreparer.prepare(section)

        Log.log(LogMessageType.OTHER, LogLevels.WARN){"Culling now..."}

        val culledMesh = culledPreparer.prepare(section)
        for (i in 0 until 1000){
            culledPreparer.prepare(section)
        }
        val greedyMesh = greedyPreparer.prepare(section)

        Log.log(LogMessageType.OTHER,LogLevels.INFO){"Culling has ${culledMesh.data.size / ChunkSectionMesh.SectionArrayMeshStruct.FLOATS_PER_VERTEX}, greedy meshed has  ${greedyMesh.data.size / ChunkSectionMesh.SectionArrayMeshStruct.FLOATS_PER_VERTEX}."}

         */
        mesh.load()
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
