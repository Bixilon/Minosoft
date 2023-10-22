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

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import org.lwjgl.system.MemoryUtil.memAllocFloat

class SkeletalManager(
    val context: RenderContext,
) {
    private val uniformBuffer = context.system.createFloatUniformBuffer(memAllocFloat(MAX_TRANSFORMS * Mat4.length))
    val shader = context.system.createShader(minosoft("skeletal")) { SkeletalShader(it, uniformBuffer) }
    private val temp = Mat4()

    fun init() {
        uniformBuffer.init()
    }

    fun postInit() {
        shader.native.defines["TRANSFORMS"] = MAX_TRANSFORMS
        shader.load()
        shader.light = 0xFF
    }

    fun upload(instance: SkeletalInstance) {
        instance.transform.pack(uniformBuffer.buffer, instance.position, temp)
        uniformBuffer.upload(0, instance.model.transformCount * Mat4.length)
    }

    fun draw(instance: SkeletalInstance, light: Int) {
        shader.light = light


        instance.model.mesh.draw()
    }

    companion object {
        private const val MAX_TRANSFORMS = 128
    }
}
