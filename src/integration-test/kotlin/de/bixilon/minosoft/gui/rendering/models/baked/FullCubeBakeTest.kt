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

package de.bixilon.minosoft.gui.rendering.models.baked

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bake
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.gyrb
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.rbgy
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.yrbg
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.assertFace
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createTextureManager
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.positions
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.annotations.Test

@Test(groups = ["models"])
class FullCubeBakeTest {

    fun cube() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, floatArrayOf(0f, 0f, 0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f, 0f, 0f), floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f), 0.5f)
        baked.assertFace(Directions.UP, floatArrayOf(0f, 1f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f), floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f), 1.0f)
        baked.assertFace(Directions.NORTH, floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 0f), floatArrayOf(1f, 1f, 0f, 1f, 0f, 0f, 1f, 0f), 0.8f)
        baked.assertFace(Directions.SOUTH, floatArrayOf(0f, 0f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 0f, 1f), floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f), 0.8f)
        baked.assertFace(Directions.WEST, floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f), floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f), 0.6f)
        baked.assertFace(Directions.EAST, floatArrayOf(1f, 0f, 0f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 0f), floatArrayOf(1f, 1f, 0f, 1f, 0f, 0f, 1f, 0f), 0.6f)
    }

    fun y90() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!


        // rotating 90° -> only top/bottom texture rotated, rest is the same
        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f), 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f), 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f), 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f), 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f), 0.6f)
    }

    fun y180() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 2)

        val baked = model.bake(createTextureManager("block/test"))!!


        // rotating 180° -> only top/bottom texture rotated, rest is the same
        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f), 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f), 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f), 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f), 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f), 0.6f)
    }

    fun y270() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 3)

        val baked = model.bake(createTextureManager("block/test"))!!


        // rotating 270° -> only top/bottom texture rotated, rest is the same
        baked.assertFace(Directions.DOWN, uv = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
        baked.assertFace(Directions.UP, uv = floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f))
        baked.assertFace(Directions.NORTH, uv = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
        baked.assertFace(Directions.SOUTH, uv = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f))
        baked.assertFace(Directions.WEST, uv = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f))
        baked.assertFace(Directions.EAST, uv = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
    }

    fun x90() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f), 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.6f)
    }

    fun x180() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 2)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 0.6f)
    }

    fun x270() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 3)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, uv = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f))
        baked.assertFace(Directions.UP, uv = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
        baked.assertFace(Directions.NORTH, uv = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f))
        baked.assertFace(Directions.SOUTH, uv = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f))
        baked.assertFace(Directions.WEST, uv = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))
        baked.assertFace(Directions.EAST, uv = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f))
    }

    fun x90y90() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1, y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), gyrb, 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), yrbg, 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), yrbg, 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), gyrb, 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), yrbg, 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), rbgy, 0.6f)
    }

    fun x180y180() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 2, y = 2)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, positions(Directions.DOWN, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.5f)
        baked.assertFace(Directions.UP, positions(Directions.UP, from, to), floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f), 1.0f)
        baked.assertFace(Directions.NORTH, positions(Directions.NORTH, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 0.8f)
        baked.assertFace(Directions.SOUTH, positions(Directions.SOUTH, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.8f)
        baked.assertFace(Directions.WEST, positions(Directions.WEST, from, to), floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), 0.6f)
        baked.assertFace(Directions.EAST, positions(Directions.EAST, from, to), floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f), 0.6f)
    }
}
