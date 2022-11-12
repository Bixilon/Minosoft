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

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.camera.FogManager
import de.bixilon.minosoft.gui.rendering.shader.MinosoftShader
import de.bixilon.minosoft.gui.rendering.shader.types.*
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.world.light.LightmapBuffer

class SkeletalShader(
    override val native: Shader,
    buffer: FloatUniformBuffer,
) : MinosoftShader(), TextureShader, AnimatedShader, LightShader, ViewProjectionShader, FogShader {
    override var textures: TextureManager by textureManager()
    override val lightmap: LightmapBuffer by lightmap()
    override var viewProjectionMatrix: Mat4 by viewProjectionMatrix()
    override var cameraPosition: Vec3 by cameraPosition()
    override var fog: FogManager by fog()

    var light by uniform("uLight", 0x00, Shader::setUInt)
    var skeletalBuffer by uniform("uSkeletalBuffer", buffer)
}
