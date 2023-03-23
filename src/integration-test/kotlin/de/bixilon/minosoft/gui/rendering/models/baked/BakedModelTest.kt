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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import de.bixilon.minosoft.gui.rendering.models.block.element.face.ModelFace
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureManager
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import org.testng.annotations.Test

@Test(groups = ["models"])
class BakedModelTest {

    private fun createTextureManager(vararg names: String): DummyTextureManager = TODO()

    private fun createFaces(texture: String): Map<Directions, ModelFace> {
        val map: MutableMap<Directions, ModelFace> = mutableMapOf()

        for (direction in Directions) {
            map[direction] = ModelFace(texture, FaceUV(Vec2(0), Vec2(1)), cull = direction, rotation = 0)
        }

        return map
    }

    private fun BakedModel.assertFace(directions: Directions, start: Vec3, end: Vec3, uvStart: Vec2, uvEnd: Vec2, shade: Float, texture: String? = null): Unit = TODO()

    fun simpleBlock() {
        val model = SingleBlockStateApply(BlockModel(elements = listOf(ModelElement(Vec3(0), Vec3(1), faces = createFaces("#test"))), textures = mapOf("test" to minecraft("block/test").texture())))

        val baked = model.bake(createTextureManager("block/test"))


        baked.assertFace(Directions.UP, Vec3(0, 1, 0), Vec3(1, 1, 1), Vec2(0, 0), Vec2(1, 1), 1.0f)
        baked.assertFace(Directions.DOWN, Vec3(0, 0, 0), Vec3(1, 0, 1), Vec2(0, 1), Vec2(1, 0), 0.5f)
        baked.assertFace(Directions.NORTH, Vec3(1, 0, 0), Vec3(0, 1, 0), Vec2(0, 1), Vec2(1, 0), 0.8f)
        baked.assertFace(Directions.SOUTH, Vec3(0, 0, 0), Vec3(1, 1, 0), Vec2(0, 1), Vec2(1, 0), 0.8f)
        baked.assertFace(Directions.WEST, Vec3(0, 0, 0), Vec3(0, 1, 1), Vec2(0, 1), Vec2(1, 0), 0.5f)
        baked.assertFace(Directions.EAST, Vec3(1, 0, 1), Vec3(1, 1, 0), Vec2(0, 1), Vec2(1, 0), 0.5f)
    }
}
