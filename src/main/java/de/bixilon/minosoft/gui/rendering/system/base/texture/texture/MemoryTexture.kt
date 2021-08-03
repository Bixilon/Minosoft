/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.matthiasmann.twl.utils.PNGDecoder
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class MemoryTexture(
    override val resourceLocation: ResourceLocation,
    override val size: Vec2i,
    override var properties: ImageProperties = ImageProperties(),
    generator: ((x: Int, y: Int) -> RGBColor)? = null,
) : AbstractTexture {
    override lateinit var textureArrayUV: Vec2
    override lateinit var singlePixelSize: Vec2
    override var renderData: TextureRenderData? = null
    override var transparency: TextureTransparencies = TextureTransparencies.OPAQUE
        private set
    override var data: ByteBuffer? = null

    init {
        val data = BufferUtils.createByteBuffer(size.x * size.y * PNGDecoder.Format.RGBA.numComponents)

        generator?.let {
            var index = 0
            for (x in 0 until size.x) {
                for (y in 0 until size.x) {
                    val pixel = it(x, y)

                    if (pixel.alpha == 0x00 && transparency != TextureTransparencies.TRANSLUCENT) {
                        transparency = TextureTransparencies.TRANSPARENT
                    } else if (pixel.alpha < 0xFF) {
                        transparency = TextureTransparencies.TRANSLUCENT
                    }

                    data.put(index++, pixel.red.toByte())
                    data.put(index++, pixel.green.toByte())
                    data.put(index++, pixel.blue.toByte())
                    data.put(index++, pixel.alpha.toByte())
                }
            }
        }

        this.data = data
    }

    override val state: TextureStates = TextureStates.LOADED


    override fun load(assetsManager: AssetsManager) {}
}
