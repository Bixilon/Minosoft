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

package de.bixilon.minosoft.gui.rendering.camera.arm

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.shader.types.TintedShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.EMPTY_INSTANCE

class ArmShader(override val native: NativeShader) : Shader(), TintedShader, TextureShader {
    override var textures: TextureManager by textureManager()
    var texture by uniform("uIndexLayer", 0x00, NativeShader::setUInt)
    override var tint by uniform("uTintColor", ChatColors.WHITE) { shader, name, value -> shader.setUInt(name, value.rgb) }
    var skinParts by uniform("uSkinParts", 0xFF, NativeShader::setUInt)
    var transform by uniform("uTransform", Mat4.EMPTY_INSTANCE)
}
