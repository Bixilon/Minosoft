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

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.SingleChunkMesh
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.Shades
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshOrder
import de.bixilon.minosoft.test.IT.OBJENESIS
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.collections.floats.FragmentedArrayFloatList
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class BakedFaceTest {
    private val texture = "block/test"

    private fun texture(): Texture {
        val manager = BakedModelTestUtil.createTextureManager(texture)
        return manager.staticTextures.createTexture(texture.toResourceLocation())
    }

    private fun singleMesh(): SingleChunkMesh {
        val mesh = OBJENESIS.newInstance(SingleChunkMesh::class.java)
        mesh::quadType.forceSet(PrimitiveTypes.QUAD)
        mesh::order.forceSet(MeshOrder.QUAD)

        mesh.data = FragmentedArrayFloatList(1000) // TODO: kutil 1.24

        mesh::initialCacheSize.forceSet(1000)

        return mesh
    }

    private fun mesh(): ChunkMesh {
        val mesh = OBJENESIS.newInstance(ChunkMesh::class.java)
        mesh::opaqueMesh.forceSet(singleMesh())

        return mesh

    }

    fun mixed() {
        val face = BakedFace(floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f), floatArrayOf(-1f, -2f, -3f, -4f, -5f, -6f, -7f, -8f), Shades.UP, -1, null, texture())

        val mesh = mesh()

        face.render(floatArrayOf(0.0f, 0.0f, 0.0f), mesh, byteArrayOf(0, 0, 0, 0, 0, 0, 0), null)

        val texture = 0.buffer()
        val lightTint = 0xFFFFFF.buffer()

        val data = mesh.opaqueMesh!!.data.toArray()
        val expected = floatArrayOf(
            0f, 1f, 2f, -1f, -2f, texture, lightTint,
            9f, 10f, 11f, -7f, -8f, texture, lightTint,
            6f, 7f, 8f, -5f, -6f, texture, lightTint,
            3f, 4f, 5f, -3f, -4f, texture, lightTint,
        )


        assertEquals(data, expected)
    }

    fun blockSouth() {
        val face = BakedFace(floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f), floatArrayOf(0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f), Shades.UP, -1, null, texture())

        val mesh = mesh()

        face.render(floatArrayOf(0.0f, 0.0f, 0.0f), mesh, byteArrayOf(0, 0, 0, 0, 0, 0, 0), null)

        val texture = 0.buffer()
        val lightTint = 0xFFFFFF.buffer()

        val data = mesh.opaqueMesh!!.data.toArray()
        val expected = floatArrayOf(
            0f, 0f, 0f, 0f, 0f, texture, lightTint,
            0f, 0f, 1f, 1f, 0f, texture, lightTint,
            0f, 1f, 1f, 1f, 1f, texture, lightTint,
            0f, 1f, 0f, 0f, 1f, texture, lightTint,
        )


        assertEquals(data, expected)
    }


    // TODO: triangle order
}
