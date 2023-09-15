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
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bake
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.block
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.assertFace
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.annotations.Test

@Test(groups = ["models"])
class XYRotationTest {

    fun `x=90_y=90`() {
        val from = Vec3(6, 0, 6) / ModelElement.BLOCK_SIZE
        val to = Vec3(10, 16, 16) / ModelElement.BLOCK_SIZE

        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1, y = 1)

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!
        baked.assertFace(Directions.DOWN, block(0, 6, 6, 0, 6, 10, 16, 6, 10, 16, 6, 6))
        baked.assertFace(Directions.UP, block(0, 16, 6, 16, 16, 6, 16, 16, 10, 0, 16, 10))
        baked.assertFace(Directions.NORTH, block(0, 6, 6, 16, 6, 6, 16, 16, 6, 0, 16, 6))
        baked.assertFace(Directions.SOUTH, block(0, 6, 10, 0, 16, 10, 16, 16, 10, 16, 6, 10))
        baked.assertFace(Directions.WEST, block(0, 6, 6, 0, 16, 6, 0, 16, 10, 0, 6, 10))
        baked.assertFace(Directions.EAST, block(16, 6, 6, 16, 6, 10, 16, 16, 10, 16, 16, 6))
    }

    fun `x=90_y=270`() {
        val from = Vec3(6, 0, 6) / ModelElement.BLOCK_SIZE
        val to = Vec3(10, 16, 16) / ModelElement.BLOCK_SIZE

        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces())), textures = mapOf("test" to minecraft("block/test").texture())), x = 1, y = 3)

        val baked = model.bake(BakedModelTestUtil.createTextureManager("block/test"))!!
        baked.assertFace(Directions.DOWN, block(0, 6, 6, 0, 6, 10, 16, 6, 10, 16, 6, 6))
        baked.assertFace(Directions.UP, block(0, 16, 6, 16, 16, 6, 16, 16, 10, 0, 16, 10))
    }

    // TODO: 90x180, 90x270, 180x90, 180x180, 180x270, 270x90, 270x180, 270x270
}
