/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.collections.primitive.floats.HeapFloatList
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshBuilder
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.Shades
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureLoader
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureRenderData
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.UnpackedUVArray
import de.bixilon.minosoft.test.ITUtil.allocate
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class BakedFaceTest {
    private val texture = "block/test"

    private fun texture(): Texture {
        val manager = BakedModelTestUtil.createTextureManager(texture)
        return manager.static.create(texture.toResourceLocation()).apply { this::loader.forceSet(DummyTextureLoader); load(RenderContext::class.java.allocate()); renderData = DummyTextureRenderData; transparency = TextureTransparencies.OPAQUE }
    }

    private fun singleMesh(): ChunkMeshBuilder {
        val mesh = ChunkMeshBuilder::class.java.allocate()

        mesh::primitive.forceSet(PrimitiveTypes.QUAD)
        mesh::struct.forceSet(ChunkMeshBuilder.ChunkMeshStruct)

        mesh::class.java.getFieldOrNull("_data")!!.forceSet(mesh, HeapFloatList(1000))

        mesh::estimate.forceSet(1000)

        return mesh
    }

    private fun mesh(): ChunkMeshesBuilder {
        val mesh = ChunkMeshesBuilder::class.java.allocate()
        mesh::opaque.forceSet(singleMesh())

        return mesh
    }

    fun mixed() {
        // TODO: negative uv is not supported anymore
        val face = BakedFace(floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f), UnpackedUVArray(floatArrayOf(-1f, -2f, -3f, -4f, -5f, -6f, -7f, -8f)), Shades.UP, -1, null, texture())

        val mesh = mesh()

        face.render(Vec3f(0.0f, 0.0f, 0.0f), mesh, null, byteArrayOf(0, 0, 0, 0, 0, 0, 0), AmbientOcclusionUtil.EMPTY)

        val texture = 0.buffer()
        val lightTint = 0xFFFFFF.buffer()

        val data = mesh.opaque.data.toArray()
        val expected = floatArrayOf(
            0f, 1f, 2f, PackedUV(-1f, -2f).raw, texture, lightTint,
            3f, 4f, 5f, PackedUV(-3f, -4f).raw, texture, lightTint,
            6f, 7f, 8f, PackedUV(-5f, -6f).raw, texture, lightTint,
            9f, 10f, 11f, PackedUV(-7f, -8f).raw, texture, lightTint,
        )


        assertEquals(data, expected)
    }

    fun blockSouth() {
        val face = BakedFace(floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 1f), UnpackedUVArray(floatArrayOf(0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f)), Shades.UP, -1, null, texture())

        val mesh = mesh()

        face.render(Vec3f(0.0f, 0.0f, 0.0f), mesh, null, byteArrayOf(0, 0, 0, 0, 0, 0, 0), AmbientOcclusionUtil.EMPTY)

        val texture = 0.buffer()
        val lightTint = 0xFFFFFF.buffer()

        val data = mesh.opaque.data.toArray()
        val expected = floatArrayOf(
            0f, 0f, 0f, PackedUV(0f, 0f).raw, texture, lightTint,
            0f, 1f, 0f, PackedUV(0f, 1f).raw, texture, lightTint,
            0f, 1f, 1f, PackedUV(1f, 1f).raw, texture, lightTint,
            0f, 0f, 1f, PackedUV(1f, 0f).raw, texture, lightTint,
        )


        assertEquals(data, expected)
    }


    // TODO: triangle order
}
