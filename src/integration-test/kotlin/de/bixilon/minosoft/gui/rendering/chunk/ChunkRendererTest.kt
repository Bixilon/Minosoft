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

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.time.TimeUtil.sleep
import de.bixilon.minosoft.camera.SessionCamera
import de.bixilon.minosoft.config.profile.ProfileTestUtil.createProfiles
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributes
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkData
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.occlusion.OcclusionGraph
import de.bixilon.minosoft.gui.rendering.camera.occlusion.SectionPositionSet
import de.bixilon.minosoft.gui.rendering.camera.occlusion.WorldOcclusionManager
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.queue.culled.CulledQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshUnloadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.ChunkMeshingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.MeshQueueItem
import de.bixilon.minosoft.gui.rendering.light.RenderLight
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureManager
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.physics.entities.living.player.PlayerPhysics
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.annotations.Test
import kotlin.time.Duration.Companion.milliseconds

@Test(groups = ["chunk_renderer"])
class ChunkRendererTest {
    private val VIEW_DISTANCE = CulledQueue::class.java.getFieldOrNull("viewDistance")!!.field
    private val CULLED = CulledQueue::class.java.getFieldOrNull("culled")!!.field
    private val MESHING_QUEUE = ChunkMeshingQueue::class.java.getFieldOrNull("queue")!!.field
    private val LOADING_QUEUE = MeshLoadingQueue::class.java.getFieldOrNull("meshes")!!.field
    private val LOADED = LoadedMeshes::class.java.getFieldOrNull("meshes")!!.field
    private val UNLOADING_QUEUE = MeshUnloadingQueue::class.java.getFieldOrNull("meshes")!!.field


    private val OCCLUSION = WorldOcclusionManager::class.java.getFieldOrNull("graph")!!.field

    private fun ChunkRenderer.create(position: ChunkPosition, visible: Boolean, neighbours: Boolean): Chunk {
        val chunk = world.chunks.update(position, ChunkData(), true)

        if (neighbours) {
            for (x in position.x - 1..position.x + 1) {
                for (z in position.x - 1..position.x + 1) {
                    if (x == position.x && z == position.z) continue
                    world.chunks.update(ChunkPosition(x, z), ChunkData(), false)
                }
            }
        }
        if (!visible) {
            OCCLUSION[context.camera.occlusion] = SectionPositionSet(ChunkPosition(0, 0), 5, 0, 10)
        }

        return chunk
    }

    private fun ChunkRenderer.isOutOfViewDistance(chunk: Chunk): Boolean {
        return chunk in VIEW_DISTANCE.get<HashSet<Chunk>>(culledQueue)
    }

    private fun ChunkRenderer.isCulled(section: ChunkSection): Boolean {
        val map = CULLED.get<HashMap<ChunkPosition, HashSet<ChunkSection>>>(culledQueue)

        return map[section.chunk.position]?.contains(section) ?: false
    }

    private fun ChunkRenderer.isMeshing(section: ChunkSection): Boolean {
        val items = MESHING_QUEUE.get<List<MeshQueueItem>>(meshingQueue)

        return items.find { it.section == section } != null
    }

    private fun ChunkRenderer.isLoading(section: ChunkSection): Boolean {
        val queue = LOADING_QUEUE.get<ArrayDeque<ChunkMeshes>>(loadingQueue)

        return queue.find { it.section == section } != null
    }

    private fun ChunkRenderer.isLoaded(section: ChunkSection): Boolean {
        val map = LOADED.get<MutableMap<ChunkPosition, Int2ObjectOpenHashMap<ChunkMeshes>>>(loaded)

        return map[section.chunk.position]?.get(section.height) != null
    }

    private fun ChunkRenderer.isUnloading(section: ChunkSection): Boolean {
        val meshes = UNLOADING_QUEUE.get<ArrayDeque<ChunkMeshes>>(unloadingQueue)

        return meshes.find { it.section == section } != null
    }

    private fun create(): ChunkRenderer {
        val player = LocalPlayerEntity::class.java.allocate()

        val session = PlaySession::class.java.allocate()
        session::player.forceSet(player)
        session::profiles.forceSet(createProfiles())
        session::world.forceSet(World((session)).apply { view.viewDistance = 10 })
        session::events.forceSet(EventMaster())
        session::registries.forceSet(Registries(version = IT.VERSION).apply { parent = IT.REGISTRIES })

        player::session.forceSet(session)
        player::attributes.forceSet(EntityAttributes())
        player::class.java.getFieldOrNull("physics")!!.field[player] = PlayerPhysics(player)


        session::camera.forceSet(SessionCamera(session))
        session.camera::entity.forceSet(DataObserver(player))

        val context = RenderContext::class.java.allocate()
        context::session.forceSet(session)
        context::system.forceSet(DummyRenderSystem(context))
        context::textures.forceSet(DummyTextureManager(context))
        context::light.forceSet(RenderLight(context))
        context::camera.forceSet(Camera(context))
        context::state.forceSet(DataObserver(RenderingStates.RUNNING))
        context::tints.forceSet(TintManager(session))


        return ChunkRenderer(session, context)
    }

    fun `directly add to out of view distance`() {
        val renderer = create()
        val chunk = renderer.create(ChunkPosition(100, 100), true, true)

        renderer.invalidate(chunk)
        assert(renderer.isOutOfViewDistance(chunk))
    }
    // TODO: work on out of view distance

    fun `directly add to culled queue if invisible`() {
        val renderer = create()
        val chunk = renderer.create(ChunkPosition(2, 2), false, true)
        val section = chunk.sections.create(1)!!.apply { this.blocks[1, 2, 3] = TestBlockStates.MODEL1 }

        renderer.invalidate(section)

        assert(renderer.isCulled(section))
        assert(!renderer.isMeshing(section))
    }
    // TODO: work on culled queue

    fun `directly add to meshing queue if visible`() {
        val renderer = create()
        val chunk = renderer.create(ChunkPosition(2, 2), true, true)
        val section = chunk.sections.create(1)!!.apply { this.blocks[1, 2, 3] = TestBlockStates.MODEL1 }

        renderer.invalidate(section)

        assert(!renderer.isCulled(section))
        assert(renderer.isMeshing(section))
    }

    fun `meshing queue mesh it`() {
        val renderer = create()
        val chunk = renderer.create(ChunkPosition(2, 2), true, true)
        val section = chunk.sections.create(1)!!.apply { this.blocks[1, 2, 3] = TestBlockStates.MODEL1 }

        renderer.invalidate(section)

        renderer.meshingQueue.work()
        assert(!renderer.isMeshing(section))
        for (i in 0 until 100) {
            sleep(10.milliseconds)
            if (renderer.isLoading(section)) break
        }
        assert(renderer.isLoading(section))
    }

    fun `loading queue load it`() {
        val renderer = create()
        val chunk = renderer.create(ChunkPosition(2, 2), true, true)
        val section = chunk.sections.create(1)!!.apply { this.blocks[1, 2, 3] = TestBlockStates.MODEL1 }

        renderer.invalidate(section)

        renderer.meshingQueue.work()
        for (i in 0 until 100) {
            sleep(10.milliseconds)
            if (renderer.isLoading(section)) break
        }
        renderer.loadingQueue.work()
        assert(!renderer.isLoading(section))
        assert(renderer.isLoaded(section))
    }

    // TODO: all the short paths, interrupting, visible meshes, level of detail
    // TODO: is unloading
    // TODO: remove if empty
}
