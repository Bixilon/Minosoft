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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.light.LightmapBuffer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.types.*
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderUniforms
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

class ParticleShader(
    override val native: NativeShader,
    override val transparent: Boolean,
) : Shader(), TextureShader, AnimatedShader, LightShader, TransparentShader, ViewProjectionShader {
    override var textures: TextureManager by textureManager()
    override val lightmap: LightmapBuffer by lightmap()
    override var viewProjectionMatrix: Mat4 by viewProjectionMatrix()
    var cameraRight by uniform(ShaderUniforms.CAMERA_RIGHT, Vec3(), NativeShader::setVec3)
    var cameraUp by uniform(ShaderUniforms.CAMERA_UP, Vec3(), NativeShader::setVec3)
}
