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

package de.bixilon.minosoft.gui.rendering.models.baked.rotation

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.block
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.assertFace
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.annotations.Test

@Test(groups = ["models"])
class XRotationTest {


    fun rotatedDown() {
        val from = Vec3(6, 0, 6) / ModelElement.BLOCK_SIZE
        val to = Vec3(10, 16, 16) / ModelElement.BLOCK_SIZE

        fun bake(rotation: Int): BakedModel {
            val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = mapOf(Directions.DOWN to createFaces(from, to)[Directions.DOWN]!!))), textures = mapOf("test" to Namespaces.minecraft("block/test").texture())), x = rotation)

            return model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!
        }


        bake(1).assertFace(Directions.DOWN, block(6, 6, 0, 6, 6, 16, 10, 6, 16, 10, 6, 0))
        bake(2).assertFace(Directions.DOWN, block(6, 0, 0, 6, 0, 10, 10, 0, 10, 10, 0, 0))
        bake(3).assertFace(Directions.DOWN, block(6, 0, 0, 6, 0, 16, 10, 0, 16, 10, 0, 0))
    }

    fun rotatedUp() {
        val from = Vec3(6, 0, 6) / ModelElement.BLOCK_SIZE
        val to = Vec3(10, 16, 16) / ModelElement.BLOCK_SIZE

        fun bake(rotation: Int): BakedModel {
            val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = mapOf(Directions.UP to createFaces(from, to)[Directions.UP]!!))), textures = mapOf("test" to Namespaces.minecraft("block/test").texture())), x = rotation)

            return model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!
        }


        bake(1).assertFace(Directions.UP, block(6, 16, 0, 10, 16, 0, 10, 16, 16, 6, 16, 16))
        bake(2).assertFace(Directions.UP, block(6, 16, 0, 10, 16, 0, 10, 16, 10, 6, 16, 10))
        bake(3).assertFace(Directions.UP, block(6, 10, 0, 10, 10, 0, 10, 10, 16, 6, 10, 16))
    }

    fun rotatedNorth() {
        val from = Vec3(6, 0, 6) / ModelElement.BLOCK_SIZE
        val to = Vec3(10, 16, 16) / ModelElement.BLOCK_SIZE

        fun bake(rotation: Int): BakedModel {
            val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = mapOf(Directions.NORTH to createFaces(from, to)[Directions.NORTH]!!))), textures = mapOf("test" to Namespaces.minecraft("block/test").texture())), x = rotation)

            return model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!
        }


        bake(1).assertFace(Directions.NORTH, block(6, 6, 0, 10, 6, 0, 10, 16, 0, 6, 16, 0))
        bake(2).assertFace(Directions.NORTH, block(6, 0, 0, 10, 0, 0, 10, 16, 0, 6, 16, 0))
        bake(3).assertFace(Directions.NORTH, block(6, 0, 0, 10, 0, 0, 10, 16, 0, 6, 16, 0))
    }

    // TODO: south, west, east

    // TODO: combined
    // TODO: split rotations in all tests (directions)
}
