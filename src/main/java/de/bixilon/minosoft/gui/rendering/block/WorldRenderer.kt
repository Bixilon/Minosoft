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
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshes
import de.bixilon.minosoft.gui.rendering.block.preparer.AbstractSectionPreparer
import de.bixilon.minosoft.gui.rendering.block.preparer.GenericSectionPreparer
import de.bixilon.minosoft.gui.rendering.input.camera.Frustum
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.ChunkUnloadEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.collections.SynchronizedMap
import glm_.vec2.Vec2i
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

class WorldRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable, TranslucentDrawable, TransparentDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val transparentShader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val world: World = connection.world
    private val sectionPreparer: AbstractSectionPreparer = GenericSectionPreparer(renderWindow)
    private val lightMap = LightMap(connection)
    private val meshes: SynchronizedMap<Vec2i, SynchronizedMap<Int, ChunkSectionMeshes>> = synchronizedMapOf()
    private var visibleMeshes: MutableSet<ChunkSectionMeshes> = mutableSetOf() // ToDo: Split in opaque, transparent, translucent meshes


    val visibleSize: Int
        get() = visibleMeshes.size
    val preparedSize: Int
        get() = visibleMeshes.size

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

        transparentShader.defines["TRANSPARENT"] = ""
        transparentShader.load()
        renderWindow.textureManager.staticTextures.use(transparentShader)
        renderWindow.textureManager.staticTextures.animator.use(transparentShader)
        lightMap.use(transparentShader)

        connection.registerEvent(CallbackEventInvoker.of<FrustumChangeEvent> { onFrustumChange(it.frustum) })


        connection.registerEvent(CallbackEventInvoker.of<ChunkDataChangeEvent> {
            val sections = it.chunk.sections ?: return@of
            for ((sectionHeight, section) in sections) {
                prepareSection(it.chunkPosition, sectionHeight, section)
            }
        })

        connection.registerEvent(CallbackEventInvoker.of<ChunkUnloadEvent> {
            val meshes = this.meshes.remove(it.chunkPosition)?.values ?: return@of

            renderWindow.queue += {
                for (mesh in meshes) {
                    mesh.unload()
                    this.visibleMeshes -= mesh
                }
            }
        })
    }

    @Synchronized
    private fun prepareSection(chunkPosition: Vec2i, sectionHeight: Int, section: ChunkSection? = world[chunkPosition]?.sections?.get(sectionHeight)) {
        if (section == null) {
            return
        }

        val mesh = sectionPreparer.prepare(chunkPosition, sectionHeight, section, arrayOfNulls(6))

        val meshes = this.meshes.getOrPut(chunkPosition) { synchronizedMapOf() }
        val currentMesh = meshes.remove(sectionHeight)

        renderWindow.queue += {
            if (currentMesh != null) {
                currentMesh.unload()
                this.visibleMeshes -= currentMesh
            }

            mesh.load()
            meshes[sectionHeight] = mesh
            this.visibleMeshes += mesh
        }
    }

    override fun setupOpaque() {
        super.setupOpaque()
        shader.use()
    }

    override fun drawOpaque() {
        for (mesh in visibleMeshes) {
            mesh.opaqueMesh?.draw()
        }
    }

    override fun setupTranslucent() {
        super.setupTranslucent()
        shader.use()
    }

    override fun drawTranslucent() {
        for (mesh in visibleMeshes) {
            mesh.translucentMesh?.draw()
        }
    }

    override fun setupTransparent() {
        super.setupTransparent()
        transparentShader.use()
    }

    override fun drawTransparent() {
        for (mesh in visibleMeshes) {
            mesh.transparentMesh?.draw()
        }
    }

    private fun onFrustumChange(frustum: Frustum) {
        val visible: MutableSet<ChunkSectionMeshes> = mutableSetOf()

        // ToDo
        for ((chunkPosition, meshes) in this.meshes.toSynchronizedMap()) {
            for ((sectionHeight, mesh) in meshes) {
                if (!frustum.containsChunk(chunkPosition, sectionHeight, mesh.minPosition, mesh.maxPosition)) {
                    continue
                }
                visible += mesh
            }
        }

        this.visibleMeshes = visible
    }


    companion object : RendererBuilder<WorldRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:world_renderer")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): WorldRenderer {
            return WorldRenderer(connection, renderWindow)
        }
    }
}
