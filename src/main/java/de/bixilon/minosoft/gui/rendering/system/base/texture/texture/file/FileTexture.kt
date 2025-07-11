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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file

import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import de.bixilon.minosoft.gui.rendering.textures.TextureAnimation
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.properties.AnimationProperties
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileNotFoundException

abstract class FileTexture(
    val file: ResourceLocation,
    override var mipmaps: Int,
) : Texture {
    override lateinit var renderData: TextureRenderData
    override lateinit var array: TextureArrayProperties
    override lateinit var size: Vec2i
    override lateinit var transparency: TextureTransparencies
    override lateinit var data: TextureData

    override var properties: ImageProperties = ImageProperties.DEFAULT
    override var animation: TextureAnimation? = null
    override var state: TextureStates = TextureStates.DECLARED


    @Synchronized
    override fun load(context: RenderContext) {
        if (state == TextureStates.LOADED) return

        updateImageProperties(context.session.assetsManager)
        val properties = this.properties
        val buffer = tryRead(context.session.assetsManager)


        if (properties.animation != null) {
            loadSprites(context, properties.animation, buffer)
        } else {
            load(buffer)
        }

        state = TextureStates.LOADED
    }

    private fun loadSprites(context: RenderContext, properties: AnimationProperties, buffer: TextureBuffer) {
        val (frames, animation) = context.textures.static.animator.create(this, buffer, properties)
        this.animation = animation
        this.size = frames.size

        var transparency = TextureTransparencies.OPAQUE

        for (sprite in animation.sprites) {
            when (sprite.transparency) {
                TextureTransparencies.OPAQUE -> continue
                TextureTransparencies.TRANSPARENT -> transparency = TextureTransparencies.TRANSPARENT
                TextureTransparencies.TRANSLUCENT -> {
                    transparency = TextureTransparencies.TRANSLUCENT; break
                }
            }
        }
        this.transparency = transparency
    }

    private fun load(buffer: TextureBuffer) {
        val data = createData(mipmaps, buffer)

        this.size = data.size
        this.transparency = buffer.getTransparency()
        this.data = data
    }

    private fun tryRead(assets: AssetsManager): TextureBuffer {
        try {
            return read(assets)
        } catch (error: Throwable) {
            state = TextureStates.ERRORED
            Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Can not load texture $file: $error" }
            if (error !is FileNotFoundException) {
                Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { error }
            }
            return assets[RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION].readTexture()
        }
    }

    protected abstract fun read(assets: AssetsManager): TextureBuffer


    override fun toString(): String {
        return file.toString()
    }

    private fun updateImageProperties(assets: AssetsManager) {
        properties = assets.readImageProperties(file) ?: return
    }

    companion object {

        fun AssetsManager.readImageProperties(texture: ResourceLocation): ImageProperties? {
            try {
                val stream = this.getOrNull("$texture.mcmeta".toResourceLocation()) ?: return null
                return stream.readJson(reader = ImageProperties.READER)
            } catch (error: Throwable) {
                error.printStackTrace()
            }
            return null
        }
    }
}
