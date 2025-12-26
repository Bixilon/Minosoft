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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.animator.TextureAnimation
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.MipmapTextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.loader.TextureLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.loader.TextureLoaderResult
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.properties.AnimationProperties
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.FileNotFoundException

class Texture(
    val loader: TextureLoader,
    val mipmaps: Int = 1,
) : ShaderTexture {
    var state = TextureStates.DECLARED
    private var _size: Vec2i? = null
    val size: Vec2i get() = _size!!
    var properties: ImageProperties = unsafeNull()
    var animation: TextureAnimation? = null
    override var transparency: TextureTransparencies = unsafeNull()

    var renderData: TextureRenderData = unsafeNull()

    var data: TextureData = unsafeNull()

    override val shaderId get() = renderData.shaderTextureId


    @Synchronized
    fun load(context: RenderContext) {
        if (state == TextureStates.LOADED) return

        val (buffer, properties) = tryRead(context)
        this.properties = properties ?: ImageProperties.DEFAULT


        this.properties.animation?.let { loadSprites(context, it, buffer) } ?: load(buffer)

        state = TextureStates.LOADED
    }


    private fun loadSprites(context: RenderContext, properties: AnimationProperties, buffer: TextureBuffer) {
        val animation = context.textures.static.animator.create(this, buffer, properties)
        this.animation = animation
        this._size = animation.frame.data.size

        this.data = animation.frame.data
        this.transparency = buffer.getTransparency()
    }

    private fun load(buffer: TextureBuffer) {
        val data = createData(mipmaps, buffer)

        this._size = data.size
        this.transparency = buffer.getTransparency()
        this.data = data
    }

    private fun tryRead(context: RenderContext): TextureLoaderResult {
        try {
            return loader.load(context)
        } catch (error: Throwable) {
            Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Can not load texture ${loader}: $error" }
            if (error !is FileNotFoundException) {
                Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { error }
            }
            return TextureLoaderResult(context.session.assetsManager[RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION].readTexture(), null)
        }
    }

    override fun transformUV(uv: Vec2f) = renderData.transformUV(uv)
    override fun transformUV(u: Float, v: Float) = renderData.transformUV(u, v)
    override fun transformU(u: Float) = renderData.transformU(u)
    override fun transformV(v: Float) = renderData.transformV(v)
    override fun transformUV(uv: PackedUV) = renderData.transformUV(uv)

    fun createData(mipmaps: Int = this.mipmaps, buffer: TextureBuffer): TextureData {
        if (mipmaps <= 0) return TextureData(buffer)
        return MipmapTextureData(buffer, mipmaps)
    }
}
