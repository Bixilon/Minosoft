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
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.bake
import de.bixilon.minosoft.gui.rendering.models.ModelTestUtil.rbgy
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.assertFace
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createFaces
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModelTestUtil.createTextureManager
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ElementRotation
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.annotations.Test

@Test(groups = ["models"])
class ElementRotationTest {

    private fun block(rotation: ElementRotation): SingleBlockStateApply {
        val from = Vec3(0.0f)
        val to = Vec3(1.0f)
        return SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces(), rotation = rotation)), textures = mapOf("test" to minecraft("block/test").texture())))
    }

    fun `rotate block around origin 45 degree on the y axis`() {
        val model = block(ElementRotation(axis = Axes.Y, angle = 45.0f))

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.UP, floatArrayOf(-0.2f, 1.0f, 0.5f, 0.5f, 1.0f, -0.2f, 1.2f, 1.0f, 0.5f, 0.5f, 1.0f, 1.2f), rbgy, 1.0f)
    }

    fun `rotate block around origin -45 degree on the y axis`() {
        val model = block(ElementRotation(axis = Axes.Y, angle = -45.0f))

        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.UP, floatArrayOf(0.5f, 1.0f, -0.2f, 1.2f, 1.0f, 0.5f, 0.5f, 1.0f, 1.2f, -0.2f, 1.0f, 0.5f), rbgy, 1.0f)
    }

    fun `rotate grass around origin 45 degree on the y axis and rescale`() {
        val from = Vec3(0.8, 0, 8) / 16
        val to = Vec3(15.2, 16, 8) / 16
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(from, to, faces = createFaces(), rotation = ElementRotation(origin = Vec3(0.5f), axis = Axes.Y, angle = 45.0f, rescale = true))), textures = mapOf("test" to minecraft("block/test").texture())))


        val baked = model.bake(createTextureManager("block/test"))!!


        baked.assertFace(Directions.NORTH, floatArrayOf(0.05f, 0.0f, 0.95f, 0.95f, 0.0f, 0.05f, 0.95f, 1.0f, 0.05f, 0.05f, 1.0f, 0.95f))
    }

    // TODO: a,z axis
}
