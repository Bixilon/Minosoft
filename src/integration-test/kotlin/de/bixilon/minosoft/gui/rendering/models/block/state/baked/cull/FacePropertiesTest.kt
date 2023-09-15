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
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bake
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.WeightedBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["models", "culling"])
class FacePropertiesTest {

    private fun BlockRender.assertProperties(direction: Directions, vararg properties: FaceProperties) {
        val actual = this.getProperties(direction) ?: return assertEquals(properties.size, 0)

        assertEquals(actual.faces, properties)
    }

    fun `full cube`() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!


        baked.assertProperties(Directions.DOWN, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.UP, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.NORTH, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.SOUTH, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.WEST, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.EAST, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
    }

    fun `smaller cube`() {
        val from = Vec3(0.1f)
        val to = Vec3(0.9f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!


        baked.assertProperties(Directions.DOWN)
        baked.assertProperties(Directions.UP)
        baked.assertProperties(Directions.NORTH)
        baked.assertProperties(Directions.SOUTH)
        baked.assertProperties(Directions.WEST)
        baked.assertProperties(Directions.EAST)
    }

    fun fullBlock() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertEquals(baked.getProperties(Directions.DOWN)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.UP)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.NORTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.SOUTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.WEST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.EAST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
    }

    fun lowerSlab() {
        val from = Vec3(0.0f, 0.0f, 0.0f)
        val to = Vec3(1.0f, 0.5f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertEquals(baked.getProperties(Directions.DOWN)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertNull(baked.getProperties(Directions.UP)?.faces)
        assertEquals(baked.getProperties(Directions.NORTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.SOUTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.WEST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(0.5f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.EAST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(0.5f, 1.0f), TextureTransparencies.OPAQUE)))
    }

    fun upperSlab() {
        val from = Vec3(0.0f, 0.5f, 0.0f)
        val to = Vec3(1.0f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertNull(baked.getProperties(Directions.DOWN)?.faces)
        assertEquals(baked.getProperties(Directions.UP)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.NORTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.SOUTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.WEST)?.faces, arrayOf(FaceProperties(Vec2(0.5f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.EAST)?.faces, arrayOf(FaceProperties(Vec2(0.5f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
    }

    fun northSlab() {
        val from = Vec3(0.0f, 0.0f, 0.0f)
        val to = Vec3(1.0f, 1.0f, 0.5f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertEquals(baked.getProperties(Directions.NORTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertNull(baked.getProperties(Directions.SOUTH)?.faces)

        assertEquals(baked.getProperties(Directions.DOWN)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.UP)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.WEST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.EAST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE)))
    }

    fun southSlab() {
        val from = Vec3(0.0f, 0.0f, 0.5f)
        val to = Vec3(1.0f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertEquals(baked.getProperties(Directions.SOUTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertNull(baked.getProperties(Directions.NORTH)?.faces)

        assertEquals(baked.getProperties(Directions.DOWN)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.UP)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.WEST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.EAST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
    }

    fun westSlab() {
        val from = Vec3(0.0f, 0.0f, 0.0f)
        val to = Vec3(0.5f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertEquals(baked.getProperties(Directions.WEST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertNull(baked.getProperties(Directions.EAST)?.faces)

        assertEquals(baked.getProperties(Directions.DOWN)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(0.5f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.UP)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(0.5f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.NORTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(0.5f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.SOUTH)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(0.5f, 1.0f), TextureTransparencies.OPAQUE)))
    }

    fun eastSlab() {
        val from = Vec3(0.5f, 0.0f, 0.0f)
        val to = Vec3(1.0f, 1.0f, 1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertEquals(baked.getProperties(Directions.EAST)?.faces, arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertNull(baked.getProperties(Directions.WEST)?.faces)

        assertEquals(baked.getProperties(Directions.DOWN)?.faces, arrayOf(FaceProperties(Vec2(0.5f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.UP)?.faces, arrayOf(FaceProperties(Vec2(0.5f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.NORTH)?.faces, arrayOf(FaceProperties(Vec2(0.5f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(baked.getProperties(Directions.SOUTH)?.faces, arrayOf(FaceProperties(Vec2(0.5f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
    }

    fun `mini cube`() {
        val from = Vec3(0.1f, 0.2f, 0.3f)
        val to = Vec3(0.7f, 0.8f, 0.9f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!

        assertNull(baked.getProperties(Directions.DOWN)?.faces)
        assertNull(baked.getProperties(Directions.UP)?.faces)
        assertNull(baked.getProperties(Directions.NORTH)?.faces)
        assertNull(baked.getProperties(Directions.SOUTH)?.faces)
        assertNull(baked.getProperties(Directions.WEST)?.faces)
        assertNull(baked.getProperties(Directions.EAST)?.faces)
    }

    fun `full cube weighted model`() { // TODO: still wrong, grass block is failing
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)

        val weighted = WeightedBlockStateApply(listOf(
            WeightedBlockStateApply.WeightedApply(1, SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 0)),
            WeightedBlockStateApply.WeightedApply(1, SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 1)),
            WeightedBlockStateApply.WeightedApply(1, SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 2)),
            WeightedBlockStateApply.WeightedApply(1, SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = BakedModelTestUtil.createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 3)),
        ))

        weighted.load(BakedModelTestUtil.createTextureManager("block/test"))
        val baked = weighted.bake()!!

        baked.assertProperties(Directions.DOWN, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.UP, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.NORTH, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.SOUTH, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.WEST, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
        baked.assertProperties(Directions.EAST, FaceProperties(Vec2(0, 0), Vec2(1, 1), TextureTransparencies.OPAQUE))
    }
}
