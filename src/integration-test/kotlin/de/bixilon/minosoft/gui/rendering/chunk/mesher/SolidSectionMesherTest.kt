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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.kmath.vec.vec2.f.Vec2f
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
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.RenderedBlockEntity
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WorldRenderProps
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.gui.rendering.tint.tints.StaticTintProvider
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["mesher"], dependsOnGroups = ["rendering", "block"])
class SolidSectionMesherTest {

    private fun createContext(session: PlaySession): RenderContext {
        val context = RenderContext::class.java.allocate()
        context::session.forceSet(session)
        context::system.forceSet(DummyRenderSystem(context))
        context::camera.forceSet(Camera(context))
        context::tints.forceSet(TintManager(session))


        return context
    }

    private fun createSession(blocks: Map<BlockPosition, BlockState?>): PlaySession {
        val session = SessionTestUtil.createSession(worldSize = 2)
        for ((position, block) in blocks) {
            session.world[position] = block!!
        }

        return session
    }

    private fun PlaySession.mesh(): ChunkMeshes? {
        val context = createContext(this)
        val mesher = SolidSectionMesher(context)

        val chunk = world.chunks[0, 0]!!
        val meshes = ChunkMeshesBuilder(context, 16, 1)

        mesher.mesh(chunk.sections[0]!!, chunk.neighbours.neighbours, chunk.sections[0]!!.neighbours!!, meshes)

        return meshes.build(SectionPosition.of(chunk.position, 0))
    }


    private fun mesh(blocks: Map<BlockPosition, BlockState?>): ChunkMeshes? {
        val session = createSession(blocks)

        return session.mesh()
    }

    fun `test simple stone block`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val meshes = mesh(mapOf(BlockPosition(2, 2, 2) to stone))!!

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 2, 2), stone),
        )

        assertEquals(meshes.min, InSectionPosition(2, 2, 2))
        assertEquals(meshes.max, InSectionPosition(2, 2, 2))
    }

    fun `tinted and untinted block`() {
        val queue = TestQueue()
        val untinted = queue.fullOpaque()
        val tinted = queue.tinted()
        mesh(mapOf(
            BlockPosition(2, 2, 2) to untinted,
            BlockPosition(2, 3, 2) to tinted,
        ))

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 2, 2), untinted),
            TestQueue.RenderedBlock(BlockPosition(2, 3, 2), tinted, 0x123456.rgb()),
        )
    }

    fun `test 2 stone block in extreme directions`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val meshes = mesh(mapOf(
            BlockPosition(0, 0, 0) to stone,
            BlockPosition(15, 15, 15) to stone,
        ))!!

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(0, 0, 0), stone),
            TestQueue.RenderedBlock(BlockPosition(15, 15, 15), stone),
        )

        assertEquals(meshes.min, InSectionPosition(0, 0, 0))
        assertEquals(meshes.max, InSectionPosition(15, 15, 15))
        assertEquals(meshes.entities, null)
    }

    @Test(enabled = false)
    fun `optimize out when all neighbour blocks are full opaque`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val small = queue.nonTouching()
        mesh(mapOf(
            BlockPosition(2, 1, 2) to stone,
            BlockPosition(2, 3, 2) to stone,
            BlockPosition(2, 2, 1) to stone,
            BlockPosition(2, 2, 3) to stone,
            BlockPosition(1, 2, 2) to stone,
            BlockPosition(3, 2, 2) to stone,

            BlockPosition(2, 2, 2) to small,
        ))

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 1, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 3, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 2, 1), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 2, 3), stone),
            TestQueue.RenderedBlock(BlockPosition(1, 2, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(3, 2, 2), stone),
        )
    }

    fun `render stub block entity`() {
        val queue = TestQueue()
        val entity = queue.blockEntity()
        val stone = queue.fullOpaque()
        val meshes = mesh(mapOf(
            BlockPosition(2, 2, 2) to entity,
            BlockPosition(2, 5, 2) to stone,
        ))!!

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 5, 2), stone),
        )
        queue.assert(
            TestQueue.RenderedEntity(BlockPosition(2, 2, 2), entity),
        )
        assertEquals(meshes.entities?.size, 1)
    }

    @Test(enabled = false)
    fun `optimize out block entity if all neighbour blocks are full opaque`() {
        val queue = TestQueue()
        val entity = queue.blockEntity()
        val stone = queue.fullOpaque()
        mesh(mapOf(
            BlockPosition(2, 1, 2) to stone,
            BlockPosition(2, 3, 2) to stone,
            BlockPosition(2, 2, 1) to stone,
            BlockPosition(2, 2, 3) to stone,
            BlockPosition(1, 2, 2) to stone,
            BlockPosition(3, 2, 2) to stone,

            BlockPosition(2, 2, 2) to entity,
        ))

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 1, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 3, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 2, 1), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 2, 3), stone),
            TestQueue.RenderedBlock(BlockPosition(1, 2, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(3, 2, 2), stone),
        )
        assertEquals(queue.entities.size, 0)
    }

    fun `not optimize out block entity if one neighbour blocks is not full opaque`() {
        val queue = TestQueue()
        val stone = queue.fullOpaque()
        val small = queue.nonTouching()
        mesh(mapOf(
            BlockPosition(2, 1, 2) to stone,
            BlockPosition(2, 3, 2) to stone,
            BlockPosition(1, 2, 2) to stone,
            BlockPosition(3, 2, 2) to stone,
            BlockPosition(2, 2, 1) to stone,
            BlockPosition(2, 2, 3) to small,

            BlockPosition(2, 2, 2) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 1, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 3, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 2, 1), stone),
            TestQueue.RenderedBlock(BlockPosition(2, 2, 3), small),
            TestQueue.RenderedBlock(BlockPosition(1, 2, 2), stone),
            TestQueue.RenderedBlock(BlockPosition(3, 2, 2), stone),

            TestQueue.RenderedBlock(BlockPosition(2, 2, 2), stone),
        )
        assertEquals(queue.entities.size, 0)
    }

    fun `render meshed`() {
        val queue = TestQueue()
        val entity = queue.blockEntity()
        val stone = queue.fullOpaque()
        mesh(mapOf(
            BlockPosition(2, 2, 2) to entity,
            BlockPosition(2, 5, 2) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 5, 2), stone),
        )
        queue.assert(
            TestQueue.RenderedEntity(BlockPosition(2, 2, 2), entity),
        )
    }

    fun `render meshed only`() {
        val queue = TestQueue()
        val entity = queue.meshedOnlyEntity()
        val stone = queue.fullOpaque()
        mesh(mapOf(
            BlockPosition(2, 2, 2) to entity,
            BlockPosition(2, 5, 2) to stone,
        ))

        queue.assert(
            TestQueue.RenderedBlock(BlockPosition(2, 5, 2), stone),
        )
        queue.assert(
            TestQueue.RenderedEntity(BlockPosition(2, 2, 2), entity, true),
        )
    }

    private operator fun ByteArray.set(x: Int, y: Int, z: Int, value: Int) {
        val index = (y shl 8) or (z shl 4) or x
        this[index] = value.toByte()
    }

    fun `simple light`() {
        val queue = TestQueue()
        val stone = queue.lighted()
        val session = createSession(emptyMap())
        session.world.dimension = DimensionProperties()
        session.world[BlockPosition(6, 7, 9)] = stone
        val chunk = session.world.chunks[0, 0]!!
        val section = chunk.sections[0]!!
        section.light.light[InSectionPosition(6, 6, 9)] = LightLevel(block = 1, sky = 0)
        section.light.light[InSectionPosition(6, 8, 9)] = LightLevel(block = 2, sky = 0)
        section.light.light[InSectionPosition(6, 7, 8)] = LightLevel(block = 3, sky = 0)
        section.light.light[InSectionPosition(6, 7, 10)] = LightLevel(block = 4, sky = 0)
        section.light.light[InSectionPosition(5, 7, 9)] = LightLevel(block = 5, sky = 0)
        section.light.light[InSectionPosition(7, 7, 9)] = LightLevel(block = 6, sky = 0)

        section.light.light[InSectionPosition(6, 7, 9)] = LightLevel(block = 7, sky = 0)

        session.mesh()
    }

    fun `neighbours are correctly set`() {
        val queue = TestQueue()
        val blocks = Array(6) { queue.nonTouching(it) }
        mesh(mapOf(
            BlockPosition(2, 1, 2) to blocks[0],
            BlockPosition(2, 3, 2) to blocks[1],
            BlockPosition(2, 2, 1) to blocks[2],
            BlockPosition(2, 2, 3) to blocks[3],
            BlockPosition(1, 2, 2) to blocks[4],
            BlockPosition(3, 2, 2) to blocks[5],

            BlockPosition(2, 2, 2) to queue.neighbours(blocks),
        ))

        assertEquals(queue.blocks.size, 7)
    }
    /*
    @Test
    fun benchmark() {
        MinosoftSIT.setup()
        RenderTestLoader().init()
        val session = SessionTestUtil.createSession(worldSize = 2)
        val context = createContext(session)
        val mesher = SolidSectionMesher(context)
        
        val chunk = session.world.chunks[0,0]!!
        val section = chunk.getOrPut(2,false)!!
        
        val random = Random(12L.murmur64())
        val block = session.registries.block[StoneBlock.Block]!!.states.default
        for(index in 0 until ChunkSize.BLOCKS_PER_SECTION) {
            if(!random.nextBoolean()) continue
            section.blocks[index] = block
        }
        
        println("Starting")

       val time = measureTime {
            for (i in 0 until 30_000) {
                val meshes = ChunkMeshes(context, chunk.chunkPosition, 2, true)

                mesher.mesh(chunk.chunkPosition, 2, chunk, section, chunk.neighbours.get()!!, section.neighbours!!, meshes)
                
                meshes.unload()
            }
        }
        println("Took: ${time.format()}")
    }
     */

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
            val position: BlockPosition,
            val block: BlockState,
            val tint: RGBColor? = null,
        )

        data class RenderedEntity(
            val position: BlockPosition,
            val block: BlockState,
            val meshed: Boolean = false,
        )
    }

    fun block(index: Int = 0): Block = object : Block(minosoft("test$index"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())) {
        override val hardness get() = 0.0f
    }

    fun TestQueue.fullOpaque(index: Int = 0): BlockState {
        val state = BlockState(block(index), 0)
        state.model = TestModel(this, SideProperties(arrayOf(FaceProperties(Vec2f.EMPTY, Vec2f(1.0f), TextureTransparencies.OPAQUE)), TextureTransparencies.OPAQUE))

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
            override val tintProvider = StaticTintProvider(0x123456.rgb())
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
            override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
                assertEquals(props.light.size, 7)
                for ((index, entry) in props.light.withIndex()) {
                    assertEquals(required[index], entry.toInt() and 0xFF)
                }
                return super.render(props, position, state, entity, tints)
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
            override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
                assertEquals(props.neighbours.size, 6)
                for ((index, entry) in props.neighbours.withIndex()) {
                    assertEquals(required[index], entry)
                }
                return super.render(props, position, state, entity, tints)
            }
        }

        return state
    }

    fun TestQueue.blockEntity(): BlockState {
        val block = object : Block(minosoft("test2"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())), BlockWithEntity<BlockEntity> {
            override val hardness get() = 0.0f
            override fun createBlockEntity(session: PlaySession) = object : BlockEntity(session), RenderedBlockEntity<BlockEntityRenderer<*>> {
                override var renderer: BlockEntityRenderer<*>? = null

                override fun createRenderer(context: RenderContext, state: BlockState, position: BlockPosition, light: Int) = object : BlockEntityRenderer<BlockEntity> {
                    override var light = 0
                    override var state = state

                    init {
                        entities.add(TestQueue.RenderedEntity(position, state, false)).let { if (!it) throw IllegalArgumentException("Twice!!!") }
                    }

                    override fun drop() = Broken()
                    override fun load() = Broken()
                    override fun unload() = Broken()
                    override fun draw(context: RenderContext) = Broken()
                }
            }
        }
        val state = BlockState(block, 0)

        return state
    }

    fun TestQueue.meshedOnlyEntity(): BlockState {
        val block = object : Block(minosoft("test4"), BlockSettings.of(IT.VERSION, IT.REGISTRIES, emptyMap())), BlockWithEntity<BlockEntity> {
            override val hardness get() = 0.0f
            override fun createBlockEntity(session: PlaySession) = object : BlockEntity(session) {
            }

            init {
                this.model = object : BlockRender {

                    override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
                        entities.add(TestQueue.RenderedEntity(position, state, true)).let { if (!it) throw IllegalArgumentException("Twice!!!") }

                        return true
                    }

                    override fun render(gui: GUIRenderer, offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2f, stack: ItemStack, tints: RGBArray?) = Broken()
                    override fun render(mesh: BlockVertexConsumer, state: BlockState, tints: RGBArray?) = Broken()
                    override fun render(mesh: BlockVertexConsumer, stack: ItemStack, tints: RGBArray?) = Broken()
                }
            }

        }
        val state = BlockState(block, 0)

        return state
    }

    private open class TestModel(val queue: TestQueue, val properties: SideProperties?) : BlockRender {

        override fun render(gui: GUIRenderer, offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2f, stack: ItemStack, tints: RGBArray?) = Broken()
        override fun render(mesh: BlockVertexConsumer, state: BlockState, tints: RGBArray?) = Broken()
        override fun render(mesh: BlockVertexConsumer, stack: ItemStack, tints: RGBArray?) = Broken()

        override fun getProperties(direction: Directions) = this.properties

        override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
            (props.mesh as ChunkMeshesBuilder).opaque.addVertex(1f, 1f, 1f, 1f, 1f, 1f)
            queue.blocks.add(TestQueue.RenderedBlock(position, state, tints?.getOrNull(0))).let { if (!it) throw IllegalArgumentException("Twice!!!") }
            return true
        }
    }
}
