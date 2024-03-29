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
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.block
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.assertFace
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createTextureManager
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.annotations.Test

@Test(groups = ["models"])
class CuboidBakeTest {


    fun noRotation() {
        val from = Vec3(6, 0, 6) / BLOCK_SIZE
        val to = Vec3(10, 16, 16) / BLOCK_SIZE

        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, block(6, 0, 6, 6, 0, 16, 10, 0, 16, 10, 0, 6))
        baked.assertFace(Directions.UP, block(6, 16, 6, 10, 16, 6, 10, 16, 16, 6, 16, 16))
        baked.assertFace(Directions.NORTH, block(6, 0, 6, 10, 0, 6, 10, 16, 6, 6, 16, 6))
        baked.assertFace(Directions.SOUTH, block(6, 0, 16, 6, 16, 16, 10, 16, 16, 10, 0, 16))
        baked.assertFace(Directions.WEST, block(6, 0, 6, 6, 16, 6, 6, 16, 16, 6, 0, 16))
        baked.assertFace(Directions.EAST, block(10, 0, 6, 10, 0, 16, 10, 16, 16, 10, 16, 6))
    }

    fun cuboidY90_1() {
        val from = Vec3(1, 0, 0) / BLOCK_SIZE
        val to = Vec3(16, 16, 16) / BLOCK_SIZE
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, block(0, 0, 1, 0, 0, 16, 16, 0, 16, 16, 0, 1), block(1, 0, 16, 0, 16, 16, 1, 16), 0.5f)
        baked.assertFace(Directions.UP, block(0, 16, 1, 16, 16, 1, 16, 16, 16, 0, 16, 16), block(1, 16, 1, 0, 16, 0, 16, 16), 1.0f)
        baked.assertFace(Directions.NORTH, block(0, 0, 1, 16, 0, 1, 16, 16, 1, 0, 16, 1), block(16, 16, 0, 16, 0, 0, 16, 0), 0.8f)
        baked.assertFace(Directions.SOUTH, block(0, 0, 16, 0, 16, 16, 16, 16, 16, 16, 0, 16), block(0, 16, 0, 0, 16, 0, 16, 16), 0.8f)
        baked.assertFace(Directions.WEST, block(0, 0, 1, 0, 16, 1, 0, 16, 16, 0, 0, 16), block(1, 16, 1, 0, 16, 0, 16, 16), 0.6f)
        baked.assertFace(Directions.EAST, block(16, 0, 1, 16, 0, 16, 16, 16, 16, 16, 16, 1), block(15, 16, 0, 16, 0, 0, 15, 0), 0.6f)
    }

    fun cuboidY90() {
        val from = Vec3(1, 2, 3) / BLOCK_SIZE
        val to = Vec3(16, 15, 14) / BLOCK_SIZE
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, block(2, 2, 1, 2, 2, 16, 13, 2, 16, 13, 2, 1), block(1, 2, 16, 2, 16, 13, 1, 13), 0.5f)
        baked.assertFace(Directions.UP, block(2, 15, 1, 13, 15, 1, 13, 15, 16, 2, 15, 16), block(1, 14, 1, 3, 16, 3, 16, 14), 1.0f)
        baked.assertFace(Directions.NORTH, block(2, 2, 1, 13, 2, 1, 13, 15, 1, 2, 15, 1), block(14, 14, 3, 14, 3, 1, 14, 1), 0.8f)
        baked.assertFace(Directions.SOUTH, block(2, 2, 16, 2, 15, 16, 13, 15, 16, 13, 2, 16), block(2, 14, 2, 1, 13, 1, 13, 14), 0.8f)
        baked.assertFace(Directions.WEST, block(2, 2, 1, 2, 15, 1, 2, 15, 16, 2, 2, 16), block(1, 14, 1, 1, 16, 1, 16, 14), 0.6f)
        baked.assertFace(Directions.EAST, block(13, 2, 1, 13, 2, 16, 13, 15, 16, 13, 15, 1), block(15, 14, 0, 14, 0, 1, 15, 1), 0.6f)
    }

    fun x90y90() {
        val from = Vec3(1, 2, 3) / BLOCK_SIZE
        val to = Vec3(16, 15, 14) / BLOCK_SIZE
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1, y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, block(2, 3, 1, 2, 3, 16, 15, 3, 16, 15, 3, 1), block(15, 14, 0, 14, 0, 1, 15, 1))
        baked.assertFace(Directions.UP, block(2, 14, 1, 15, 14, 1, 15, 14, 16, 2, 14, 16), block(1, 14, 1, 1, 16, 1, 16, 14))
    }
}
