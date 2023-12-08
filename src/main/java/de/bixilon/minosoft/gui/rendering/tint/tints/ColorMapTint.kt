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

package de.bixilon.minosoft.gui.rendering.tint.tints

import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

abstract class ColorMapTint(
    private val file: ResourceLocation,
) : TintProvider {
    protected var map: IntArray? = null

    fun init(assets: AssetsManager) {
        val map = ignoreAll { assets[file].readTexture() } ?: return
        if (map.size.x != SIZE || map.size.y != SIZE) {
            Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Color map ($file) has invalid size: ${map.size}" }
            return
        }
        this.map = map.toColorMap()
    }

    private fun TextureBuffer.toColorMap(): IntArray {
        val array = IntArray(size.x * size.y)
        var index = 0
        for (y in 0 until size.y) {
            for (x in 0 until size.x) {
                array[index++] = getRGB(x, y)
            }
        }
        return array
    }

    companion object {
        const val SIZE = 256

        fun getIndex(value: Float): Int {
            if (value <= 0.0f) return SIZE - 1
            if (value >= 1.0f) return 0

            val delta = 1.0f - value
            return (delta * (SIZE - 1)).toInt()
        }
    }
}
