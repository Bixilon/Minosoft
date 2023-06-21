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
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.SideSize
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.MemoryTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import org.testng.Assert.assertFalse
import org.testng.annotations.Test

@Test(groups = ["models", "culling"])
class FaceCullingTest {

    private fun createFace(size: SideSize.FaceSize? = SideSize.FaceSize(Vec2(0), Vec2(1)), transparency: TextureTransparencies = TextureTransparencies.OPAQUE): BakedFace {
        return BakedFace(floatArrayOf(), floatArrayOf(), 1.0f, -1, null, MemoryTexture(minosoft("test"), Vec2i.EMPTY), size)
    }

    private fun createNeighbour(size: SideSize? = SideSize(arrayOf(SideSize.FaceSize(Vec2(0), Vec2(1)))), transparency: TextureTransparencies = TextureTransparencies.OPAQUE): BlockState {
        val block = object : Block(minosoft("dummy"), BlockSettings()) {
            override val hardness: Float get() = Broken()
        }

        val state = BlockState(block, 0)

        state.model = BakedModel(emptyArray(), arrayOf(size, size, size, size, size, size), null)

        return state
    }

    fun noNeighbour() {
        val face = createFace()
        assertFalse(FaceCulling.canCull(face, Directions.DOWN, null))
    }

    @Test
    fun selfNotTouching() {
        val face = createFace(null)
        val neighbour = createNeighbour()
        assertFalse(FaceCulling.canCull(face, Directions.DOWN, neighbour))
    }

    @Test
    fun neighbourNotTouching() {
        val face = createFace()
        val neighbour = createNeighbour(null)
        assertFalse(FaceCulling.canCull(face, Directions.DOWN, neighbour))
    }

    // TODO: force no cull (e.g. leaves), mix of transparency, mix of side sizes
}
