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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["models", "culling"])
class FaceCullingTest {

    private fun createFace(transparency: TextureTransparencies = TextureTransparencies.OPAQUE, properties: FaceProperties? = FaceProperties(Vec2(0), Vec2(1), transparency)): FaceProperties? {
        return properties
    }

    private fun createBlock(transparency: TextureTransparencies = TextureTransparencies.OPAQUE, properties: SideProperties? = SideProperties(arrayOf(FaceProperties(Vec2(0), Vec2(1), transparency)), transparency), type: Int = 0): BlockState {
        val block = object : Block(minosoft("dummy$type"), BlockSettings()) {
            override val hardness: Float get() = Broken()
        }

        return createBlock(block, transparency, properties)
    }

    private fun createBlock(block: Block, transparency: TextureTransparencies = TextureTransparencies.OPAQUE, properties: SideProperties? = SideProperties(arrayOf(FaceProperties(Vec2(0), Vec2(1), transparency)), transparency)): BlockState {
        val state = BlockState(block, 0)

        state.model = BakedModel(Array(Directions.SIZE) { emptyArray() }, arrayOf(properties, properties, properties, properties, properties, properties), null)

        return state
    }

    private fun createState(type: Int = 0) = createBlock(type = type)

    fun noNeighbour() {
        val face = createFace()
        assertFalse(FaceCulling.canCull(createState(), face, Directions.DOWN, null))
    }

    fun selfNotTouching() {
        val face = createFace(properties = null)
        val neighbour = createBlock()
        assertFalse(FaceCulling.canCull(createState(), face, Directions.DOWN, neighbour))
    }

    fun neighbourNotTouching() {
        val face = createFace()
        val neighbour = createBlock(properties = null)
        assertFalse(FaceCulling.canCull(createState(), face, Directions.DOWN, neighbour))
    }

    fun fullNeighbour() {
        val face = createFace()
        val neighbour = createBlock()
        assertTrue(FaceCulling.canCull(createState(), face, Directions.DOWN, neighbour))
    }

    fun sizeMatch() {
        val face = createFace(properties = FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun greaterNeighbour() {
        val face = createFace(properties = FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 0.6f), TextureTransparencies.OPAQUE)))
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun smallerNeighbour() {
        val face = createFace(properties = FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 0.4f), TextureTransparencies.OPAQUE)))
        assertFalse(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun noSize() {
        val face = createFace(properties = FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.1f, 0.5f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun shiftedNeighbour1() {
        val face = createFace(properties = FaceProperties(Vec2(0.0f, 0.4f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.1f, 0.4f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertFalse(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun shiftedNeighbour2() {
        val face = createFace(properties = FaceProperties(Vec2(0.0f, 0.4f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.1f, 0.4f), Vec2(1.0f, 0.6f), TextureTransparencies.OPAQUE)))
        assertFalse(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun shiftedNeighbour3() {
        val face = createFace(properties = FaceProperties(Vec2(0.1f, 0.8f), Vec2(0.9f, 0.9f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.1f, 0.5f), Vec2(0.95f, 0.95f), TextureTransparencies.OPAQUE)))
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun multipleNeighbourFaces() {
        val face = createFace(properties = FaceProperties(Vec2(0.1f, 0.3f), Vec2(0.9f, 0.9f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.1f, 0.2f), Vec2(0.95f, 0.4f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.1f, 0.4f), Vec2(0.95f, 0.6f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.1f, 0.6f), Vec2(0.95f, 0.95f), TextureTransparencies.OPAQUE)))
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun `transparent side on opaque neighbour`() {
        val face = createFace(transparency = TextureTransparencies.TRANSPARENT)
        val neighbour = createBlock(transparency = TextureTransparencies.OPAQUE)
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun `translucent side on opaque neighbour`() {
        val face = createFace(transparency = TextureTransparencies.TRANSLUCENT)
        val neighbour = createBlock(transparency = TextureTransparencies.OPAQUE)
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun `opaque side on transparent neighbour`() {
        val face = createFace(transparency = TextureTransparencies.OPAQUE)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSPARENT)
        assertFalse(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun `opaque side on translucent neighbour`() {
        val face = createFace(transparency = TextureTransparencies.OPAQUE)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSLUCENT)
        assertFalse(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun `same block, both sides transparent`() {
        val face = createFace(transparency = TextureTransparencies.TRANSPARENT)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSPARENT)
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun `same block, both sides translucent`() {
        val face = createFace(transparency = TextureTransparencies.TRANSLUCENT)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSLUCENT)
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }

    fun `different block, both sides transparent`() {
        val face = createFace(transparency = TextureTransparencies.TRANSPARENT)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSPARENT)
        assertFalse(FaceCulling.canCull(createState(1), face, Directions.EAST, neighbour))
    }

    fun `different block, both sides translucent`() {
        val face = createFace(transparency = TextureTransparencies.TRANSLUCENT)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSLUCENT)
        assertFalse(FaceCulling.canCull(createState(1), face, Directions.EAST, neighbour))
    }

    fun `same block, transparent sides, force no cull`() {
        val face = createFace(transparency = TextureTransparencies.TRANSPARENT)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSPARENT)
        assertFalse(FaceCulling.canCull(createBlock(forceNoCull()), face, Directions.EAST, neighbour))
    }

    fun `same block, translucent sides, force no cull`() {
        val face = createFace(transparency = TextureTransparencies.TRANSLUCENT)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSLUCENT)
        assertFalse(FaceCulling.canCull(createBlock(forceNoCull()), face, Directions.EAST, neighbour))
    }

    fun `opaque side on transparent block`() {
        val face = createFace(transparency = TextureTransparencies.OPAQUE)
        val neighbour = createBlock(transparency = TextureTransparencies.TRANSPARENT)
        assertFalse(FaceCulling.canCull(createBlock(), face, Directions.EAST, neighbour))
    }

    fun `transparent side on opaque block`() {
        val face = createFace(transparency = TextureTransparencies.TRANSPARENT)
        val neighbour = createBlock(transparency = TextureTransparencies.OPAQUE)
        assertTrue(FaceCulling.canCull(createBlock(), face, Directions.EAST, neighbour))
    }

    fun `opaque but no invoked custom cull`() {
        val block = object : Block(minosoft("dummy"), BlockSettings()), CustomBlockCulling {
            override val hardness get() = Broken()

            override fun shouldCull(state: BlockState, properties: FaceProperties, directions: Directions, neighbour: BlockState): Boolean {
                throw AssertionError("shouldCall invoked!")
            }
        }
        val face = createFace(transparency = TextureTransparencies.OPAQUE)
        val neighbour = createBlock(transparency = TextureTransparencies.OPAQUE)
        assertTrue(FaceCulling.canCull(createBlock(block), face, Directions.EAST, neighbour))
    }


    fun `opaque but neighbour opaque and transparent`() { // grass block + overlay
        val face = createFace(TextureTransparencies.OPAQUE, properties = FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE))
        val neighbour = createBlock(properties = side(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.TRANSPARENT), transparency = null))
        assertTrue(FaceCulling.canCull(createState(), face, Directions.EAST, neighbour))
    }


    private fun side(vararg properties: FaceProperties, transparency: TextureTransparencies? = properties.first().transparency): SideProperties {
        return SideProperties(arrayOf(*properties), transparency)
    }

    private fun forceNoCull() = object : Block(minosoft("dummy"), BlockSettings()), CustomBlockCulling {
        override val hardness get() = Broken()

        override fun shouldCull(state: BlockState, properties: FaceProperties, directions: Directions, neighbour: BlockState): Boolean {
            return false
        }
    }
}
