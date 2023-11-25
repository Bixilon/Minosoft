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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture.dynamic

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.gui.rendering.system.opengl.MemoryLeakException
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glFormat
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glType
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer

class OpenGLDynamicTextureArray(
    context: RenderContext,
    val renderSystem: OpenGLRenderSystem,
    val index: Int = renderSystem.textureBindingIndex++,
    initialSize: Int = 32,
    val resolution: Int,
    mipmaps: Int,
) : DynamicTextureArray(context, initialSize, mipmaps) {
    private val empty = IntArray(resolution * resolution) { 0x00 }
    private var handle = -1

    override fun upload(index: Int, texture: DynamicTexture) {
        glBindTexture(GL_TEXTURE_2D_ARRAY, handle)

        unsafeUpload(index, texture)
        context.textures.static.activate() // TODO: why?
        texture.state = DynamicTextureState.LOADED
    }

    private fun unsafeUpload(index: Int, texture: DynamicTexture) {
        val data = texture.data ?: throw IllegalArgumentException("No texture data?")
        if (data.size.x > resolution || data.size.y > resolution) {
            Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Dynamic texture is too big: $texture" }
        }
        for ((level, buffer) in data.collect().withIndex()) {
            if (data.size.x != resolution || data.size.y != resolution) {
                // clear first
                glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, index, resolution shr level, resolution shr level, 1, GL_RGBA, GL_UNSIGNED_BYTE, empty)
            }
            buffer.data.flip()
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, index, buffer.size.x, buffer.size.y, 1, buffer.glFormat, buffer.glType, buffer.data)
        }
    }

    override fun upload() {
        if (handle >= 0) throw MemoryLeakException("Texture was not unloaded!")
        val handle = OpenGLTextureUtil.createTextureArray(mipmaps)
        for (level in 0..mipmaps) {
            glTexImage3D(GL_TEXTURE_2D_ARRAY, level, GL_RGBA, resolution shr level, resolution shr level, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        for ((index, textureReference) in textures.withIndex()) {
            val texture = textureReference?.get() ?: continue
            if (texture.data == null) continue
            unsafeUpload(index, texture)
        }
        this.handle = handle

        for (shader in shaders) {
            unsafeUse(shader)
        }

        context.textures.static.activate() // TODO: why?

    }

    override fun activate() {
        glActiveTexture(GL_TEXTURE0 + index)
        glBindTexture(GL_TEXTURE_2D_ARRAY, handle)
    }

    override fun unsafeUse(shader: NativeShader, name: String) {
        shader.use()
        activate()
        shader.setTexture("$name[$index]", index)
    }

    override fun unload() {
        if (handle < 0) throw IllegalStateException("Not loaded!")
        glDeleteTextures(handle)
        this.handle = -1
    }

    private fun createShaderIdentifier(array: Int = this.index, index: Int): Int {
        check(array >= 0 && index >= 0) { "Array not initialized or index < 0" }
        return (array shl 28) or (index shl 12) or 0
    }

    override fun createTexture(identifier: Any, index: Int): DynamicTexture {
        return OpenGLDynamicTexture(identifier, createShaderIdentifier(index = index))
    }
}
