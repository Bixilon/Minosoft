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

package de.bixilon.minosoft.gui.rendering.skeletal

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader.Companion.loadAnimated
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import org.lwjgl.system.MemoryUtil.memAllocFloat

class SkeletalManager(
    val renderWindow: RenderWindow,
) {
    val shader = renderWindow.renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "skeletal"))
    private val uniformBuffer = renderWindow.renderSystem.createFloatUniformBuffer(memAllocFloat(TRANSFORMS * MAT4_SIZE))

    fun init() {
        uniformBuffer.init()
    }

    fun postInit() {
        shader.defines["TRANSFORMS"] = TRANSFORMS
        shader.loadAnimated()
        renderWindow.textureManager.dynamicTextures.use(shader)
        shader["uSkeletalBuffer"] = uniformBuffer
        shader.setUInt("uLight", 0xFF)
        renderWindow.lightMap.use(shader)
    }

    private fun prepareDraw() {
        if (renderWindow.renderSystem.shader == shader) {
            // probably already prepared
            return
        }
        renderWindow.renderSystem.reset()
        shader.use()
    }

    fun draw(instance: SkeletalInstance, light: Int) {
        prepareDraw()
        shader.setUInt("uLight", light)
        val transforms = instance.calculateTransforms()
        var stride = 0
        for (transform in transforms) {
            for (byte in transform.array) {
                uniformBuffer.buffer.put(stride++, byte)
            }
        }
        uniformBuffer.upload(0 until (transforms.size * MAT4_SIZE))

        instance.model.mesh.draw()
    }

    private companion object {
        private const val TRANSFORMS = 32
        private const val MAT4_SIZE = 4 * 4
    }
}
