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

package de.bixilon.minosoft.gui.rendering.shader.types

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.minosoft.gui.rendering.shader.AbstractMinosoftShader
import de.bixilon.minosoft.gui.rendering.shader.uniform.ShaderUniform
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderUniforms

interface ViewProjectionShader : AbstractMinosoftShader {
    var viewProjectionMatrix: Mat4


    fun viewProjectionMatrix(): ShaderUniform<Mat4> {
        return uniform(ShaderUniforms.VIEW_PROJECTION_MATRIX, Mat4(), Shader::setMat4)
    }
}
