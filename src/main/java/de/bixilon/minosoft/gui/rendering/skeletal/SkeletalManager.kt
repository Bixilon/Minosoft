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

package de.bixilon.minosoft.gui.rendering.skeletal

import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import org.lwjgl.system.MemoryUtil.memAllocFloat

class SkeletalManager(
    val context: RenderContext,
) {
    private val uniformBuffer = context.system.createFloatUniformBuffer(memAllocFloat(TRANSFORMS * MAT4_SIZE))
    val shader = context.system.createShader(minosoft("skeletal")) { SkeletalShader(it, uniformBuffer) }

    fun init() {
        uniformBuffer.init()
    }

    fun postInit() {
        shader.native.defines["TRANSFORMS"] = TRANSFORMS
        shader.load()
        shader.light = 0xFF
    }

    private fun prepareDraw() {
        if (context.system.shader == shader.native) {
            // probably already prepared
            return
        }
        context.system.reset()
        shader.use()
    }

    fun draw(instance: SkeletalInstance, light: Int) {
        prepareDraw()
        shader.light = light
        val transforms = instance.calculateTransforms()
        var stride = 0
        for (transform in transforms) {
            for (float in transform.array) {
                uniformBuffer.buffer.put(stride++, float)
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
