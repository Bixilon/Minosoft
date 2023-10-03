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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.MemoryAssetsManager
import de.bixilon.minosoft.assets.TestAssetsManager.box
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.models.block.element.face.ModelFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.memory.MemoryTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.testng.Assert
import kotlin.math.abs

object BakedModelTestUtil {
    private val texture = Minosoft::class.java.getResourceAsStream("/assets/minosoft/textures/debug.png")!!.readAllBytes()

    fun createTextureManager(vararg names: String): TextureManager {
        val connection = ConnectionTestUtil.createConnection()
        val assets = MemoryAssetsManager()
        for (name in names) {
            assets.push(name.toResourceLocation().texture(), texture)
        }
        connection::assetsManager.forceSet(assets.box())
        val rendering = Rendering(connection)
        rendering.context.textures::debugTexture.forceSet(MemoryTexture(Vec2i(0, 0)))

        return rendering.context.textures
    }

    fun createFaces(rotation: Int = 0, texture: String = "#test"): Map<Directions, ModelFace> {
        val map: MutableMap<Directions, ModelFace> = mutableMapOf()

        for (direction in Directions) {
            map[direction] = ModelFace(texture, null, rotation = rotation)
        }

        return map
    }

    fun BakedModel.assertFace(direction: Directions, vertices: FloatArray? = null, uv: FloatArray? = null, shade: Float? = null, texture: String? = null) {
        val faces = this.faces[direction.ordinal]
        if (faces.size != 1) throw IllegalArgumentException("Model has more/less than once face: ${faces.size}!")
        val face = faces.first()

        vertices?.let { assertMatches(face.positions, it, "Vertices mismatch") }
        uv?.let { if (!face.uv.contentEquals(it)) throw AssertionError("UV mismatch, expected [${uv[0]}|${uv[1]}], but got [${face.uv[0]}|${face.uv[1]}]") } // printing the first element is fine, it is always clockwise
        shade?.let { Assert.assertEquals(face.shade, it, "Shade mismatch") }
        texture?.toResourceLocation()?.texture()?.let { Assert.assertEquals(face.texture, it, "Texture mismatch") }
    }

    private fun assertMatches(actual: FloatArray, expected: FloatArray, message: String) {
        if (actual.size != expected.size) throw AssertionError("Size mismatch!")

        for (i in actual.indices) {
            val delta = abs(actual[i] - expected[i])
            if (delta > 0.01f) throw AssertionError("$message: Delta is too high at index $i: ${actual[i]}, expected ${expected[i]}")
        }
    }
}
