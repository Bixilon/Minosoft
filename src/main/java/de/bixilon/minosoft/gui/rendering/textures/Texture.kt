/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.json.JSONSerializer
import de.matthiasmann.twl.utils.PNGDecoder
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import java.io.FileNotFoundException
import java.nio.ByteBuffer


class Texture(
    val resourceLocation: ResourceLocation,
) {
    var arrayId = -1
    var arrayLayer = -1
    lateinit var size: Vec2i
    lateinit var transparency: TextureTransparencies
        private set
    lateinit var uvEnd: Vec2

    lateinit var properties: ImageProperties

    var arraySinglePixelFactor = 1.0f

    var buffer: ByteBuffer? = null

    var isLoaded = false
        private set

    fun inherit(texture: Texture) {
        size = texture.size
        transparency = texture.transparency
        uvEnd = texture.uvEnd
        properties = ImageProperties()
        arraySinglePixelFactor = texture.arraySinglePixelFactor
        isLoaded = true
    }

    fun load(assetsManager: AssetsManager) {
        if (isLoaded) {
            return
        }

        val decoder = PNGDecoder(assetsManager.readAssetAsStream(resourceLocation))
        val buffer = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGBA.numComponents)
        decoder.decode(buffer, decoder.width * PNGDecoder.Format.RGBA.numComponents, PNGDecoder.Format.RGBA)

        size = Vec2i(decoder.width, decoder.height)
        buffer.rewind()
        transparency = TextureTransparencies.OPAQUE
        for (i in 0 until buffer.limit() step 4) {
            val color = RGBColor(buffer.get(), buffer.get(), buffer.get(), buffer.get())
            if (color.alpha == 0x00 && transparency != TextureTransparencies.TRANSLUCENT) {
                transparency = TextureTransparencies.TRANSPARENT
            } else if (color.alpha < 0xFF) {
                transparency = TextureTransparencies.TRANSLUCENT
            }
        }
        buffer.flip()

        // load .mcmeta
        properties = try {
            JSONSerializer.IMAGE_PROPERTIES_ADAPTER.fromJson(assetsManager.readStringAsset(ResourceLocation("$resourceLocation.mcmeta")))!!
        } catch (exception: FileNotFoundException) {
            ImageProperties()
        }
        this.buffer = buffer

        isLoaded = true
    }

    companion object {
        fun getResourceTextureIdentifier(namespace: String = ProtocolDefinition.DEFAULT_NAMESPACE, textureName: String): ResourceLocation {
            var namespace = namespace
            var texturePath = textureName

            if (texturePath.contains(":")) {
                val split = texturePath.split(":")
                namespace = split[0]
                texturePath = split[1]
            }

            texturePath = texturePath.removePrefix("/")

            if (!texturePath.startsWith("textures/")) {
                texturePath = "textures/$texturePath"
            }
            if (!texturePath.endsWith(".png")) {
                texturePath = "$texturePath.png"
            }
            return ResourceLocation(namespace, texturePath)
        }
    }
}
