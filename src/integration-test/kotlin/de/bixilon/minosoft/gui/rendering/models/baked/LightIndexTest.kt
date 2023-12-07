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
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bake
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createTextureManager
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["models"])
class LightIndexTest {
    private val lightIndex = BakedFace::class.java.getFieldOrNull("lightIndex")!!

    private fun BakedModel.assertLight(direction: Directions, self: Boolean) {
        val faces = this.faces[direction.ordinal]
        if (faces.size != 1) throw IllegalArgumentException("Model has more/less than once face: ${faces.size}!")
        val face = faces.first()

        val index = lightIndex.getInt(face)
        if (self) {
            assertTrue(index == 6, "Light is propagating wrongly from direction ${Directions.getOrNull(index)}")
            return
        }
        val lightDirection = Directions[index]
        assertEquals(direction, lightDirection)

    }

    fun cube() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))!!

        for (direction in Directions) {
            baked.assertLight(direction, false)
        }
    }

    fun smallerCube() {
        val from = Vec3(0.1f)
        val to = Vec3(0.9f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))!!

        for (direction in Directions) {
            baked.assertLight(direction, true)
        }
    }

    fun rotatedCube() {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1, y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!

        for (direction in Directions) {
            baked.assertLight(direction, false)
        }
    }

    fun smallerCubeOddSize() {
        val from = Vec3(0.1f, 0.0f, 0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertLight(Directions.DOWN, false)
        baked.assertLight(Directions.UP, false)
        baked.assertLight(Directions.NORTH, false)
        baked.assertLight(Directions.SOUTH, false)
        baked.assertLight(Directions.WEST, true)
        baked.assertLight(Directions.EAST, false)
    }

    fun smallerCubeOddSizeRotated() {
        val from = Vec3(0.1f, 0.0f, 0.0f)
        val to = Vec3(1.0f)
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1, y = 1)

        val baked = model.bake(createTextureManager("block/test"))!!

        baked.assertLight(Directions.DOWN, false)
        baked.assertLight(Directions.UP, false)
        baked.assertLight(Directions.NORTH, true)
        baked.assertLight(Directions.SOUTH, false)
        baked.assertLight(Directions.WEST, false)
        baked.assertLight(Directions.EAST, false)
    }
}
