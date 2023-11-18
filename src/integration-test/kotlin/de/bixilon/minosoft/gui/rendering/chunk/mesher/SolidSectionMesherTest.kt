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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.RenderedBlockEntity
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.gui.rendering.tint.tints.StaticTintProvider
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.*

@Test(groups = ["mesher"], dependsOnGroups = ["rendering", "block"])
class SolidSectionMesherTest {

    private fun createContext(connection: PlayConnection): RenderContext {
        val context = IT.OBJENESIS.newInstance(RenderContext::class.java)
        context::connection.forceSet(connection)
        context::system.forceSet(DummyRenderSystem(context))
        context::camera.forceSet(Camera(context))
        context::tints.forceSet(TintManager(connection))


        return context
    }

    private fun createConnection(blocks: Map<Vec3i, BlockState?>): PlayConnection {
        val connection = ConnectionTestUtil.createConnection(worldSize = 2)
        for ((position, block) in blocks) {
            connection.world[position] = block!!
        }

        return connection
    }

    private fun PlayConnection.mesh(): ChunkMeshes {
        val context = createContext(this)
        val mesher = SolidSectionMesher(context)

        val chunk = world.chunks[0, 0]!!
        val meshes = ChunkMeshes(context, chunk.chunkPosition, 0, true)

        mesher.mesh(chunk.chunkPosition, 0, chunk, chunk.sections[0]!!, chunk.neighbours.get()!!, chunk.sections[0]!!.neighbours!!, meshes)

        return meshes
    }


    private fun mesh(blocks: Map<Vec3i, BlockState?>): ChunkMeshes {
        val connection = createConnection(blocks)

        return connection.mesh()
    }

    fun `test simple stone block`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val meshes = mesh(mapOf(Vec3i(2, 2, 2) to stone))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 2, 2), stone),
        )

        assertEquals(meshes.minPosition, Vec3i(2, 2, 2))
        assertEquals(meshes.maxPosition, Vec3i(2, 2, 2))
    }

    fun `tinted and untinted block`() {
        val queue = TestQueue()
        val untinted = queue.fullOpaque()
        val tinted = queue.tinted()
        mesh(mapOf(
            Vec3i(2, 2, 2) to untinted,
            Vec3i(2, 3, 2) to tinted,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 2, 2), untinted),
            TestQueue.RenderedBlock(Vec3i(2, 3, 2), tinted, 0x123456),
        )
    }

    fun `test 2 stone block in extreme directions`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val meshes = mesh(mapOf(
            Vec3i(0, 0, 0) to stone,
            Vec3i(15, 15, 15) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(0, 0, 0), stone),
            TestQueue.RenderedBlock(Vec3i(15, 15, 15), stone),
        )

        assertEquals(meshes.minPosition, Vec3i(0, 0, 0))
        assertEquals(meshes.maxPosition, Vec3i(15, 15, 15))
        assertEquals(meshes.blockEntities?.size, 0)
    }

    @Test(enabled = false)
    fun `optimize out when all neighbour blocks are full opaque`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val small = queue.nonTouching()
        mesh(mapOf(
            Vec3i(2, 1, 2) to stone,
            Vec3i(2, 3, 2) to stone,
            Vec3i(2, 2, 1) to stone,
            Vec3i(2, 2, 3) to stone,
            Vec3i(1, 2, 2) to stone,
            Vec3i(3, 2, 2) to stone,

            Vec3i(2, 2, 2) to small,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 1, 2), stone),
            TestQueue.RenderedBlock(Vec3i(2, 3, 2), stone),
            TestQueue.RenderedBlock(Vec3i(2, 2, 1), stone),
            TestQueue.RenderedBlock(Vec3i(2, 2, 3), stone),
            TestQueue.RenderedBlock(Vec3i(1, 2, 2), stone),
            TestQueue.RenderedBlock(Vec3i(3, 2, 2), stone),
        )
    }

    fun `render stub block entity`() {
        val queue = TestQueue()
        val entity = queue.blockEntity()
        val stone = queue.fullOpaque()
        val meshes = mesh(mapOf(
            Vec3i(2, 2, 2) to entity,
            Vec3i(2, 5, 2) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 5, 2), stone),
        )
        queue.assert(
            TestQueue.RenderedEntity(Vec3i(2, 2, 2), entity),
        )
        assertEquals(meshes.blockEntities?.size, 1)
    }

    @Test(enabled = false)
    fun `optimize out block entity if all neighbour blocks are full opaque`() {
        val queue = TestQueue()
        val entity = queue.blockEntity()
        val stone = queue.fullOpaque()
        mesh(mapOf(
            Vec3i(2, 1, 2) to stone,
            Vec3i(2, 3, 2) to stone,
            Vec3i(2, 2, 1) to stone,
            Vec3i(2, 2, 3) to stone,
            Vec3i(1, 2, 2) to stone,
            Vec3i(3, 2, 2) to stone,

            Vec3i(2, 2, 2) to entity,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 1, 2), stone),
            TestQueue.RenderedBlock(Vec3i(2, 3, 2), stone),
            TestQueue.RenderedBlock(Vec3i(2, 2, 1), stone),
            TestQueue.RenderedBlock(Vec3i(2, 2, 3), stone),
            TestQueue.RenderedBlock(Vec3i(1, 2, 2), stone),
            TestQueue.RenderedBlock(Vec3i(3, 2, 2), stone),
        )
        assertEquals(queue.entities.size, 0)
    }

    fun `not optimize out block entity if one neighbour blocks is not full opaque`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val small = queue.nonTouching()
        mesh(mapOf(
            Vec3i(2, 1, 2) to stone,
            Vec3i(2, 3, 2) to stone,
            Vec3i(1, 2, 2) to stone,
            Vec3i(3, 2, 2) to stone,
            Vec3i(2, 2, 1) to stone,
            Vec3i(2, 2, 3) to small,

            Vec3i(2, 2, 2) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 1, 2), stone),
            TestQueue.RenderedBlock(Vec3i(2, 3, 2), stone),
            TestQueue.RenderedBlock(Vec3i(2, 2, 1), stone),
            TestQueue.RenderedBlock(Vec3i(2, 2, 3), small),
            TestQueue.RenderedBlock(Vec3i(1, 2, 2), stone),
            TestQueue.RenderedBlock(Vec3i(3, 2, 2), stone),

            TestQueue.RenderedBlock(Vec3i(2, 2, 2), stone),
        )
        assertEquals(queue.entities.size, 0)
    }

    fun `render meshed`() {
        val queue = TestQueue()
        val entity = queue.blockEntity()
        val stone = queue.fullOpaque()
        mesh(mapOf(
            Vec3i(2, 2, 2) to entity,
            Vec3i(2, 5, 2) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 5, 2), stone),
        )
        queue.assert(
            TestQueue.RenderedEntity(Vec3i(2, 2, 2), entity),
        )
    }

    fun `render meshed only`() {
        val queue = TestQueue()
        val entity = queue.meshedOnlyEntity()
        val stone = queue.fullOpaque()
        mesh(mapOf(
            Vec3i(2, 2, 2) to entity,
            Vec3i(2, 5, 2) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(Vec3i(2, 5, 2), stone),
        )
        queue.assert(
            TestQueue.RenderedEntity(Vec3i(2, 2, 2), entity, true),
        )
    }

    private operator fun ByteArray.set(x: Int, y: Int, z: Int, value: Int) {
        val index = (y shl 8) or (z shl 4) or x
        this[index] = value.toByte()
    }

    fun `simple light`() {
        val queue = TestQueue()
        val stone = queue.lighted()
        val connection = createConnection(emptyMap())
        connection.world.dimension = DimensionProperties()
        connection.world[Vec3i(6, 7, 9)] = stone
        val chunk = connection.world.chunks[0, 0]!!
        val section = chunk.sections[0]!!
        section.light.light[6, 6, 9] = 0x01
        section.light.light[6, 8, 9] = 0x02
        section.light.light[6, 7, 8] = 0x03
        section.light.light[6, 7, 10] = 0x04
        section.light.light[5, 7, 9] = 0x05
        section.light.light[7, 7, 9] = 0x06

        section.light.light[6, 7, 9] = 0x07

        connection.mesh()
    }

    fun `neighbours are correctly set`() {
        val queue = TestQueue()
        val blocks = Array(6) { queue.nonTouching(it) }
        mesh(mapOf(
            Vec3i(2, 1, 2) to blocks[0],
            Vec3i(2, 3, 2) to blocks[1],
            Vec3i(2, 2, 1) to blocks[2],
            Vec3i(2, 2, 3) to blocks[3],
            Vec3i(1, 2, 2) to blocks[4],
            Vec3i(3, 2, 2) to blocks[5],

            Vec3i(2, 2, 2) to queue.neighbours(blocks),
        ))

        assertEquals(queue.blocks.size, 7)
    }
    // TODO: test sign block entity rendering
    // TODO: test skylight (w/ heightmap), fast bedrock, camera offset, block random offset

    class TestQueue {
        val blocks: MutableSet<RenderedBlock> = mutableSetOf()
        val entities: MutableSet<RenderedEntity> = mutableSetOf()


        fun assert(vararg blocks: RenderedBlock) {
            assertEquals(this.blocks, blocks.toSet())
        }

        fun assert(vararg entities: RenderedEntity) {
            assertEquals(this.entities, entities.toSet())
        }

        data class RenderedBlock(
            val position: Vec3i,
            val block: BlockState,
            val tint: Int? = null,
        )

        data class RenderedEntity(
            val position: Vec3i,
            val block: BlockState,
            val meshed: Boolean = false,
        )
    }

    fun block(index: Int = 0): Block = object : Block(minosoft("test$index"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())) {
        override val hardness get() = 0.0f
    }

    fun TestQueue.fullOpaque(index: Int = 0): BlockState {
        val state = BlockState(block(index), 0)
        state.model = TestModel(this, SideProperties(arrayOf(FaceProperties(Vec2.EMPTY, Vec2(1.0f), TextureTransparencies.OPAQUE)), TextureTransparencies.OPAQUE))

        return state
    }

    fun TestQueue.nonTouching(index: Int = 0): BlockState {
        val state = BlockState(block(index), 0)
        state.model = TestModel(this, null)

        return state
    }

    fun TestQueue.tinted(): BlockState {
        val block = object : Block(minosoft("test3"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())), TintedBlock {
            override val hardness get() = 0.0f
            override val tintProvider = StaticTintProvider(0x123456)
        }
        val state = BlockState(block, 0)
        state.model = TestModel(this, null)

        return state
    }

    fun TestQueue.lighted(required: IntArray = IntArray(7) { it + 1 }): BlockState {
        val block = object : Block(minosoft("testdroelf"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())) {
            override val hardness get() = 0.0f
        }
        val state = BlockState(block, 0)
        state.model = object : TestModel(this, null) {
            override fun render(position: BlockPosition, offset: FloatArray, mesh: BlockVertexConsumer, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?, entity: BlockEntity?): Boolean {
                assertEquals(light.size, 7)
                for ((index, entry) in light.withIndex()) {
                    assertEquals(required[index], entry.toInt() and 0xFF)
                }
                return super.render(position, offset, mesh, random, state, neighbours, light, tints, entity)
            }
        }

        return state
    }

    fun TestQueue.neighbours(required: Array<BlockState>): BlockState {
        val block = object : Block(minosoft("testdroelfe"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())) {
            override val hardness get() = 0.0f
        }
        val state = BlockState(block, 0)
        state.model = object : TestModel(this, null) {
            override fun render(position: BlockPosition, offset: FloatArray, mesh: BlockVertexConsumer, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?, entity: BlockEntity?): Boolean {
                assertEquals(neighbours.size, 6)
                for ((index, entry) in neighbours.withIndex()) {
                    assertEquals(required[index], entry)
                }
                return super.render(position, offset, mesh, random, state, neighbours, light, tints, entity)
            }
        }

        return state
    }

    fun TestQueue.blockEntity(): BlockState {
        val block = object : Block(minosoft("test2"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())), BlockWithEntity<BlockEntity> {
            override val hardness get() = 0.0f
            override fun createBlockEntity(connection: PlayConnection) = object : BlockEntity(connection), RenderedBlockEntity<BlockEntityRenderer<*>> {
                override var renderer: BlockEntityRenderer<*>? = null

                override fun createRenderer(context: RenderContext, state: BlockState, position: Vec3i, light: Int) = object : BlockEntityRenderer<BlockEntity> {
                    override var light = 0
                    override var state = state

                    init {
                        entities.add(TestQueue.RenderedEntity(Vec3i(position), state, false)).let { if (!it) throw IllegalArgumentException("Twice!!!") }
                    }
                }
            }
        }
        val state = BlockState(block, 0)

        return state
    }

    fun TestQueue.meshedOnlyEntity(): BlockState {
        val block = object : Block(minosoft("test4"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())), BlockWithEntity<BlockEntity> {
            override val hardness get() = 0.0f
            override fun createBlockEntity(connection: PlayConnection) = object : BlockEntity(connection) {
            }

            init {
                this.model = object : BlockRender {

                    override fun render(position: BlockPosition, offset: FloatArray, mesh: BlockVertexConsumer, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?, entity: BlockEntity?): Boolean {
                        entities.add(TestQueue.RenderedEntity(Vec3i(position), state, true)).let { if (!it) throw IllegalArgumentException("Twice!!!") }

                        return true
                    }

                    override fun render(gui: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack, tints: IntArray?) = Broken()
                    override fun render(mesh: BlockVertexConsumer, state: BlockState, tints: IntArray?) = Broken()
                    override fun render(mesh: BlockVertexConsumer, stack: ItemStack, tints: IntArray?) = Broken()
                }
            }

        }
        val state = BlockState(block, 0)

        return state
    }

    private open class TestModel(val queue: TestQueue, val properties: SideProperties?) : BlockRender {

        override fun render(gui: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack, tints: IntArray?) = Broken()
        override fun render(mesh: BlockVertexConsumer, state: BlockState, tints: IntArray?) = Broken()
        override fun render(mesh: BlockVertexConsumer, stack: ItemStack, tints: IntArray?) = Broken()
        override fun getProperties(direction: Directions): SideProperties? {
            return this.properties
        }

        override fun render(position: BlockPosition, offset: FloatArray, mesh: BlockVertexConsumer, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?, entity: BlockEntity?): Boolean {
            queue.blocks.add(TestQueue.RenderedBlock(Vec3i(position), state, tints?.getOrNull(0))).let { if (!it) throw IllegalArgumentException("Twice!!!") }
            return true
        }
    }
}
