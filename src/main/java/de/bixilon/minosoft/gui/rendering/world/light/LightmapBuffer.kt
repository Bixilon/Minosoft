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

package de.bixilon.minosoft.gui.rendering.world.light

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import org.lwjgl.system.MemoryUtil.memAllocFloat

class LightmapBuffer(renderSystem: RenderSystem) {
    val buffer = renderSystem.createFloatUniformBuffer(memAllocFloat(UNIFORM_BUFFER_SIZE))
    private var upload = false

    fun init() {
        // Set Alpha for all of them
        for (i in 0 until UNIFORM_BUFFER_SIZE / 4) {
            buffer.buffer.put(i * 4 + 3, 1.0f)
        }
        buffer.init()
        buffer.upload()
    }

    operator fun set(sky: Int, block: Int, color: Vec3) {
        val index = ((sky shl 4) or block) * 4

        if (upload || buffer.buffer.get(index + 0) != color.r) {
            buffer.buffer.put(index + 0, color.r)
            upload = true
        }
        if (upload || buffer.buffer.get(index + 1) != color.g) {
            buffer.buffer.put(index + 1, color.g)
            upload = true
        }
        if (upload || buffer.buffer.get(index + 2) != color.b) {
            buffer.buffer.put(index + 2, color.b)
            upload = true
        }
    }

    fun upload() {
        if (!upload) {
            return
        }
        buffer.upload()
        upload = false
    }

    fun use(shader: Shader, name: String) {
        buffer.use(shader, name)
    }


    private companion object {
        private const val UNIFORM_BUFFER_SIZE = ProtocolDefinition.LIGHT_LEVELS * ProtocolDefinition.LIGHT_LEVELS * 4 // skyLight * blockLight * RGBA
    }
}
