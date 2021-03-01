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

import de.bixilon.minosoft.data.assets.MinecraftAssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer


class Texture(
    val resourceLocation: ResourceLocation,
    val id: Int,
) {
    var width: Int = 0
    var height: Int = 0
    var isTransparent: Boolean = false
    lateinit var buffer: ByteBuffer
    var loaded = false

    var widthFactor = 1f
    var heightFactor = 1f

    var animations: Int = 0
    var animationFrameTime: Int = 0

    fun load(assetsManager: MinecraftAssetsManager) {
        if (loaded) {
            return
        }

        val decoder = PNGDecoder(assetsManager.readAssetAsStream(resourceLocation))
        buffer = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGBA.numComponents)
        decoder.decode(buffer, decoder.width * PNGDecoder.Format.RGBA.numComponents, PNGDecoder.Format.RGBA)
        width = decoder.width
        height = decoder.height
        buffer.rewind()
        for (i in 0 until buffer.limit() step 4) {
            val color = RGBColor(buffer.get(), buffer.get(), buffer.get(), buffer.get())
            if (color.alpha < 0xFF) {
                isTransparent = true
            }
        }
        buffer.flip()

        // load .mcmeta
        try {
            val json = assetsManager.readJsonAsset(ResourceLocation("$resourceLocation.mcmeta"))
            animationFrameTime = json["animation"].asJsonObject["frametime"].asInt
        } catch (exception: Exception) {
        }

        loaded = true
    }

    companion object {
        fun getResourceTextureIdentifier(namespace: String = ProtocolDefinition.DEFAULT_NAMESPACE, textureName: String): ResourceLocation {
            var texturePath = textureName
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
