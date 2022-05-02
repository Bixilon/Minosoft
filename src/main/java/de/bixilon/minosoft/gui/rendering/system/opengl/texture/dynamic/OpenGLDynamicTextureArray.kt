/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer
import java.util.*

class OpenGLDynamicTextureArray(
    val renderWindow: RenderWindow,
    val index: Int = 6,
    val resolution: Int,
) : DynamicTextureArray {
    private val textures: Array<OpenGLDynamicTexture?> = arrayOfNulls(32)
    private var textureId = -1

    override val size: Int
        get() = 2

    override fun remove(uuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun remove(texture: DynamicTexture) {
        TODO("Not yet implemented")
    }

    override fun push(identifier: UUID, data: () -> ByteBuffer): OpenGLDynamicTexture {
        val bytes = data()

        check(bytes.limit() == resolution * resolution * 4) { "Texture must have a size of ${resolution}x${resolution}" }

        val mipmaps = OpenGLTextureUtil.generateMipMaps(bytes, Vec2i(resolution, resolution))

        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        for ((level, mipmap) in mipmaps.withIndex()) {
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, level, resolution, resolution, 1, GL_RGBA, GL_UNSIGNED_BYTE, mipmap)
        }

        return OpenGLDynamicTexture(identifier, createShaderIdentifier(index = 0))
    }

    private fun createShaderIdentifier(array: Int = this.index, index: Int): Int {
        return (array shl 28) or (index shl 12) or 0
    }


    override fun load(latch: CountUpAndDownLatch) {
        val textureId = OpenGLTextureUtil.createTextureArray()
        this.textureId = textureId


        for (level in 0 until OpenGLTextureUtil.MAX_MIPMAP_LEVELS) {
            glTexImage3D(GL_TEXTURE_2D_ARRAY, level, GL12.GL_RGBA, resolution shr level, resolution shr level, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }


        for (i in textures.indices) {
            for (level in 0 until OpenGLTextureUtil.MAX_MIPMAP_LEVELS) {
                glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, i, resolution, resolution, 1, GL_RGBA, GL_UNSIGNED_BYTE, ByteBuffer.wrap(ByteArray(resolution * resolution * 4)))
            }
        }


        this.textureId = textureId
    }

    override fun use(shader: Shader, name: String) {
        shader.use()

        glActiveTexture(GL_TEXTURE0 + index)
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        shader.setTexture("$name[$index]", index)
    }
}
