package de.bixilon.minosoft.data.world.container.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["chunk"], dependsOnGroups = ["block"])
class BlockSectionDataProviderTest {

    private fun create(): BlockSectionDataProvider {
        return BlockSectionDataProvider(null)
    }

    fun `initial empty`() {
        val blocks = create()
        assertTrue(blocks.isEmpty)
        assertEquals(blocks.fluidCount, 0)
        assertEquals(blocks.count, 0)
    }

    fun `single block set and removed`() {
        val blocks = create()
        blocks[0] = StoneTest0.state
        blocks[0] = null
        assertTrue(blocks.isEmpty)
        assertEquals(blocks.fluidCount, 0)
        assertEquals(blocks.count, 0)
    }

    fun `single block set`() {
        val blocks = create()
        blocks[0] = StoneTest0.state
        assertFalse(blocks.isEmpty)
        assertEquals(blocks.fluidCount, 0)
        assertEquals(blocks.count, 1)
    }

    fun `initial min max position`() {
        val blocks = create()
        assertEquals(blocks.minPosition, Vec3i(16, 16, 16))
        assertEquals(blocks.maxPosition, Vec3i(0, 0, 0))
    }

    fun `set min max position`() {
        val blocks = create()
        blocks[0] = StoneTest0.state
        assertEquals(blocks.minPosition, Vec3i(0, 0, 0))
        assertEquals(blocks.maxPosition, Vec3i(0, 0, 0))
    }

    fun `set min max position but block not on edge`() {
        val blocks = create()
        blocks[3, 5, 8] = StoneTest0.state
        assertEquals(blocks.minPosition, Vec3i(3, 5, 8))
        assertEquals(blocks.maxPosition, Vec3i(3, 5, 8))
    }

    fun `set min max position but multiple blocks set`() {
        val blocks = create()
        blocks[3, 5, 8] = StoneTest0.state
        blocks[1, 2, 12] = StoneTest0.state
        assertEquals(blocks.minPosition, Vec3i(1, 2, 8))
        assertEquals(blocks.maxPosition, Vec3i(3, 5, 12))
    }

    fun `remove one min max position but multiple blocks set`() {
        val blocks = create()
        blocks[3, 5, 8] = StoneTest0.state
        blocks[1, 2, 12] = StoneTest0.state
        blocks[15, 14, 13] = StoneTest0.state
        assertEquals(blocks.minPosition, Vec3i(1, 2, 8))
        assertEquals(blocks.maxPosition, Vec3i(15, 14, 13))
        blocks[15, 14, 13] = null
        assertEquals(blocks.maxPosition, Vec3i(3, 5, 12))
    }

    // TODO: test initial block set
}
