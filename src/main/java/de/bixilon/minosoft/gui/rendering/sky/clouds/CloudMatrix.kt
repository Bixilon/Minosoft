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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import java.util.*

class CloudMatrix {
    private val matrix = BitSet(CLOUD_MATRIX_SIZE * CLOUD_MATRIX_SIZE)


    fun load(assetsManager: AssetsManager) {
        val data = assetsManager[CLOUD_MATRIX].readTexture()

        if (data.size.x != CLOUD_MATRIX_SIZE || data.size.y != CLOUD_MATRIX_SIZE) {
            throw IllegalStateException("Cloud matrix has invalid size: ${data.size}")
        }

        for (i in 0 until CLOUD_MATRIX_SIZE * CLOUD_MATRIX_SIZE) {
            if (DebugOptions.CLOUD_RASTER) {
                matrix[i] = if ((i / CLOUD_MATRIX_SIZE) % 2 == 0) (i + 1) % 2 == 0 else (i % 2) == 0
            } else {
                matrix[i] = data.buffer.getInt(i * 4) ushr 24 == 0xFF
            }
        }

    }


    operator fun get(x: Int, z: Int): Boolean {
        val offset = (x and CLOUD_MATRIX_MASK) + (z and CLOUD_MATRIX_MASK) * CLOUD_MATRIX_SIZE
        return matrix[offset]
    }

    companion object {
        private val CLOUD_MATRIX = minecraft("environment/clouds").texture()
        const val CLOUD_MATRIX_SIZE = 256
        const val CLOUD_MATRIX_MASK = 0xFF
    }
}
