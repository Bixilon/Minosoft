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

package de.bixilon.minosoft.gui.rendering.sky.box

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.shader.types.TintedShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

class SkyboxTextureShader(
    override val native: NativeShader,
) : Shader(), TextureShader, TintedShader {
    override var textures: TextureManager by textureManager()

    var skyViewProjectionMatrix by uniform("uSkyViewProjectionMatrix", Mat4())
    var textureIndexLayer by uniform("uIndexLayerAnimation", 0, NativeShader::setUInt)
    override var tint by uniform("uTintColor", RGBColor(0.15f, 0.15f, 0.15f, 1.0f))
}
