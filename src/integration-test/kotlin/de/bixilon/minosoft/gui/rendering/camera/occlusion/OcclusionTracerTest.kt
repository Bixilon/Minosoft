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

package de.bixilon.minosoft.gui.rendering.camera.occlusion

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.WorldOffset
import de.bixilon.minosoft.gui.rendering.camera.frustum.Frustum
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.annotations.Test

@Test(groups = ["rendering"])
class OcclusionTracerTest {
    private val opaque by lazy { IT.REGISTRIES.block[StoneBlock.Block]!!.states.default }
    private val fullOpaque by lazy { Array(ProtocolDefinition.BLOCKS_PER_SECTION) { opaque } }


    private fun create(block: (World) -> Unit): OcclusionGraph {
        val session = createSession(worldSize = 5)
        block(session.world)

        val chunk = session.world.chunks[ChunkPosition(1, 1)]!!

        val camera = Camera::class.java.allocate()
        camera::offset.forceSet(WorldOffset(camera))
        val context = RenderContext::class.java.allocate()
        context::session.forceSet(session)
        camera::context.forceSet(context)
        camera::frustum.forceSet(Frustum::class.java.allocate().apply { this::class.java.getDeclaredField("camera").field[this] = camera }) // empty frustum, everything always visible

        val graph = OcclusionTracer(SectionPosition(1, 1, 1), DimensionProperties(minY = -128, height = 256), camera, 5)

        return graph.trace(chunk)
    }

    private fun <T> OcclusionGraph.assertVisible(vararg position: T) = assertVisible1(positions = position.unsafeCast())
    private fun OcclusionGraph.assertVisible1(positions: Array<SectionPosition>) {
        for (position in positions) {
            if (isOccluded(position)) throw AssertionError("Position $position is occluded!")
        }
    }

    private fun <T> OcclusionGraph.assertOccluded(vararg position: T) = assertOccluded1(positions = position.unsafeCast())
    private fun OcclusionGraph.assertOccluded1(positions: Array<SectionPosition>) {
        for (position in positions) {
            if (!isOccluded(position)) throw AssertionError("Position $position is visible!")
        }
    }

    // TODO: below 0, above max section, corner visible from one/two/three path (out of 3)

    private operator fun World.set(position: SectionPosition, state: OcclusionState) {
        val section = chunks[position.chunkPosition]!!.getOrPut(position.y)!!
        when (state) {
            OcclusionState.NONE -> section.blocks.clear()
            OcclusionState.FULL_OPAQUE -> section.blocks.setData(fullOpaque.unsafeCast())
        }
    }

    fun `empty world`() {
        val graph = create { }

        graph.assertVisible(SectionPosition(1, 1, 1))
        graph.assertOccluded<SectionPosition>()
    }

    fun `blocking section is visible`() {
        val graph = create {
            it[SectionPosition(1, 2, 1)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 2, 1))
    }

    fun `trace up through air`() {
        val graph = create {
            it[SectionPosition(1, 4, 1)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 1, 1), SectionPosition(1, 2, 1), SectionPosition(1, 3, 1), SectionPosition(1, 4, 1))
        graph.assertOccluded(SectionPosition(1, 5, 1))
    }

    fun `up blocked`() {
        val graph = create {
            it[SectionPosition(1, 2, 1)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 1, 1), SectionPosition(1, 2, 1))
        graph.assertOccluded(SectionPosition(1, 3, 1), SectionPosition(1, 4, 1))
    }

    fun `down blocked`() {
        val graph = create {
            it[SectionPosition(1, 0, 1)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 1, 1), SectionPosition(1, 0, 1))
        graph.assertOccluded(SectionPosition(1, -1, 1), SectionPosition(1, -2, 1))
    }

    fun `north blocked`() {
        val graph = create {
            it[SectionPosition(1, 1, 0)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 1, 1), SectionPosition(1, 1, 0))
        graph.assertOccluded(SectionPosition(1, 1, -1), SectionPosition(1, 1, -2))
    }

    fun `south blocked`() {
        val graph = create {
            it[SectionPosition(1, 1, 2)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 1, 1), SectionPosition(1, 1, 2))
        graph.assertOccluded(SectionPosition(1, 1, 3), SectionPosition(1, 1, 4))
    }

    fun `west blocked`() {
        val graph = create {
            it[SectionPosition(0, 1, 1)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 1, 1), SectionPosition(0, 1, 1))
        graph.assertOccluded(SectionPosition(-1, 1, 1), SectionPosition(-2, 1, 1))
    }

    fun `east blocked`() {
        val graph = create {
            it[SectionPosition(2, 1, 1)] = OcclusionState.FULL_OPAQUE
        }

        graph.assertVisible(SectionPosition(1, 1, 1), SectionPosition(2, 1, 1))
        graph.assertOccluded(SectionPosition(3, 1, 1), SectionPosition(4, 1, 1))
    }


    enum class OcclusionState {
        NONE,
        FULL_OPAQUE,
        ;
    }
}
