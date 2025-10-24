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

package de.bixilon.minosoft.gui.rendering.light

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.gui.rendering.shader.types.LightShader
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import org.lwjgl.system.MemoryUtil.memAllocFloat

class LightmapBuffer(renderSystem: RenderSystem) {
    val buffer = renderSystem.createFloatUniformBuffer(memAllocFloat(UNIFORM_BUFFER_SIZE))
    private var upload = false

    fun init() {
        // Set Alpha for all of them
        for (i in 0 until UNIFORM_BUFFER_SIZE / 4) {
            buffer.data.put(i * 4 + 3, 1.0f)
        }
        buffer.init()
        buffer.upload()
    }

    operator fun set(sky: Int, block: Int, color: Vec3f) {
        val index = ((sky shl 4) or block) * 4

        if (upload || buffer.data.get(index + 0) != color.x) {
            buffer.data.put(index + 0, color.x)
            upload = true
        }
        if (upload || buffer.data.get(index + 1) != color.y) {
            buffer.data.put(index + 1, color.y)
            upload = true
        }
        if (upload || buffer.data.get(index + 2) != color.z) {
            buffer.data.put(index + 2, color.z)
            upload = true
        }
    }

    operator fun get(sky: Int, block: Int) = get((sky shl 4) or block)
    operator fun get(light: Int): RGBColor {
        val offset = light * 4
        return RGBColor(buffer.data.get(offset + 0), buffer.data.get(offset + 1), buffer.data.get(offset + 2))
    }

    fun upload() {
        if (!upload) {
            return
        }
        buffer.upload()
        upload = false
    }

    fun use(shader: LightShader, name: String) {
        buffer.use(shader, name)
    }


    private companion object {
        private const val UNIFORM_BUFFER_SIZE = LightLevel.LEVELS * LightLevel.LEVELS * 4 // skyLight * blockLight * RGBA
    }
}
