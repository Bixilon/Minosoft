/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.sky.clouds.generator

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.bit.set.AbstractBitSet
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import kotlin.math.abs

class TextureCloudGenerator(
    val size: Vec2i,
    val matrix: AbstractBitSet,
) : CloudGenerator {

    init {
        assert(size.x * size.y == matrix.capacity()) { "Invalid matrix size: $size" }
    }


    override operator fun get(x: Int, z: Int): Boolean {
        val realX = abs(x % size.x)
        val realZ = abs(z % size.y)
        return matrix[(realZ * size.x) + realX]
    }

    companion object {
        private val CLOUD_MATRIX = Namespaces.minecraft("environment/clouds").texture()


        fun load(texture: ResourceLocation = CLOUD_MATRIX, assets: AssetsManager): TextureCloudGenerator? {
            val buffer = assets.getOrNull(texture)?.readTexture() ?: return null

            return load(buffer)
        }

        fun load(buffer: TextureBuffer): TextureCloudGenerator {
            val matrix = AbstractBitSet.of(buffer.size.x * buffer.size.y)
            for (index in 0 until buffer.size.x * buffer.size.y) {
                val z = index / buffer.size.x
                val x = index % buffer.size.x
                matrix[index] = buffer.getA(x, z) == 0xFF
            }

            return TextureCloudGenerator(buffer.size, matrix)
        }
    }
}
