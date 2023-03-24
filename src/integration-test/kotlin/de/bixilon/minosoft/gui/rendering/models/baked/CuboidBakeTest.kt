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

    private fun block(vararg elements: Int): FloatArray {
        val result = FloatArray(elements.size)

        for ((index, value) in elements.withIndex()) {
            result[index] = value / BLOCK_SIZE
        }

        return result
    }

    fun cuboidY90() {
        val from = Vec3(1, 2, 3) / BLOCK_SIZE
        val to = Vec3(16, 15, 14) / BLOCK_SIZE
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces(from, to))), textures = mapOf("test" to minecraft("block/test").texture())), y = 90)

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.DOWN, block(2, 2, 1, 2, 2, 16, 13, 2, 16, 13, 2, 1), block(1, 14, 16, 14, 16, 3, 1, 3), 0.5f)
        baked.assertFace(Directions.UP, block(2, 15, 1, 13, 15, 1, 13, 15, 16, 2, 15, 16), block(1, 2, 1, 13, 16, 13, 16, 2), 1.0f)
    }
}
