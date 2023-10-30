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
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerShader
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.shader.LightmapSkeletalShader
import de.bixilon.minosoft.gui.rendering.skeletal.shader.SkeletalShader
import org.lwjgl.system.MemoryUtil.memAllocFloat

class SkeletalManager(
    val context: RenderContext,
) {
    private val uniformBuffer = context.system.createFloatUniformBuffer(memAllocFloat(MAX_TRANSFORMS * Mat4.length))
    val shader = context.system.createShader(minosoft("skeletal/normal")) { SkeletalShader(it, uniformBuffer) }
    val lightmapShader = context.system.createShader(minosoft("skeletal/lightmap")) { LightmapSkeletalShader(it, uniformBuffer) }
    private val temp = Mat4()

    val playerShader = context.system.createShader(minosoft("entities/player")) { PlayerShader(it, uniformBuffer) } // TODO: move somewhere else

    fun init() {
        uniformBuffer.init()
    }

    fun postInit() {
        shader.load()
        lightmapShader.load()
        playerShader.load()
    }

    fun upload(instance: SkeletalInstance, matrix: Mat4) {
        instance.transform.pack(uniformBuffer.buffer, matrix, temp)
        uniformBuffer.upload(0, instance.model.transformCount * Mat4.length)
    }

    companion object {
        const val MAX_TRANSFORMS = 128
    }
}
