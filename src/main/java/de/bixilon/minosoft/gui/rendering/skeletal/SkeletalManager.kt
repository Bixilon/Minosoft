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

package de.bixilon.minosoft.gui.rendering.skeletal

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.shader.LightmapSkeletalShader
import de.bixilon.minosoft.gui.rendering.skeletal.shader.SkeletalShader
import org.lwjgl.system.MemoryUtil.memAllocFloat

class SkeletalManager(
    val context: RenderContext,
) {
    val buffer = context.system.createFloatUniformBuffer(memAllocFloat(MAX_TRANSFORMS * Mat4f.LENGTH))
    val shader = context.system.shader.create(minosoft("skeletal/normal")) { SkeletalShader(it, buffer) }
    val lightmapShader = context.system.shader.create(minosoft("skeletal/lightmap")) { LightmapSkeletalShader(it, buffer) }

    fun init() {
        buffer.init()
    }

    fun postInit() {
        shader.load()
        lightmapShader.load()
    }

    fun upload(instance: SkeletalInstance) {
        instance.transform.pack(buffer.data)
        buffer.upload(0, instance.model.transformCount * Mat4f.LENGTH)
    }

    companion object {
        const val MAX_TRANSFORMS = 64
    }
}
